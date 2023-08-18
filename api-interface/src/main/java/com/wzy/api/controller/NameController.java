package com.wzy.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试 API
 *
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(Object name) throws Exception {
        byte[] bytes = name.toString().getBytes("iso8859-1");
        name = new String(bytes,"utf-8");
        return "GET 你的名字是：" + name;
    }
}
