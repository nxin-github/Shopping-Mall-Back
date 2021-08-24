package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @Author：王木风
 * @date 2021/8/24 20:58
 * @description：
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public Long saveOrderInfo(OrderInfo orderInfo) {
//        获取总金额
        orderInfo.sumTotalAmount();
//        设置订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
//        订单编号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
//        创建时间
        orderInfo.setCreateTime(new Date());

        // 设置过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
//        设置进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);

        //  订单明细：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetail.setCreateTime(new Date());
            orderDetailMapper.insert(orderDetail);
        }

        //  返回orderId
        return orderInfo.getId();
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String redisTradeNo =(String) redisTemplate.opsForValue().get(tradeNoKey);
        return tradeCodeNo.equals(redisTradeNo);
    }

    @Override
    public void deleteTradeNo(String userId) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        // 删除数据
        redisTemplate.delete(tradeNoKey);
    }

    @Override
    public String getTradeNo(String userId) {
        String tradeNoKey = "user:" + userId + ":tradeCode";
        // 定义一个流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }
}
