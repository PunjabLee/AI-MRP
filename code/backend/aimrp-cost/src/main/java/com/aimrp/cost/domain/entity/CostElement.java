package com.aimrp.cost.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本要素实体
 */
@Data
@TableName("t_cost_element")
public class CostElement {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 成本要素编码 */
    private String costCode;
    
    /** 成本要素名称 */
    private String costName;
    
    /** 成本类型：MATERIAL-材料/ Labor-人工/OVERHEAD-制造费用 */
    private String costType;
    
    /** 单位成本 */
    private BigDecimal unitCost;
    
    /** 单位 */
    private String unit;
    
    /** 说明 */
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
