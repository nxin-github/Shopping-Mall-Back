package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/24 19:03
 * @description：获取用户地址实现类
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    private UserAddressMapper userAddressMapper;

    /*
     *   功能描述:获取用户地址
     *   @Param:Long userId
     *   @Return:List<UserAddress>
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        return userAddressMapper.selectList(queryWrapper);
    }
}
