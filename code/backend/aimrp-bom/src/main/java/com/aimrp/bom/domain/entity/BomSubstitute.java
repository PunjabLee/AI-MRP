package com.aimrp.bom.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BOM替代料实体
 */
@Data
@TableName("t_bom_substitute")
public class BomSubstitute {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 父物料编码 */
    private String itemCode;
    
    /** 替代物料编码 */
    private String substituteCode;
    
    /** 替代物料名称 */
    private String substituteName;
    
    /** 替代比例 */
    private BigDecimal substituteRatio;
    
    /** 优先级(1-10, 1最高) */
    private Integer priority;
    
    /** 生效日期 */
    private LocalDateTime effectiveDate;
    
    /** 失效日期 */
    private LocalDateTime expiryDate;
    
    /** 状态：ACTIVE-生效/INACTIVE-停用 */
    private String status;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
