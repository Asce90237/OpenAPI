package com.wzy.api.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import common.AuthPhoneNumber;
import common.ErrorCode;
import common.Exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 号码归属地 API
 *
 */
@RestController
@RequestMapping("/tel")
public class TelController {

    @GetMapping("/getNumberHome")
    public String getNumberHome(Object tel) throws Exception {
        HttpResponse response = null;
        String num = tel.toString();
        AuthPhoneNumber authPhoneNumber = new AuthPhoneNumber();
        boolean isValid = authPhoneNumber.isPhoneNum(num);
        if (!isValid) {
            throw new BusinessException(ErrorCode.API_INVOKE_ERROR, "手机号非法");
        }
        // 访问第三方接口
        response = HttpRequest.post("https://zj.v.api.aa1.cn/api/phone-02/?num=" + num)
                .timeout(30000)//超时，毫秒
                .execute();
        String address = null;
        if (response.getStatus() != 200) {
            throw new BusinessException(ErrorCode.API_INVOKE_ERROR, "第三方接口调用错误");
        }
        String body = response.body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String code = jsonObject.getStr("code");
        if (!code.equals("200")) {
            throw new BusinessException(ErrorCode.API_INVOKE_ERROR, "第三方接口调用错误");
        }
        String data = jsonObject.getStr("data");
        JSONObject jsonObject1 = JSONUtil.parseObj(data);
        address = jsonObject1.getStr("address");
        return address;
    }

}
