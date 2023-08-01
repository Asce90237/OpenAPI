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
     * 获取所有接口信息
     * 这段代码是一个Spring Boot应用程序中的一个组件类，它的作用是获取所有控制器方法的信息并将其存储在一个Map中。
     * 具体而言，该组件类在Spring Boot应用程序中通过@Component注解进行注入，
     * 并且使用WebApplicationObjectSupport类继承了ApplicationContextAware接口，
     * 这样就可以获取Spring容器中的应用上下文。
     * 然后，它使用@Bean注解标注的getController方法来获取所有控制器方法的信息，这个方法会遍历所有的请求映射处理方法，
     * 并将请求路径作为key，处理请求的类名和方法名组合作为value，存储在一个Map中，最后输出这个Map。
     * 总的来说，这段代码的作用是获取Spring Boot应用程序中所有控制器方法的信息，并将其存储在一个Map中，以便后续使用。
     */
    @Bean
    public void getController() {
        //获取WebApplicationContext，用于获取Bean
        WebApplicationContext webApplicationContext = getWebApplicationContext();
        //获取spring容器中的RequestMappingHandlerMapping todo 想要获得所有controller里的方法需要通过这个bean获得  todo 替换RequestMappingHandlerMapping.class
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) webApplicationContext.getBean("requestMappingHandlerMapping");
        //获取应用中所有的请求方法
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()){
            //1、获取所有的请求路径
            RequestMappingInfo requestMappingInfo = entry.getKey();
            log.info(requestMappingInfo.toString());
            PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
            log.info(patternsCondition.toString());
            Set<String> patterns = patternsCondition.getPatterns();
            log.info(patterns.toString());
            //value为处理请求信息的方法，即code
            HandlerMethod handlerMethod = entry.getValue();
            //2、获取类 获取控制器类名getBeanType()
            String type = handlerMethod.getBeanType().getName();
            //3、获取方法 获取控制器方法名getMethod()
            String method = handlerMethod.getMethod().getName();
            hashmap.put(patterns.toString(),type+"-"+method);
        }
        log.info(hashmap.toString());
    }
}
