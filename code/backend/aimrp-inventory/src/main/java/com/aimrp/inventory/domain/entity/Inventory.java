package com.aimrp.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("m_inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String itemCode;
    private String itemName;
    private String warehouseCode;
    private String warehouseName;
    private BigDecimal onHandQty;
    private BigDecimal allocatedQty;
    private BigDecimal availableQty;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
