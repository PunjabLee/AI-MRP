package com.aimrp.forecast.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预测结果模型
 */
@Data
public class ForecastResult {
    /**
     * 物料编码
     */
    private String itemCode;
    
    /**
     * 预测日期
     */
    private LocalDate forecastDate;
    
    /**
     * 预测数量
     */
    private BigDecimal forecastQty;
    
    /**
     * 置信度下限
     */
    private BigDecimal lowerBound;
    
    /**
     * 置信度上限
     */
    private BigDecimal upperBound;
    
    /**
     * 预测类型（DAILY/WEEKLY/MONTHLY）
     */
    private String forecastType;
}
