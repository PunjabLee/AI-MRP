package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工单工序明细
 */
@Data
@TableName("t_mo_operation")
public class MoOperation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 工单ID */
    private Long moId;
    
    /** 工序ID */
    private Long operationId;
    
    /** 工序号 */
    private Integer operationNo;
    
    /** 工序名称 */
    private String operationName;
    
    /** 工作中心 */
    private String workCenterCode;
    
    /** 标准工时 */
    private BigDecimal stdHours;
    
    /** 计划数量 */
    private BigDecimal planQty;
    
    /** 状态：PENDING/IN_PROGRESS/COMPLETED/SKIPPED */
    private String status;
    
    /** 计划开始日期 */
    private LocalDate planStartDate;
    
    /** 计划结束日期 */
    private LocalDate planEndDate;
    
    /** 实际开始日期 */
    private LocalDate actualStartDate;
    
    /** 实际结束日期 */
    private LocalDate actualEndDate;
    
    /** 完成数量 */
    private BigDecimal completedQty;
    
    /** 报废数量 */
    private BigDecimal scrappedQty;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
