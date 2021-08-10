package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author：王木风
 * @date 2021/8/10 19:14
 * @description：
 */
@Mapper
@Repository
public interface BaseCategoryViewMapper extends BaseMapper<BaseCategoryView> {
}
