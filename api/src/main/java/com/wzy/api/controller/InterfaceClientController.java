package com.wzy.api.controller;

import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoInvokRequest;
import com.wzy.api.service.InterfaceClientService;
import com.wzy.apiclient.common.BaseResponse;
import common.Exception.BusinessException;
import common.model.enums.ErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class InterfaceClientController {

    @Resource
    private InterfaceClientService interfaceClientService;

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
        return interfaceClientService.getResult(userRequestParams);
    }
}
