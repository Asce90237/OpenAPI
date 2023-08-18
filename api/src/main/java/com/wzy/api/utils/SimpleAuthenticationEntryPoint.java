package com.wzy.api.utils;


import com.google.gson.Gson;
import common.model.BaseResponse;
import common.model.enums.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Asce
 * 自定义认证异常处理类：对应401
 */
@Component
public class SimpleAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        BaseResponse error = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "认证失败，请重新登录");
        Gson gson = new Gson();
        String json = gson.toJson(error);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(json);
    }
}
