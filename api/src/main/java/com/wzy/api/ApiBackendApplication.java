package com.wzy.api;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.wzy.api.mapper")
public class ApiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiBackendApplication.class, args);
    }



    /**
     * 让RabbitMQ转为JSON发送数据,使用json的方式进行序列化，注意消息接收也需要这个配置，在第三方启动器里
     * 发送方和接收方需要使用同样的MessageConverter
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
