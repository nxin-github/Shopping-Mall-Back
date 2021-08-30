package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author：王木风
 * @date 2021/8/27 21:05
 * @description：
 */
@RestController
@RequestMapping("/mq")
@Api(tags = "mq操作")
public class MqController {
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 消息发送
     */
    //http://localhost:8282/mq/sendConfirm
    //  发送消息的控制器
    @ApiOperation(value = "测试发送消息")
    @GetMapping("sendConfirm")
    public Result sendConfirm() {
        //  发送消息！
        rabbitService.sendMessage("exchange.confirm", "routing.confirm6666", "来人了，开始接客吧...");
        //  默认返回
        return Result.ok();
    }

    @GetMapping("sendDeadLettle")
    @ApiOperation(value = "发送死信")
    public Result sendDeadLettle() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, "ok");
        System.out.println(sdf.format(new Date()) + " Delay sent.");
        return Result.ok();
    }

    @GetMapping("sendDelay")
    @ApiOperation(value = "发送延迟队列")
    public Result sendDelay(){
        //  记录一个时间！
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //  发送消息 因为没有设置消息的TTL
        //  this.rabbitService.sendMessage(DelayedMqConfig.exchange_delay,DelayedMqConfig.routing_delay,"主人，开门....");
        rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "主人，关门吧....", (message)->{
            //  设置消息的ttl
            message.getMessageProperties().setDelay(10000);
            return message;
        });
        System.out.println("发送消息的时间:\t"+sdf.format(new Date()));
        return Result.ok();
    }
}
