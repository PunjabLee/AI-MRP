package com.aimrp.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志切面 - 统一日志规范
 */
@Slf4j
@Aspect
@Component
public class LogAspect {
    
    /**
     * 切入点：所有Controller
     */
    @Pointcut("execution(* com.aimrp..api.controller..*.*(..))")
    public void controllerPointcut() {}
    
    /**
     * 切入点：所有Service
     */
    @Pointcut("execution(* com.aimrp..application.service..*.*(..))")
    public void servicePointcut() {}
    
    /**
     * Controller日志
     */
    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 请求参数（脱敏处理）
        String params = maskSensitiveData(Arrays.toString(args));
        log.info("[REQUEST] {}.{} | params: {}", className, methodName, params);
        
        Object result = null;
        long costTime = 0;
        
        try {
            result = joinPoint.proceed();
            costTime = System.currentTimeMillis() - startTime;
            
            // 响应日志
            if (result != null) {
                log.info("[RESPONSE] {}.{} | cost: {}ms | success", className, methodName, costTime);
            }
            return result;
        } catch (Exception e) {
            costTime = System.currentTimeMillis() - startTime;
            log.error("[ERROR] {}.{} | cost: {}ms | error: {}", 
                     className, methodName, costTime, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Service日志
     */
    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.debug("[SERVICE] {}.{} | start", className, methodName);
        
        try {
            Object result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            log.debug("[SERVICE] {}.{} | cost: {}ms | done", className, methodName, costTime);
            return result;
        } catch (Exception e) {
            log.error("[SERVICE] {}.{} | error: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 脱敏处理
     */
    private String maskSensitiveData(String data) {
        if (data == null) return "";
        
        String masked = data;
        // 脱敏密码
        masked = masked.replaceAll("(?i)(password[\"']?\\s*[:=]\\s*)[^,\\s]+", "$1******");
        masked = masked.replaceAll("(?i)(token[\"']?\\s*[:=]\\s*)[^,\\s]+", "$1******");
        
        return masked;
    }
}
