package com.atguigu.gmall.product.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/10 8:55
 * @description：
 */

//fallback兜底类
@FeignClient(value = "service-product", fallback = ProductDegradeFeignClient.class)
//@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @GetMapping("admin/product/getSkuById/{skuId}")
    SkuInfo getSkuById(@PathVariable Long skuId);

    @GetMapping("admin/product/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    @GetMapping("admin/product/getPrice/{skuId}")
    BigDecimal getProce(@PathVariable Long skuId);

    @GetMapping("admin/product/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId);

    @GetMapping("admin/product/getSkuValueIdsMap/{spuId}")
    Map getSkuValueIdsMap(@PathVariable Long spuId);

    @GetMapping("admin/product/getBaseCategoryList")
    Result getBaseCategoryList();

    @GetMapping("admin/product/getTrademark/{id}")
    BaseTrademark getTrademark(@PathVariable Long id);

    @GetMapping("admin/product/getSearchaAttrs/{id}")
    public List<SearchAttr> getSearchaAttrs(@PathVariable Long skuId);
}
