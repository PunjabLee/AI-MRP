package com.aimrp.quality.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 不良品登记请求
 */
@Data
public class DefectCreateRequest {
    
    private Long inspectionId;
    
    private String inspectionNo;
    
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    
    private String itemName;
    
    @NotNull(message = "不良品数量不能为空")
    private BigDecimal defectQty;
    
    @NotBlank(message = "不良品类型不能为空")
    private String defectType;
    
    private String reason;
    
    private String remark;
}
