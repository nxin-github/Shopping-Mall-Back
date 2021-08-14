package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author：王木风
 * @date 2021/8/9 20:22
 * @description：
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor poolExecutor;

    //  利用异步编排优化
    @Override
    public Map<String, Object> getItemById(Long skuId) {
        //  声明对象
        Map<String, Object> result = new HashMap<>();

        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuById(skuId);
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, poolExecutor);

//        保存商品价格
        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getProce(skuId);
            result.put("price", skuPrice);
        }, poolExecutor);
//      保存分类数据
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        },poolExecutor);
        //  获取到销售属性+销售属性值+锁定
        CompletableFuture<Void> supCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        },poolExecutor);
        //  获取到销售属性值Id 与 skuId 组成的map 集合
        CompletableFuture<Void> jsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            Map idsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //  需要将这个map 转换为Json 字符串！
            String idsJson = JSON.toJSONString(idsMap);
            result.put("valuesSkuJson", idsJson);
        },poolExecutor);

        //  获取到数据之后，将数据进行整合！allOf !
        //  汇总数据
        CompletableFuture.allOf(skuInfoCompletableFuture,
                skuPriceCompletableFuture,
                categoryViewCompletableFuture,
                supCompletableFuture,
                jsonCompletableFuture).join();
        //  最终返回的map！
        return result;
    }

        // 用户通过网关 -- web-all --feign{api/item/{skuId}}--> service-item --feign{url}--> service-product
    /*@Override
    public Map<String, Object> getItemById(Long skuId) {
        //  声明对象
        Map<String, Object> result = new HashMap<>();
        //  调用数据：
        //  skuName , skuId ,defaultImage ..... skuImageList
        SkuInfo skuInfo = productFeignClient.getSkuById(skuId);
        //  获取价格
        BigDecimal proce = productFeignClient.getProce(skuId);
        //  想一件事！ map 集合的key 应该是谁？ key 就是页面需要的${key} ,现在没有页面！ 从课件找！
        result.put("skuInfo",skuInfo);
        result.put("price",proce);
        if (skuInfo != null) {
            //  获取分类数据
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView",categoryView);
            //  获取到销售属性+销售属性值+锁定
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);

            //  获取到销售属性值Id 与 skuId 组成的map 集合
            Map idsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //  需要将这个map 转换为Json 字符串！
            String idsJson = JSON.toJSONString(idsMap);
            result.put("valuesSkuJson",idsJson);
        }
        return result;
    }*/
    }
