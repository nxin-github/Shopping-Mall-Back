package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author atguigu-mqx
 */
@Service
public class SearchServiceImpl  implements SearchService {

    //  调用service-product 微服务提供的远程调用！
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ThreadPoolExecutor poolExecutor;

    /*
     *   功能描述:需要获得：1、sku基本信息
     *                  2、sku分类信息
     *                  3、sku品牌信息
     *                  4、sku对应平台属性
     *   @Param:
     *   @Return:
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
//      sku基本信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SkuInfo skuInfo = productFeignClient.getSkuById(skuId);
                goods.setId(skuInfo.getId());
                goods.setTitle(skuInfo.getSkuName()); // skuName
                goods.setPrice(skuInfo.getPrice().doubleValue()); // 数据类型转换
                goods.setDefaultImg(skuInfo.getSkuDefaultImg());
                goods.setCreateTime(new Date());
                return skuInfo;
            } catch (Exception e) {
                System.out.println("SearchServiceImpl中的upperGoods方法的skuInfoCompletableFuture线程出错了");
            }
            return null;
        }, poolExecutor);
//        品牌信息
        CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }, poolExecutor);
//        sku分类信息
        CompletableFuture<Void> CategoryCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }, poolExecutor);
//        sku对应平台属性
        CompletableFuture<Void> searchaAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SearchAttr> searchaAttrs = productFeignClient.getSearchaAttrs(skuId);
            goods.setAttrs(searchaAttrs);
        }, poolExecutor);
        CompletableFuture.allOf(skuInfoCompletableFuture, trademarkCompletableFuture, CategoryCompletableFuture, searchaAttrCompletableFuture).join();
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        /*
        1.  先准备生产dsl 语句
        2.  执行dsl 语句
        3.  获取到执行结果，将数据封装到SearchResponseVo 对象中
         */
        //  定义key ；
        String hotScoreKey = "hotScore";
        //  ZINCRBY key increment member
//        返回值是加了几次
        Double count = redisTemplate.opsForZSet().incrementScore(hotScoreKey, "skuId:" + skuId, 1);
        //  访问十次加一下热度
        if (count%10==0){
            //  es---- hotScore
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(count.longValue());
            this.goodsRepository.save(goods);
        }
    }

    @SneakyThrows
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
//      获取到查询请求！
        SearchRequest searchRequest = buildQueryDsl(searchParam);
        //  执行dsl语句并返回执行的结果对象
        //  SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //  将 searchResponse --> SearchResponseVo 前四个属性 在 parseSearchResult 方法中赋值
        SearchResponseVo searchResponseVo = parseSearchResult(searchResponse);
        searchResponseVo.setPageSize(searchParam.getPageSize());
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //  分页公式： 10 3 4 | 9 3 3
        Long totalPages = searchResponseVo.getTotal()%searchResponseVo.getPageSize()==0?searchResponseVo.getTotal()/searchResponseVo.getPageSize():searchResponseVo.getTotal()/searchResponseVo.getPageSize()+1;
        //  赋值总页数
        searchResponseVo.setTotalPages(totalPages);
        //  返回的对象
        return searchResponseVo;
    }

    /**
     * 获取es 中的数据
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
//        //品牌 此时vo对象中的id字段保留（不用写） name就是“品牌” value: [{id:100,name:华为,logo:xxx},{id:101,name:小米,log:yyy}]
//        private List<SearchResponseTmVo> trademarkList;
//        //所有商品的顶头显示的筛选属性
//        private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
//        //检索出来的商品信息
//        private List<Goods> goodsList = new ArrayList<>();
//        private Long total;//总记录数

        SearchResponseVo searchResponseVo = new SearchResponseVo();
//      获取返回的结果集
        SearchHits hits = searchResponse.getHits();
        // 设置品牌的对象集合数据 : 品牌数据从哪里来？ 从聚合中获取！
        //  思路：通过tmIdAgg 来获取到 桶，从桶中再获取到数据！
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //  通过tmIdAgg 来获取到数据
        //  Aggregation 这个对象中不具备获取桶的方法！ Id 的数据类型应该Long
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //  创建一个对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //  bucket: 相当于集合中的一个对象
            String tmId = bucket.getKeyAsString();//这里和老师的不一样
            searchResponseTmVo.setTmId(Long.parseLong(tmId));
            //  赋值品牌的名称 , 需要将tmNameAgg 转换成 map ,name数据类型应该是String
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
//            赋予品牌url
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("tmLogoUrlAgg");
            String keyAsString = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(keyAsString);
//          将品牌对象返回
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);
//      设置平台属性集合数据 【nested 数据类型】
        //  先通过map 来获取到attrAgg
        ParsedNested attrAgg =(ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

            Number attrId = bucket.getKeyAsNumber();//不一样
            searchResponseAttrVo.setAttrId(attrId.longValue());

            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");//不一样
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            //  赋值平台属性值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");//不一样
//          本质：通过key 来获取到 对应的平台属性值数据！
//            通过流取出key
            List<String> attrValueList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(attrValueList);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(attrsList);


//      设置商品的集合数据
        SearchHit[] subHits = hits.getHits();
//      声明一个集合来存储Goods数据
        List<Goods> goodsList = new ArrayList<>();
        Arrays.stream(subHits).forEach(subHit -> {//不一样
            //  获取对应的Json 字符串
            String sourceAsString = subHit.getSourceAsString();
            //  将这个Json 字符串变成Goods 对象
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            //  细节问题！ 如何用户全文检索，那么name 应该高亮!
            if(subHit.getHighlightFields().get("title")!=null){
                //  按照全文检索的方式检索数据的！ 获取到高亮字段！
                Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                goods.setTitle(title.toString());
            }
            goodsList.add(goods);
        });
        searchResponseVo.setGoodsList(goodsList);

//      设置总记录数
        searchResponseVo.setTotal(hits.getTotalHits().value);

        return searchResponseVo;
    }


    /**
     * 生产查询请求对象
     *
     * @param searchParam
     * @return
     */
