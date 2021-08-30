package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

/**
 * @Author：王木风
 * @date 2021/8/29 22:46
 * @description：
 */
public interface AlipayService {
    String createaliPay(Long orderId) throws AlipayApiException;

    boolean refund(Long orderId);

    Boolean closePay(Long orderId);

    Boolean checkPayment(Long orderId);
}
