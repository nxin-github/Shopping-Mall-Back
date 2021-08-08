package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/6 19:25
 * @description：spu接口管理
 */
public interface SpuService {
    /*
     *   功能描述:获取spu分页列表
     *   @Param:三级分类id
     *   @Return:
     */
    IPage getSpuInfoPage(Long page, Long limit, SpuInfo spuInfo);

    /*
     *   功能描述:获取销售属性
     *   @Param:
     *   @Return:
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /*
     *   功能描述:获取品牌属性
     *   @Param:
     *   @Return:
     */
    List<BaseTrademark> getTrademarkList();

    /*
     *   功能描述:添加spu
     *   @Param:SpuInfo
     *   @Return:
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /*
     *   功能描述:文件上传
     *   @Param:file
     *   @Return:
     */
    String fileUpload(MultipartFile file);
}
