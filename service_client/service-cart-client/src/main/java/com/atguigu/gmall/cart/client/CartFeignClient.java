package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author atguigu-mqx王木风
 **/
@FeignClient(value = "service-cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    //  将添加购物车的接口发布到feign 上！ 在这地方不需要写 HttpServletRequest!
    @GetMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    Result addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum);
}
