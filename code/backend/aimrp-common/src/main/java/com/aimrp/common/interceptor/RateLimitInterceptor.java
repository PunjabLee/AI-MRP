package com.aimrp.common.interceptor;

import com.aimrp.common.annotation.RateLimit;
import com.aimrp.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流拦截器
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, 
                            jakarta.servlet.http.HttpServletResponse response, 
                            Object handler) {
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return true;
        }
        
        String key = getKey(request, rateLimit);
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo(rateLimit.count()));
        
        // 检查是否过期
        if (info.isExpired()) {
            info.reset(rateLimit.count());
        }
        
        // 尝试增加计数
        if (!info.tryAcquire()) {
            throw new BusinessException("RATE_LIMIT", rateLimit.message());
        }
        
        return true;
    }
    
    private String getKey(jakarta.servlet.http.HttpServletRequest request, RateLimit rateLimit) {
        String baseKey = rateLimit.key();
        if (baseKey.isEmpty()) {
            // 默认使用 URI + IP
            baseKey = request.getRequestURI() + ":" + getClientIP(request);
        }
        return "rate_limit:" + baseKey;
    }
    
    private String getClientIP(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 限流信息
     */
    private static class RateLimitInfo {
        private final int maxCount;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long expireTime;
        
        public RateLimitInfo(int maxCount) {
            this.maxCount = maxCount;
            reset(maxCount);
        }
        
        public void reset(int maxCount) {
            this.count.set(0);
            this.expireTime = System.currentTimeMillis() + 60000; // 60秒窗口
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
        
        public boolean tryAcquire() {
            return count.incrementAndGet() <= maxCount;
        }
    }
}
