package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author：王木风
 * @date 2021/8/9 20:22
 * @description：
 */
@Api(tags = "商品详情模块")
@RestController
@RequestMapping("api/item")
public class ItemApiController {
    @Autowired
    private ItemService itemService;

    //  自定义一个远程路径：
    @GetMapping("{skuId}")
    public Result getItemBySkuId(@PathVariable Long skuId){
        //  调用服务层
        Map<String, Object> map = itemService.getItemById(skuId);
        //  返回result
        return Result.ok(map);
    }
    //  这个控制器作用： 将servic-item 汇总好的数据 发送给web-all ;
    //  用户通过网关 -- web-all --feign{api/item/{skuId}}--> service-item
}
