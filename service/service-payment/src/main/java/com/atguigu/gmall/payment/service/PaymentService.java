package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/29 21:38
 * @description：
 */
public interface PaymentService {
    /**
     * 保存交易记录
     * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    void savePaymentInfo(OrderInfo orderInfo, String paymentType);

    //  查询交易记录
    PaymentInfo getPaymentInfo(String outTradeNo, String paymentType);

    void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap);

    void updatePaymentInfo(String outTradeNo, String paymentType, PaymentInfo paymentInfo);
}
