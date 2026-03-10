package com.aimrp.supplier.api.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 供应商创建请求
 */
@Data
public class SupplierCreateRequest {
    
    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;
    
    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;
    
    private String contact;
    private String phone;
    private String email;
    private String address;
    private Integer leadTimeDays;
    private String paymentTerms;
    private String remark;
}
