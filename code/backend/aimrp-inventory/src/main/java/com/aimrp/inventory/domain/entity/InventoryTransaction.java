package com.aimrp.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存交易记录实体
 */
@Data
@TableName("t_inventory_transaction")
public class InventoryTransaction {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 交易单号 */
    private String transNo;
    
    /** 交易类型：PURCHASE_IN-采购入库/PRODUCTION_IN-生产入库/TRANSFER_IN-调拨入库
     * SALES_OUT-销售出库/PRODUCTION_OUT-生产领料/TRANSFER_OUT-调拨出库/ADJUST-盘点调整 */
    private String transType;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 仓库编码 */
    private String warehouseCode;
    
    /** 库位编码 */
    private String locationCode;
    
    /** 交易数量 */
    private BigDecimal transQty;
    
    /** 库存余量 */
    private BigDecimal afterQty;
    
    /** 批次号 */
    private String batchNo;
    
    /** 关联单据号 */
    private String refNo;
    
    /** 交易日期 */
    private LocalDateTime transDate;
    
    /** 操作人 */
    private String operator;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
