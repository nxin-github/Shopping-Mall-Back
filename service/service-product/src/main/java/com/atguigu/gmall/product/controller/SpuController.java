package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SpuService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/7 13:37
 * @description：spu操作
 */
@Api(tags = "spu操作接口")
@RestController
@RequestMapping("admin/product")
public class SpuController {
    @Autowired
    private SpuService spuService;

    /*
     *   功能描述:获取spu分页列表
     *   @Param:三级分类id
     *   @Return:
     */
    @ApiOperation(value = "获取spu分页列表")
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo) {//api文档传的是id，为什么这例用SpuInfo，用SpuInfo为什么不加requestBody
        IPage spuInfoPage = spuService.getSpuInfoPage(page, limit, spuInfo);
        return Result.ok(spuInfoPage);
    }

    /*
     *   功能描述:获取销售属性
     *   @Param:
     *   @Return:
     */
    @ApiOperation(value = "获取销售属性")
    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = spuService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrs);
    }

    /*
     *   功能描述:获取品牌属性
     *   @Param:
     *   @Return:
     */
    @ApiOperation(value = "获取品牌属性")
    @GetMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        List<BaseTrademark> baseTrademarkList = spuService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }

    /*
     *   功能描述:添加spu
     *   @Param:SpuInfo
     *   @Return:
     */
    @ApiOperation(value = "添加spu")
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /*
     *   功能描述:文件上传
     *   @Param:file
     *   @Return:
     */
    @ApiOperation(value = "文件上传")
    @PostMapping("fileUpload")
    public Result fileUpload(@RequestBody MultipartFile file) {
        String url = spuService.fileUpload(file);
        System.out.println(url);
        return Result.ok(url);
    }
}
