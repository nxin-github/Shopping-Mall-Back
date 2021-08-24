package com.atguigu.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author：王木风
 * @date 2021/8/24 19:51
 * @description：启动类
 */
@ComponentScan("com.atguigu.gmall")
@SpringBootApplication
@EnableFeignClients("com.atguigu.gmall")
@EnableDiscoveryClient
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}
