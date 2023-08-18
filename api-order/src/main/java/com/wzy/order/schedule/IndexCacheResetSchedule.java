package com.wzy.order.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.service.ApiOrderService;
import common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Asce
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class IndexCacheResetSchedule {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ApiOrderService apiOrderService;

    /**
     * 更新首页订单成交数缓存
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetField() {
        String cnt = String.valueOf(apiOrderService.count(new QueryWrapper<ApiOrder>().eq("status",1)));
        stringRedisTemplate.opsForValue().set(RedisConstant.API_INDEX_ORDER_CNT, cnt, 1, TimeUnit.DAYS);
    }
}
