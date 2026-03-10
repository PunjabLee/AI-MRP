package com.aimrp.mrp.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * MRP运行请求
 */
@Data
public class MrpRunRequest {
    
    private String runType;
    private LocalDate planStartDate;
    private LocalDate planEndDate;
}
