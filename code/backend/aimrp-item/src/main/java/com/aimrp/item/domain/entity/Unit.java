package com.aimrp.item.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 单位实体
 */
@Data
@TableName("m_unit")
public class Unit {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String unitCode;       // 单位编码
    
    private String unitName;       // 单位名称
    
    private String status;         // 状态
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
