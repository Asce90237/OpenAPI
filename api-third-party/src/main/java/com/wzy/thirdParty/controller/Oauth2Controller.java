package com.wzy.thirdParty.controller;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.wzy.thirdParty.model.entity.LoginUser;
import com.wzy.thirdParty.utils.TokenUtils;
import common.constant.CommonConstant;
import common.constant.CookieConstant;
import common.dubbo.ApiInnerService;
import common.model.BaseResponse;
import common.model.entity.User;
import common.model.to.Oauth2ResTo;
import common.model.vo.LoginUserVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @DubboReference(timeout = 10000) // dubbo 远程调用超时
    private ApiInnerService apiInnerService;

    @Autowired
    private TokenUtils tokenUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping(value =  "/gitee",params = "error=access_denied")
    public String handleAccessDeniedGitee() {
        return redirect_uri;
    }

    @GetMapping("/handleGiteeState")
    public String handleGiteeState() {
        String url = "https://gitee.com/oauth/authorize?client_id="+gitee_client_id+"&redirect_uri="+gitee_redirect_uri+"&response_type=code";
        return "redirect:" + url;
    }

    @GetMapping("/gitee")
    public String gitee(@RequestParam("code") String code, HttpServletResponse res) {
        //4.接收授权码
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code=" + code +
                "&client_id=" + gitee_client_id +
                "&redirect_uri=" + gitee_redirect_uri +
                "&client_secret=" + gitee_client_secret;
        HttpResponse response = null;
        try {
            //5.获取访问令牌
            response = HttpRequest.post(url)
                    .timeout(20000)//超时，毫秒
                    .execute();
        }catch (Exception e){
            return  redirect_uri;
        }
        if (response.getStatus() == 200){
            //5.获取访问令牌
            Oauth2ResTo oauth2ResTo = JSONUtil.toBean(response.body(), Oauth2ResTo.class);
            BaseResponse baseResponse = apiInnerService.oauth2Login(oauth2ResTo,"gitee");
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) return redirect_uri;
        }
        return redirect_uri;
    }

    @GetMapping("/handleGithubState")
    public String handleGithubState() {
        String url = "https://github.com/login/oauth/authorize?client_id="+github_client_id+"&redirect_uri="+github_redirect_uri;
        return "redirect:" + url;
    }

    //oauth/github?error=access_denied&error_description=The+user+has+denied+your+application+access.
    @GetMapping(value =  "/github",params = "error=access_denied")
    public String handleAccessDeniedGithub() throws IOException {
        return redirect_uri;
    }

    @GetMapping("/github")
    public String github(@RequestParam("code") String code, HttpServletResponse res) {
        String url = "https://github.com/login/oauth/access_token?client_id=" + github_client_id +
                "&client_secret=" + github_client_secret +
                "&code=" + code;
        HttpResponse response = null;
        try {
             response = HttpRequest.post(url)
                    .timeout(30000)//超时，毫秒
                    .execute();
        }catch (Exception e){
            return redirect_uri;
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
            BaseResponse baseResponse = apiInnerService.oauth2Login(oauth2ResTo,"github");
            //拿到token，远程调用查询用户是否注册、未注册的自动进行注册，已经完成注册的，则进行登录
            if (cookieResUtils(res, baseResponse)) return redirect_uri;
        }
        return redirect_uri;
    }

    private boolean cookieResUtils(HttpServletResponse res, @NotNull BaseResponse baseResponse) {
        if (baseResponse.getCode() != 0){
            return true;
        }
        Object data = baseResponse.getData();
        LoginUser loginUser = JSONUtil.toBean(JSONUtil.parseObj(data), LoginUser.class);
        //token载荷信息包括用户id
        User user = loginUser.getUser();
        String token = tokenUtils.generateToken(String.valueOf(user.getId()));
        Cookie cookie = new Cookie(CookieConstant.headAuthorization,token);
        cookie.setPath("/");
        //不设置过期时间，这样cookie就是会话级别的，关闭浏览器就会消失
//        cookie.setMaxAge(CookieConstant.expireTime);
        res.addCookie(cookie);
        Gson gson = new Gson();
        String json = gson.toJson(loginUser);
        stringRedisTemplate.opsForValue().set(CommonConstant.JWT_CACHE_PREFIX + loginUser.getUser().getId(), json, 1, TimeUnit.HOURS);
        return false;
    }
}
