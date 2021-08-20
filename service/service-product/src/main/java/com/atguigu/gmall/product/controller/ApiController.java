package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.mapper.SearchaAttrMapper;
import com.atguigu.gmall.product.service.ApiService;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/16 19:58
 * @description：
 */
@Api(tags = "product内部（feign）获取调用api")
@RestController
@RequestMapping("admin/product")
public class ApiController {
    @Autowired
    private ApiService apiService;
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /*
     *   功能描述:商品的名称，重量，默认图片，skuImageList图片信息
     *   @Param:skuid
     *   @Return:SkuInfo
     */
    @ApiOperation(value = "商品的名称，重量，默认图片，skuImageList图片信息")
    @GetMapping("getSkuById/{skuId}")
    public SkuInfo getSkuById(@PathVariable Long skuId) {
        return apiService.getSkuById(skuId);
    }

    /*
     *   功能描述:获取到category1Name，category2Name，category3Name 拼接 skuName!
     *   @Param:skuId
     *   @Return:
     */
    @ApiOperation(value = "获取到category1Name，category2Name，category3Name 拼接 skuName!")
    @GetMapping("getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        return apiService.getCategoryView(category3Id);
    }

    /*
     *   功能描述:根据skuid查询价格
     *   @Param:skuId
     *   @Return:BigDecimal
     */
    @ApiOperation(value = "根据skuid查询价格")
    @GetMapping("getPrice/{skuId}")
    public BigDecimal getProce(@PathVariable Long skuId) {
        return apiService.getProce(skuId);
    }

    /*
     *   功能描述:回显销售属性 + 销售属性值 + 锁定功能
     *   @Param:skuId、spuId
     *   @Return:List<SpuSaleAttr>
     */
    @ApiOperation(value = "回显销售属性 + 销售属性值 + 锁定")
    @GetMapping("getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId) {
        return apiService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /*
     *   功能描述:获取销售属性值id雨skuid组成的map
     *   @Param:spuId
     *   @Return:Map
     */
    @ApiOperation(value = "获取销售属性值id雨skuid组成的map")
    @GetMapping("getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId) {
        return apiService.getSkuValueIdsMap(spuId);
    }

    /*
     *   功能描述:获取全部分类信息
     *   @Param:
     *   @Return:
     */
    @ApiOperation(value = "获取全部分类信息")
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList() {
        List<JSONObject> list = apiService.getBaseCategroyList();
        return Result.ok(list);
    }

    /*
     *   功能描述:根据Id获取品牌
     *   @Param:品牌Id
     *   @Return:
     */
    @ApiOperation(value = "根据id获取品牌")
    @GetMapping("getTrademark/{id}")
    public BaseTrademark getTrademark(@PathVariable Long id) {
        return baseTrademarkService.getById(id);
    }

    /**
     * 功能描述:获取sku对应平台属性
     *
     * @Param:skuId
     * @Return:SearchaAttr
     */
    @ApiOperation(value = "获取sku对应平台属性")
    @GetMapping("getSearchaAttrs/{skuId}")
    public List<SearchAttr> getSearchaAttrs(@PathVariable Long skuId) {
        List<SearchAttr>  searchaAttrs = apiService.getSearchaAttrs(skuId);
        return searchaAttrs;
    }
}
