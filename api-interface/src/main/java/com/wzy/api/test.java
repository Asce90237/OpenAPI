package com.wzy.api;

import com.alibaba.nacos.common.model.RestResult;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;

import javax.annotation.Resource;


/**
 * ak 、sk 生成算法
 */
public class test {

    public static void main(String[] args) {
        String res = new RestResult<>(403, "接口调用次数不足").toString();
        System.out.printf(res);
    }

}
