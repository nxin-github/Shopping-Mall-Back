package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/21 8:58
 * @description：
 */
@Api(tags = "购物车操作")
@RestController
@RequestMapping("api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    /*
     *   功能描述:添加到购物车
     *   @Param:skuId
     *   @Param:skuNum
     *   @Return:result
     */
    @ApiOperation(value = "添加购物车")
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {//未登录
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.addToCart(skuId, userId, skuNum);
        return Result.ok();
    }

    /**
     * 查询购物车
     * @param request
     * @return
     */
    @ApiOperation(value = "查询购物车")
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
//        获取用户id
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.carList(userId,userTempId);
        return Result.ok(cartInfoList);
    }
}
