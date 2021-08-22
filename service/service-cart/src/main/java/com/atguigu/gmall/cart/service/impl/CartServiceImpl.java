package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cache.GmallCache;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.cart.CarInfoVo;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jodd.time.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author：王木风
 * @date 2021/8/21 8:30
 * @description：
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;

    /*
     *   功能描述:添加到购物车
     *   @Param:Long skuId
     *   @Param:String skuId
     *   @Param:Integer skuNum
     *   @Return:void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        /*
            1.  查看购物车中，是否有该商品
                true:
                    商品数量相加
                false:
                    直接数据
            2.  同步缓存，想缓存中添加数据！
         */
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("sku_id", skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(queryWrapper);
        if (cartInfoExist != null) {
            //  商品数量相加
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + 1);
            //  赋值实时价格
            //  什么是实时价格；就是skuInfo 表中的price
            cartInfoExist.setSkuPrice(productFeignClient.getProce(skuId));
            //  设置复选状态！一开始进来checked = 1 ，通过页面修改成 0 ，添加一件商品 1
            cartInfoExist.setIsChecked(1);
            //  设置添加的时间在第一次添加的时候已经赋值了。但是我们需要处理的是更新时间！
            cartInfoExist.setUpdateTime(new Timestamp(new Date().getTime()));
            //  更新数据：
            cartInfoMapper.updateById(cartInfoExist);

            //  更新 操作缓存 redisTemplate
        } else {
            //  第一次添加购物车，直接将数据添加到数据库
            SkuInfo skuInfo = this.productFeignClient.getSkuById(skuId);

            CartInfo cartInfo = new CartInfo();
            //  给cartInfo 赋值
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
            cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
            //  插入数据库！
            cartInfoMapper.insert(cartInfo);
            //  废物再利用
            cartInfoExist = cartInfo;
            //  也需要操作缓存
        }
        //  有关于购物车缓存 redis  和  mysql 的操作问题！
        //  查询数据的时候，先看缓存，如果缓存没有，再看数据库并将数据load 到缓存中！
        //  购物车：再添加购物车时候，同时放入缓存一份！ insert into mysql and redis!
        String cartKey = getCarteKey(userId);
        //  向缓存放入数据
        redisTemplate.opsForHash().put(cartKey,skuId.toString(),cartInfoExist);
        //  设置一个过期时间
        setCartKeyExpire(cartKey);
    }

    /*
     *   功能描述:展示购物车
     *   @Param:String userId
     *   @Param:String userTempId
     *   @Return:List<CarInfo>
     */
    @Override
    public List<CartInfo> carList(String userId,String userTempId) {
//        只有一个id，直接展示购物车
        if (userId == null && userTempId != null) {
            List<CartInfo> cartListRedis = getCartListRedis(userTempId);
            if (!CollectionUtils.isEmpty(cartListRedis)) {
                return cartListRedis;
            }
            List<CartInfo> cartListMySQL = getCartListMySQL(userTempId);
            if (!CollectionUtils.isEmpty(cartListMySQL)) {
                return cartListMySQL;
            }
        }
        if (userId != null && userTempId == null) {
            List<CartInfo> cartListRedis = getCartListRedis(userId);
            if (!CollectionUtils.isEmpty(cartListRedis)) {
                return cartListRedis;
            }
            List<CartInfo> cartListMySQL = getCartListMySQL(userId);
            if (!CollectionUtils.isEmpty(cartListMySQL)) {
                return cartListMySQL;
            }
        }
//        两个id，合并购物车
        if (userId != null && userTempId != null) {
            return MergeCart(userId, userTempId);
        }
        System.out.println("userId 和 userTempId都是空");
        return null;
    }

    /*
     *   功能描述:合并购物车
     *   @Param:String userId
     *   @Param:String userTempId
     *   @Return:List<CartInfo>
     */
    private List<CartInfo> MergeCart(String userId, String userTempId) {
//        获取未登录的购物车
        List<CartInfo> noCartList = getCartListMySQL(userTempId);
//        把未登录的购物车转换成map，相当于按照skuID分类
        Map<Long, CartInfo> noCartMap = noCartList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
//        获取登录购物车
        List<CartInfo> cartListMySQL = getCartListMySQL(userId);
        for (CartInfo cartInfo : cartListMySQL) {
            CartInfo noCart = noCartMap.get(cartInfo.getSkuId());
//            已经登录的购物车包含这个商品
            if (noCartMap.containsKey(cartInfo.getSkuId())) {
                //  数量相加
                //  获取未登录的购物车对象
                noCart.setId(cartInfo.getId());
                noCart.setSkuNum(noCart.getSkuNum() + cartInfo.getSkuNum());
                //  合并完了，还要更新一下updateTime
                noCart.setUpdateTime(new Timestamp(new Date().getTime()));
                noCart.setIsChecked(1);
                cartInfoMapper.updateById(noCart);
            } else {
                //  细节： 修改userId,将
                noCart.setUserId(cartInfo.getUserId());
                noCart.setUpdateTime(new Timestamp(new Date().getTime()));
                cartInfoMapper.updateById(noCart);
            }
        }
        List<CartInfo> cartListMySQLAfter = getCartListMySQL(userId);
        loadCartToCache(cartListMySQLAfter,getCarteKey(userId));
        return cartListMySQLAfter;
    }
    
    /*
     *   功能描述:缓存查询购物车
     *   @Param:String userId
     *   @Return:List<CartInfo>
     */
    private List<CartInfo> getCartListRedis(String userId) {
        String carteKey = getCarteKey(userId);
        return redisTemplate.opsForHash().values(carteKey);
    }

    /*
     *   功能描述:缓存查询购物车
     *   @Param:String userId
     *   @Return:List<CartInfo>
     */
    private List<CartInfo> getCartListMySQL(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        return cartInfoMapper.selectList(queryWrapper);
    }

    /*
     *   功能描述:添加购物车到缓存
     *   @Param:String userId
     *   @Param:String carteKey
     *   @Return:List<CartInfo>
     */
    private List<CartInfo> loadCartToCache(List<CartInfo> cartInfoList,String carteKey) {
//        如果是空的直接返回，不加入缓存
        if (CollectionUtils.isEmpty(cartInfoList)) return cartInfoList;

//            将查询内容加入缓存
        HashMap<String, Object> map = new HashMap<>();
        cartInfoList.stream().forEach(cartInfo -> {
            Long skuId = cartInfo.getSkuId();
            //  细节： 查询购物车的时候： 有个字段 实时价格并不在数据库中！
            cartInfo.setSkuPrice(productFeignClient.getProce(cartInfo.getSkuId()));
            map.put(skuId.toString(), cartInfo);
        });
//        先存到map中防止和缓存交互过多
        redisTemplate.opsForHash().putAll(carteKey, map);
        setCartKeyExpire(carteKey);
        return cartInfoList;
    }

    /*
     *   功能描述:设置过期时间
     *   @Param:String cartKey
     *   @Return:void
     */
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    /*
     *   功能描述:获取缓存的key
     *   @Param:String userId
     *   @Return:String
     */
    private String getCarteKey(String userId) {
        //  缓存的类型选中谁? hash  hset key field value   hget key field;
        //  key = user:userId:cart  field = skuId  value = cartInfo;
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

}
