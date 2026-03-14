package com.aimrp.common.interceptor;

import com.aimrp.common.annotation.RequiresPermissions;
import com.aimrp.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限校验拦截器
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresPermissions permissions = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
        
        // 没有权限注解，放行
        if (permissions == null) {
            return true;
        }
        
        // 获取用户权限（从Redis或数据库获取）
        Long userId = (Long) request.getAttribute("userId");
        List<String> userPermissions = getUserPermissions(userId);
        
        // 权限校验
        String[] requiredPermissions = permissions.value();
        String mode = permissions.mode();
        
        boolean hasPermission;
        if ("ANY".equals(mode)) {
            // 具备其一即可
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        } else {
            // 必须全部具备
            hasPermission = Arrays.stream(requiredPermissions)
                    .allMatch(userPermissions::contains);
        }
        
        if (!hasPermission) {
            throw new BusinessException("PERMISSION_DENIED", "权限不足");
        }
        
        return true;
    }
    
    /**
     * 获取用户权限（实际应从Redis/数据库获取）
     */
    private List<String> getUserPermissions(Long userId) {
        // TODO: 从Redis或数据库获取用户权限
        // 模拟返回
        return List.of("user:read", "user:write", "order:read", "order:write");
    }
}
