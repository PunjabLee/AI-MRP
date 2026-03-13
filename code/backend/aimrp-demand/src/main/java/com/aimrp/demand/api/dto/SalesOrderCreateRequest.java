package com.aimrp.demand.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售订单创建请求
 */
@Data
public class SalesOrderCreateRequest {
    
    @NotBlank(message = "客户编码不能为空")
    private String customerCode;
    
    private String customerName;
    
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    
    private String itemName;
    
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须为正数")
    private BigDecimal qty;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalAmount;
    
    private LocalDate deliveryDate;
    
    private Integer priority;
    
    private String remark;
}
