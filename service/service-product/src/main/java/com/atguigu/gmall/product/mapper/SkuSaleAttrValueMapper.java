package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/8 20:28
 * @description：
 */
@Mapper
@Repository
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /*
     *   功能描述:获取销售属性值id和skuId
     *   @Param:
     *   @Return:
     */
    List<Map> selectSkuValueIdsMap(Long spuId);
}
