package com.wzy.api;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.model.RestResult;
import com.wzy.apiclient.model.Api;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * ak 、sk 生成算法
 */
public class test {

    public static void main(String[] args) throws UnsupportedEncodingException {
        Api api = new Api();
        api.setInterfaceId(31L);
        String encodedParameter = URLEncoder.encode("好笑", StandardCharsets.UTF_8.toString());
        api.setParameter(encodedParameter);
        String json = JSONUtil.toJsonStr(api);
        System.out.println(json);
    }

}
