package com.wzy.api.service;

import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoInvokRequest;
import com.wzy.apiclient.common.BaseResponse;

public interface InterfaceClientService {

    BaseResponse<Object> getResult(InterfaceInfoInvokRequest userRequestParams);
}
