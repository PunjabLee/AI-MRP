package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资源（设备/人员）
 */
@Data
@TableName("m_resource")
public class Resource {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 资源编码 */
    private String resourceCode;
    
    /** 资源名称 */
    private String resourceName;
    
    /** 所属工作中心 */
    private String workCenterCode;
    
    /** 资源类型：MACHINE/WORKER */
    private String resourceType;
    
    /** 产能（小时） */
    private BigDecimal capacity;
    
    /** 效率系数 */
    private BigDecimal efficiency;
    
    /** 状态：ACTIVE/MAINTENANCE/INACTIVE */
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
