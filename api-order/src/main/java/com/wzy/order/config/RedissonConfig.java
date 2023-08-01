package com.wzy.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Asce
 * redissen 配置
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress);
        config.useSingleServer().setPassword(redisPassword);
        return Redisson.create(config);
    }
}
