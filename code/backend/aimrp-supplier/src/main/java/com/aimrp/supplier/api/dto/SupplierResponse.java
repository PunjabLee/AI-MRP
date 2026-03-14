package com.aimrp.supplier.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商响应
 */
@Data
public class SupplierResponse {
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private Integer leadTimeDays;
    private String paymentTerms;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
