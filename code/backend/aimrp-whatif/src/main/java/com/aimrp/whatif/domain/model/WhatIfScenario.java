package com.aimrp.whatif.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * What-if 场景模型
 */
@Data
public class WhatIfScenario {
    
    /**
     * 场景ID
     */
    private Long scenarioId;
    
    /**
     * 场景名称
     */
    private String scenarioName;
    
    /**
     * 场景描述
     */
    private String description;
    
    /**
     * 场景类型
     */
    private ScenarioType scenarioType;
    
    /**
     * 变更项列表
     */
    private List<ChangeItem> changes;
    
    /**
     * 基准数据ID（基于哪个计划/订单）
     */
    private Long baselineId;
    
    /**
     * 场景状态
     */
    private ScenarioStatus status;
    
    /**
     * 创建时间
     */
    private LocalDate createdAt;
    
    /**
     * 场景类型枚举
     */
    public enum ScenarioType {
        DEMAND_CHANGE,      // 需求变更
        ORDER_PRIORITY,     // 订单优先级调整
        LEAD_TIME_CHANGE,   // 交期变更
        INVENTORY_CHANGE,   // 库存变化
        SUPPLY_CHANGE,     // 供应变化
        CAPACITY_CHANGE     // 产能变化
    }
    
    /**
     * 场景状态
     */
    public enum ScenarioStatus {
        DRAFT,      // 草稿
        RUNNING,    // 计算中
        COMPLETED,  // 已完成
        COMPARED,   // 已对比
        APPLIED     // 已应用
    }
    
    /**
     * 变更项
     */
    @Data
    public static class ChangeItem {
        /**
         * 变更类型
         */
        private String changeType;
        
        /**
         * 目标类型（ORDER/ITEM/SUPPLIER）
         */
        private String targetType;
        
        /**
         * 目标编码
         */
        private String targetCode;
        
        /**
         * 字段名称
         */
        private String fieldName;
        
        /**
         * 原值
         */
        private String originalValue;
        
        /**
         * 新值
         */
        private String newValue;
        
        /**
         * 变更说明
         */
        private String description;
    }
}
