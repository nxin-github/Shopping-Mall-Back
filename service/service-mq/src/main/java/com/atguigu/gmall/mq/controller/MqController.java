package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MqController {
    @Autowired
    private RabbitService rabbitService;

    /**
     * 消息发送
     */
    //http://localhost:8282/mq/sendConfirm
    @GetMapping("sendConfirm")
    public Result sendConfirm() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitService.sendMessage("exchange.confirm", "routing.confirm", sdf.format(new Date()));
        return Result.ok();
    }
}
