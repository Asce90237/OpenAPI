package com.wzy.api.controller;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 外卖评价生成器 API
 *
 */
@RestController
@RequestMapping("/rating")
public class RatingGenerationController {

    @Resource
    private YuCongMingClient client;

    @Value("${api_interface.rating_generation}")
    private String modelId;

    @GetMapping("/generation")
    public String chatWithAI(Object name) throws Exception {
        byte[] bytes = name.toString().getBytes("iso8859-1");
        name = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(Long.valueOf(modelId));
        devChatRequest.setMessage(name.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        return response.getData().getContent();
    }

}
