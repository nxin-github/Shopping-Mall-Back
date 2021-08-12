package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @Author：王木风
 * @date 2021/8/11 21:27
 * @description：
 */
@FeignClient(value = "service-item" , fallback = ItemDegradeFeignClient.class)
//@FeignClient(value = "service-item")
public interface ItemFeignClient {

    //  将service-item 中的控制器url 地址发布到feign上！

    /**
     * @param skuId
     * @return
     */
    @GetMapping("/api/item/{skuId}")
    Result getItem(@PathVariable("skuId") Long skuId);
}