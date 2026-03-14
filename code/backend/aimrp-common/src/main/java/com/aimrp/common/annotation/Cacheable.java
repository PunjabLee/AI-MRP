package com.aimrp.common.annotation;

import java.lang.annotation.*;

/**
 * 缓存注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {
    
    /**
     * 缓存key
     */
    String key() default "";
    
    /**
     * 过期时间(秒)
     */
    int expire() default 3600;
    
    /**
     * 缓存条件
     */
    String condition() default "";
}
