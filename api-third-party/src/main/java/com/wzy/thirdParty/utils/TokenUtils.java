package com.wzy.thirdParty.utils;

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
    //签名器
    private final JWTSigner signer = JWTSignerUtil.hs512(data);

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
                put(CommonConstant.TOKEN_EXP_TIME, System.currentTimeMillis() + 1000 * 60 * 60); // 这个是自定义的过期时间，便于判定是否需要刷新
            }
        };
        String token = JWTUtil.createToken(payload, signer);
        return token;
    }


    /**
     * 验证token是否合法
     * @param authorization
     * @return
     */
    public boolean verifyToken(String authorization){
        try {
            JWT jwt = JWTUtil.parseToken(authorization);
            // HS512
            String algorithm = jwt.getAlgorithm();
            if (!"HS512".equals(algorithm)){
                return false;
            }
            boolean verifyKey = jwt.setSigner(signer).verify();
            if (!verifyKey){
                return false;
            }
            return true;
        }catch (Exception e){
            log.error("verifyKey方法error--->{}",String.valueOf(e));
            return false;
        }
    }

    /**
     * 验证token是否过期
     * Parameters:
     * leeway - 容忍空间，单位：秒。当不能晚于当前时间时，向后容忍；不能早于向前容忍。
     * @param authorization
     * @return
     */
    public boolean verifyTime(String authorization){
        JWT jwt = JWTUtil.parseToken(authorization);
        boolean verifyTime = jwt.validate(0);
        return !verifyTime;
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
