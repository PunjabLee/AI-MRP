package com.aimrp.common.result;

import lombok.Getter;

/**
 * 结果码枚举
 */
@Getter
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_DISABLED(2002, "用户已被禁用"),
    PASSWORD_ERROR(2003, "密码错误"),
    
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态异常"),
    
    ITEM_NOT_FOUND(4001, "物料不存在"),
    INVENTORY_NOT_ENOUGH(4002, "库存不足"),
    
    BOM_NOT_FOUND(5001, "BOM不存在"),
    
    MRP_RUNNING(6001, "MRP正在运行中"),
    MRP_RUN_FAILED(6002, "MRP运行失败"),
    
    ;
    
    private final int code;
    private final String msg;
    
    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
