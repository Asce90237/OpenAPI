package com.wzy.api.controller;

import com.google.gson.Gson;
import com.wzy.api.service.AuthService;
import common.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class MainController {
    @Autowired
    private AuthService authService;

    /**
     * 请求转发
     *
     * @param request
     */
    @RequestMapping("/main")
    public String MainRedirect(HttpServletRequest request) {
        String res = null;
        Gson gson = new Gson();
        try {
            res = gson.toJson(ResultUtils.success(authService.mainRedirect(request)));
        } catch (Exception e) {
            res = gson.toJson(ResultUtils.error(ErrorCode.API_INVOKE_ERROR));
        }
        return res;
    }

}
