package com.aimrp.mps.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MPS主生产计划实体
 */
@Data
@TableName("t_mps_plan")
public class MpsPlan {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 计划编号 */
    private String planNo;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 计划数量 */
    private BigDecimal planQty;
    
    /** 已排产数量 */
    private BigDecimal scheduledQty;
    
    /** 计划开始日期 */
    private LocalDate startDate;
    
    /** 计划结束日期 */
    private LocalDate endDate;
    
    /** 状态：DRAFT-草稿/RELEASED-已下达/COMPLETED-已完成 */
    private String status;
    
    /** 优先级 */
    private Integer priority;
    
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
