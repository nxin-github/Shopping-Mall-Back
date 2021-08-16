package com.atguigu.gmall.list.service;

/**
 * @Author：王木风
 * @date 2021/8/16 19:23
 * @description：
 */
public interface SearchService {
    /*
     *   功能描述:上架
     *   @Param:sku的id
     *   @Return:
     */
    void upperGoods(Long skuId);

    /*
     *   功能描述:下架
     *   @Param:sku的id
     *   @Return:
     */
    void lowerGoods(Long skuId);
}
