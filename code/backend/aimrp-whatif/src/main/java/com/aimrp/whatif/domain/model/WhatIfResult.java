package com.aimrp.whatif.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * What-if 分析结果
 */
@Data
public class WhatIfResult {
    
    /**
     * 结果ID
     */
    private Long resultId;
    
    /**
     * 场景ID
     */
    private Long scenarioId;
    
    /**
     * 场景名称
     */
    private String scenarioName;
    
    /**
     * 分析完成时间
     */
    private String analyzedAt;
    
    /**
     * 计算耗时（毫秒）
     */
    private Long elapsedMs;
    
    /**
     * 影响分析结果
     */
    private ImpactAnalysis impactAnalysis;
    
    /**
     * 对比结果（与基准对比）
     */
    private ComparisonResult comparison;
    
    /**
     * 建议
     */
    private List<String> recommendations;
    
    /**
     * 是否可应用
     */
    private boolean applicable;
    
    /**
     * 影响分析
     */
    @Data
    public static class ImpactAnalysis {
        /**
         * 影响的订单数
         */
        private int affectedOrders;
        
        /**
         * 影响的物料数
         */
        private int affectedItems;
        
        /**
         * 预计完工时间变化
         */
        private String completionDateChange;
        
        /**
         * 总成本变化
         */
        private BigDecimal totalCostChange;
        
        /**
         * 资源利用率变化
         */
        private BigDecimal resourceUtilizationChange;
        
        /**
         * 详细影响列表
         */
        private List<ImpactDetail> details;
        
        @Data
        public static class ImpactDetail {
            private String targetType;
            private String targetCode;
            private String impactType;
            private String beforeValue;
            private String afterValue;
            private String impactDescription;
        }
    }
    
    /**
     * 对比结果
     */
    @Data
    public static class ComparisonResult {
        /**
         * 是否改善
         */
        private boolean improved;
        
        /**
         * 改善/恶化百分比
         */
        private BigDecimal improvementPercent;
        
        /**
         * 关键指标对比
         */
        private Map<String, String> keyMetrics;
        
        /**
         * 主要变化点
         */
        private List<String> mainChanges;
    }
}
