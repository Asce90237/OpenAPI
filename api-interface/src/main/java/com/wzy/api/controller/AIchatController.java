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
 * AI chat API
 *
 */
@RestController
@RequestMapping("/ai")
public class AIchatController {

    @Resource
    private YuCongMingClient client;

    @Value("${api_interface.ai_chat}")
    private String modelId;

    @GetMapping("/chat")
    public String chatWithAI(Object name) throws Exception {
        byte[] bytes = name.toString().getBytes("iso8859-1");
        name = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(Long.valueOf(modelId));
        devChatRequest.setMessage(name.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if (response == null || response.getCode() != 0 || response.getData() == null) {
            throw new RuntimeException();
        }
        return response.getData().getContent();
    }

}
