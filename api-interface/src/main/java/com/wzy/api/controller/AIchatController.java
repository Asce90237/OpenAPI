package com.wzy.api.controller;

import com.google.gson.Gson;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import common.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Random;

/**
 * AI chat API
 *
 */
@RestController
@RequestMapping("/ai")
public class AIchatController {

    @Resource
    private YuCongMingClient client;

    @GetMapping("/chat")
    public String chatWithAI(Object name) throws Exception {
        byte[] bytes = name.toString().getBytes("iso8859-1");
        name = new String(bytes,"utf-8");
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1680787048375554050L);
        devChatRequest.setMessage(name.toString());

        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        return response.getData().getContent();
    }

}
