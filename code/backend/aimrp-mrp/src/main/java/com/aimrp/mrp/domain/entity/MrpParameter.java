package com.aimrp.mrp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MRP 参数配置实体
 * 
 * 存储 MRP 计算的全局参数和物料级参数
 */
@Data
@TableName("t_mrp_parameter")
public class MrpParameter {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 参数键 */
    private String paramKey;
    
    /** 参数值 */
    private String paramValue;
    
    /** 参数类型：GLOBAL-全局/MATERIAL-物料 */
    private String paramType;
    
    /** 物料编码（物料级参数时使用） */
    private String itemCode;
    
    /** 参数分组 */
    private String paramGroup;
    
    /** 参数说明 */
    private String description;
    
    /** 是否可编辑 */
    private Boolean isEditable;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
