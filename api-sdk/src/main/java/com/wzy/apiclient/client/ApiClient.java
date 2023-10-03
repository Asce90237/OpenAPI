package com.wzy.apiclient.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.wzy.apiclient.common.BaseResponse;
import com.wzy.apiclient.model.Api;
import com.wzy.apiclient.utils.SignUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    private String accessKey;
    private String secretKey;
    private final String url = "https://www.openapi.love/api/main";
//    private final String url = "http://localhost:88/api/main";

    public ApiClient() {
    }

    public ApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public BaseResponse getResult(Api api) {
        String encodedParameter = "";
        try {
            encodedParameter = URLEncoder.encode(api.getParameter(), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
        } finally {
            api.setParameter(encodedParameter);
        }
        String json = JSONUtil.toJsonStr(api);
        String result =  HttpRequest.post(url)
                .header("Accept","application/json;charset=UTF-8")
                .addHeaders(getHeaders(json))
                .charset("UTF-8")
                .body(json)
                .execute().body();
        Gson gson = new Gson();
        BaseResponse res = gson.fromJson(result, BaseResponse.class);
        return res;
    }

    /**
     * 设置请求参数
     * @param body
     * @return
     */
    private Map<String, String> getHeaders(String body) {
        Map<String, String> map = new HashMap<>();
        map.put("accessKey", this.accessKey);
        map.put("body", body);
        // 加密防篡改和泄露sk
        map.put("sign", SignUtils.genSign(body, this.secretKey));
        // 增加随机数，只能使用一次，防重放攻击，服务端需保存该随机数
//        map.put("nonce", RandomUtil.randomNumbers(8));
        // 随机数会定时清除，再添加时间戳
//        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return map;
    }


}
