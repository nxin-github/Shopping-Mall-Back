package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.user.client.UserFeignClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author：王木风
 * @date 2021/8/24 19:56
 * @description：订单接口类
 */
@Api(tags = "订单接口类")
@RequestMapping("api/order")
@RestController
public class OrderApiController {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private ThreadPoolExecutor poolExecutor;
    @Autowired
    private OrderService orderService;

    /**
     * 确认订单
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "确认订单")
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) throws ExecutionException, InterruptedException {
//        获取userId
        String userId = AuthContextHolder.getUserId(request);

        CompletableFuture<List<UserAddress>> userAddressListFuture = CompletableFuture.supplyAsync(() -> {
            return userFeignClient.findUserAddressListByUserId(userId);
        }, poolExecutor);

        CompletableFuture<List<CartInfo>> cartInfoListFuture = CompletableFuture.supplyAsync(() -> {
            return cartFeignClient.getCartCheckedList(userId);
        }, poolExecutor);

        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();

        List<CartInfo> cartInfoList = cartInfoListFuture.get();
        cartInfoList.stream().forEach(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());

            // 添加到集合
            detailArrayList.add(orderDetail);
        });
        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressListFuture.get());
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalNum", detailArrayList.size());
        result.put("totalAmount", orderInfo.getTotalAmount());
//  存储流水号
        result.put("tradeNo",orderService.getTradeNo(userId));
        return Result.ok(result);
    }

    /**
     * 提交订单
     * @param orderInfo
     * @param request
     * @return
     */
    @ApiOperation(value = "提交订单")
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        //  获取到页面的流水号
        String tradeNo = request.getParameter("tradeNo");
        //  比较
        Boolean flag = orderService.checkTradeCode(tradeNo, userId);
        if (!flag){
            //  不正常，不能提交的！
            return Result.fail().message("不能重复提交订单!");
        }
        //  删除流水号
        orderService.deleteTradeNo(userId);
        // 验证通过，保存订单！
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }
}
