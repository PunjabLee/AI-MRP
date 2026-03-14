package com.aimrp.equipment.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备维护计划创建请求
 */
@Data
public class MaintenanceCreateRequest {
    
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;
    
    private String equipmentCode;
    
    private String equipmentName;
    
    @NotBlank(message = "维护类型不能为空")
    private String maintenanceType;
    
    @NotNull(message = "计划日期不能为空")
    private LocalDate planDate;
    
    @NotBlank(message = "维护内容不能为空")
    private String content;
    
    private String maintainer;
    
    private BigDecimal duration;
    
    private BigDecimal cost;
    
    private String remark;
}
