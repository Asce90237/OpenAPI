package com.wzy.order.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.google.gson.Gson;
import com.wzy.order.model.entity.LoginUser;
import com.wzy.order.utils.TokenUtils;
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
        //todo 刷新token
        //解析token
        JWT jwt = JWTUtil.parseToken(authorization);
        String id = (String) jwt.getPayload("id");
        //读缓存中的用户信息
        String json = stringRedisTemplate.opsForValue().get(CommonConstant.JWT_CACHE_PREFIX + id);
        if (json == null) {
            throw new RuntimeException("用户登录信息过期");
        }
        Gson gson = new Gson();
        LoginUser user = gson.fromJson(json, LoginUser.class);
        //存入security context
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
