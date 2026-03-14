package com.aimrp.warehouse.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class WarehouseCreateRequest {
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;
    private String warehouseType;
    private String address;
    private String contact;
    private String phone;
    private String remark;
}
