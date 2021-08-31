package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
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
 * @Author：王木风
 * @date 2021/8/31 21:24
 * @description：
 */
@Component
public class SeckillReceiver {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importDataToRedis(String msg, Message message, Channel channel) {
        /*
        1.  查询哪些商品是属于今天的秒杀商品！
               new Date();  1;  剩余库存>0;
        2.  将秒杀商品存储到redis ，分析如何存储？ 使用哪种数据类型，以及key ！

        3.  如何控制库存超买？ --- 存储一个redis数据类型中！

         */
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("status","1");
        seckillGoodsQueryWrapper.gt("stock_count","0");
        seckillGoodsQueryWrapper.eq("date_format(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
        for (SeckillGoods seckillGoods : seckillGoodsList) {
//  key = seckill:goods  field = skuId.toString(); value = SeckillGoods
            String seckillKey = RedisConst.SECKILL_GOODS;
            //  如果缓存中有了field ，则还需要存储么?
            Boolean flag = this.redisTemplate.boundHashOps(seckillKey).hasKey(seckillGoods.getSkuId().toString());
            if (flag){
                //  表示缓存中有数据?
                //  break; // 结束当前循环！ //   return; 结束！ // continue; 结束本次循环！
                continue;
            }
            //  如何防止商品超卖！ 将商品的数量存储在list 队列中！
            //  lpush/rpush  lpop/rpop
            for (Integer i = 0; i < seckillGoods.getNum(); i++) { // 10
                //  key = seckill:stock:skuId skuId
                //  seckill:stock:44  44
                redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX+seckillGoods.getSkuId(),seckillGoods.getSkuId().toString());
            }
            //  每个商品的flag 状态位： 都是1
            redisTemplate.convertAndSend("seckillpush",seckillGoods.getSkuId()+":1");
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
