package com.atguigu.gmall.all.controller;

import client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author atguigu-mqx
 */
@Controller
public class SeckillController {

    @Autowired
    private ActivityFeignClient activityFeignClient;

    //  http://activity.gmall.com/seckill.html
    @GetMapping("seckill.html")
    public String seckill(Model model){
        Result result = this.activityFeignClient.findAll();
        model.addAttribute("list",result.getData());
        //  返回视图名称
        return "seckill/index";
    }

    //  秒杀的详情： th:href="'/seckill/'+${item.skuId}+'.html'"  返回商品秒杀的详情 item.html
    @GetMapping("seckill/{skuId}.html")
    public String seckillItem(@PathVariable Long skuId, Model model){
        Result result = this.activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item",result.getData());
        //  返回秒杀详情
        return "seckill/item";
    }

    //  window.location.href = '/seckill/queue.html?skuId='+this.skuId+'&skuIdStr='+skuIdStr
    //  跳转到排队页面！
    @GetMapping("/seckill/queue.html")
    public String seckillQueue(HttpServletRequest request){
        //  获取url 的参数！
        String skuId = request.getParameter("skuId");
        String skuIdStr = request.getParameter("skuIdStr");
        //  需要一个远程调用！
        //  this.activityFeignClient.xxx();
        request.setAttribute("skuId",skuId);
        request.setAttribute("skuIdStr",skuIdStr);

        //  返回排队页面！
        return "seckill/queue";
    }

    //  去下单
    @GetMapping("seckill/trade.html")
    public String seckillTrade(Model model){
        //  后台需要存储：userAddressList, detailArrayList,totalNum,totalAmount
        Result<Map<String,Object>> result = this.activityFeignClient.trade();
        //  判断
        if (result.isOk()){
            //  存储数据
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        }else {
            //  不OK!
            model.addAttribute("message","下单异常.....");
            return "seckill/fail";
        }
    }
}
