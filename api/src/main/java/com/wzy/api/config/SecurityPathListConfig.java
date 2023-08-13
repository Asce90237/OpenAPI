package com.wzy.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "pathlist")
public class SecurityPathListConfig {
    private List<String> white;
    private List<String> login;

    public List<String> getWhite() {
        return white;
    }

    public void setWhite(List<String> white) {
        this.white = white;
    }

    public List<String> getLogin() {
        return login;
    }

    public void setLogin(List<String> login) {
        this.login = login;
    }
}