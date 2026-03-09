package com.aimrp.mrp.domain.valueobject;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MRP 计算结果
 */
@Data
@Builder
public class MrpResult {
    
    /** 运行 ID */
    private Long runId;
    
    /** 运行状态 */
    private String status;
    
    /** 运行耗时（毫秒） */
    private Long runTimeMs;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 计算统计 */
    private Statistics statistics;
    
    /** 采购建议列表 */
    @Builder.Default
    private List<Suggestion> purchaseSuggestions = new ArrayList<>();
    
    /** 生产建议列表 */
    @Builder.Default
    private List<Suggestion> productionSuggestions = new ArrayList<>();
    
    /** 风险预警列表 */
    @Builder.Default
    private List<RiskAlert> riskAlerts = new ArrayList<>();
    
    // ==================== 内部类 ===================
    
    @Data
    @Builder
    public static class Statistics {
        private Integer totalItems;        // 计算的物料数
        private Integer totalDemands;     // 需求单数
        private Integer totalSuggestions; // 建议总数
        private Integer purchaseSuggestions; // 采购建议数
        private Integer productionSuggestions; // 生产建议数
        private Integer riskCount;       // 风险数
    }
    
    @Data
    @Builder
    public static class Suggestion {
        private String suggestionType;  // PURCHASE/PRODUCTION
        private String itemCode;
        private String itemName;
        private BigDecimal suggestQty;
        private LocalDate needDate;
        private LocalDate suggestOrderDate;
        private LocalDate suggestFinishDate;
        private String demandSource;
        private Long demandId;
        private Integer priority;
        private String reason;           // 建议原因
    }
    
    @Data
    @Builder
    public static class RiskAlert {
        private String riskType;        // STOCKOUT/DELAY/OVERSTOCK
        private String itemCode;
        private String message;
        private String severity;        // LOW/MEDIUM/HIGH
    }
}
