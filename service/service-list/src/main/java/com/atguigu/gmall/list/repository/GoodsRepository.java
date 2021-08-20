package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Author：王木风
 * @date 2021/8/16 19:32
 * @description：
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {

}
