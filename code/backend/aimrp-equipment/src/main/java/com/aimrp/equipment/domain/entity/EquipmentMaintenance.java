package com.aimrp.equipment.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备维护计划实体
 */
@Data
@TableName("t_equipment_maintenance")
public class EquipmentMaintenance {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 计划编码 */
    private String planNo;
    
    /** 设备ID */
    private Long equipmentId;
    
    /** 设备编码 */
    private String equipmentCode;
    
    /** 设备名称 */
    private String equipmentName;
    
    /** 维护类型：PREVENTIVE-预防性/CORRECTIVE-修复性/PREDICTIVE-预测性 */
    private String maintenanceType;
    
    /** 计划维护日期 */
    private LocalDate planDate;
    
    /** 实际维护日期 */
    private LocalDate actualDate;
    
    /** 维护内容 */
    private String content;
    
    /** 维护人员 */
    private String maintainer;
    
    /** 维护时长(小时) */
    private BigDecimal duration;
    
    /** 维护费用 */
    private BigDecimal cost;
    
    /** 状态：PENDING-待执行/IN_PROGRESS-执行中/COMPLETED-已完成 */
    private String status;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
