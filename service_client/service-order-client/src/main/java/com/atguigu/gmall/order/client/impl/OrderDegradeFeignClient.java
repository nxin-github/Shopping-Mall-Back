package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/24 20:44
 * @description：
 */
@Component
public class OrderDegradeFeignClient implements OrderFeignClient {

    @Override
    public Result<Map<String, Object>> trade() {
        return Result.fail();
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }
}
