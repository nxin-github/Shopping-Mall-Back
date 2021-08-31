package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/29 23:03
 * @description：
 */
@Api(tags = "支付模块")
@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentService paymentService;

    @ApiOperation(value = "提交订单")
    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String aliPay(@PathVariable Long orderId) {
        String from = "";
        try {
            from = alipayService.createaliPay(orderId);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(from);
        return from;
    }

    /**
     * 支付宝回调
     *
     * @return
     */
    @ApiOperation(value = "支付宝回调")
    @RequestMapping("callback/return")
    public String callBack() {
        // 同步回调给用户展示信息
        return "redirect:" + AlipayConfig.return_order_url;
    }

    /**
     * 支付宝异步回调  必须使用内网穿透
     *
     * @param paramMap
     * @return
     */
    @ApiOperation(value = "支付宝异步回调")
    @RequestMapping("callback/notify")
    @ResponseBody
    public String alipayNotify(@RequestParam Map<String, String> paramMap) {
        System.out.println("回来了");
        boolean signVerified = false;//调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //  获取到支付宝传递过来的out_trade_no , total_amount , app_id;
        String outTradeNo = paramMap.get("out_trade_no");
        String status = paramMap.get("trade_status");
        if (signVerified) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //  只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            //  利用outTradeNo 查询交易记录
            //  select * from payment_info where out_trade_no = ? and payment_type = ?
            //  select * from payment_info where out_trade_no = ?
            PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
            //  判断
            if (paymentInfoQuery == null) {
                return "failure";
            }
            if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
                //  更新交易记录的状态！payment_status = PAID;
                paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY.name(), paramMap);
                return "success";
            } else {
                return "failure";
            }
        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
    }

    /*
     *   功能描述:发起退款
     *   @Param:
     *   @Return:
     */
    @ApiOperation(value = "发起退款")
    @RequestMapping("refund/{orderId}")
    @ResponseBody
    public Result refund(@PathVariable Long orderId) {
        // 调用退款接口
        boolean flag = alipayService.refund(orderId);

        return Result.ok(flag);
    }

    //    关闭支付宝交易
    @ApiOperation(value = "关闭支付宝交易")
    @RequestMapping("closePay/{orderId}")
    public Boolean closePay(@PathVariable Long orderId) {
        Boolean aBoolean = alipayService.closePay(orderId);
        return aBoolean;
    }

    //  查看交易状态！
    @ApiOperation(value = "查看交易状态")
    @RequestMapping("checkPayment/{orderId}")
    @ResponseBody
    public Boolean checkPayment(@PathVariable Long orderId) {
        Boolean flag = alipayService.checkPayment(orderId);
        return flag;
    }

//通过outTradeNo 来获取到PaymentInfo ：给取消订单业务使用！
    @GetMapping("getPaymentInfo/{outTradeNo}")
    @ResponseBody
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo){
        //  调用服务层方法
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        //  返回数据
        return paymentInfo;
    }
}
