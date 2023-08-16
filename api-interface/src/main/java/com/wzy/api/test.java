package com.wzy.api;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * ak 、sk 生成算法
 */
public class test {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "Asce";

    public static void main(String[] args) {
        long begin = System.currentTimeMillis();
        String userAccount = "user";
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        System.out.println(accessKey);
        long end = System.currentTimeMillis();
        System.out.println(end - begin);
        long begin1 = System.currentTimeMillis();
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String accessKey1 = DigestUtil.md5Hex(salt + userAccount + RandomUtil.randomNumbers(5));
        System.out.println(accessKey1);
        long end1 = System.currentTimeMillis();
        System.out.println(end1 - begin1);
    }

}
