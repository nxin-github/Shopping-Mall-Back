package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author：王木风
 * @date 2021/8/6 21:25
 * @description：品牌管理接口实现
 */
@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public IPage<BaseTrademark> getBaseTrademarkPage(Long page, Long limit) {
        IPage<BaseTrademark> baseTrademarkIPage = new Page<>(page, limit);
        return baseTrademarkMapper.selectPage(baseTrademarkIPage, null);
    }

    @Override
    public void save(BaseTrademark baseTrademark) {
        baseTrademarkMapper.insert(baseTrademark);
    }

//    这个方法我是把原来的直接删掉，新的插进去，不知道行不行
    @Override
    public void update(BaseTrademark baseTrademark) {
        baseTrademarkMapper.deleteById(baseTrademark.getId());
        baseTrademarkMapper.insert(baseTrademark);
    }

    @Override
    public void remove(Long id) {
        baseTrademarkMapper.deleteById(id);
    }

    @Override
    public BaseTrademark getById(Long id) {
        return baseTrademarkMapper.selectById(id);
    }
}
