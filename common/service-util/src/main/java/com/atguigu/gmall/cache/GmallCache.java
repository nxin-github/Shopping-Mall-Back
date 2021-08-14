package com.atguigu.gmall.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author：王木风
 * @date 2021/8/14 12:27
 * @description：自定义缓存帮助注解
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GmallCache {
    /**
     * 缓存key的前缀
     * @return
     */
    String prefix() default "cache";
}
