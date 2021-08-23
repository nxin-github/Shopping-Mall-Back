package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @PostMapping("addToCart/{skuId}/{skuNum}")
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
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "查询购物车")
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
//        获取用户id
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.carList(userId, userTempId);
        return Result.ok(cartInfoList);
    }

    /*
     *   功能描述:更改选中状态
     *   @Param:HttpServiceRequest request
     *   @Param:Long skuId
     *   @Param:Integer isChecked
     *   @Return:Result
     */
    @ApiOperation("更改购物项选中状态")
    @PutMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId, @PathVariable Integer isChecked, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.checkCart(skuId, isChecked, userId);
        return Result.ok();
    }

    /*
     *   功能描述:删除购物车
     *   @Param:Long skuId
     *   @Param:HttpServletRequest request
     *   @Return:Result
     */
    @ApiOperation(value = "删除购物车")
    @DeleteMapping("/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId, HttpServletRequest request) {
        //  删除购物车，在登录，未登录的情况下都可以操作！
        //  跟用户Id 有关系！
        String userId = AuthContextHolder.getUserId(request);
        //  获取临时用户Id
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用删除购物车数据的方法
        cartService.deleteCart(skuId,userId);
        return Result.ok();
    }
}
