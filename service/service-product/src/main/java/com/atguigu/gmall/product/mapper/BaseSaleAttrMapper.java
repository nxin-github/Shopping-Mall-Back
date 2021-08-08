package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author：王木风
 * @date 2021/8/6 18:53
 * @description：操作base_sake_attr表，对应BaseSaleAttr类
 */
@Mapper
@Repository
public interface BaseSaleAttrMapper extends BaseMapper<BaseSaleAttr> {
}
