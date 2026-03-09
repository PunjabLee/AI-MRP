package com.aimrp.demand.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售订单行项目
 */
@Data
@TableName("t_sales_order_line")
public class SalesOrderLine {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;            // 订单ID
    private String itemCode;         // 物料编码
    private String itemName;         // 物料名称
    private BigDecimal qty;          // 数量
    private BigDecimal unitPrice;    // 单价
    private BigDecimal amount;       // 金额
    private LocalDateTime dueDate;   // 交货日期
    private String status;           // 状态
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
