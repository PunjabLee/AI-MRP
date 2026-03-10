package com.aimrp.warehouse.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仓库实体
 */
@Data
@TableName("t_warehouse")
public class Warehouse {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 仓库编码 */
    private String warehouseCode;
    
    /** 仓库名称 */
    private String warehouseName;
    
    /** 仓库类型：MAIN-主仓库/RAW-原料仓/FINISHED-成品仓 */
    private String warehouseType;
    
    /** 组织ID */
    private Long orgId;
    
    /** 状态：ENABLED-启用/DISABLED-停用 */
    private String status;
    
    /** 地址 */
    private String address;
    
    /** 联系人 */
    private String contact;
    
    /** 电话 */
    private String phone;
    
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
