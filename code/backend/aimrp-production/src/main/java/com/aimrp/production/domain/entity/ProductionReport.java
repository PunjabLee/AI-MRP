package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产报工记录
 */
@Data
@TableName("t_production_report")
public class ProductionReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 报工编号 */
    private String reportNo;
    
    /** 工单ID */
    private Long orderId;
    
    /** 工单编号 */
    private String moNo;
    
    /** 报工类型：START-开工/END-完工/OUTPUT-产出 */
    private String reportType;
    
    /** 产出数量 */
    private BigDecimal outputQty;
    
    /** 合格数量 */
    private BigDecimal qualifiedQty;
    
    /** 报废数量 */
    private BigDecimal scrappedQty;
    
    /** 返工数量 */
    private BigDecimal reworkQty;
    
    /** 人工工时 */
    private BigDecimal laborHours;
    
    /** 机器工时 */
    private BigDecimal machineHours;
    
    /** 开工时间 */
    private LocalDateTime startTime;
    
    /** 完工时间 */
    private LocalDateTime endTime;
    
    /** 状态：PENDING/APPROVED/REJECTED */
    private String status;
    
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
