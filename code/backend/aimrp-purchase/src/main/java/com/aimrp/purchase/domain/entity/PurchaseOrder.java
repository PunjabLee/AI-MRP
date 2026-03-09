package com.aimrp.purchase.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购订单
 */
@Data
@TableName("t_purchase_order")
public class PurchaseOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 采购单号 */
    private String poNo;
    
    /** 供应商编码 */
    private String supplierCode;
    
    /** 供应商名称 */
    private String supplierName;
    
    /** 订单日期 */
    private LocalDate orderDate;
    
    /** 预计到货日期 */
    private LocalDate expectDate;
    
    /** 状态：DRAFT/CONFIRMED/PARTIAL_RECEIVED/RECEIVED/CANCELLED */
    private String status;
    
    /** 订单金额 */
    private BigDecimal totalAmount;
    
    /** 已到货金额 */
    private BigDecimal receivedAmount;
    
    /** 备注 */
    private String memo;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
