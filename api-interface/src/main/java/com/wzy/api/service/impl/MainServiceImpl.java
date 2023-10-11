package com.wzy.api.service.impl;

import com.wzy.api.service.MainService;
import com.wzy.api.util.ApiUriUtil;
import com.wzy.api.util.MethodUrlMapUtils;
import com.wzy.apiclient.model.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Asce
 * @description 利用反射通过url方式调用接口
 * @createDate 2023-01-17 10:33:59
 */
@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private MethodUrlMapUtils utils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApiUriUtil apiUriUtil;

    @Override
    public String mainRedirect(Api api) {
        // 获取接口id和url的映射关系
        Map<String, String> map = apiUriUtil.map;
        // 1、获取当前请求路径中的类名和方法
        Map<String, String> hashmap = utils.hashmap;
//        String body = request.getHeader("body");
//        Api api = JSONUtil.toBean(body, Api.class);
        Long interfaceId = api.getInterfaceId();
        // 接口id可能不存在或接口已下线
        String url = map.get(String.valueOf(interfaceId));
        String key = "[" + url + "]" ;
        // 获得的res是方法名
        String res = hashmap.get(key);
        if(res == null) {
            throw new RuntimeException("接口不存在");
        }
        Object result = null;
        try {
            //通过反射构造，获得class对象
            Class<?> forName = Class.forName("com.wzy.api.controller.InterfaceController");
            //由于是object对象，所以实例化对象需要从容器中拿到
            //getMethod方法第一个参数是方法名，后面是参数的类型，可以有多个
            Method classMethod = forName.getMethod(res, Object.class);
            //调用方法 invoke方法第一个参数是实例对象（方法的调用值），后面是参数，可以有多个，调用方法得到的是object类型
            result = classMethod.invoke(context.getBean(forName), api.getParameter());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return String.valueOf(result);
    }
}




