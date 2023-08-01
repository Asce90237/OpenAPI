package com.wzy.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.api.mapper.AuthMapper;
import com.wzy.api.service.AuthService;
import com.wzy.api.util.AuthUtils;
import com.wzy.api.util.RequireAllControllerMethodsUtils;
import common.model.entity.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Asce
 * @description 针对表【auth】的数据库操作Service实现
 * @createDate 2023-01-17 10:33:59
 */
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth>
        implements AuthService {

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private RequireAllControllerMethodsUtils utils;


    @Autowired
    private ApplicationContext context;

    @Override
    public String mainRedirect(HttpServletRequest request) {
        Map<String, String> headers = authUtils.getHeaders(request);
        //验证请求参数和密钥等是否合法
        boolean isAuth = authUtils.isAuth(headers);
        if (isAuth) {
            //1、获取当前请求路径中的类名和方法
            Map<String, String> hashmap = utils.hashmap;
            String url = headers.get("url");
            String key = "[" + url + "]" ;
            // 获得的res是类名+方法名
            String res = hashmap.get(key);
            if(res == null){
                return null;
            }
            String[] split = res.split("-");
            Object body = null;
            try {
                //通过反射构造，获得class对象 0-类名
                Class<?> forName = Class.forName(split[0]);
                //由于是object对象，所以实例化对象需要从容器中拿到 1-方法名
                //getMethod方法第一个参数是方法名，后面是参数的类型，可以有多个
                Method classMethod = forName.getMethod(split[1], Object.class);
                //调用方法 invoke方法第一个参数是实例对象，后面是参数，可以有多个，调用方法得到的是object类型
                body = classMethod.invoke(context.getBean(forName), headers.get("body"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(body);
        }
        return null;
    }
}




