package com.wzy.api.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.api.mapper.AuthMapper;
import common.model.entity.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Asce
 */
@Component
public class AuthUtils {

    @Resource
    private JwtUtils jwtUtils;

    @Autowired
    private AuthMapper authMapper;

    /**
     * 验证用户的调用信息是否正确
     *
     * @param headers
     * @return
     */
    public boolean isAuth(Map<String, String> headers) {
        String userId = headers.get("userId");
        String appId = headers.get("appId");
        if(userId.isEmpty() || appId.isEmpty()){
            return false;
        }
        Auth auth = authMapper.selectOne(new QueryWrapper<Auth>().eq("userid", userId).eq("appid", appId));
        if (auth == null) {
            return false;
        }
        String accessKey = headers.get("accessKey");
        String secretKey = headers.get("secretKey");
        if(accessKey.isEmpty() || secretKey.isEmpty()){
            return false;
        }
        if (!auth.getAccesskey().equals(accessKey) || !auth.getSecretkey().equals(secretKey)) {
            return false;
        }
        String timestamp = headers.get("timestamp");
        String now = String.valueOf(DateUtil.date(System.currentTimeMillis()));
        // 添加timestamp来防止重放攻击
        // DateUtil.between(date1,date2,DateUnit.HOUR) 判断date1与date2相差多少小时
        if (timestamp == null || timestamp.isEmpty() || DateUtil.between(DateUtil.parse(timestamp), DateUtil.parse(now), DateUnit.HOUR) > 1) {
            return false;
        }
        String token = auth.getToken();
        if(token.isEmpty()){
            return false;
        }
        boolean isToken = jwtUtils.isToken(appId, token, accessKey, secretKey);
        return isToken;
    }

    /**
     * 获取请求头中的信息
     *
     * @param request
     * @return
     */
    public Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("body", request.getHeader("body"));
        return map;
    }
}
