package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/9 20:22
 * @description：
 */

public interface ItemService {
    Map<String, Object> getItemById(Long skuId);
}
