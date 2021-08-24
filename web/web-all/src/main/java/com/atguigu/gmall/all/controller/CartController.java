package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author：王木风
 * @date 2021/8/23 15:46
 * @description：
 */
@Controller
public class CartController {
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private ProductFeignClient productFeignClient;

    //  http://cart.gmall.com/addCart.html?skuId=47&skuNum=1;
    @RequestMapping("addCart.html")
    public String addCart(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        //  将上述两个参数传入方法中！
        cartFeignClient.addToCart(Long.parseLong(skuId),Integer.parseInt(skuNum));
        //  获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuById(Long.parseLong(skuId));
        request.setAttribute("skuNum",skuNum);
        request.setAttribute("skuInfo",skuInfo);
        //  返回添加成功页面！
        return "cart/addCart";
    }

    //  href="/cart.html"
    // @RequestMapping("cart.html")
    @GetMapping("cart.html")
    public String cartList(){
        //  不需要添加任何东西！  因为页面在加载的时候会自动通过异步方式来获取后台的数据！
        //  返回购物车列表页面
        return "cart/index";
    }
}
