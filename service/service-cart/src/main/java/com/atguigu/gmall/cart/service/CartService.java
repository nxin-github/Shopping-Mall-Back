package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/21 8:29
 * @description：
 */
public interface CartService {
    //  添加购物车数据接口！
    void addToCart(Long skuId,String userId,Integer skuNum);

    /**
     * 查询购物车
     * @param userId
     * @return List<CartInfo>
     */
    List<CartInfo> carList(String userId,String userTempId);
}
