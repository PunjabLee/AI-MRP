package com.aimrp.demand.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单
 */
@Data
@TableName("t_sales_order")
public class SalesOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;          // 订单编号
    private String customerCode;     // 客户编码
    private String customerName;     // 客户名称
    private LocalDate orderDate;     // 订单日期
    private LocalDate dueDate;       // 交货日期
    private String status;           // 状态
    private BigDecimal totalAmount;  // 订单金额
    private String memo;             // 备注
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
