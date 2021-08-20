package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @Author：王木风
* @date 2021/8/20 8:47
* @description：
*/
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     * @param userInfo
     * @param request
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        UserInfo info = userService.login(userInfo);

        if (info != null) {
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            HashMap<String, Object> map = new HashMap<>();
            map.put("nickName", info.getNickName());
            map.put("token", token);

            JSONObject userJson = new JSONObject();
            userJson.put("userId", info.getId().toString());
//            防止盗用cookie，增加一项ip判断
            userJson.put("ip", IpUtil.getIpAddress(request));
//            放到redis实现服务期间共享
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token, userJson.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(map);
        } else {
            return Result.fail().message("用户名或密码错误");
        }
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + request.getHeader("token"));
        return Result.ok();
    }
}
