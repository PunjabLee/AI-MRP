package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产工单
 */
@Data
@TableName("t_production_order")
public class ProductionOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 工单编号 */
    private String moNo;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 计划数量 */
    private BigDecimal planQty;
    
    /** 已完成数量 */
    private BigDecimal completedQty;
    
    /** 报废数量 */
    private BigDecimal scrappedQty;
    
    /** 计划开始日期 */
    private LocalDate startDate;
    
    /** 计划结束日期 */
    private LocalDate endDate;
    
    /** 实际开始日期 */
    private LocalDate actualStartDate;
    
    /** 实际结束日期 */
    private LocalDate actualEndDate;
    
    /** 状态：DRAFT/RELEASED/PROCESSING/COMPLETED/CANCELLED */
    private String status;
    
    /** 优先级：1-10 */
    private Integer priority;
    
    /** 来源：MANUAL-手动/MRP-建议生成 */
    private String source;
    
    /** MRP建议ID */
    private Long suggestionId;
    
    /** 备注 */
    private String memo;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
