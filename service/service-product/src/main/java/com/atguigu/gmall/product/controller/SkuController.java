package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
