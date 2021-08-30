package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author atguigu-mqx
 */
@Component
public class OrderReceiver {
    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @Autowired
    private OrderService orderService;

    //  必须要制作一个配置类！
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) {
        /*
        1.  先判断当前的订单Id 是否为空！
            true:
                不为空，根据这个orderId,将订单取消！
                本质：更改状态！
            false:
        2.  手动确认消息！
         */
        //  判断
        try {
            if (orderId != null) {
                //  根据这个订单id来获取订单信息！
                //  select * from order_info where id = orderId;
                OrderInfo orderInfo = orderService.getById(orderId);
                //  判断是否支付了
                if ("UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())) {
//                    判断是否有本地交易记录
                    PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                    //  判断
                    if (paymentInfo != null && "UNPAID".equals(paymentInfo.getPaymentStatus())) {
//                      说明有orderInfo 同时有本地交易记录 判断是否与支付宝产生了交易记录
                        Boolean flag = paymentFeignClient.checkPayment(orderId);
                        if (flag) {
//                            有支付宝的交易记录
                            Boolean result = paymentFeignClient.closePay(orderId);
                            //  true:
                            if (result){
                                //  可以关闭，表示扫描了二维码，但是没有支付！
                                //  只需要关闭orderInfo 与 paymentInfo！
                                orderService.execExpiredOrder(orderId,"2");
                            }else {
                                //  可以关闭，表示扫描了二维码有支付成功！
                                //  支付成功，会走异步回调！ paymentService.paySuccess(outTradeNo,PaymentType.ALIPAY.name(),paramsMap);
                            }
                        }else {
                            //  只需要关闭orderInfo 与paymentInfo！
                            orderService.execExpiredOrder(orderId,"2");
                        }
                    }else {
                        //  关闭订单！说明只有orderInfo
                        orderService.execExpiredOrder(orderId,"1");
                    }
                }
            }
        } catch (Exception e) {
            //  如果消费未成功，，则记录日志，数据！
            e.printStackTrace();
        }
        //  手动确认消息 如果不确认，有可能会到消息残留。
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void updateOrderStatus(Long orderId , Message message, Channel channel) {
        //  判断：
        try {
            if (orderId!=null){
                OrderInfo orderInfo = orderService.getById(orderId);
                //  订单处于未支付的时候，更新为支付！
                if ("UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())){
                    //  执行更新状态！
                    orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                    //  发送消息给库存！ 需要orderInfo 中的数据，同时还需要orderDetail 中的数据！
                    orderService.sendOrderStatus(orderId);
                }
            }
        } catch (Exception e) {
            //  日志记录，将数据写入数据库，做消息补偿.....
            e.printStackTrace();
        }
        //  手动确认消息！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER, durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void updateOrder(String strJson, Message message, Channel channel) throws IOException {
        try {
            if (!StringUtils.isEmpty(strJson)) {
                //  将字符串转换为Map
                Map map = JSON.parseObject(strJson, Map.class);
                String orderId = (String) map.get("orderId");
                String status = (String) map.get("status");
                if ("DEDUCTED".equals(status)) {
                    orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
                } else {
                    //  出现了库存异常：有可能是超卖！
                    //  记录信息： orderId ,放入一个表中进行存储！ 远程补货！ 再次发送一个消息：更新当前的减库存状态! 数据的最终一致性！
                    //  分布式事务：解决方案！
                    //  亲，小哥哥你等会。我们正在补货！ 人工客服！ 退款！
                    orderService.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.STOCK_EXCEPTION);
                }
            }
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
