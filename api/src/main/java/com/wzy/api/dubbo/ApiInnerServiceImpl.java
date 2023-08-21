/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wzy.api.dubbo;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wzy.api.constant.CommonConstant;
import com.wzy.api.mapper.AuthMapper;
import com.wzy.api.mapper.InterfaceChargingMapper;
import com.wzy.api.mapper.InterfaceInfoMapper;
import com.wzy.api.model.entity.InterfaceCharging;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.entity.LoginUser;
import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.service.InterfaceChargingService;
import com.wzy.api.service.InterfaceInfoService;
import com.wzy.api.service.UserInterfaceInfoService;
import com.wzy.api.utils.Oauth2LoginUtils;
import com.wzy.api.utils.RedisTemplateUtils;
import common.Exception.BusinessException;
import common.Utils.ResultUtils;
import common.constant.RedisConstant;
import common.dubbo.ApiInnerService;
import common.model.BaseResponse;
import common.model.entity.ApiInfo;
import common.model.entity.Auth;
import common.model.enums.ErrorCode;
import common.model.to.LeftNumUpdateTo;
import common.model.to.Oauth2ResTo;
import common.model.vo.LockChargingVo;
import common.model.vo.LoginUserVo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author Asce
 */
@DubboService
public class ApiInnerServiceImpl implements ApiInnerService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private RedisTemplateUtils redisTemplateUtils;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private AuthMapper authMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private InterfaceChargingMapper interfaceChargingMapper;

    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Autowired
    private Oauth2LoginUtils oauth2LoginUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Auth getAuthByAk(String accessKey) {
        Auth auth = authMapper.getAuthByAk(accessKey);
        return auth;
    }

    /**
     * 接口调用次数更新
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //todo 该部分需要加锁，实现一个分布式锁，保证数据的一致性

        // 判断
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        return userInterfaceInfoService.update(updateWrapper);
    }

    /**
     * 判断用户在该接口上是否还有调用次数
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    @Override
    public boolean hasCount(long interfaceInfoId, long userId) {
        //判空
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo one = userInterfaceInfoService.getOne(new QueryWrapper<UserInterfaceInfo>()
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .gt("leftNum", 0));
        return one != null;
    }

    /**
     * 根据接口id获取接口参数
     * @param id
     * @return
     */
    @Override
    public ApiInfo getApiInfoById(long id) {
        return interfaceInfoMapper.getApiInfoById(id);
    }

    /**
     * 获取当前接口的剩余库存
     * @param interfaceInfoId
     * @return
     */
    @Override
    public String getPresentAvailablePieces(long interfaceInfoId) {
        String availablePieces = interfaceChargingMapper.getPresentAvailablePieces(interfaceInfoId);
        return availablePieces;
    }

    /**
     * 远程获取接口信息
     * @param interfaceInfoId
     * @return
     */
    @Override
    public BaseResponse getOrderInterfaceInfo(long interfaceInfoId) {
        return ResultUtils.success(interfaceInfoService.getById(interfaceInfoId));
    }

    /**
     * 更新库存
     * @param lockChargingVo
     * @return
     */
    @Override
    @Transactional
    public BaseResponse updateAvailablePieces(LockChargingVo lockChargingVo) {
        return interfaceChargingService.updateAvailablePieces(lockChargingVo);
    }

    /**
     * 解锁库存
     * @param lockChargingVo
     * @return
     */
    @Override
    @Transactional
    public BaseResponse unlockAvailablePieces(LockChargingVo lockChargingVo) {
        Long interfaceId = lockChargingVo.getInterfaceid();
        Long orderNum = lockChargingVo.getOrderNum();
        try {
            interfaceChargingService.update(new UpdateWrapper<InterfaceCharging>().eq("interfaceid",interfaceId)
                    .setSql("availablePieces = availablePieces + "+orderNum));
        }catch (Exception e){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"解锁库存失败");
        }
        redisTemplateUtils.delAllOnlinePage();
        return ResultUtils.success(null);
    }

    /**
     * 更新用户剩余可调用次数
     * @param leftNumUpdateTo
     * @return
     */
    @Override
    @Transactional
    public BaseResponse updateUserLeftNum(LeftNumUpdateTo leftNumUpdateTo){
        return userInterfaceInfoService.updateUserLeftNum(leftNumUpdateTo);
    }

    /**
     * 通过第三方登录
     * @param oauth2ResTo
     * @param type
     * @return
     */
    @Override
    @Transactional
    public BaseResponse oauth2Login(Oauth2ResTo oauth2ResTo, String type) {
        String accessToken = oauth2ResTo.getAccess_token();
        if (null == accessToken){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        //拿到用户的信息
        LoginUser loginUser = null;
        if ("gitee".equals(type)) {
            //6.访问用户信息
            HttpResponse giteeResponse = HttpRequest.get("https://gitee.com/api/v5/user?access_token=" + accessToken).execute();
            loginUser = oauth2LoginUtils.giteeOrGithubOauth2Login(type, giteeResponse);
        }else {
            HttpResponse githubResponse = HttpRequest.get("https://api.github.com/user")
                    .header("Authorization","Bearer "+accessToken)
                    .timeout(30000)
                    //超时，毫秒
                    .execute();
            loginUser = oauth2LoginUtils.giteeOrGithubOauth2Login(type, githubResponse);
        }
        return ResultUtils.success(loginUser);
    }

    /**
     * 判断是否是重放攻击
     * @param userId
     * @param nonce
     * @param timestamp
     * @return
     */
    @Override
    public boolean isReProduct(String userId, String nonce, String timestamp) {
        // 判断随机数是否已经使用过
        long bit = Long.parseLong(nonce);
        Boolean b = stringRedisTemplate.opsForValue().getBit(RedisConstant.USER_BITMAP + userId, bit);
        if (b != null && b) {
            return true;
        }
        // 判断时间戳是否过期
        long time = Long.parseLong(timestamp);
        boolean timeIsValid = Math.abs(time - System.currentTimeMillis()) > 1000 * 60 * 60 * 6;
        if (timeIsValid) {
            return true;
        }
        // 保存随机数，设置过期时间6小时
        stringRedisTemplate.opsForValue().setBit(RedisConstant.USER_BITMAP + userId, bit, true);
        stringRedisTemplate.expire(RedisConstant.USER_BITMAP + userId, 6, TimeUnit.HOURS);
        return false;
    }
}
