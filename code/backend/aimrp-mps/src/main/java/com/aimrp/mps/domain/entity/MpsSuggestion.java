package com.aimrp.mps.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MPS 建议实体
 */
@Data
@TableName("t_mps_suggestion")
public class MpsSuggestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的MPS计划ID */
    private Long planId;

    /** 物料ID */
    private Long itemId;

    /** 物料编码 */
    private String itemCode;

    /** 物料名称 */
    private String itemName;

    /** 建议生产量 */
    private BigDecimal suggestedQty;

    /** 需求日期 */
    private LocalDate dueDate;

    /** 优先级 */
    private Integer priority;

    /** 来源类型：ORDER/FORECAST/MANUAL */
    private String sourceType;

    /** 来源单号 */
    private String sourceNo;

    /** 状态：PENDING/APPROVED/REJECTED/CONVERTED */
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
