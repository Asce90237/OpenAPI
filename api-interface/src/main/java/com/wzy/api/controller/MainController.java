package com.wzy.api.controller;

import com.google.gson.Gson;
import com.wzy.api.service.MainService;
import com.wzy.apiclient.model.Api;
import common.model.enums.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class MainController {
    @Autowired
    private MainService mainService;

    /**
     * 请求转发
     *
     * @param api
     */
    @PostMapping("/main")
    public String MainRedirect(@RequestBody Api api) {
        String res = null;
        Gson gson = new Gson();
        try {
            res = gson.toJson(ResultUtils.success(mainService.mainRedirect(api)));
        } catch (Exception e) {
            res = gson.toJson(ResultUtils.error(ErrorCode.API_INVOKE_ERROR));
        }
        return res;
    }

}
