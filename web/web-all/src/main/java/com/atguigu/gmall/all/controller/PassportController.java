package com.atguigu.gmall.all.controller;

import org.apache.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author：王木风
 * @date 2021/8/20 20:08
 * @description：登录
 */
@Controller
public class PassportController {

    //  http://passport.gmall.com/login.html?originUrl=http://www.gmall.com/
    //  从哪里点击的URL 被记录在 originUrl 参数中！
    @GetMapping("login.html")
    public String login(HttpServletRequest request) {
        //  登录完成之后，调回到原来的的页面！
        String originUrl = request.getParameter("originUrl");
        //  保存到作用域中
        request.setAttribute("originUrl",originUrl);
        return "login";
    }
}
