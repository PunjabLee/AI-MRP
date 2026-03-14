package com.aimrp.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流key
     */
    String key() default "";
    
    /**
     * 限流时间窗口(秒)
     */
    int time() default 60;
    
    /**
     * 允许最大请求次数
     */
    int count() default 100;
    
    /**
     * 提示信息
     */
    String message() default "请求过于频繁，请稍后重试";
}
