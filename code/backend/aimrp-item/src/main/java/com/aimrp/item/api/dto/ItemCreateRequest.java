package com.aimrp.item.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 物料创建请求
 */
@Data
public class ItemCreateRequest {
    
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    
    @NotBlank(message = "物料名称不能为空")
    private String itemName;
    
    private String itemType;
    private String unit;
    private String source;
    private Integer leadTime;
    private BigDecimal safetyStock;
    private BigDecimal minLotSize;
    private BigDecimal maxLotSize;
    private BigDecimal unitCost;
    private String remark;
}
