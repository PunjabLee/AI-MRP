package com.aimrp.inventory.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 库存创建请求
 */
@Data
public class InventoryCreateRequest {
    
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;
    
    private String locationCode;
    
    @Positive(message = "数量必须为正数")
    private BigDecimal onHandQty;
    
    private String remark;
}