//    这个方法有问题===========================================================================================
    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //  第一个入口：分类Id
        //  需要构建bool { query -- bool}
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

//  判断一级分类Id
        if(!StringUtils.isEmpty(searchParam.getCategory1Id())){
            //  可能按照分类Id 过滤 {query -- bool -- filter -- term }
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }

        //  判断二级分类Id
        if(!StringUtils.isEmpty(searchParam.getCategory2Id())){
            //  可能按照分类Id 过滤 {query -- bool -- filter -- term }
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }

        //  判断三级分类Id
        if(!StringUtils.isEmpty(searchParam.getCategory3Id())){
            //  可能按照分类Id 过滤 {query -- bool -- filter -- term }
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }

        //  第二个入口：全文检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
        }

        //  还可以通过品牌进行检索： trademark=2:华为
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
//          分割字符串
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
//          品牌id
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //  平台属性值过滤！
        //  &props=24:128G:机身内存&props=23:8G:运行内存  属性Id：属性值名称：属性名
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
//            循环取出每个参数
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3) {
//                    取出每个大参数里的小参数
                    //  创建2个boolQuery
                    BoolQueryBuilder queryBulider = QueryBuilders.boolQuery();
//                  里面一层
                    BoolQueryBuilder subQueryBuilder = QueryBuilders.boolQuery();
//                  属性id
                    queryBulider.must((QueryBuilders.termQuery("attrs.attrId", split[0])));
                    subQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    // {must -->nested}
                    queryBulider.must(QueryBuilders.nestedQuery("attrs", subQueryBuilder, ScoreMode.None));
                    //  将 queryBuilder 放入外层的 boolQueryBuilder
                    boolQueryBuilder.filter(queryBulider);
                }
            }
        }
        //  {query}
        searchSourceBuilder.query(boolQueryBuilder);
//        -----------------------------------------查询结束，下面是高亮-----------------------------------------------------
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
//        -----------------------------------------高亮结束，下面是分页-----------------------------------------------------
        //  设置分页
        //  分页公式： 0 ,3  3,3
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());
//        -----------------------------------------分页结束，下面是排序-----------------------------------------------------
        String order = searchParam.getOrder();
        //  排序：综合{hotScore}：order=1:desc  order=1:asc || 价格{price}：order=2:desc  order=2:asc
        if (!StringUtils.isEmpty(order)) {
            //  进行分割
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                //  在此声明一个字段，记录需要排谁！
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }
//        -----------------------------------------排序结束，下面是聚合-----------------------------------------------------
//  品牌聚合：
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));

        //  平台属性聚合：属于 nested 数据类型
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //  细节：设置索引库 GET /goods/_search
        //  设置查询的字段数据显示
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.source(searchSourceBuilder);
        //  返回查询请求
        return searchRequest;
    }
}
