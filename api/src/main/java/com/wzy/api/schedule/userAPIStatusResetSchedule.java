package com.wzy.api.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wzy.api.mapper.UserInterfaceInfoMapper;
import com.wzy.api.mapper.UserMapper;
import com.wzy.api.model.entity.InterfaceInfo;
import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.service.InterfaceInfoService;
import com.wzy.api.service.UserInterfaceInfoService;
import common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Asce
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class userAPIStatusResetSchedule {

    @Autowired
    private UserInterfaceInfoService userInterfaceInfoService;

    @Autowired
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    /**
     * 每天凌晨更新用户领取状态
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetField() {
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("status", 1).set("status", 0);
        userInterfaceInfoMapper.update(null, updateWrapper);
    }

    /**
     * 更新首页数据缓存
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetIndexCache() {
        // 1. 全站接口已上线数
        String cnt = String.valueOf(interfaceInfoService.count(new QueryWrapper<InterfaceInfo>().eq("isDelete",0).eq("status",1)));
        stringRedisTemplate.opsForValue().set(RedisConstant.API_INDEX_INTERFACE_CNT, cnt, 1, TimeUnit.DAYS);
        // 2. 接口总调用次数
        String total = userInterfaceInfoMapper.getTotalInvokeCount();
        stringRedisTemplate.opsForValue().set(RedisConstant.API_INDEX_INVOKE_CNT, total, 1, TimeUnit.DAYS);
    }

}
