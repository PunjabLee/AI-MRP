package com.aimrp.bom.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class BomCreateRequest {
    @NotBlank(message = "父物料编码不能为空")
    private String itemCode;
    @NotBlank(message = "子物料编码不能为空")
    private String childItemCode;
    private BigDecimal usageQty;
    private BigDecimal lossRate;
    private Integer level;
}
