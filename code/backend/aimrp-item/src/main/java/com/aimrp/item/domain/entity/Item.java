package com.aimrp.item.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料主数据实体
 */
@Data
@TableName("m_item")
public class Item {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String itemCode;
    private String itemName;
    private String itemType;
    private String spec;
    private String unit;
    private Long categoryId;
    private String source;
    private BigDecimal safetyStock;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private Integer leadTime;
    private BigDecimal moq;
    private BigDecimal standardCost;
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
