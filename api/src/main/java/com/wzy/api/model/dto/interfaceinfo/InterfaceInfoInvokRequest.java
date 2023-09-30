package com.wzy.api.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口调用请求
 *
 * @author Asce
 * @TableName product
 */
@Data
public class InterfaceInfoInvokRequest implements Serializable {


    /**
     * 接口id
     */
    private Long  id;

    /**
     * 请求参数
     */
    private String userRequestParams;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 随机数
     */
    private String nonce;

    /**
     * 时间戳
     */
    private String timestamp;

}