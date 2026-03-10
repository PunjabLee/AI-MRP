package com.aimrp.equipment.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class EquipmentCreateRequest {
    @NotBlank(message = "设备编码不能为空")
    private String equipmentCode;
    @NotBlank(message = "设备名称不能为空")
    private String equipmentName;
    private String equipmentType;
    private String spec;
    private String workshopCode;
    private String workCenterCode;
    private BigDecimal capacity;
    private String responsible;
    private String remark;
}
