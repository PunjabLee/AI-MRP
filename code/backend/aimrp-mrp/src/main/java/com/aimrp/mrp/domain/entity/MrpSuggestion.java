package com.aimrp.mrp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MRP 建议实体
 * 
 * 存储 MRP 计算生成的采购/生产建议
 */
@Data
@TableName("t_mrp_suggestion")
public class MrpSuggestion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** MRP 运行 ID */
    private Long runId;
    
    /** 建议类型：PURCHASE-采购/PRODUCTION-生产 */
    private String suggestionType;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 建议数量 */
    private BigDecimal suggestQty;
    
    /** 需求日期 */
    private LocalDate needDate;
    
    /** 建议下单日期 */
    private LocalDate suggestOrderDate;
    
    /** 建议到货/完工日期 */
    private LocalDate suggestFinishDate;
    
    /** 需求来源：ORDER-订单/FORECAST-预测/POOL-需求池 */
    private String demandSource;
    
    /** 需求单ID */
    private Long demandId;
    
    /** 优先级：1-10，数字越大优先级越高 */
    private Integer priority;
    
    /** 状态：PENDING-待处理/ACCEPTED-已确认/REJECTED-已拒绝/CONVERTED-已转换 */
    private String status;
    
    /** 拒绝原因 */
    private String rejectReason;
    
    /** 转换后的单据ID */
    private Long convertedOrderId;
    
    /** 转换后的单据号 */
    private String convertedOrderNo;
    
    /** 备注 */
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
