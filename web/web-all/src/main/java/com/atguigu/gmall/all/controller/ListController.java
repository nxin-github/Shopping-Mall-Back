package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 王木风
 */
@Controller
public class ListController {
    @Autowired
    private ListFeignClient listFeignClient;

    /**
     * 列表搜索
     * @param searchParam
     * @return
     */
    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        Result<Map> result = listFeignClient.list(searchParam);
        model.addAllAttributes(result.getData());
        //  需要存储${searchParam.keyword} ${trademarkParam} ${urlParam} ${propsParamList} ${trademarkList}
        //  ${attrsList} ${orderMap.type == '1' ? 'active': ''}  ${goodsList}
        model.addAttribute("searchParam",searchParam);

        //  先完成urlParam 参数的设置！ 主要功能记录当前查询的条件！
        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);

        //  品牌面包屑： "品牌： 品牌名称" 品牌Id:品牌名
        String trademarkParam = makeTradeMarkParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam",trademarkParam);

//  平台属性面包屑： 平台属性名：平台属性值名 : 传入的参数：用户点击的平台属性值过滤props=23:4G:运行内存
        //  根据前端页面 ： prop.attrId prop.attrValue prop.attrName  | SearchAttr
        List<Map> propsParamList = makePropsParamList(searchParam.getProps());
        model.addAttribute("propsParamList",propsParamList);

        //  后台存储orderMap： 看做一个map
        Map orderMap = makeOrderMap(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        return "list/index";
    }

    //  排序使用！
    private Map makeOrderMap(String order) {
        HashMap<String, Object> map = new HashMap<>();
        //  orderMap.type 表示按照综合，还是价格进行排序
        //  orderMap.sort 表示排序方式
        //  判断 综合 1：asc  或者 1：desc  价格 2：asc 或者 2：desc
        if (!StringUtils.isEmpty(order)){
            //  分割数据
            String[] split = order.split(":");
            if(split!=null && split.length==2){
                map.put("type",split[0]);
                map.put("sort",split[1]);
            }
        }else {
            map.put("type","1");
            map.put("sort","desc");
        }
        //  返回数据
        return map;
    }

    //  平台属性面包屑：
    private List<Map> makePropsParamList(String[] props) {
        //  声明一个集合来存储map 集合数据
        List<Map> list = new ArrayList<>();
        //  判断
        if (props!=null && props.length>0){
            //  循环遍历
            for (String prop : props) {
                //  prop = 23:8G:运行内存
                //  进行分割
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("attrId",split[0]);
                    map.put("attrValue",split[1]);
                    map.put("attrName",split[2]);
                    list.add(map);
                }
            }
        }
        return list;
    }

    //  品牌面包屑
    private String makeTradeMarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)){
            //  trademark = 品牌Id:品牌名
            //  分割：
            String[] split = trademark.split(":");
            //  判断
            if (split!=null && split.length==2){
                return "品牌:"+split[1];
            }
        }
        return null;
    }

    /**
     * 制作urlParam 参数
     * @param searchParam 获取到用户之前通过哪些值进行检索！
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        //  创建一个对象
        StringBuilder sb = new StringBuilder();
        //  判断这个用户通过哪些属性进行检索过
        //  http://list.gmall.com/list.html?category3Id=61
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            sb.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            sb.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            sb.append("category1Id=").append(searchParam.getCategory1Id());
        }

        //  判断是否根据关键字检索
        //  http://list.gmall.com/list.html?keyword=手机
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            sb.append("keyword=").append(searchParam.getKeyword());
        }
        //  还可以通过平台属性值进行过滤！
        //  http://list.atguigu.cn/list.html?category3Id=61&props=106:安卓手机:手机系统&props=23:4G:运行内存
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            //  循环遍历
            for (String prop : props) {  // prop= 106:安卓手机:手机系统 prop = 23:4G:运行内存
                if (sb.length()>0){
                    sb.append("&props=").append(prop);
                }
            }
        }
        //  还有可能根据品牌进行过滤
        //  http://list.atguigu.cn/list.html?category3Id=61&trademark=1:小米
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            if (sb.length()>0){
                sb.append("&trademark=").append(trademark);
            }
        }
        //  重新查询了一次，
        return "list.html?"+sb.toString();
    }
}
