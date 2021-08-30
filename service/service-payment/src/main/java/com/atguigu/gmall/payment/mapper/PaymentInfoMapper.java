package com.atguigu.gmall.payment.mapper;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author：王木风
 * @date 2021/8/29 21:39
 * @description：
 */
@Mapper
@Repository
public interface PaymentInfoMapper extends BaseMapper<PaymentInfo> {

}
