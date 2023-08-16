package com.wzy.api.common;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
        DateTime newTime = now.offsetNew(DateField.HOUR, 168); //过期时间7天
        // payload荷载信息
        Map<String, Object> payload  = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("id", id);
                //签发时间
                put(RegisteredPayload.ISSUED_AT, now);
                //过期时间
                put(RegisteredPayload.EXPIRES_AT, newTime);
            }
        };
        String token = JWTUtil.createToken(payload, signer);
        return token;
    }


    /**
     * 验证token是否合法
     * @param token
     * @return
     */
    public boolean verifyToken(String token){
        try {
            JWT jwt = JWTUtil.parseToken(token);
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
     * 验证JWT是否有效，验证包括：
     * Token是否正确
     * RegisteredPayload.NOT_BEFORE：生效时间不能晚于当前时间
     * RegisteredPayload.EXPIRES_AT：失效时间不能早于当前时间
     * RegisteredPayload.ISSUED_AT： 签发时间不能晚于当前时间
     * Parameters:
     * leeway - 容忍空间，单位：秒。当不能晚于当前时间时，向后容忍；不能早于向前容忍。
     * @param token
     * @return
     */
    public  boolean verifyTime(String token){
        JWT jwt = JWTUtil.parseToken(token);
        boolean verifyTime = jwt.validate(0);
        return !verifyTime;
    }

    /**
     * 刷新token
     * @param token
     * @return
     */
    public String refreshToken(String token){
        JWT jwt = JWTUtil.parseToken(token);
        String id = (String) jwt.getPayload("id");
        String generateToken = generateToken(id);
        return generateToken;
    }
}
