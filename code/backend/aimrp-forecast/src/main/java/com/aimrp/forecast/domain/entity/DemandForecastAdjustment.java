package com.aimrp.forecast.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 需求预测调整实体
 */
@Data
@TableName("t_demand_forecast_adjustment")
public class DemandForecastAdjustment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 预测日期 */
    private LocalDate forecastDate;
    
    /** 原始预测数量 */
    private BigDecimal originalQty;
    
    /** 调整后数量 */
    private BigDecimal adjustedQty;
    
    /** 调整类型：MANUAL-手动调整/OVERRIDE-覆盖 */
    private String adjustType;
    
    /** 调整原因 */
    private String reason;
    
    /** 调整人 */
    private String adjustBy;
    
    /** 调整时间 */
    private LocalDateTime adjustTime;
    
    /** 状态：ACTIVE-生效/CANCELLED-已取消 */
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
