package com.aimrp.item.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料响应
 */
@Data
public class ItemResponse {
    private Long id;
    private String itemCode;
    private String itemName;
    private String itemType;
    private String unit;
    private String source;
    private Integer leadTime;
    private BigDecimal safetyStock;
    private BigDecimal minLotSize;
    private BigDecimal maxLotSize;
    private BigDecimal unitCost;
    private String remark;
    private String status;
    private LocalDateTime createdAt;
}
