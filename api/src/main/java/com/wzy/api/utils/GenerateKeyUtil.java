package com.wzy.api.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class GenerateKeyUtil {

    public String generateAk(String userAccount) {
        // 生成 AK
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String accessKey = DigestUtil.md5Hex(salt + userAccount + RandomUtil.randomNumbers(5));
        return accessKey;
    }

    public String generateSk(String userAccount) {
        // 生成 SK
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String secretKey = DigestUtil.md5Hex(salt + userAccount + RandomUtil.randomNumbers(5));
        return secretKey;
    }
}
