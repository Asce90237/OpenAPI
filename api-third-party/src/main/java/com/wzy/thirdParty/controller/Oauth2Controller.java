package com.wzy.thirdParty.controller;


import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.wzy.thirdParty.common.GithubLoginStateGenerator;
import com.wzy.thirdParty.constants.OauthConstants;
import com.wzy.thirdParty.feign.UserFeignServices;
import common.BaseResponse;
import common.Utils.CookieUtils;
import common.constant.CookieConstant;
import common.to.Oauth2ResTo;
import common.vo.LoginUserVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


/**
 * 第三方登录
 */
@RequestMapping("/oauth")
@Controller
public class Oauth2Controller {

    @Value("${redirect.uri}")
    private String redirect_uri;

    @Value("${gitee.client_id}")
    private String gitee_client_id;

    @Value("${gitee.client_secret}")
    private String gitee_client_secret;

    @Value("${gitee.redirect_uri}")
    private String gitee_redirect_uri;

    @Value("${github.client_id}")
    private String github_client_id;

    @Value("${github.client_secret}")
    private String github_client_secret;

    @Value("${github.redirect_uri}")
    private String github_redirect_uri;

    @Autowired
    private UserFeignServices userFeignServices;

    @Autowired
    private RedisTemplate redisTemplate;

    //error=access_denied&error_description=用户或服务器拒绝了请求
    @GetMapping(value =  "/gitee",params = "error=access_denied")
    public String handleAccessDeniedGitee(HttpServletResponse res) throws IOException {
        PrintWriter writer = res.getWriter();
        writer.println("<script>alert('用户拒绝授权')</script>");
        return  redirect_uri;
    }

    @GetMapping("/handleGiteeState")
    public String handleGiteeState(HttpSession session) {
        String key = (String) session.getAttribute(OauthConstants.GITEE_SESSION_KEY);
        if (key == null) {
            key = OauthConstants.GITEE_LOGIN_PREFIX + UUID.randomUUID().toString();
            session.setAttribute(OauthConstants.GITEE_SESSION_KEY,key);
        }
        String state = (String) redisTemplate.opsForValue().get(key);
        if (state == null) {
            //生成随机state，防跨域攻击
            GithubLoginStateGenerator generator = new GithubLoginStateGenerator();
            state = generator.generate();
            redisTemplate.opsForValue().set(key, state, 10, TimeUnit.MINUTES);
        }
        String url = "https://gitee.com/oauth/authorize?client_id="+gitee_client_id+"&redirect_uri="+gitee_redirect_uri+"&response_type=code"+"&state=" + state;
        return "redirect:" + url;
    }

