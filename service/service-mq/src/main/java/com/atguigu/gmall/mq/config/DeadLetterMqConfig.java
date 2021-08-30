package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @Author：王木风
 * @date 2021/8/28 9:04
 * @description：
 */
@Configuration
public class DeadLetterMqConfig {
    //  定义变量
    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";

    // 将这个交换机注入到spring 容器中！
    @Bean
    public DirectExchange exchange(){
        //  返回
        //  最后一个参数：传递的是map 如果在交换机中要设置一些其他的参数设置，可以通过map 实现！
        return new DirectExchange(exchange_dead,true,false);
    }

    //  队列1
    @Bean
    public Queue queue1(){
        //  实现延迟队列！
        //  设置消息的过期时间：
        HashMap<String, Object> map = new HashMap<>();
        //  10秒钟！ 24*60*60*1000
        map.put("x-message-ttl",10000);
        //  绑定其他参数
        map.put("x-dead-letter-exchange",exchange_dead);
        map.put("x-dead-letter-routing-key",routing_dead_2);
        //  返回
        //  第三个参数：表示是否排外
        return new Queue(queue_dead_1,true,false,false,map);
    }

    //  设置绑定关系1
    @Bean
    public Binding binding1(){
        //  返回数据
        return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
    }

    //  声明第二个队列
    @Bean
    public Queue queue2(){
        //  只是一个单纯的队列！
        return new Queue(queue_dead_2,true,false,false);
    }

    //  设置绑定关系2
    @Bean
    public Binding binding2(){
        //  返回数据
        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }

}
