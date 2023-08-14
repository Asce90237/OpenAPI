package com.wzy.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.wzy.api.constant.AuthConstant;
import com.wzy.api.constant.CommonConstant;
import common.Exception.BusinessException;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoInvokRequest;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.entity.User;
import com.wzy.api.model.enums.InterFaceInfoEnum;
import com.wzy.api.service.AuthService;
import com.wzy.api.service.InterfaceInfoService;
import com.wzy.apiclient.client.ApiClient;
import com.wzy.apiclient.model.Api;
import common.BaseResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class InterfaceClientController {

    @Autowired
    private AuthService authService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 接口在线调用
     * @param userRequestParams
     * @param request
     * @return
     */
    @PostMapping("/apiclient")
    public BaseResponse<Object> apiClient(@RequestBody InterfaceInfoInvokRequest userRequestParams, HttpServletRequest request) throws UnsupportedEncodingException {
        if (userRequestParams == null || userRequestParams.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 接口id
        long id = userRequestParams.getId();
        // todo 优化为只查询一个字段判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String params = userRequestParams.getUserRequestParams();
        if(params == null && !oldInterfaceInfo.getRequestParams().equals(CommonConstant.INTERFACE_PARAM_STATUS)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请求参数错误");
        }
        Api api = new Api();
        api.setInterfaceId(id);
        api.setParameter(params);
        Auth auth = authService.getOne(new QueryWrapper<Auth>()
                .eq("userid", currentUser.getId())
                .ne("status", 1));
        if (auth == null) {
            throw new BusinessException(ErrorCode.SK_ERROR, "ak被禁用");
        }
        ApiClient apiClient = new ApiClient(auth.getAccesskey(),auth.getSecretkey());
        String result = apiClient.getResult(api);
        Gson gson = new Gson();
        BaseResponse res = gson.fromJson(result, BaseResponse.class);
        return res;
    }
}
