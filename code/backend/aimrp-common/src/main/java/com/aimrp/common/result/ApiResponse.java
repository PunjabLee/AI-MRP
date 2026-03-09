package com.aimrp.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一 API 响应结果
 * 
 * 命名：ApiResponse - 更语义化的命名
 */
@Data
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int code;        // 状态码
    private String msg;     // 消息
    private T data;        // 数据
    private long timestamp; // 时间戳
    
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ApiResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), msg, data);
    }
    
    /**
     * 失败响应（默认）
     */
    public static <T> ApiResponse<T> fail() {
        return new ApiResponse<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
    }
    
    /**
     * 失败响应（自定义消息）
     */
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(ResultCode.FAIL.getCode(), msg);
    }
    
    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<>(code, msg);
    }
    
    /**
     * 失败响应（使用 ResultCode）
     */
    public static <T> ApiResponse<T> fail(ResultCode resultCode) {
        return new ApiResponse<>(resultCode.getCode(), resultCode.getMsg());
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
