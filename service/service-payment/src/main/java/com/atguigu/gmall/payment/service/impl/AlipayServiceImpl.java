package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author：王木风
 * @date 2021/8/29 22:47
 * @description：
 */
@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public String createaliPay(Long orderId) throws AlipayApiException {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
//        保存交易记录
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());
//        生产二维码
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应request
//        同步回调
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
//        异步回调
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 参数
        // 声明一个map 集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject","test");

        alipayRequest.setBizContent(JSON.toJSONString(map));

        return alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单;
    }

    @Override
    public boolean refund(Long orderId) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "颜色浅了点");

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            // 更新交易记录 ： 关闭
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.ClOSED.name());
            paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(),PaymentType.ALIPAY.name(),paymentInfo);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean closePay(Long orderId) {
        OrderInfo orderInfo = this.orderFeignClient.getOrderInfo(orderId);
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        //  bizContent.put("trade_no", paymentInfo.getTradeNo());
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    @Override
    public Boolean checkPayment(Long orderId) {
        OrderInfo orderInfo = this.orderFeignClient.getOrderInfo(orderId);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //        request.setBizContent("{" +
        //                "\"out_trade_no\":\"20150320010101001\"," +
        //                "\"trade_no\":\"2014112611001004680 073956707\"," +
        //                "\"org_pid\":\"2088101117952222\"," +
        //                "      \"query_options\":[" +
        //                "        \"trade_settle_info\"" +
        //                "      ]" +
        //                "  }");
        JSONObject bizContent = new JSONObject();
        //  bizContent.put("trade_no", paymentInfo.getTradeNo());
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }
}
