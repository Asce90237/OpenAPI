package com.wzy.api.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import com.google.gson.Gson;
import com.wzy.api.model.entity.LoginUser;
import com.wzy.api.utils.TokenUtils;
import common.constant.CommonConstant;
import common.constant.CookieConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class JWTAuthenticationTokenFilter implements Filter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenUtils tokenUtils;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //获取token
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorization = null;
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (CookieConstant.headAuthorization.equals(name)){
                authorization = cookie.getValue();
            }
        }
        if (authorization == null || authorization.length() == 0){
            filterChain.doFilter(request, response);
            return;
        }
        //验证token是否合法
        boolean verifyToken = tokenUtils.verifyToken(authorization);
        if (!verifyToken) {
            throw new RuntimeException("token非法");
        }
        boolean verifyTime = tokenUtils.verifyTime(authorization);
        if (!verifyTime) {
            throw new RuntimeException("token过期");
        }
        //解析token
        JWT jwt = JWTUtil.parseToken(authorization);
        String id = (String) jwt.getPayload("id");
        //读缓存中的用户信息
        String json = stringRedisTemplate.opsForValue().get(CommonConstant.JWT_CACHE_PREFIX + id);
        if (json == null) {
            throw new RuntimeException("用户登录信息过期");
        }
        //判断是否刷新token，若jwt过期时间与当前时间差值小于10分钟，则重新颁发，且刷新缓存
        long dateTime = (long) jwt.getPayload(CommonConstant.TOKEN_EXP_TIME);
        boolean ifRefresh = tokenUtils.ifRefresh(dateTime);
        if (ifRefresh) {
            // 若即将过期，重新颁发，达到刷新
            stringRedisTemplate.opsForValue().set(CommonConstant.JWT_CACHE_PREFIX + id, json, 1, TimeUnit.HOURS);
            String newToken = tokenUtils.generateToken(id);
            Cookie cookie = new Cookie(CookieConstant.headAuthorization, newToken);
            cookie.setPath("/"); // 设置 Cookie 的路径
            response.addCookie(cookie);
        }
        Gson gson = new Gson();
        LoginUser user = gson.fromJson(json, LoginUser.class);
        //存入security context
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
