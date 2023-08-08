package com.wzy.order.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.service.ApiOrderService;
import common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Asce
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class IndexCacheResetSchedule {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ApiOrderService apiOrderService;

    /**
     * 更新首页数据缓存
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetField() {
        long begin = System.currentTimeMillis();
        String cnt = (String) redisTemplate.opsForValue().get(RedisConstant.API_INDEX_ORDER_CNT);
        if (cnt == null) {
            cnt = String.valueOf(apiOrderService.count(new QueryWrapper<ApiOrder>().eq("status",1)));
            redisTemplate.opsForValue().set(RedisConstant.API_INDEX_ORDER_CNT, cnt, 1, TimeUnit.DAYS);
        }
        long end = System.currentTimeMillis();
        log.info("更新领取状态耗时：{}",end - begin);
    }
}
