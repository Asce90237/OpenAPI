package com.wzy.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoInvokRequest;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.User;
import com.wzy.api.service.AuthService;
import com.wzy.apiclient.client.ApiClient;
import com.wzy.apiclient.common.BaseResponse;
import com.wzy.apiclient.model.Api;
import common.ErrorCode;
import common.Exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class InterfaceClientController {

    @Autowired
    private AuthService authService;

    /**
     * 接口在线调用
     * @param userRequestParams
     * @param request
     * @return
     */
    @PostMapping("/apiclient")
    public BaseResponse<Object> apiClient(@RequestBody InterfaceInfoInvokRequest userRequestParams, HttpServletRequest request) {
        if (userRequestParams == null || userRequestParams.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 优化为只查询一个字段判断是否存在
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Api api = new Api();
        api.setInterfaceId(userRequestParams.getId());
        api.setParameter(userRequestParams.getUserRequestParams());
        Auth auth = authService.getOne(new QueryWrapper<Auth>()
                .eq("userid", currentUser.getId())
                .ne("status", 1));
        if (auth == null) {
            throw new BusinessException(ErrorCode.AK_NOT_FOUND, "ak被禁用或不存在");
        }
        ApiClient apiClient = new ApiClient(auth.getAccesskey(),auth.getSecretkey());
        com.wzy.apiclient.common.BaseResponse result = apiClient.getResult(api);
        return result;
    }
}
