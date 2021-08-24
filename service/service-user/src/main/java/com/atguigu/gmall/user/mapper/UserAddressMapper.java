package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author：王木风
 * @date 2021/8/24 19:05
 * @description：获取用户地址mapper
 */
@Mapper
@Repository
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
