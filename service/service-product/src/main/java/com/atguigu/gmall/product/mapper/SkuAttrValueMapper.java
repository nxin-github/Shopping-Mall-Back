package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author：王木风
 * @date 2021/8/8 20:26
 * @description：
 */
@Mapper
@Repository
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
}
