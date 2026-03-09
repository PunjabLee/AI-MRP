package com.aimrp.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果
 */
@Data
public class R<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String msg;
    private T data;
    private long timestamp;
    
    public R() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.timestamp = System.currentTimeMillis();
    }
    
    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
    }
    
    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }
    
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }
    
    public static <T> R<T> fail() {
        return new R<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
    }
    
    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.FAIL.getCode(), msg);
    }
    
    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg);
    }
    
    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMsg());
    }
    
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
