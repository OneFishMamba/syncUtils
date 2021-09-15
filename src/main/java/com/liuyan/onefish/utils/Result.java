package com.liuyan.onefish.utils;

import lombok.Data;

/**
 * 接口返回信息实体类
 * @author Administrator
 */
@Data
public class Result<T> {
    /**
     * 状态码
     */
    private String code;
    /**
     * 状态信息
     */
    private String msg;
    /**
     * 响应数据
     */
    private T data;
}
