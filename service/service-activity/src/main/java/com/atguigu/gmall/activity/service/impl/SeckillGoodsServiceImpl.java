package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 王木风
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    //  通常调用mapper ！但是，缓存中有了数据！
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public List<SeckillGoods> findAll() {
        //  获取到hash 的所有 value！
        //  hvals key
        List<SeckillGoods> seckillGoodsList = this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        //  返回
        return seckillGoodsList;
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        //  hget key field;
        SeckillGoods seckillGoods = (SeckillGoods) this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
        return seckillGoods;
    }

    @Override
    public void seckillOrder(String userId, Long skuId) {
        //  业务逻辑： 判断状态位
        String status = (String) CacheHelper.get(skuId.toString());
        //  null
        if (StringUtils.isEmpty(status) || "0".equals(status)){
            return;
        }
        //  判断用户是否已经下过订单！ setnx seckill:user:userId skuId
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        //  判断 flag = true; 第一次下单 flag = false; 已经下单了。
        if(!flag){
            return;
        }
        //  预减库存！
        String skuIdStock = (String) this.redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX + skuId);

        //  判断skuIdStock 是否能够获取到数据
        if (StringUtils.isEmpty(skuIdStock)){
            //  已经售罄！ 通知其他兄弟节点！
            this.redisTemplate.convertAndSend("seckillpush",skuId+":0");
            return;
        }
        //  上述判断没有问题，将订单记录保存到缓存中！
        //  封装好的对象！
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setOrderStr(MD5.encrypt(userId+skuId));
        orderRecode.setNum(1);
        orderRecode.setUserId(userId);
        orderRecode.setSeckillGoods(this.getSeckillGoods(skuId));
        // hset key=seckill:orders field=userId value=orderRecode
        this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userId,orderRecode);

        //  更新库存！ redis ，mysql!
        this.updateStockCount(skuId);
    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        /*
        1.  判断用户是否在缓存中存在
        2.  判断用户是否抢单成功
        3.  判断用户是否下过订单
        4.  判断状态位
         */
        //  获取key
        Boolean flag = this.redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        // 判断
        if(flag){
            //  用户在缓存中存在！才有可能抢单成功！
//            Boolean result = this.redisTemplate.hasKey(RedisConst.SECKILL_ORDERS);
            Boolean result = this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            //  判断
            if(result){
                //  说明抢单成功！
                OrderRecode orderRecode = (OrderRecode) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }
        //  判断用户是否下过订单:  当用户点击去下单时，我们还需要将数据再次保存到缓存！
        //  RedisConst.SECKILL_ORDERS_USERS; 用户点击去下单之后的操作！将数据再次放入缓存的key！ field = userId value = orderId;
        Boolean isExist = this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        //  String orderId = (String) this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
        if (isExist){
            //  下过订单，提示查看我的订单！
            String orderId = (String) this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        //  默认排队中！
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    //  更新库存！
    @Transactional(rollbackFor = Exception.class)
    public void updateStockCount(Long skuId) {
        //  上锁：
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            //  缓存，数据库！
            Long count = this.redisTemplate.opsForList().size(RedisConst.SECKILL_STOCK_PREFIX + skuId);
            //  细节处理：为了避免频繁与数据库进行更新！
            if (count%2==0){
                //  开始更新：
                //  表示根据skuId 获取缓存数据！
                SeckillGoods seckillGoods = this.getSeckillGoods(skuId);
                seckillGoods.setStockCount(count.intValue());
                seckillGoodsMapper.updateById(seckillGoods);

                //  更新缓存！直接覆盖！
                this.redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS,skuId.toString(),seckillGoods);
            }
        } catch (Exception e) {
            //  记录日志.....
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
