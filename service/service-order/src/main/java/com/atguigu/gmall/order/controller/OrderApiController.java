package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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
    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 确认订单
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "确认订单")
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request){
//        获取userId
        String userId = AuthContextHolder.getUserId(request);

        Map<Object, Object> FutureMap = new HashMap<>();

        CompletableFuture<Void> userAddressList1 = CompletableFuture.runAsync(() -> {
            List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
            FutureMap.put("userAddressList", userAddressList);
        }, poolExecutor);

        CompletableFuture<Void> cartInfoList1 = CompletableFuture.runAsync(() -> {
            List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);
            FutureMap.put("cartInfoList", cartInfoList);
        }, poolExecutor);

        CompletableFuture.allOf(userAddressList1,cartInfoList1).join();
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();

        List<CartInfo> cartInfoList = (List<CartInfo>) FutureMap.get("cartInfoList");

        cartInfoList.forEach(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());

            // 添加到集合
            detailArrayList.add(orderDetail);
        });

        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList",(List<UserAddress>) FutureMap.get("userAddressList"));
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
        Boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            //  不正常，不能提交的！
            return Result.fail().message("不能重复提交订单!");
        }
        //  删除流水号
        orderService.deleteTradeNo(userId);

        List<String> errorList = new ArrayList<>();
        List<CompletableFuture> futureList = new ArrayList<>();
        // 验证库存：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            CompletableFuture<Void> checkStockCompletableFuture = CompletableFuture.runAsync(() -> {
                // 验证库存：
                boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!result) {
                    errorList.add(orderDetail.getSkuName() + "库存不足！");
                }
            }, poolExecutor);
            futureList.add(checkStockCompletableFuture);

            CompletableFuture<Void> checkPriceCompletableFuture = CompletableFuture.runAsync(() -> {
                // 验证价格：
                BigDecimal skuPrice = productFeignClient.getProce(orderDetail.getSkuId());
                if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                    // 重新查询价格！
                    cartFeignClient.loadCartCache(userId);
                    errorList.add(orderDetail.getSkuName() + "价格有变动！");
                }
            }, poolExecutor);
            futureList.add(checkPriceCompletableFuture);
        }

        //合并线程
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        if(errorList.size() > 0) {
            return Result.fail().message(StringUtils.join(errorList, ","));
        }
        // 验证通过，保存订单！
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }
}
