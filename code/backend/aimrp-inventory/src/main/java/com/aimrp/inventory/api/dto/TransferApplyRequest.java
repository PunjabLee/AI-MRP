package com.aimrp.inventory.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 调拨申请请求
 */
@Data
public class TransferApplyRequest {
    
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;
    
    private String itemName;
    
    @NotBlank(message = "源仓库不能为空")
    private String fromWarehouse;
    
    @NotBlank(message = "目标仓库不能为空")
    private String toWarehouse;
    
    @NotNull(message = "调拨数量不能为空")
    private BigDecimal transferQty;
    
    @NotBlank(message = "申请人不能为空")
    private String applyUser;
    
    private String remark;
}
