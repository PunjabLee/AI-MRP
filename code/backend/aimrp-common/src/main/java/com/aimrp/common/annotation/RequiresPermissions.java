package com.aimrp.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {
    
    /**
     * 需要具备的权限
     */
    String[] value() default {};
    
    /**
     * 验证模式：ALL-必须全部具备，ANY-具备其一即可
     */
    String mode() default "ALL";
}
