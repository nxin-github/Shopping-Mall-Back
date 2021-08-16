package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @Author：王木风
* @date 2021/8/16 20:00
* @description：
*/
public interface ApiService {

    /*
     *   功能描述:商品的名称，重量，默认图片，skuImageList图片信息
     *   @Param:skuid
     *   @Return:SkuInfo
     */
    SkuInfo getSkuById(Long skuId);

    /*
     *   功能描述:获取到category1Name，category2Name，category3Name 拼接 skuName!
     *   @Param:skuId
     *   @Return:
     */
    BaseCategoryView getCategoryView(Long category3Id);


    /*
     *   功能描述:根据skuid查询价格
     *   @Param:skuId
     *   @Return:BigDecimal
     */
    BigDecimal getProce(Long skuId);

    /*
     *   功能描述:回显销售属性 + 销售属性值 + 锁定功能
     *   @Param:skuId、spuId
     *   @Return:List<SpuSaleAttr>
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /*
     *   功能描述:获取销售属性值id雨skuid组成的map
     *   @Param:spuId
     *   @Return:Map
     */
    Map getSkuValueIdsMap(Long spuId);

    /*
     *   功能描述:获取全部分类信息
     *   @Param:
     *   @Return:
     */
    List<JSONObject> getBaseCategroyList();

    List<SearchAttr> getSearchaAttrs(Long skuId);
}
