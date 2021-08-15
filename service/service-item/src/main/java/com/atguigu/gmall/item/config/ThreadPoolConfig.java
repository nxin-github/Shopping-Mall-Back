package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author atguigu-mqx
 */
//  自定义线程池
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        //  查询数据库！
        return new ThreadPoolExecutor(
               8,
               20,
               3L,
               TimeUnit.SECONDS,
               new ArrayBlockingQueue<>(5)
//                ,
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
