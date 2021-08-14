package com.atguigu.gmall.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Author：王木风
 * @date 2021/8/14 12:33
 * @description：缓存加锁的切面方法
 */
@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    //  切GmallCache注解
    @SneakyThrows//编译代码时自动捕获异常
    @Around("@annotation(com.atguigu.gmall.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) {
        Object object = new Object();
//        具体逻辑
//        获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallCache  = signature.getMethod().getAnnotation(GmallCache.class);
//        组成redis的key
        String prefix = gmallCache.prefix();
        Object[] args = joinPoint.getArgs();
        String key = prefix + args;
        try {
            //  从缓存中获取数据
            //  类似于skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            object = cacheHit(key,signature);
            if (object == null) {
                //缓存中没有  从数据库中获取数据
//                准备个锁
                String lockKey = prefix + "lock";
                RLock lock = redissonClient.getLock(lockKey);
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.MINUTES);
                if (result) {
//                    上锁成功
                    try {
                        //  表示执行方法体 getSkuInfoDB(skuId);
                        object = joinPoint.proceed(joinPoint.getArgs());
                        if (object == null) {
//                                数据库中没有
                            Object object1 = new Object();
                            redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(object1), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.MINUTES);
                            return object1;
                        } else {
                            redisTemplate.opsForValue().setIfAbsent(key, object, RedisConst.SKUKEY_TIMEOUT, TimeUnit.MINUTES);
                            return object;
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
//                    上锁失败
                    Thread.sleep(100);
                    return cacheAroundAdvice(joinPoint);
                }
            } else {
                return object;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return joinPoint.proceed(joinPoint.getArgs());
    }

    /**
     * 表示从缓存中获取数据
     *
     * @param key       缓存的key
     * @param signature 获取方法的返回值类型
     * @return
     */
    private Object cacheHit(String key, MethodSignature signature) {
        //  通过key 来获取缓存的数据
        String strJson = (String) redisTemplate.opsForValue().get(key);
        //  表示从缓存中获取到了数据
        if (!StringUtils.isEmpty(strJson)){
            //  字符串存储的数据是什么?   就是方法的返回值类型
            Class returnType = signature.getReturnType();
            //  将字符串变为当前的返回值类型
            return JSON.parseObject(strJson,returnType);
        }
        return null;
    }
}
