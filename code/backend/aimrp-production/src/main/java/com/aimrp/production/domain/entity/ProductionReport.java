package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产报工记录实体
 */
@Data
@TableName("t_production_report")
public class ProductionReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 报工单号 */
    private String reportNo;
    
    /** 工单ID */
    private Long moId;
    
    /** 工单号 */
    private String moNo;
    
    /** 工序ID */
    private Long operationId;
    
    /** 工序名称 */
    private String operationName;
    
    /** 报工数量 */
    private BigDecimal reportQty;
    
    /** 合格数量 */
    private BigDecimal qualifiedQty;
    
    /** 不良数量 */
    private BigDecimal defectiveQty;
    
    /** 报废数量 */
    private BigDecimal scrappedQty;
    
    /** 返工数量 */
    private BigDecimal reworkQty;
    
    /** 工艺工时(小时) */
    private BigDecimal laborHours;
    
    /** 机器工时(小时) */
    private BigDecimal machineHours;
    
    /** 报工日期 */
    private LocalDateTime reportDate;
    
    /** 报工人 */
    private String reporter;
    
    /** 审核人 */
    private String approver;
    
    /** 审核时间 */
    private LocalDateTime approveTime;
    
    /** 状态：DRAFT-草稿/PENDING-待审核/APPROVED-已审核/REJECTED-已驳回 */
    private String status;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
