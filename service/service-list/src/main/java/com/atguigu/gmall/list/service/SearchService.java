package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

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

    /**
     *   功能描述:热度排名
     *   @Param:skuId
     *   @Return:Result
     */
    void incrHotScore(Long skuId);

    /**
     * 搜索列表
     * @param searchParam
     * @return
     * @throws IOException
     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
