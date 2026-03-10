package com.aimrp.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存调拨单实体
 */
@Data
@TableName("t_inventory_transfer")
public class InventoryTransfer {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 调拨单号 */
    private String transferNo;
    
    /** 调拨类型：TRANSFER-调拨 */
    private String transferType;
    
    /** 状态：DRAFT-草稿/PENDING-待审核/APPROVED-已审核/EXECUTING-执行中/COMPLETED-已完成/CANCELLED-已取消 */
    private String status;
    
    /** 调出仓库 */
    private String fromWarehouseCode;
    
    /** 调出仓库名称 */
    private String fromWarehouseName;
    
    /** 调入仓库 */
    private String toWarehouseCode;
    
    /** 调入仓库名称 */
    private String toWarehouseName;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 调拨数量 */
    private BigDecimal transferQty;
    
    /** 单位 */
    private String unit;
    
    /** 调拨日期 */
    private LocalDate transferDate;
    
    /** 预计到货日期 */
    private LocalDate expectDate;
    
    /** 实际到货日期 */
    private LocalDate actualDate;
    
    /** 调拨原因 */
    private String reason;
    
    /** 备注 */
    private String remark;
    
    /** 审核人 */
    private String approver;
    
    /** 审核时间 */
    private LocalDateTime approveTime;
    
    /** 审核意见 */
    private String approveRemark;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
