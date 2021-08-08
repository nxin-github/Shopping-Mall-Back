package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：王木风
 * @date 2021/8/6 21:24
 * @description：品牌接口
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /*
     *   功能描述:获取品牌分页列表
     *   @Param:page：第几页
                limit：每页数量
     *   @Return:
     */
    @ApiOperation(value = "获取品牌分页列表")
    @GetMapping("{page}/{limit}")
    public Result getBaseTrademarkPage(@PathVariable Long page, @PathVariable Long limit) {
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.getBaseTrademarkPage(page, limit);
        return Result.ok(baseTrademarkIPage);
    }

    /*
     *   功能描述:添加品牌
     *   @Param:BaseTrademark
     *   @Return:
     */
    @ApiOperation(value = "添加品牌")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    /*
     *   功能描述:修改品牌
     *   @Param:baseTrademark的json字符串
     *   @Return:
     */
    @ApiOperation(value = "修改品牌")
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.update(baseTrademark);
        return Result.ok();
    }

    /*
     *   功能描述:删除品牌
     *   @Param:品牌Id
     *   @Return:
     */
    @ApiOperation(value = "删除品牌")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        baseTrademarkService.remove(id);
        return Result.ok();
    }

    /*
     *   功能描述:5、根据Id获取品牌
     *   @Param:品牌Id
     *   @Return:
     */
    @ApiOperation(value = "根据id获取品牌")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.ok(baseTrademarkService.getById(id));
    }
}
