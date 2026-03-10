package com.aimrp.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存盘点单实体
 */
@Data
@TableName("t_inventory_check")
public class InventoryCheck {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 盘点单号 */
    private String checkNo;
    
    /** 盘点类型：REGULAR-定期/SPOT-抽盘 */
    private String checkType;
    
    /** 状态：DRAFT-草稿/PENDING-待审核/APPROVED-已审核/EXECUTING-盘点中/COMPLETED-已完成 */
    private String status;
    
    /** 仓库编码 */
    private String warehouseCode;
    
    /** 仓库名称 */
    private String warehouseName;
    
    /** 盘点日期 */
    private LocalDate checkDate;
    
    /** 计划开始日期 */
    private LocalDate planStartDate;
    
    /** 计划结束日期 */
    private LocalDate planEndDate;
    
    /** 实际完成日期 */
    private LocalDate actualEndDate;
    
    /** 盘点人 */
    private String checker;
    
    /** 审核人 */
    private String approver;
    
    /** 审核时间 */
    private LocalDateTime approveTime;
    
    /** 备注 */
    private String remark;
    
    /** 账面总数 */
    private Integer bookCount;
    
    /** 盘点总数 */
    private Integer checkCount;
    
    /** 差异数 */
    private Integer diffCount;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

/**
 * 盘点明细实体
 */
@Data
@TableName("t_inventory_check_line")
class InventoryCheckLine {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 盘点单ID */
    private Long checkId;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 库位 */
    private String locationCode;
    
    /** 账面数量 */
    private BigDecimal bookQty;
    
    /** 盘点数量 */
    private BigDecimal checkQty;
    
    /** 差异数量 */
    private BigDecimal diffQty;
    
    /** 差异类型：LOSS-盘盈/GAIN-盘盈/NORMAL-正常 */
    private String diffType;
    
    /** 备注 */
    private String remark;
    
    /** 处理状态：PENDING-待处理/RESOLVED-已处理 */
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
