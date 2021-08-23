package com.atguigu.gmall.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.util.List;

/**
 * @Author：王木风
 * @date 2021/8/20 20:40
 * @description：过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Autowired
    private RedisTemplate redisTemplate;

    //  获取白名单中控制器路径
    @Value("${authUrls.url}")//再nacos配置文件中
    private String authUrl; // authUrl=trade.html,myOrder.html,list.html #
    //  引入一个对象
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //  用户不能通过浏览器访问内部数据接口！
        //  获取用户请求的url 路径
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (antPathMatcher.match("/**/inner/**", path)) {//符合，意味来自内部路径
            ServerHttpResponse response = exchange.getResponse();
            //  停止代码运行
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //  获取用户Id 存储在缓存中的，key = token 组成
        //  userId 返回 -1 的话，相当于用户盗用了token ，不能正常返回userId
        String userId = getUserId(request);
        String userTempId = getuserTempId(request);
        //  判断
        if ("-1".equals(userId)){
            //  应该做出响应
            ServerHttpResponse response = exchange.getResponse();
            //  停止代码运行
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //  判断用户是否访问带有 这样的资源路径 /api/**/auth/** ，则用户必须登录！
        //  path =item.gmall.com/api/aaa/bbb/auth/findAll
        if (antPathMatcher.match("/api/**/auth/**",path)){
            //  是否访问关键看用户Id
            if (StringUtils.isEmpty(userId)){
                //  应该做出响应
                ServerHttpResponse response = exchange.getResponse();
                //  停止代码运行
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //  判断用户访问的控制器 是否在 authUrl 变量中！(白名单)
        //  authUrl=trade.html,myOrder.html,list.html
        //  http://list.gmall.com/list.html?category3Id=61
        String[] split = authUrl.split(",");
        for (String url : split) {
            //  split[0] = trade.html  split[1] = myOrder.html split[2] = list.html
            //  表示用户访问的路径中包含 上述的控制器 ,但是，此时用户未登录！
            if (path.indexOf(url)!=-1 && StringUtils.isEmpty(userId)){
                //  如果是上述情况则需要跳转到登录页面！
                ServerHttpResponse response = exchange.getResponse();
                //  设置参数
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //  设置跳转的页面
                response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+request.getURI());
                //  设置一下重定向
                return response.setComplete();
            }
        }


        //  如果上述没有问题，则需要将用户Id 进行保存 ！ 记住的！
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            if(!StringUtils.isEmpty(userId)) {
                request.mutate().header("userId", userId).build();
            }
            if(!StringUtils.isEmpty(userTempId)) {
                request.mutate().header("userTempId", userTempId).build();
            }
            //  返回数据
            return chain.filter(exchange.mutate().request(request).build());
        }
        //  默认返回
        return chain.filter(exchange);
    }

    private String getuserTempId(ServerHttpRequest request) {
        String userTempId = "";
//        先从cookie中获取数据
        List<String> tokenList  = request.getHeaders().get("userTempId");
        if (tokenList != null) {
            userTempId = tokenList.get(0);
        } else {
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if (cookie != null) {
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }

    /**
     * 获取用户Id方法
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        //  用户Id 存储在缓存中的 缓存的key = user:token
        //  如何获取token ： cookie 或者 header 中！
        String token = "";
        //  从header 中获取token
        List<String> stringList = request.getHeaders().get("token");
        if (!CollectionUtils.isEmpty(stringList)) {
            token = stringList.get(0);
        } else {
            //  从cookie 中获取
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            if (httpCookie!=null){
                token = httpCookie.getValue();
            }
        }

        //  组成缓存key
        if (!StringUtils.isEmpty(token)) {
            String key = "user:login:"+token;
            //  获取缓存的数据
            String strJson = (String) redisTemplate.opsForValue().get(key);
            //  进行判断ip 地址是否正确！
            JSONObject jsonObject = JSON.parseObject(strJson, JSONObject.class);
            //  缓存中的IP地址
            String ip = (String) jsonObject.get("ip");
            //  缓存的Ip 地址与 要登录的ip 地址进行比较
            if (ip.equals(IpUtil.getGatwayIpAddress(request))){
                //  获取缓存中的userId
                String userId = (String) jsonObject.get("userId");
                //  返回
                return userId;
            }else {
                //  有人在盗用token 获取登录权限
                return "-1";
            }
        }
        return null;
    }

    /**
     * 提示用户方法
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //  将用户提示的信息输入到页面！
        Result<Object> result = Result.build(null, resultCodeEnum);
        //  此时，需要将result 这个对象转换为字符串
        String str = JSONObject.toJSONString(response);
        //  有了数据流
        DataBuffer wrap = response.bufferFactory().wrap(str.getBytes());
        //  要输出内容，则需要设置一下页面的格式！
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        //  将用户信息写入到页面！
        return response.writeWith(Mono.just(wrap));
    }
}
