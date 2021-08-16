package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.mapper.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author：王木风
 * @date 2021/8/16 19:23
 * @description：
 */

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ThreadPoolExecutor poolExecutor;

    /*
     *   功能描述:需要获得：1、sku基本信息
     *                  2、sku分类信息
     *                  3、sku品牌信息
     *                  4、sku对应平台属性
     *   @Param:
     *   @Return:
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
//      sku基本信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SkuInfo skuInfo = productFeignClient.getSkuById(skuId);
                goods.setId(skuInfo.getId());
                goods.setTitle(skuInfo.getSkuName()); // skuName
                goods.setPrice(skuInfo.getPrice().doubleValue()); // 数据类型转换
                goods.setDefaultImg(skuInfo.getSkuDefaultImg());
                goods.setCreateTime(new Date());
                return skuInfo;
            } catch (Exception e) {
                System.out.println("SearchServiceImpl中的upperGoods方法的skuInfoCompletableFuture线程出错了");
            }
            return null;
        }, poolExecutor);
//        品牌信息
        CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }, poolExecutor);
//        sku分类信息
        CompletableFuture<Void> CategoryCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }, poolExecutor);
//        sku对应平台属性
        CompletableFuture<Void> searchaAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SearchAttr> searchaAttrs = productFeignClient.getSearchaAttrs(skuId);
            goods.setAttrs(searchaAttrs);
        }, poolExecutor);
        CompletableFuture.allOf(skuInfoCompletableFuture, trademarkCompletableFuture, CategoryCompletableFuture, searchaAttrCompletableFuture);
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }
}
