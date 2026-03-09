package com.aimrp.purchase.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购订单行项目
 */
@Data
@TableName("t_purchase_order_line")
public class PurchaseOrderLine {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 采购订单ID */
    private Long orderId;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 采购数量 */
    private BigDecimal orderQty;
    
    /** 到货数量 */
    private BigDecimal receivedQty;
    
    /** 单价 */
    private BigDecimal unitPrice;
    
    /** 金额 */
    private BigDecimal amount;
    
    /** 预计到货日期 */
    private LocalDate expectDate;
    
    /** 实际到货日期 */
    private LocalDate receiveDate;
    
    /** 状态：DRAFT/CONFIRMED/PARTIAL_RECEIVED/RECEIVED/CANCELLED */
    private String status;
    
    /** 来源：MANUAL-手动/MRP-建议生成 */
    private String source;
    
    /** MRP建议ID */
    private Long suggestionId;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
