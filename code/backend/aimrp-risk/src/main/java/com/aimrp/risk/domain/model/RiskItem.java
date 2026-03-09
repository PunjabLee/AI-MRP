package com.aimrp.risk.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险项模型
 */
@Data
public class RiskItem {
    
    /**
     * 风险ID
     */
    private Long riskId;
    
    /**
     * 风险编码
     */
    private String riskCode;
    
    /**
     * 风险类型
     */
    private RiskType riskType;
    
    /**
     * 风险等级
     */
    private RiskLevel riskLevel;
    
    /**
     * 风险标题
     */
    private String title;
    
    /**
     * 风险描述
     */
    private String description;
    
    /**
     * 关联类型（ORDER/ITEM/SUPPLIER）
     */
    private String relatedType;
    
    /**
     * 关联编码
     */
    private String relatedCode;
    
    /**
     * 风险值（0-100）
     */
    private BigDecimal riskValue;
    
    /**
     * 触发条件
     */
    private String triggerCondition;
    
    /**
     * 建议措施
     */
    private String suggestedAction;
    
    /**
     * 状态
     */
    private RiskStatus status;
    
    /**
     * 发现时间
     */
    private LocalDateTime detectedAt;
    
    /**
     * 风险类型枚举
     */
    public enum RiskType {
        SUPPLIER_DELAY,     // 供应商延迟
        INVENTORY_SHORTAGE, // 库存短缺
        DEMAND_SURGE,       // 需求突变
        PRODUCTION_DELAY,   // 生产延期
        QUALITY_ISSUE,      // 质量问题
        LOGISTICS_DELAY,    // 物流延迟
        CAPACITY_SHORTAGE   // 产能不足
    }
    
    /**
     * 风险等级
     */
    public enum RiskLevel {
        LOW,      // 低风险
        MEDIUM,   // 中风险
        HIGH,     // 高风险
        CRITICAL  // 严重
    }
    
    /**
     * 风险状态
     */
    public enum RiskStatus {
        ACTIVE,    // 活动中
        MONITORING, // 监控中
        RESOLVED,  // 已解决
        IGNORED    // 已忽略
    }
}
