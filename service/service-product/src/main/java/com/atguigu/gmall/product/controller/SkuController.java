package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.redisson.transaction.operation.map.MapOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/7 13:41
 * @description：spk操作接口
 */
@Api(tags = "spk操作接口")
@RestController
@RequestMapping("admin/product")
public class SkuController {
    @Autowired
    private SkuService skuService;

    /*
     *   功能描述:根据spuId获取图片列表
     *   @Param:spuId
     *   @Return:
     */
    @ApiOperation(value = "根据spuId获取图片列表")
    @GetMapping("spuImageList/{spuId}")
    public Result getImageBySpuId(@PathVariable Long spuId) {
        List<SpuImage> spuImageList = skuService.getImageBySpuId(spuId);
        return Result.ok(spuImageList);
    }

    /*
     *   功能描述:根据spuId获取销售属性
     *   @Param:spuId
     *   @Return:
     */
    @ApiOperation(value = "根据spuId获取销售属性")
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs = skuService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrs);
    }

    /*
     *   功能描述:添加sku
     *   @Param:SkuInfo
     *   @Return:
     */
    @ApiOperation(value = "添加sku")
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /*
     *   功能描述:获取sku分页列表
     *   @Param:page：第几页
                limit：每页数量
     *   @Return:
     */
    @ApiOperation(value = "获取sku分页列表")
    @GetMapping("list/{page}/{limit}")
    public Result getSkuPage(@PathVariable Long page, @PathVariable Long limit) {
        IPage skuPage = skuService.getSkuPage(page, limit);
        return Result.ok(skuPage);
    }

    /*
     *   功能描述:上架
     *   @Param:skuId
     *   @Return:
     */
    @ApiOperation(value = "上架")
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {
        skuService.onSale(skuId);
        return Result.ok();
    }

    /*
     *   功能描述:下架
     *   @Param:skuId
     *   @Return:
     */
    @ApiOperation(value = "上架")
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {
        skuService.onSale(skuId);
        return Result.ok();
    }

    //    -----------------------------------------------------------------本地模块调用----------------------------------------------
    /*
     *   功能描述:商品的名称，重量，默认图片，skuImageList图片信息
     *   @Param:skuid
     *   @Return:SkuInfo
     */
    @ApiOperation(value = "商品的名称，重量，默认图片，skuImageList图片信息")
    @GetMapping("getSkuById/{skuId}")
    public SkuInfo getSkuById(@PathVariable Long skuId) {
        return skuService.getSkuById(skuId);
    }

    /*
     *   功能描述:获取到category1Name，category2Name，category3Name 拼接 skuName!
     *   @Param:skuId
     *   @Return:
     */
    @ApiOperation(value = "获取到category1Name，category2Name，category3Name 拼接 skuName!")
    @GetMapping("getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        return skuService.getCategoryView(category3Id);
    }

    /*
     *   功能描述:根据skuid查询价格
     *   @Param:skuId
     *   @Return:BigDecimal
     */
    @ApiOperation(value = "根据skuid查询价格")
    @GetMapping("getPrice/{skuId}")
    public BigDecimal getProce(@PathVariable Long skuId) {
        return skuService.getProce(skuId);
    }

    /*
     *   功能描述:回显销售属性 + 销售属性值 + 锁定功能
     *   @Param:skuId、spuId
     *   @Return:List<SpuSaleAttr>
     */
    @ApiOperation(value = "回显销售属性 + 销售属性值 + 锁定")
    @GetMapping("getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId) {
        return skuService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @ApiOperation(value = "获取销售属性值id雨skuid组成的map")
    @GetMapping("getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId) {
        return skuService.getSkuValueIdsMap(spuId);
    }
}
