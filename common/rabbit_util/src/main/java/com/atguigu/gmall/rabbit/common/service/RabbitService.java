package com.atguigu.gmall.rabbit.common.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送延迟消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message, int delayTime) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(delayTime*1000);
                return message;
            }
        });
        return true;
    }

    //  发送普通消息
    public boolean sendMessage(String exchange, String routingKey, Object message){
        //  发送消息方法！
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        //  默认返回
        return true;
    }
}
