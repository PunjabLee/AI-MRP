package com.aimrp.org.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 组织实体
 */
@Data
@TableName("t_organization")
public class Organization {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 组织编码 */
    private String orgCode;
    
    /** 组织名称 */
    private String orgName;
    
    /** 上级组织ID */
    private Long parentId;
    
    /** 组织层级 */
    private Integer level;
    
    /** 组织类型：COMPANY-公司/DEPARTMENT-部门/WORKSHOP-车间 */
    private String orgType;
    
    /** 负责人 */
    private String manager;
    
    /** 状态：ENABLED-启用/DISABLED-禁用 */
    private String status;
    
    /** 备注 */
    private String remark;
    
    /** 权限标识 */
    private String permissionKey;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
