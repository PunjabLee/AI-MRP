package com.aimrp.inventory.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 库存交易请求
 */
@Data
public class InventoryTransRequest {
    
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;
    
    private String locationCode;
    
    private String transType;
    
    private java.math.BigDecimal qty;
    
    private String refNo;
    
    private String remark;
}
