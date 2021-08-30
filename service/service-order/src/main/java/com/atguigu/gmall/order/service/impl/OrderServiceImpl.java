package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${ware.url}")
    private String WARE_URL;

    @Autowired
    private RabbitService rabbitService;

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
        Long orderId = orderInfo.getId();
//  发送想消息： 内容是根据消费者要做什么功能来决定的?
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL, MqConst.ROUTING_ORDER_CANCEL, orderId, MqConst.DELAY_TIME);
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

    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        // 远程调用http://localhost:9001/hasStock?skuId=10221&num=2
        String result = HttpClientUtil.doGet(WARE_URL + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        boolean b = "1".equals(result);
        return b;
    }

    @Override
    public void execExpiredOrder(Long orderId, String flag) {
        //  必须关闭orderInfo
        updateOrderStatus(orderId, ProcessStatus.CLOSED);
        //  发送消息关闭paymentInfo
        if("2".equals(flag)){
            //  发送消息更新paymentInfo 记录！
            this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,MqConst.ROUTING_PAYMENT_CLOSE,orderId);
        }
    }

    //  后续会根据订单Id 进行更新状态！ 更新成支付，发货等！ 做方法抽取！
    //  从进度状态中能够获取订单状态！
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        //  更新数据！
        orderInfoMapper.updateById(orderInfo);
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    @Override
    public void sendOrderStatus(Long orderId) {
        updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        String wareJson = initWareOrder(orderId);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK, MqConst.ROUTING_WARE_STOCK, wareJson);
    }

    // 根据orderId 获取json 字符串
    private String initWareOrder(Long orderId) {
        // 通过orderId 获取orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);

        // 将orderInfo中部分数据转换为Map
        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }

    @Override
    public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());

        ArrayList<Map> mapArrayList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> orderDetailMap = new HashMap<>();
            orderDetailMap.put("skuId", orderDetail.getSkuId());
            orderDetailMap.put("skuNum", orderDetail.getSkuNum());
            orderDetailMap.put("skuName", orderDetail.getSkuName());
            mapArrayList.add(orderDetailMap);
        }
        map.put("details", mapArrayList);
        return map;
    }

    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        //  写拆单业务逻辑！
        /*
        1.  知道原始订单是谁！
        2.  wareSkuMap [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}] 必须将其转换为我们能操作的对象！
        3.  创建新的子订单 new OrderInfo(); 并给子订单赋值！
        4.  将子订单保存到数据库中！
        5.  将子订单对象添加到这个集合中！
        6.  修改原始订单状态！
         */
        OrderInfo orderInfoOrigin = getOrderInfo(Long.parseLong(orderId));
//      将对象转换为List<Map>
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
//      循环这个集合
        for (Map map : mapList) {
//          获取到仓库id
            String wareId = (String) map.get("wareId");
//            获取仓库id对应订单明细
            List<String> skuIdList = (List<String>) map.get("skuIds");
//          创建一个新的子订单
            OrderInfo subOrderInfo = new OrderInfo();
//            属性拷贝
            BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
            //  注意事项：
            subOrderInfo.setId(null);
            //  赋值父订单Id
            subOrderInfo.setParentOrderId(Long.parseLong(orderId));
            //  赋值一个仓库Id
            subOrderInfo.setWareId(wareId);
            //  重新计算子订单价格：
            //  但是，它需要子订单的明细集合，才能调用这个方法计算！
            //  获取到原始订单明细
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
//             建立一个集合来存储子订单明细
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
                for (String skuId : skuIdList) {
                    if (orderDetail.getSkuId().longValue() == Long.parseLong(skuId)) {
                        orderDetails.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(orderDetails);
            subOrderInfo.sumTotalAmount();

            //  保存子订单
            saveOrderInfo(subOrderInfo);

//            将这个子订单添加到子订单集合
            subOrderInfoList.add(subOrderInfo);
        }
        updateOrderStatus(Long.parseLong(orderId), ProcessStatus.SPLIT);
        return subOrderInfoList;
    }
}
