package com.aimrp.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("m_role")
public class Role {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roleCode;
    
    private String roleName;
    
    private String description;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
