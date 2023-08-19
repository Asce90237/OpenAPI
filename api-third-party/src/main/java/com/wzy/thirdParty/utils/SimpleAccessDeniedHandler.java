package com.wzy.thirdParty.utils;

import com.google.gson.Gson;
import common.Utils.ResultUtils;
import common.model.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Asce
 * 自定义授权异常处理类 ：对应403
 */
@Component
public class SimpleAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        BaseResponse error = ResultUtils.error(HttpStatus.FORBIDDEN.value(), "请求被拒绝，没有权限!");
        Gson gson = new Gson();
        String json = gson.toJson(error);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(json);
    }
}
