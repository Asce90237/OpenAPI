package com.wzy.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * ACG图片 API
 *
 */
@RestController
@RequestMapping("/img")
public class ImageController {

    @Value("${img.url}")
    private String base_url;

    @GetMapping("/getByRandom")
    public String getImageByRandom(Object name) throws Exception {
        Random random = new Random();
        // 随机生成1到200数字
        int num = random.nextInt(200) + 1;
        String url = base_url + num + ".png";
        return url;
    }

}
