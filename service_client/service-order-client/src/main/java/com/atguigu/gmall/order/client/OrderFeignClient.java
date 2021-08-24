package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/24 20:43
 * @description：
 */
@FeignClient(value = "service-order", fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {
    @GetMapping("/api/order/auth/trade")
    Result<Map<String, Object>> trade();
}
