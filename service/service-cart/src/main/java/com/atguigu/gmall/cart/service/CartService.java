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
     * @param: userId
     * @param: userTempId
     * @return List<CartInfo>
     */
    List<CartInfo> carList(String userId,String userTempId);

    /*
     *   功能描述:更改选中状态
     *   @Param:String userId
     *   @Param:Long skuId
     *   @Param:Integer isChecked
     *   @Return:Result
     */
    void checkCart(Long skuId, Integer isChecked, String userId);

    /*
     *   功能描述:删除购物车
     *   @Param:Long skuId
     *   @Param:String userId
     *   @Return:Result
     */
    void deleteCart(Long skuId, String userId);

    /*
     *   功能描述:根据userId查询购物车（为订单使用）
     *   @Param:Long userId
     *   @Return:List<CarInfo>
     */
    List<CartInfo> getCartCheckedList(String userId);
}
