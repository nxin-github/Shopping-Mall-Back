package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

/**
* @Author：王木风
* @date 2021/8/11 21:29
* @description：
*/
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
