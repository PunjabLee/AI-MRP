package com.aimrp.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("m_permission")
public class Permission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String permissionCode;
    
    private String permissionName;
    
    private String resourceType;
    
    private String resourcePath;
    
    private Long parentId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
