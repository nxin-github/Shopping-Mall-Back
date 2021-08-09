package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
