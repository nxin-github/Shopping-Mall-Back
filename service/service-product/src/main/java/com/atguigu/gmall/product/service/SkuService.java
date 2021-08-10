package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/6 19:41
 * @description：关于sku的接口管理
 */
public interface SkuService {
    /*
     *   功能描述:根据spuId获取图片列表
     *   @Param:spuId
     *   @Return:
     */
    List<SpuImage> getImageBySpuId(Long spuId);

    /*
     *   功能描述:根据spuId获取销售属性
     *   @Param:spuId
     *   @Return:
     */
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    /*
     *   功能描述:添加sku
     *   @Param:SkuInfo
     *   @Return:
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /*
     *   功能描述:获取sku分页列表
     *   @Param:page：第几页
                limit：每页数量
     *   @Return:
     */
    IPage getSkuPage(Long page, Long limit);

    /*
     *   功能描述:上架
     *   @Param:skuId
     *   @Return:
     */
    void onSale(Long skuId);

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
}
