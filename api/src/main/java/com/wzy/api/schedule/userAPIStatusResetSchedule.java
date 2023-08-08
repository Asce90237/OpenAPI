package com.wzy.api.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    /**
     * 每天凌晨更新用户领取状态
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetField() {
        long begin = System.currentTimeMillis();
        LambdaQueryWrapper<UserInterfaceInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInterfaceInfo::getStatus, 1);
        List<UserInterfaceInfo> list = userInterfaceInfoService.list(lambdaQueryWrapper);
        List<UserInterfaceInfo> userInterfaceInfos = list.stream().map(a -> {
            a.setStatus(0);
            return a;
        }).collect(Collectors.toList());
        userInterfaceInfos.forEach(a -> {
            userInterfaceInfoService.updateById(a);
        });
        long end = System.currentTimeMillis();
        log.info("更新领取状态耗时：{}",end - begin);
    }

    /**
     * 更新首页数据缓存
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetIndexCache() {
        long begin = System.currentTimeMillis();
        // 1. 全站接口可调用数
        String cnt = String.valueOf(interfaceInfoService.count(new QueryWrapper<InterfaceInfo>().eq("isDelete",0).eq("status",1)));
        redisTemplate.opsForValue().set(RedisConstant.API_INDEX_INTERFACE_CNT, cnt, 1, TimeUnit.DAYS);
        // 2. 接口总调用次数
        String total = userInterfaceInfoMapper.getTotalInvokeCount();
        redisTemplate.opsForValue().set(RedisConstant.API_INDEX_INVOKE_CNT, total, 1, TimeUnit.DAYS);
        long end = System.currentTimeMillis();
        log.info("更新缓存信息耗时：{}",end - begin);
    }
}
