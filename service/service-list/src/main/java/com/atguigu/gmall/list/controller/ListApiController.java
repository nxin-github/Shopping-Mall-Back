package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：王木风
 * @date 2021/8/16 19:05
 * @description：
 */
@Api(tags = "全文索引控制器")
@RestController
@RequestMapping("api/list")
public class ListApiController {
    @Autowired
    private SearchService searchService;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /*
     *   功能描述:自定义一个映射器 ，访问这个映射器就能够自动在es 中创建mapping 索引库！
     *   @Param:
     *   @Return:
     */
    @ApiOperation(value = "")
    @GetMapping("inner/createIndex")
    private Result createIndex() {
        elasticsearchRestTemplate.createIndex(Goods.class);//  创建对应的Index！
        elasticsearchRestTemplate.putMapping(Goods.class); // 创建对应的mapping 映射！
        return Result.ok();
    }

    /*
     *   功能描述:上架
     *   @Param:sku的id
     *   @Return:
     */
    @ApiOperation(value = "上架")
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId) {
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    /*
     *   功能描述:下架
     *   @Param:sku的id
     *   @Return:
     */
    @DeleteMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId) {
        searchService.lowerGoods(skuId);
        return Result.ok();
    }
}
