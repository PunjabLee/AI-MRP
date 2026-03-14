package com.aimrp.supplierportal.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商门户用户实体
 */
@Data
@TableName("t_supplier_user")
public class SupplierUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联供应商ID */
    private Long supplierId;
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 姓名 */
    private String realName;
    
    /** 手机 */
    private String phone;
    
    /** 邮箱 */
    private String email;
    
    /** 状态：ENABLED-启用/DISABLED-停用 */
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
