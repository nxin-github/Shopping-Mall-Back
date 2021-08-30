package com.atguigu.gmall.rabbit.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author：王木风
 * @date 2021/8/27 14:16
 * @description：
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     *   功能描述:确认消息是否发送到交换机
     *   @Param:correlationData 可以承载消息
     *   @Param:ack 是否发送到交换机
     *   @Param:cause 原因
     *   @Return:
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("发送成功");
        } else {
            System.out.println("发送失败");
        }
    }

    /**
     *   功能描述:没有被正确消费才走这个
     *   @Param:message 消息
     *   @Param:replyCode 状态码
     *   @Param:replyText 消息
     *   @Param:exchange 消息
     *   @Param:routingKey 消息
     *   @Return:
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

    }

    // 修饰一个非静态的void（）方法,在服务器加载Servlet的时候运行，并且只会被服务器执行一次在构造函数之后执行，init（）方法之前执行。
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //指定 ConfirmCallback
        rabbitTemplate.setReturnCallback(this);             //指定 ReturnCallback
    }
}
