package com.wzy.apiclient.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Asce
 */
@Data
public class Api implements Serializable {
    /**
     * 接口id
     */
    Long interfaceId;
    /**
     * 请求参数
     */
    String parameter;
}
