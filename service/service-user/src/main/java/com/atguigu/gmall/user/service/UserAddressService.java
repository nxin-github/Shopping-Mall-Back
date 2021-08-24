package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/24 19:03
 * @description：获取用户地址接口
 */
public interface UserAddressService {
    /*
     *   功能描述:获取用户地址
     *   @Param:Long userId
     *   @Return:List<UserAddress>
     */
    List<UserAddress> findUserAddressListByUserId(String userId);
}
