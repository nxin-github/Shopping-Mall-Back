package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @Author：王木风
 * @date 2021/8/20 8:46
 * @description：
 */
public interface UserService {
    /**
     * 登录方法
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);
}
