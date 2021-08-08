package com.atguigu.gmall.product.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
* @Author：王木风
* @date 2021/8/6 21:25
* @description：
*/public interface BaseTrademarkService{
    /*
     *   功能描述:获取品牌分页列表
     *   @Param:page：第几页
                limit：每页数量
     *   @Return:
     */
    IPage<BaseTrademark> getBaseTrademarkPage(Long page, Long limit);

    /*
     *   功能描述:添加品牌
     *   @Param:BaseTrademark
     *   @Return:
     */
    void save(BaseTrademark baseTrademark);

    /*
     *   功能描述:修改品牌
     *   @Param:baseTrademark的json字符串
     *   @Return:
     */
    void update(BaseTrademark baseTrademark);

    /*
     *   功能描述:4、删除品牌
     *   @Param:品牌Id
     *   @Return:
     */
    void remove(Long id);

    /*
     *   功能描述:5、根据Id获取品牌
     *   @Param:品牌Id
     *   @Return:
     */
    BaseTrademark getById(Long id);
}
