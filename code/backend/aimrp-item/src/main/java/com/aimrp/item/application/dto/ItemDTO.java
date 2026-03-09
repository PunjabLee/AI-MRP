package com.aimrp.item.application.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 物料创建/更新 DTO
 */
@Data
public class ItemDTO {
    
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
}
