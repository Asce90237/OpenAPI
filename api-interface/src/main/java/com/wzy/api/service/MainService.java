package com.wzy.api.service;


import javax.servlet.http.HttpServletRequest;

/**
* @author Asce
* @description 利用反射通过url方式调用接口
* @createDate 2023-01-17 10:33:59
*/
public interface MainService {

    String mainRedirect(HttpServletRequest request);

}
