package com.aimrp.inventory.api.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 调拨执行请求
 */
@Data
public class TransferExecuteRequest {
    
    @NotNull(message = "实际调拨数量不能为空")
    private BigDecimal actualQty;
    
    private String executeUser;
    
    private String remark;
}
