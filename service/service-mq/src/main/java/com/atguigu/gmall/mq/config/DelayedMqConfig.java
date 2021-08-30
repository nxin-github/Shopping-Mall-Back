package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;


/**
 * @author atguigu-mqx
 */
@Configuration
public class DelayedMqConfig {

    //  定义变量
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    //  创建队列
    @Bean
    public Queue delayQueue(){
        //  创建并返回
        return new Queue(queue_delay_1,true,false,false);
    }

    //  创建一个交换机
    @Bean
    public CustomExchange delayExchange(){
        //  设置交换机类型等参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type","direct");
        //  创建对象并返回
        return new CustomExchange(exchange_delay,"x-delayed-message",true,false,map);
    }

    //  定义一个绑定关系！
    @Bean
    public Binding delayBinding(){
        //  return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
        //  特殊的返回
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(routing_delay).noargs();
    }

}
