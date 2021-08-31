package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/24 20:56
 * @description：
 */
public interface OrderService extends IService<OrderInfo> {
    /**
     * 保存订单
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 比较流水号
     * @param userId 获取缓存中的流水号
     * @param tradeCodeNo   页面传递过来的流水号
     * @return
     */
    boolean checkTradeCode(String userId, String tradeCodeNo);


    /**
     * 删除流水号
     * @param userId
     */
    void deleteTradeNo(String userId);

    /**
     * 生产流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(Long skuId, Integer skuNum);

    /**
     * 取消订单
     *
     * @param orderId
     */
    void execExpiredOrder(Long orderId ,String flag);

    /**
     * 取消订单
     * @param orderId
     */
    void execExpiredOrder(Long orderId);

    /**
     * 更新订单的状态！
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    /**
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);

    /**
     * 发送消息给库存系统：
     * @param orderId
     */
    void sendOrderStatus(Long orderId);

    /**
     * 将orderInfo 转换为Map 集合
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆单方法
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
