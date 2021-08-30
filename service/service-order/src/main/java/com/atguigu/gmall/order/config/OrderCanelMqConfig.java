package com.atguigu.gmall.order.config;

import com.atguigu.gmall.common.*;
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/28 22:28
 * @description：
 */
@Configuration
public class OrderCanelMqConfig {
    @Bean
    public Queue delayQueue() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(MqConst.QUEUE_ORDER_CANCEL, true);
    }

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding bindingDelay() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();
    }
}
