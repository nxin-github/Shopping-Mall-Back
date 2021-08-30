package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author：王木风
 * @date 2021/8/27 21:09
 * @description：
 */
@Configuration
public class ConfirmReceiver {
    //  监听者，消费者！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    public void getMsg(String msg, Message message, Channel channel){
        System.out.println("接收的消息：\t"+msg);
        System.out.println("接收的消息：\t"+ new String(message.getBody()));

        //  手动确认 第二个参数表示是否批量确认！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
