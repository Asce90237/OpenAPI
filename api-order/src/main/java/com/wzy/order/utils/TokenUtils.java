package com.wzy.order.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Asce
 */
@Component
@Slf4j
public class TokenUtils {

    //密钥
    private final byte[] data = "Asce.OpenApi".getBytes();

    /**
     * 生成token
     * @param id
     * @return
     */
    public String generateToken(String id){
        DateTime now = DateTime.now();
        DateTime newTime = now.offsetNew(DateField.HOUR, 1); //过期时间1小时
        // payload荷载信息
        Map<String, Object> payload  = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("id", id);
                //签发时间
                put(RegisteredPayload.ISSUED_AT, now);
                //过期时间
                put(RegisteredPayload.EXPIRES_AT, newTime);
                //这个是自定义的过期时间，便于判定是否需要刷新
                put(CommonConstant.TOKEN_EXP_TIME, System.currentTimeMillis() + 1000 * 60 * 60);
            }
        };
        return JWTUtil.createToken(payload, data);
    }

    /**
     * 验证token是否合法和是否过期
     * @param authorization
     * @return
     */
    public JWT ifValidReturnJWT(String authorization) {
        JWT jwt = JWTUtil.parseToken(authorization);
        if(!jwt.setKey(data).verify()) {
            throw new RuntimeException("token 非法");
        }
        if(!jwt.validate(0)) {
            throw new RuntimeException("token 已过期请重新登录");
        }
        return jwt;
    }

    /**
     * 验证是否需要刷新
     * @param time
     * @return
     */
    public boolean ifRefresh(long time) {
        long current = System.currentTimeMillis();
        long diff = Math.abs(current - time);
        return diff < 10 * 60 * 1000;
    }
}
