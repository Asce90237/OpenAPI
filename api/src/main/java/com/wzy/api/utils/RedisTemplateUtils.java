package com.wzy.api.utils;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.api.mapper.InterfaceInfoMapper;
import com.wzy.api.model.vo.AllInterfaceInfoVo;
import common.constant.LockConstant;
import common.constant.RedisConstant;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Asce
 */
@Component
public class RedisTemplateUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    /**
     * 缓存首页和请求次数中所有接口
     * @param allInterfaceInfoVoPage
     */
    public void onlinePageCache(Page<AllInterfaceInfoVo> allInterfaceInfoVoPage){
        //设置随机过期时间，解决缓存雪崩问题
        int c = RandomUtil.randomInt(60,120);
        //数据为空也缓存数据，解决缓存穿透问题
        redisTemplate.opsForValue().set(RedisConstant.onlinePageCacheKey+allInterfaceInfoVoPage.getCurrent(),allInterfaceInfoVoPage,c, TimeUnit.MINUTES);

    }

    /**
     * 根据当前页面数获取缓存
     * @param current
     * @return
     */
    public Page<AllInterfaceInfoVo> getOnlinePage(long current, long size){
        Page<AllInterfaceInfoVo> onlinePage = (Page<AllInterfaceInfoVo>) redisTemplate.opsForValue().get(RedisConstant.onlinePageCacheKey + current);
        if (onlinePage != null){
            //加入缓存后，请求时间由原来的平均68ms ，降低到平均36ms
            return onlinePage;
        }
        // 加分布式锁，同一时刻只能有一个请求数据库，其他的请求循环等待，解决缓存击穿问题.
        for (;;){
            try {
                RLock lock = redissonClient.getLock(LockConstant.interface_onlinePage_lock);
                //尝试加锁，最多等待20秒，上锁以后10秒自动解锁
                boolean b = lock.tryLock(20, 10, TimeUnit.SECONDS);
                if (b){
                    //查询数据库
                    Page<AllInterfaceInfoVo> allInterfaceInfoVoPage = interfaceInfoMapper.selectOnlinePage(new Page<>(current, size));
                    this.onlinePageCache(allInterfaceInfoVoPage);
                    lock.unlock();
                    return allInterfaceInfoVoPage;
                }
                //竞争不到锁，暂时让出CPU资源
                Thread.yield();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 删除所有缓存
     */
    public void delAllOnlinePage(){
        Set<String> keys = redisTemplate.keys(RedisConstant.onlinePageCacheKey + "*");
        redisTemplate.delete(keys);
    }
}
