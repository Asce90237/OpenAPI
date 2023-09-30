package com.wzy.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.api.model.dto.interfaceinfo.InterfaceInfoInvokRequest;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.LoginUser;
import com.wzy.api.model.entity.User;
import com.wzy.api.service.AuthService;
import com.wzy.api.service.InterfaceClientService;
import com.wzy.apiclient.client.ApiClient;
import com.wzy.apiclient.common.BaseResponse;
import com.wzy.apiclient.model.Api;
import common.Exception.BusinessException;
import common.Utils.ResultUtils;
import common.constant.RedisConstant;
import common.model.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class InterfaceClientServiceImpl implements InterfaceClientService {

    @Autowired
    private AuthService authService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public BaseResponse<Object> getResult(InterfaceInfoInvokRequest userRequestParams) {
        // todo 优化为只查询一个字段判断是否存在
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        User user = principal.getUser();
        String nonce = userRequestParams.getNonce();
        String timestamp = userRequestParams.getTimestamp();
        // 判断是否是重放攻击
        boolean reProduct = isReProduct(String.valueOf(user.getId()), nonce, timestamp);
        if (reProduct) {
            // 重放，返回空值
            return null;
        }
        Api api = new Api();
        api.setInterfaceId(userRequestParams.getId());
        api.setParameter(userRequestParams.getUserRequestParams());
        Auth auth = authService.getOne(new QueryWrapper<Auth>()
                .eq("userid", user.getId())
                .ne("status", 1));
        if (auth == null) {
            throw new BusinessException(ErrorCode.AK_NOT_FOUND, "ak被禁用或不存在");
        }
        ApiClient apiClient = new ApiClient(auth.getAccesskey(),auth.getSecretkey());
        com.wzy.apiclient.common.BaseResponse result = apiClient.getResult(api);
        return result;
    }

    public boolean isReProduct(String userId, String nonce, String timestamp) {
        // 判断随机数是否已经使用过
        long bit = Long.parseLong(nonce);
        Boolean b = stringRedisTemplate.opsForValue().getBit(RedisConstant.USER_BITMAP + userId, bit);
        if (b != null && b) {
            return true;
        }
        // 判断时间戳是否过期
        long time = Long.parseLong(timestamp);
        boolean timeIsValid = Math.abs(time - System.currentTimeMillis()) > 1000 * 60 * 6;
        if (timeIsValid) {
            return true;
        }
        // 保存随机数，设置过期时间6分钟
        stringRedisTemplate.opsForValue().setBit(RedisConstant.USER_BITMAP + userId, bit, true);
        stringRedisTemplate.expire(RedisConstant.USER_BITMAP + userId, 6, TimeUnit.MINUTES);
        return false;
    }
}
