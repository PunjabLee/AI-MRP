package com.aimrp.supplier.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("m_supplier")
public class Supplier {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String supplierType;
    private Integer leadTimeDays;
    private BigDecimal creditLimit;
    private String status;
    private String memo;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
