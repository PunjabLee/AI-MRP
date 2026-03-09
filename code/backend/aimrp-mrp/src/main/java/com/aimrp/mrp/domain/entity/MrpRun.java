package com.aimrp.mrp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MRP 运行记录实体
 * 
 * 记录每次 MRP 计算的输入参数和运行结果
 */
@Data
@TableName("t_mrp_run")
public class MrpRun {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 运行编号 */
    private String runNo;
    
    /** 运行类型：MANUAL-手动/AUTO-自动 */
    private String runType;
    
    /** 状态：PENDING/RUNNING/COMPLETED/FAILED */
    private String status;
    
    /** 计划起点日期 */
    private LocalDate planStartDate;
    
    /** 计划结束日期 */
    private LocalDate planEndDate;
    
    /** 计算的物料数 */
    private Integer itemCount;
    
    /** 需求单数 */
    private Integer demandCount;
    
    /** 生成的建议数 */
    private Integer suggestionCount;
    
    /** 采购建议数 */
    private Integer purchaseSuggestionCount;
    
    /** 生产建议数 */
    private Integer productionSuggestionCount;
    
    /** 运行耗时(毫秒) */
    private Long runTimeMs;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 运行日志 */
    private String runLog;
    
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
