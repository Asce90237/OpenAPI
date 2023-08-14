package com.wzy.apiclient.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
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
    private final String url = "http://localhost:88/api/main";

    public ApiClient() {
    }

    public ApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getResult(Api api) throws UnsupportedEncodingException {
        String encodedParameter = URLEncoder.encode(api.getParameter(), StandardCharsets.UTF_8.toString());
        api.setParameter(encodedParameter);
        String json = JSONUtil.toJsonStr(api);
        return HttpRequest.post(url)
                .header("Accept","application/json;charset=UTF-8")
                .addHeaders(getHeaders(json))
                .charset("UTF-8")
                .body(json)
                .execute().body();
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
        // todo 加密防篡改和泄露sk
        map.put("sign", SignUtils.genSign(body, this.secretKey));
        // todo 增加随机数，只能使用一次，防重放攻击，服务端需保存该随机数
//        map.put("nonce", RandomUtil.randomNumbers(4));
//        map.put("timestamp", String.valueOf(DateUtil.date(System.currentTimeMillis())));
        return map;
    }


}
