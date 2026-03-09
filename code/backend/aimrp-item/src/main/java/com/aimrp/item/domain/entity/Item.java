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
    
    private String itemCode;        // 物料编码
    
    private String itemName;        // 物料名称
    
    private String itemType;        // 物料类型: FINISHED(成品), SEMI(半成品), RAW(原材料)
    
    private String spec;            // 规格
    
    private String unit;            // 单位
    
    private Long categoryId;        // 分类ID
    
    private String source;          // 来源: MAKE(自制), BUY(采购), BOTH(自制采购)
    
    private BigDecimal safetyStock; // 安全库存
    
    private BigDecimal minStock;    // 最小库存
    
    private BigDecimal maxStock;    // 最大库存
    
    private Integer leadTime;       // 提前期(天)
    
    private BigDecimal moq;         // 最小起订量
    
    private BigDecimal standardCost;// 标准成本
    
    private String status;          // 状态: ACTIVE, INACTIVE
    
    private String memo;            // 备注
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
