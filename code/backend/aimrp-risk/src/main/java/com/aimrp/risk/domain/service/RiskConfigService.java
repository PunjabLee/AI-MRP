package com.aimrp.risk.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 风险配置服务
 */
@Slf4j
@Service
public class RiskConfigService {
    
    /**
     * 风险配置缓存
     */
    private Map<String, Object> configCache = new HashMap<>();
    
    public RiskConfigService() {
        // 初始化默认配置
        initDefaultConfig();
    }
    
    /**
     * 初始化默认配置
     */
    private void initDefaultConfig() {
        // 风险等级配置
        Map<String, Object> levelConfig = new HashMap<>();
        levelConfig.put("CRITICAL", Map.of("enabled", true, "notifyAdmins", true, "channels", Arrays.asList("EMAIL", "SMS", "SYSTEM")));
        levelConfig.put("HIGH", Map.of("enabled", true, "notifyAdmins", true, "channels", Arrays.asList("EMAIL", "SYSTEM")));
        levelConfig.put("MEDIUM", Map.of("enabled", true, "notifyAdmins", false, "channels", Arrays.asList("SYSTEM")));
        levelConfig.put("LOW", Map.of("enabled", false, "notifyAdmins", false, "channels", Arrays.asList("SYSTEM")));
        
        configCache.put("riskLevelConfig", levelConfig);
        
        // 预警接收人配置
        Map<String, Object> recipients = new HashMap<>();
        recipients.put("default", Arrays.asList("admin", "manager"));
        recipients.put("INVENTORY", Arrays.asList("warehouse_admin", "planner"));
        recipients.put("SUPPLIER", Arrays.asList("purchasing_manager", "supplier_admin"));
        recipients.put("DEMAND", Arrays.asList("sales_manager", "planner"));
        
        configCache.put("warningRecipients", recipients);
        
        // 风险类型配置
        Map<String, Object> typeConfig = new HashMap<>();
        typeConfig.put("INVENTORY", Map.of("enabled", true, "scanInterval", 3600)); // 秒
        typeConfig.put("SUPPLIER", Map.of("enabled", true, "scanInterval", 7200));
        typeConfig.put("DEMAND", Map.of("enabled", true, "scanInterval", 3600));
        
        configCache.put("riskTypeConfig", typeConfig);
        
        log.info("风险配置初始化完成");
    }
    
    /**
     * 获取预警接收人
     */
    public List<String> getWarningRecipients(String riskType) {
        @SuppressWarnings("unchecked")
        Map<String, Object> recipients = (Map<String, Object>) configCache.get("warningRecipients");
        
        if (recipients == null) {
            return Arrays.asList("admin");
        }
        
        // 先查找类型特定的接收人
        if (riskType != null && recipients.containsKey(riskType)) {
            return (List<String>) recipients.get(riskType);
        }
        
        // 返回默认接收人
        return (List<String>) recipients.getOrDefault("default", Arrays.asList("admin"));
    }
    
    /**
     * 获取风险等级配置
     */
    public Map<String, Object> getLevelConfig(String level) {
        @SuppressWarnings("unchecked")
        Map<String, Object> levelConfig = (Map<String, Object>) configCache.get("riskLevelConfig");
        
        if (levelConfig == null) {
            return Map.of("enabled", true, "notifyAdmins", true);
        }
        
        return (Map<String, Object>) levelConfig.getOrDefault(level, Map.of("enabled", true));
    }
    
    /**
     * 检查风险等级是否启用
     */
    public boolean isLevelEnabled(String level) {
        Map<String, Object> config = getLevelConfig(level);
        return (Boolean) config.getOrDefault("enabled", true);
    }
    
    /**
     * 获取通知渠道
     */
    public List<String> getNotificationChannels(String level) {
        Map<String, Object> config = getLevelConfig(level);
        Object channels = config.get("channels");
        
        if (channels instanceof List) {
            return (List<String>) channels;
        }
        
        return Arrays.asList("SYSTEM");
    }
    
    /**
     * 是否需要通知管理员
     */
    public boolean shouldNotifyAdmins(String level) {
        Map<String, Object> config = getLevelConfig(level);
        return (Boolean) config.getOrDefault("notifyAdmins", false);
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(String key, Object value) {
        configCache.put(key, value);
        log.info("风险配置更新 - key: {}", key);
    }
    
    /**
     * 获取所有配置
     */
    public Map<String, Object> getAllConfig() {
        return new HashMap<>(configCache);
    }
}
