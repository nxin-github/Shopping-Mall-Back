package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/29 21:39
 * @description：
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RabbitService rabbitService;


    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer integer = paymentInfoMapper.selectCount(queryWrapper);
        if (integer > 0) return;
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        //paymentInfo.setSubject("test");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());

        paymentInfoMapper.insert(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, String paymentType) {
        //   select * from payment_info where out_trade_no = ? and payment_type = ?
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQueryWrapper);
        //  返回对象
        return paymentInfo;
    }

    @Override
    public void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap) {
//  修改状态： 以支付PAID
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfoQuery = paymentInfoMapper.selectOne(paymentInfoQueryWrapper);
        //  为了防止服务器异常支付宝给我们发送多次通知，目的去重！
        if ("PAID".equals(paymentInfoQuery.getPaymentStatus()) ||
                "CLOSED".equals(paymentInfoQuery.getPaymentStatus())) {
            return;
        }
        //  修改状态：
        //  第一个参数表示:更新的内容，第二个表示更新条件
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTradeNo(paramsMap.get("trade_no"));
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramsMap.toString());
//  如果后续有继续更新状态的时候，可以做方法抽取！
        updatePaymentInfo(outTradeNo, paymentType, paymentInfo);

        //  发送消息给订单，通知订单更新状态！ 发送orderId
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,paymentInfoQuery.getOrderId());
    }

    //  更新交易记录
    public void updatePaymentInfo(String outTradeNo, String paymentType, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no",outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type",paymentType);
        paymentInfoMapper.update(paymentInfo,paymentInfoQueryWrapper);
    }
}
