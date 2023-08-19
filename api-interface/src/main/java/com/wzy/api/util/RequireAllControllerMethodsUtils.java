package com.wzy.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Asce
 */
@Slf4j
@Component
public class RequireAllControllerMethodsUtils extends WebApplicationObjectSupport  {

    public Map<String,String> hashmap = new HashMap<>();

    /**
     * 获取所有接口信息，放入map集合，键为请求路径，值为方法名
     */
    @Bean
    public void getController() {
        //获取WebApplicationContext，用于获取Bean
        WebApplicationContext webApplicationContext = getWebApplicationContext();
        //获取spring容器中的RequestMappingHandlerMapping来获得所有controller里的方法需要通过这个bean获得
        RequestMappingHandlerMapping requestMappingHandlerMapping = webApplicationContext.getBean(RequestMappingHandlerMapping.class);
        //获取应用中所有的请求方法
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()){
            //1、获取所有的请求路径
            RequestMappingInfo requestMappingInfo = entry.getKey();
            PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
            Set<String> patterns = patternsCondition.getPatterns();
            //value为处理请求信息的方法
            HandlerMethod handlerMethod = entry.getValue();
            //2、获取方法 获取控制器方法名getMethod()
            String method = handlerMethod.getMethod().getName();
            hashmap.put(patterns.toString(),method);
        }
    }
}
