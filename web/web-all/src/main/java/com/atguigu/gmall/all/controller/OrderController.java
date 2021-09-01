package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author 王木风
 */
@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    //  http://order.gmall.com/trade.html
    @GetMapping("trade.html")
    public String trade(Model model){

        //  远程调用获取数据
        Result<Map<String, Object>> result = orderFeignClient.trade();
        //  保存数据
        model.addAllAttributes(result.getData());

        //  返回下单页面
        return "order/trade";
    }
}
