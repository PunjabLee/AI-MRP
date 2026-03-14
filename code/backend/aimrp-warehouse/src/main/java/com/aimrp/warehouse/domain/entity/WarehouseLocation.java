package com.aimrp.warehouse.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库位实体
 */
@Data
@TableName("t_warehouse_location")
public class WarehouseLocation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 库位编码 */
    private String locationCode;
    
    /** 库位名称 */
    private String locationName;
    
    /** 仓库编码 */
    private String warehouseCode;
    
    /** 仓库名称 */
    private String warehouseName;
    
    /** 库区编码 */
    private String areaCode;
    
    /** 库区名称 */
    private String areaName;
    
    /** 库位类型：STORAGE-存储区/PICKING-拣货区/STAGING-待发区 */
    private String locationType;
    
    /** 库位状态：ACTIVE-启用/DISABLED-停用 */
    private String status;
    
    /** 容量-长(米) */
    private Double length;
    
    /** 容量-宽(米) */
    private Double width;
    
    /** 容量-高(米) */
    private Double height;
    
    /** 最大承重(kg) */
    private Double maxWeight;
    
    /** 排序 */
    private Integer sortOrder;
    
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
