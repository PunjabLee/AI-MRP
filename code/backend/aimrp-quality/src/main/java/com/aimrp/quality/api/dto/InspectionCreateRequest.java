package com.aimrp.quality.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class InspectionCreateRequest {
    @NotBlank(message = "检验类型不能为空")
    private String inspectionType;
    @NotBlank(message = "来源单号不能为空")
    private String sourceNo;
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    private String itemName;
    private BigDecimal inspectionQty;
    private String inspector;
    private String remark;
}
