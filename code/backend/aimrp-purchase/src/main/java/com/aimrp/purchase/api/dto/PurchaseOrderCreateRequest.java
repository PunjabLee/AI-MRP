package com.aimrp.purchase.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class PurchaseOrderCreateRequest {
    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    @Positive(message = "数量必须为正数")
    private BigDecimal orderQty;
    private BigDecimal unitPrice;
    private String expectDate;
    private String remark;
}
