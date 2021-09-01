package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.rabbit.common.constant.MqConst;
import com.atguigu.gmall.rabbit.common.service.RabbitService;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author atguigu-mqx
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsApiController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderFeignClient orderFeignClient;

    //  获取到所有秒杀商品数据
    @GetMapping("/findAll")
    public Result findAll(){
        List<SeckillGoods> all = seckillGoodsService.findAll();
        return Result.ok(all);
    }

    //  获取秒杀详情
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId){
        return Result.ok(seckillGoodsService.getSeckillGoods(skuId));
    }

    //  获取下单码：
    @GetMapping("auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){
        //  获取到数据：本质下单码！
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        //  获取到对应的秒杀商品！
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        //  判断
        if (!StringUtils.isEmpty(userId) && seckillGoods!=null){
            //  继续判断：new Date() 必须在秒杀开始之后，结束之前，这个时候才能获取到下单码！
            //  DateUtil.truncatedCompareTo() ---> 查询购物车：按照updTinme 来进行排序desc！
            if (DateUtil.dateCompare(seckillGoods.getStartTime(),new Date())
                && DateUtil.dateCompare(new Date(),seckillGoods.getEndTime())){
                //  生成下单码！
                String skuIdStr = MD5.encrypt(userId);
                //  String skuIdStr = DigestUtils.md5DigestAsHex(userId.getBytes());
                return Result.ok(skuIdStr);
            }
        }
        //  默认返回失败！
        return Result.fail().message("获取下单码失败!");
    }

    //  判断是否有下单资格！
    //  /api/activity/seckill/auth/seckillOrder/{skuId}?skuIdStr=skuIdStr
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId, HttpServletRequest request){
        //  获取url 上的下单码！
        String skuIdStr = request.getParameter("skuIdStr");
        //  获取userId
        String userId = AuthContextHolder.getUserId(request);
        //  校验下单码！ 页面与后台生产的下单码进行比较！
        if (!skuIdStr.equals(MD5.encrypt(userId))){
            //  返回非法请求
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        //  校验状态位：本质 map.get(skuId) ; status = 1 或 status=0;
        String status = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(status)){
            //  返回非法请求
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }else if("0".equals(status)){
            //  已售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }else {
            //  可以秒杀！ 将秒杀的人与商品发送到队列中！
            //  消息的内容： 人 商品 封装到一个对象中！
            UserRecode userRecode = new UserRecode();
            userRecode.setSkuId(skuId);
            userRecode.setUserId(userId);
            this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,userRecode);
        }
        // 返回成功！
        return Result.ok();
    }

    //  检查订单状态：
    @GetMapping(value = "auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId, HttpServletRequest request){
        //  获取用户Id！
        String userId = AuthContextHolder.getUserId(request);
        return seckillGoodsService.checkOrder(skuId, userId);
    }

    //  编写下单控制器！
    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        //  后台需要存储：userAddressList, detailArrayList,totalNum,totalAmount 存储到map 中！
        HashMap<String, Object> map = new HashMap<>();
        //  远程调用
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        //  赋值用户秒杀的商品！
        //  从缓存中数据：
        OrderRecode orderRecode = (OrderRecode) this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        //  判断！
        if (orderRecode==null){
            return Result.fail().message("订单页面显示失败....");
        }
        //  获取到了秒杀对象
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        List<OrderDetail> detailArrayList = new ArrayList<>();
        //  声明一个订单明细对象
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        detailArrayList.add(orderDetail);

        map.put("userAddressList",userAddressList);
        map.put("detailArrayList",detailArrayList);
        map.put("totalNum",orderRecode.getNum());
        //  总金额
        //        OrderInfo orderInfo = new OrderInfo();
        //        orderInfo.setOrderDetailList(detailArrayList);
        //        orderInfo.sumTotalAmount();
        //        map.put("totalAmount",orderInfo.getTotalAmount());
        map.put("totalAmount",seckillGoods.getCostPrice());
        //  返回数据
        return Result.ok(map);
    }

    //  保存订单： /api/activity/seckill/auth/submitOrder
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request){
        //  获取用户id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        //  远程调用保存方法
        Long orderId = this.orderFeignClient.submitOrder(orderInfo);
        if (orderId==null){
            return Result.fail().message("保存订单失败.....");
        }

        //  需要删除一个数据：预下单信息！
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
        //  将订单数据保存到缓存！
        this.redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId,orderId.toString());
        //  返回数据
        return Result.ok(orderId);
    }
}
