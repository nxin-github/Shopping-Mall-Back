package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author 王木风
 */
@Component
public class SeckillReceiver {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    //  监听定时任务发送过来的消息！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importDataToRedis(String msg, Message message , Channel channel){
        //  编写业务逻辑：
        /*
        1.  查询哪些商品是属于今天的秒杀商品！
               new Date();  1;  剩余库存>0;
        2.  将秒杀商品存储到redis ，分析如何存储？ 使用哪种数据类型，以及key ！

        3.  如何控制库存超买？ --- 存储一个redis数据类型中！

         */
        try {
            QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
            seckillGoodsQueryWrapper.eq("status","1");
            seckillGoodsQueryWrapper.gt("stock_count","0");
            seckillGoodsQueryWrapper.eq("date_format(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
            //  循环遍历插入redis！
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //  分析 redis 使用哪种数据类型 hash hset key field value hget key field hvals
                //  key = seckill:goods  field = skuId.toString(); value = SeckillGoods
                String seckillKey = RedisConst.SECKILL_GOODS;
                //  如果缓存中有了field ，则还需要存储么?
                Boolean flag = this.redisTemplate.boundHashOps(seckillKey).hasKey(seckillGoods.getSkuId().toString());
                if (flag){
                    //  表示缓存中有数据?
                    //  break; // 结束当前循环！ //   return; 结束！ // continue; 结束本次循环！
                    continue;
                }

                //  flag = false;
                //  this.redisTemplate.boundHashOps(seckillKey).put(seckillGoods.getSkuId().toString(),seckillGoods);
                this.redisTemplate.opsForHash().put(seckillKey,seckillGoods.getSkuId().toString(),seckillGoods);
                //  如何防止商品超卖！ 将商品的数量存储在list 队列中！
                //  lpush/rpush  lpop/rpop
                for (Integer i = 0; i < seckillGoods.getNum(); i++) { // 10
                    //  key = seckill:stock:skuId skuId
                    //  seckill:stock:44  44
                    this.redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId(),seckillGoods.getSkuId().toString());
                }
                //  每个商品的flag 状态位： 都是1
                this.redisTemplate.convertAndSend("seckillpush",seckillGoods.getSkuId()+":1");
                //  每个微服务应该订阅这个管道！

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //  秒杀监听：
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckillQueue(UserRecode userRecode, Message message, Channel channel){
        //  获取数据：
        try {
            if (userRecode!=null){
                //  执行的是预下单：
                seckillGoodsService.seckillOrder(userRecode.getUserId(),userRecode.getSkuId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_18}
    ))
    public void deleteData(String msg , Message message, Channel channel){
        try {
            //  秒杀结束：的所有商品！
            QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
            seckillGoodsQueryWrapper.eq("status",1);
            seckillGoodsQueryWrapper.le("end_time",new Date());
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
            //  查询到所有的结束秒杀商品！
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //  删除redis 秒杀商品！
                this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).delete(seckillGoods.getSkuId().toString());
                //  删除redis 对应的skuNum
                this.redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId());
            }
            //  redisTemplate.delete(RedisConst.SECKILL_GOODS);
            //  预下单数据：
            redisTemplate.delete(RedisConst.SECKILL_ORDERS);
            //  删除真正下单数据
            redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);

            //  对于数据库要做一个更新操作！
            SeckillGoods seckillGoods = new SeckillGoods();
            seckillGoods.setStatus("0"); // 秒杀结束：
            seckillGoodsMapper.update(seckillGoods,seckillGoodsQueryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
