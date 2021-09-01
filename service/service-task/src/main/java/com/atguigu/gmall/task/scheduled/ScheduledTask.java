package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author：王木风
 * @date 2021/8/30 22:40
 * @description：
 */
@Component
@EnableScheduling//开启定时任务
public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;

    //    每隔十秒
    @Scheduled(cron = "0/10 * * * * ?")
    public void testTask() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "来活了");
    }

    @Scheduled(cron = "0/40 * * * * ?")
    public void delData(){
        //  System.out.println("人来的挺多.....服务不周请多担待...");
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_18,"客人走了，出来吧...！");
    }
}
