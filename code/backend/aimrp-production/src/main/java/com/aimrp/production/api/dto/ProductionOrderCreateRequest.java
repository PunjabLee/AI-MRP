package com.aimrp.production.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ProductionOrderCreateRequest {
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    private String itemName;
    @Positive(message = "数量必须为正数")
    private BigDecimal planQty;
    private String startDate;
    private String endDate;
    private String remark;
}