    @GetMapping("/gitee")
    public String gitee(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse res, HttpSession session) throws IOException {
        // 从session中获取缓存key
        String key = (String) session.getAttribute(OauthConstants.GITEE_SESSION_KEY);
        if (key == null || "".equals(key)) {
            return redirect_uri;
        }
        // 从Redis中获取state值
        String redisState = (String) redisTemplate.opsForValue().get(key);
        if (redisState == null || !redisState.equals(state)) {
            //为空代表过期
            // Redis中的state和回调中的state不匹配，说明可能存在CSRF攻击或其他安全问题，需要进行处理
            // 这里可以跳转到一个错误页面或返回一个错误信息
            return redirect_uri;
        }
        redisTemplate.delete(key);
        //4.接收授权码
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code=" + code +
                "&client_id=" + gitee_client_id +
                "&redirect_uri=" + gitee_redirect_uri +
                "&client_secret=" + gitee_client_secret;
        HttpResponse response =null;
        try {
            //5.获取访问令牌
            response = HttpRequest.post(url)
                    .timeout(20000)//超时，毫秒
                    .execute();
        }catch (Exception e){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('请求超时，请重试')</script>");
            return  redirect_uri;
        }
        if (response.getStatus() == 200){
            //5.获取访问令牌
            Oauth2ResTo oauth2ResTo = JSONUtil.toBean(response.body(), Oauth2ResTo.class);
            BaseResponse baseResponse = userFeignServices.oauth2Login(oauth2ResTo,"gitee");
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) return redirect_uri;
        }
        return redirect_uri;
    }

    @GetMapping("/handleGithubState")
    public String handleGithubState(HttpSession session) {
        String key = (String) session.getAttribute(OauthConstants.GITHUB_SESSION_KEY);
        if (key == null) {
            key = OauthConstants.GITHUB_LOGIN_PREFIX + UUID.randomUUID().toString();
            session.setAttribute(OauthConstants.GITHUB_SESSION_KEY,key);
        }
        String state = (String) redisTemplate.opsForValue().get(key);
        if (state == null) {
            //生成随机state，防跨域攻击
            GithubLoginStateGenerator generator = new GithubLoginStateGenerator();
            state = generator.generate();
            redisTemplate.opsForValue().set(key, state, 10, TimeUnit.MINUTES);
        }
        String url = "https://github.com/login/oauth/authorize?client_id="+github_client_id+"&redirect_uri="+github_redirect_uri+"&state=" + state;
        return "redirect:" + url;
    }

    //http://localhost:88/api/oauth/github?error=access_denied&error_description=The+user+has+denied+your+application+access.
    @GetMapping(value =  "/github",params = "error=access_denied")
    public String handleAccessDeniedGithub(HttpServletResponse res) throws IOException {
        PrintWriter writer = res.getWriter();
        writer.println("<script>alert('用户拒绝授权')</script>");
        return  redirect_uri;
    }

    //如果用户接受你的请求，GitHub 会使用代码参数中的临时 code 以及你在上一步的 state 参数中提供的状态重定向回你的站点。
    // 临时代码将在 10 分钟后到期。 如果状态不匹配，然后第三方创建了请求，您应该中止此过程。
    @GetMapping("/github")
    public String github(@RequestParam("code") String code,@RequestParam("state") String state, HttpServletResponse res, HttpSession session) throws IOException {
        // 从session中获取缓存key
        String key = (String) session.getAttribute(OauthConstants.GITHUB_SESSION_KEY);
        if (key == null || "".equals(key)) {
            return redirect_uri;
        }
        // 从Redis中获取state值
        String redisState = (String) redisTemplate.opsForValue().get(key);
        if (redisState == null || !redisState.equals(state)) {
            //为空代表过期
            // Redis中的state和回调中的state不匹配，说明可能存在CSRF攻击或其他安全问题，需要进行处理
            // 这里可以跳转到一个错误页面或返回一个错误信息
            return redirect_uri;
        }
        redisTemplate.delete(key);
        String url = "https://github.com/login/oauth/access_token?client_id=" + github_client_id +
                "&client_secret=" + github_client_secret +
                "&code=" + code;
        HttpResponse response = null;
        try {
             response = HttpRequest.post(url)
                    .timeout(30000)//超时，毫秒
                    .execute();
        }catch (Exception e){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('请求超时，请重试')</script>");
            return  redirect_uri;
        }
        if (response.getStatus() == 200){
            //access_token=gho_16C7e42F292c6912E7710c838347Ae178B4a&scope=repo%2Cgist&token_type=bearer
            String s = response.body().toString();
            String[] split = s.split("&");
            String s1 = split[0];
            String[] split1 = s1.split("=");
            String token = split1[1];
            Oauth2ResTo oauth2ResTo = new Oauth2ResTo();
            oauth2ResTo.setAccess_token(token);
            BaseResponse baseResponse = userFeignServices.oauth2Login(oauth2ResTo,"github");
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) return redirect_uri;
        }
        return redirect_uri;
    }

    private boolean cookieResUtils(HttpServletResponse res, @NotNull BaseResponse baseResponse) throws IOException {
        if (baseResponse.getCode() != 0){
            PrintWriter writer = res.getWriter();
            writer.println("<script>alert('登录失败')</script>");
            return true;
        }
        Object data = baseResponse.getData();
        LoginUserVo loginUserVo = JSONUtil.toBean(JSONUtil.parseObj(data), LoginUserVo.class);
        Cookie cookie = new Cookie(CookieConstant.headAuthorization,loginUserVo.getToken());
        cookie.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        CookieUtils cookieUtils = new CookieUtils();
        String autoLoginContent = cookieUtils.generateAutoLoginContent(loginUserVo.getId().toString(), loginUserVo.getUserAccount());
        Cookie cookie1 = new Cookie(CookieConstant.autoLoginAuthCheck, autoLoginContent);
        cookie1.setPath("/");
        cookie.setMaxAge(CookieConstant.expireTime);
        res.addCookie(cookie);
        res.addCookie(cookie1);
        return false;
    }
}
