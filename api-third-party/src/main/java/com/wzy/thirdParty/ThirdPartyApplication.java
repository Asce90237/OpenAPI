package com.wzy.thirdParty;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * @author Asce
 * 第三方服务，如：gitee 、github 、腾讯云等
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableDubbo
@MapperScan("com.wzy.thirdParty.mapper")
public class ThirdPartyApplication {
    public static void main(String[] args) {SpringApplication.run(ThirdPartyApplication.class, args);}

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
