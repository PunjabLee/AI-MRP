package com.aimrp.mrp.domain.valueobject;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MRP 计算上下文
 * 
 * 封装 MRP 计算所需的全部输入数据
 */
@Data
@Builder
public class MrpContext {
    
    /** MRP 运行 ID */
    private Long runId;
    
    /** 计划起点日期 */
    private LocalDate planStartDate;
    
    /** 计划结束日期（展望期） */
    private LocalDate planEndDate;
    
    /** 时间段精度：DAY/WEEK/MONTH */
    private String timeBucket;
    
    /** 是否允许负库存 */
    private Boolean allowNegative;
    
    // ==================== 数据集合 ====================
    
    /** 物料主数据 Map<itemCode, Item> */
    private Map<String, ItemVO> items;
    
    /** BOM 展开 Map<itemCode, List<BomLineVO>> */
    private Map<String, List<BomLineVO>> bomMap;
    
    /** 库存 Map<itemCode, InventoryVO> */
    private Map<String, InventoryVO> inventoryMap;
    
    /** 销售订单需求 Map<itemCode, List<DemandVO>> */
    private Map<String, List<DemandVO>> salesDemandMap;
    
    /** 在途采购 Map<itemCode, List<PurchaseOnWayVO>> */
    private Map<String, List<PurchaseOnWayVO>> purchaseOnWayMap;
    
    /** 在制生产 Map<itemCode, List<ProductionOnWayVO>> */
    private Map<String, List<ProductionOnWayVO>> productionOnWayMap;
    
    // ==================== 内部类 ====================
    
    @Data
    @Builder
    public static class ItemVO {
        private String itemCode;
        private String itemName;
        private String itemType;        // FINISHED/SEMI/RAW
        private String source;          // MAKE/BUY/BOTH
        private Integer leadTime;       // 采购/生产提前期
        private String lotSizeRule;    // 批量规则
        private BigDecimal minLotSize;  // 最小批量
        private BigDecimal maxLotSize;  // 最大批量
        private BigDecimal safetyStock; // 安全库存
        private BigDecimal yieldRate;  // 成品率
    }
    
    @Data
    @Builder
    public static class BomLineVO {
        private Long bomId;
        private String parentItemCode;
        private String childItemCode;
        private String childItemName;
        private BigDecimal usageQty;    // 用量
        private BigDecimal lossRate;    // 损耗率
        private Integer level;         // BOM层级
    }
    
    @Data
    @Builder
    public static class InventoryVO {
        private String itemCode;
        private String warehouseCode;
        private BigDecimal onHandQty;     // 现有量
        private BigDecimal allocatedQty;  // 已分配量
        private BigDecimal availableQty;  // 可用量
    }
    
    @Data
    @Builder
    public static class DemandVO {
        private Long demandId;
        private String demandType;     // ORDER/FORECAST
        private String itemCode;
        private BigDecimal qty;
        private LocalDate dueDate;
        private Integer priority;
    }
    
    @Data
    @Builder
    public static class PurchaseOnWayVO {
        private Long poId;
        private String itemCode;
        private BigDecimal qty;
        private LocalDate expectDate;
    }
    
    @Data
    @Builder
    public static class ProductionOnWayVO {
        private Long moId;
        private String itemCode;
        private BigDecimal qty;
        private LocalDate expectDate;
    }
}
