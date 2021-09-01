package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author 王木风
 */
public interface SeckillGoodsService {

    /**
     * 返回全部列表
     * @return
     */
    List<SeckillGoods> findAll();

    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    SeckillGoods getSeckillGoods(Long id);

    /**
     * 预下单处理：
     * @param userId
     * @param skuId
     */
    void seckillOrder(String userId, Long skuId);

    /**
     * 检查状态
     * @param skuId
     * @param userId
     * @return
     */
    Result checkOrder(Long skuId, String userId);
}
