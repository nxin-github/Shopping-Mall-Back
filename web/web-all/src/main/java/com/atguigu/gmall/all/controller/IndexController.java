package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author atguigu-mqx
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;


    //  用户访问www.gmall.com 或者访问 www.gmall.com/index
    @GetMapping({"/","index.html"})
    public String index(Model model){
        //  后台存储一个 ${list} 作用域
        Result result = productFeignClient.getBaseCategoryList();
        //  存储数据并保存！
        model.addAttribute("list",result.getData());
        //  返回视图名称
        return "index/index";
    }

    //  nginx 静态化！ 生产首页数据模块！ 生产首页数据文件
    @GetMapping("createIndex")
    @ResponseBody
    public Result createIndex(){
        //  获取到后台的数据
        Result result = productFeignClient.getBaseCategoryList();
        //  需要调用模板引擎的方法来生产页面数据！
        //  IContext 接口 下有个Context 对象
        Context context = new Context();
        context.setVariable("list",result.getData());

        //  创建写对象
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("D:\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  模板引擎的名称, IContext 表示页面展示的数据，Writer
        templateEngine.process("index/index.html",context,fileWriter);
        //  返回数据
        return com.atguigu.gmall.common.result.Result.ok();
    }


}
