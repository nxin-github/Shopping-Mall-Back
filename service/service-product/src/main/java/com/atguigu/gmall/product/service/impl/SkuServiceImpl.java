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
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
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
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private RabbitService rabbitService;

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
    @Transactional
    public void onSale(Long skuId) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
        Integer isSale = skuInfo.getIsSale();
        if (isSale == 1) {
            skuInfo.setIsSale(0);
            //商品下架
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_LOWER, skuId);
        } else if (isSale == 0) {
            skuInfo.setIsSale(1);
            //商品上架
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_UPPER, skuId);
        } else {
            System.out.println("SkuServiceImpld的onSale方法出错了");
        }
        skuInfoMapper.update(skuInfo, queryWrapper);
    }
}
