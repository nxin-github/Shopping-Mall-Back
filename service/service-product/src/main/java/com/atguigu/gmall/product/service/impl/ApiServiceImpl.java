package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cache.GmallCache;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.mapper.SearchaAttrMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.service.ApiService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
* @Author：王木风
* @date 2021/8/16 20:02
* @description：
*/
@Service
public class ApiServiceImpl implements ApiService {
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SearchaAttrMapper searchaAttrMapper;

    @Override
    @GmallCache(prefix = RedisConst.SKUKEY_PREFIX)
    public SkuInfo getSkuById(Long skuId) {
        return getSkuInfoDB(skuId);
//        return getSkuInfoByRedisson(skuId);
    }

//    private SkuInfo getSkuInfoByRedisson(Long skuId) {
//        SkuInfo skuInfo;
//        //  先定义缓存的key = sku:skuId:info;  set key value  value = SkuInfo
//        String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
//        try {
//            //  第二种方式使用redisson 做分布式锁！
//            skuInfo = (SkuInfo) this.redisTemplate.opsForValue().get(skuKey);
//            //  判断
//            if (skuInfo==null){
//                //  获取数据库中的数据，并放入缓存
//                //  lockKey = sku:skuId:lock
//                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
//                RLock lock = redissonClient.getLock(lockKey);
//                //  上锁  可重入锁！
//                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
//                //  判断
//                if(result){
//                    try {
//                        //   result = true 表示获取到了锁！
//                        //   需要查询数据库！将数据放入缓存！   144 数据库没有！ 缓存也没有！
//                        skuInfo = this.getSkuInfoDB(skuId);
//                        //  判断当前这个skuInfo 是否为空！ 防止缓存穿透！
//                        if (skuInfo==null){ //  数据库中根本没有这个数据
//                            SkuInfo skuInfo1 = new SkuInfo();
//                            //  设置这个key 的过期时间是10分钟
//                            //  this.redisTemplate.opsForValue().setIfAbsent(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
//                            //  缓存中是没有这个key 的！ 我呢，就可以直接使用Set 就可以！
//                            //  redisTemplate.opsForValue().set();
//                            //  redisTemplate.expire();
//                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKULOCK_EXPIRE_PX1,TimeUnit.SECONDS);
//                            //  停止
//                            return skuInfo1;
//                        }
//                        //  数据库中有数据
//                        this.redisTemplate.opsForValue().setIfAbsent(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
//                        //  返回数据
//                        return skuInfo;
//                    } finally {
//                        //  解锁
//                        lock.unlock();
//                    }
//                }else {
//                    //  result = false 表示没有获取到了锁！ 设置一个自旋
//                    try {
//                        Thread.sleep(100);
//                        return getSkuById(skuId);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }else {
//                //  缓存中有数据，直接返回
//                return skuInfo;
//            }
//        } catch (InterruptedException e) {
//            System.out.println("redis ---- 宕机了--------记录日志------调用发送短信接口----通知人员来维修----");
//            e.printStackTrace();
//        }
//
//        //  最后数据库兜底
//        return getSkuInfoDB(skuId);
//    }
//
//    private SkuInfo getSkuInfoByRedisson1(Long skuId) {
////        redis原生方法
//        SkuInfo skuInfo = null;
//        String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
//        try {
//            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
//            if (skuInfo == null) {
////                意味缓存没有
//                String uuid = UUID.randomUUID().toString();
//                Boolean flag = redisTemplate.opsForValue().setIfAbsent(skuKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
//                if (flag) {
////                    上锁成功
//                    skuInfo = getSkuInfoDB(skuId);
//                    if (skuInfo == null) {
////                        数据库中没有
//                        SkuInfo skuInfo1 = new SkuInfo();
//                        redisTemplate.opsForValue().setIfAbsent(skuKey, skuInfo, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
////                         停止
//                        return skuInfo1;
//                    } else {
//                        redisTemplate.opsForValue().setIfAbsent(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
//
//                        //  删除锁！
//                        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call ('del',KEYS[1]) else return 0 end";
////                      RedisScript 这个是接口：
//                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//                        //  将lua 脚本放入方法中
//                        redisScript.setScriptText(script);
//                        //  设置返回值类型
//                        redisScript.setResultType(Long.class);
//
//                        //  使用lua 脚本
//                        //  第一个参数 RedisScript ，第二个参数 应该是key ,第三个参数：表示key 所对应的值
//                        redisTemplate.execute(redisScript, Arrays.asList(skuKey),uuid);
//                        //  返回数据
//                        return skuInfo;
//                    }
//                } else {
////                        上锁失败
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return getSkuById(skuId);
//                }
//            } else {
//                return skuInfo;
//            }
//        } catch (Exception e) {
//            System.out.println("redis ---- 宕机了--------记录日志------调用发送短信接口----通知人员来维修----");
//            e.printStackTrace();
//        }
//        //  数据库兜底.
//        return getSkuInfoDB(skuId);
//    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        //  先获取skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //  如何解决空指针问题?
        if (skuInfo!=null){
            //  获取到skuImageList 集合
            List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
            //  报空指针  skuInfo = null
            skuInfo.setSkuImageList(skuImageList);
        }
        //  商品的名称，图片，价格 ，skuImageList 集合
        return skuInfo;
    }

    @Override
    @GmallCache(prefix = "categoryViewByCategory3Id:")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "skuPrice:")
    public BigDecimal getProce(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    @Override
    @GmallCache(prefix = "spuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    @GmallCache(prefix = "saleAttrValuesBySpu:")
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
//      页面获取到的是“102|105” 和skuid 42
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuValueIdsMap(spuId);
//        values_ids sku_id
//      44 114|116
//        45 114|117
        for (Map skuMap : mapList) {
//            赋值
            map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
        }
        return map;
    }

    @Override
    @GmallCache(prefix = "index")
    public List<JSONObject> getBaseCategroyList() {
        List<JSONObject> list = new ArrayList<>();
        /*
        1.  需要先获取到所有的分类数据

        2.  需要将查询到的数据进行分组操作！ 分组的条件：分别是category1Id, category2Id 获取到分类的名称

        3.  将获取到的分类数据的名称，以及分类的Id 装载到list 集合中！
         */
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
//      按照category1Id 进行分组
        //  map key = category1Id   value = List<BaseCategoryView>
        Map<Long, List<BaseCategoryView>> baseCategory1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
//        序号
        int index = 1;
        for (Map.Entry<Long, List<BaseCategoryView>> entry : baseCategory1Map.entrySet()) {
            Long category1Id = entry.getKey();
            List<BaseCategoryView> baseCategoryViewList1 = entry.getValue();
            //  创建一个JsonObject 对象 一级分类对象
            JSONObject category1 = new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",category1Id); // 一级分类Id
            category1.put("categoryName",baseCategoryViewList1.get(0).getCategory1Name()); // 一级分类名称
            //  index 遍历需要迭代
            index++;
            //  处理二级分类数据： key = category2Id value =
            Map<Long, List<BaseCategoryView>> baseCategory2Map = baseCategoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //  要声明一个集合来存储一级分类下对应的二级分类名称！
            List<JSONObject> categoryChild2 = new ArrayList<>();
            for (Entry<Long, List<BaseCategoryView>> entry2 : baseCategory2Map.entrySet()) {
                Long category2Id = entry2.getKey();
                List<BaseCategoryView> baseCategoryViewList2 = entry2.getValue();
//                二级分类对像
                JSONObject category2 = new JSONObject();
                category2.put("categoryId",category2Id); // 二级分类Id
                category2.put("categoryName",baseCategoryViewList2.get(0).getCategory2Name()); // 二级分类名称
                //  将每个二级分类对应的数据 添加到这个集合中
                categoryChild2.add(category2);
                //  声明一个集合来存储三级分类数据
                List<JSONObject> categoryChild3 = new ArrayList<>();
                //  获取三级分类数据
                baseCategoryViewList2.forEach((baseCategoryView) -> {
                    //  创建一个三级分类对象
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",baseCategoryView.getCategory3Id());
                    category3.put("categoryName",baseCategoryView.getCategory3Name());

                    categoryChild3.add(category3);
                });
                //  将三级分类的集合数据添加到二级分类上！
                category2.put("categoryChild",categoryChild3);
            }
            //  将二级分类的集合数据添加到一级分类上！
            category1.put("categoryChild",categoryChild2);
            //  将所有的一级分类数据添加到集合中
            list.add(category1);
        }
        return list;
    }

    @Override
    public List<SearchAttr> getSearchaAttrs(Long skuId) {
        List<SearchAttr> searchAttr = searchaAttrMapper.getSearchaAttrs(skuId);
        System.out.println("====================================================================================");
        System.out.println("====================================================================================");
        System.out.println(searchAttr.toString());
        System.out.println("====================================================================================");
        System.out.println("====================================================================================");
        return searchAttr;
    }
}
