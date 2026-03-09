package com.aimrp.purchase.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购入库单
 */
@Data
@TableName("t_purchase_receive")
public class PurchaseReceive {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 入库单号 */
    private String receiveNo;
    
    /** 采购单ID */
    private Long orderId;
    
    /** 采购单号 */
    private String poNo;
    
    /** 入库日期 */
    private LocalDate receiveDate;
    
    /** 状态：PENDING/COMPLETED */
    private String status;
    
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
