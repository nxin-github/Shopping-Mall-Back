package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author 王木风
 */
@FeignClient(value = "service-order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    //  数据接口：
    @GetMapping("/api/order/auth/trade")
    Result<Map<String, Object>> trade();

    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId);

    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo);

}
