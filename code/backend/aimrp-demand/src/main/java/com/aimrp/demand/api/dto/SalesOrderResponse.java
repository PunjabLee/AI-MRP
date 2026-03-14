package com.aimrp.demand.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单响应
 */
@Data
public class SalesOrderResponse {
    
    private Long id;
    private String orderNo;
    private String customerCode;
    private String customerName;
    private String itemCode;
    private String itemName;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String status;
    private Integer priority;
    private String remark;
    private LocalDateTime createdAt;
}
