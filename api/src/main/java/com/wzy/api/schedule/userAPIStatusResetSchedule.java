package com.wzy.api.schedule;

import com.wzy.api.model.entity.UserInterfaceInfo;
import com.wzy.api.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
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

    /**
     * 每天凌晨更新用户领取状态
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetField() {
        long begin = System.currentTimeMillis();
        List<UserInterfaceInfo> list = userInterfaceInfoService.list();
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
}
