package com.atguigu.gmall.list.mapper;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author：王木风
 * @date 2021/8/16 19:32
 * @description：
 */
@Repository
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {

}
