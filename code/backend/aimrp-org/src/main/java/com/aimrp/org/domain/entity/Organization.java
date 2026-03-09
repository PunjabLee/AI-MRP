package com.aimrp.org.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织实体
 */
@Data
@TableName("org_organization")
public class Organization {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 组织编码 */
    private String orgCode;
    
    /** 组织名称 */
    private String orgName;
    
    /** 组织类型：GROUP/COMPANY/FACTORY/DEPARTMENT */
    private String orgType;
    
    /** 上级组织ID */
    private Long parentId;
    
    /** 层级 */
    private Integer level;
    
    /** 排序 */
    private Integer sortOrder;
    
    /** 负责人 */
    private String leader;
    
    /** 电话 */
    private String phone;
    
    /** 地址 */
    private String address;
    
    /** 状态：ENABLED/DISABLED */
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
