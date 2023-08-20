package com.wzy.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "api.url")
public class ApiPathConfig {

    private String teladdress;

    private String qqimg;

    private String baiduhot;

    private String douyinhot;

    private String weibohot;

    private String zhihuhot;

    private String randomcolor;

    private String ipinfo;

    private String telvalid;

    private String historytoday;

}
