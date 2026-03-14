package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工作中心
 */
@Data
@TableName("m_work_center")
public class WorkCenter {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 工作中心编码 */
    private String wcCode;
    
    /** 工作中心名称 */
    private String wcName;
    
    /** 产能（小时/天） */
    private BigDecimal capacity;
    
    /** 效率系数 */
    private BigDecimal efficiency;
    
    /** 状态：ACTIVE/INACTIVE */
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
