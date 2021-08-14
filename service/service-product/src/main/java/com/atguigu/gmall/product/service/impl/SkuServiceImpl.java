package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.cache.GmallCache;
import com.atguigu.gmall.common.config.RedisConfig;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.lettuce.core.RedisClient;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author：王木风
 * @date 2021/8/6 19:42
 * @description：
 */
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<SpuImage> getImageBySpuId(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(queryWrapper);
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.getspuSaleAttrListById(spuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insert(skuInfo);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
    }

    @Override
    public IPage getSkuPage(Long page, Long limit) {
        IPage<SkuInfo> skuInfoIPage = new Page<>(page, limit);
        return skuInfoMapper.selectPage(skuInfoIPage, null);
    }

    @Override
    public void onSale(Long skuId) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
        Integer isSale = skuInfo.getIsSale();
        if (isSale == 1) {
            skuInfo.setIsSale(0);
        } else if (isSale == 0) {
            skuInfo.setIsSale(1);
        } else {
            System.out.println("SkuServiceImpld的onSale方法出错了");
        }
        skuInfoMapper.update(skuInfo, queryWrapper);
    }
//----------------------------------------------------------------------------------------------------------------------------------------
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
}
