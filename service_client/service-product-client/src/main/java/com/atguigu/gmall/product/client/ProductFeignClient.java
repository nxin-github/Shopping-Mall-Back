package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/10 8:55
 * @description：
 */
@FeignClient("service-product")
public interface ProductFeignClient {

    @GetMapping("getSkuById/{skuId}")
    public SkuInfo getSkuById(@PathVariable Long skuId);

    @GetMapping("getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    @GetMapping("getPrice/{skuId}")
    public BigDecimal getProce(@PathVariable Long skuId);

    @GetMapping("getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId);
}
