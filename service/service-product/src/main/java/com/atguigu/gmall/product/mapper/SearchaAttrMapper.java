package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.list.SearchAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/16 21:01
 * @description：
 */
@Repository
@Mapper
public interface SearchaAttrMapper extends BaseMapper<SearchAttr> {
    List<SearchAttr> getSearchaAttrs(Long skuId);
}
