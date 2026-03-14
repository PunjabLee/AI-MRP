package com.aimrp.equipment.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
@TableName("t_equipment")
public class Equipment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 设备编码 */
    private String equipmentCode;
    
    /** 设备名称 */
    private String equipmentName;
    
    /** 设备类型：MACHINE-机器/TOOL-工装/Fixture-夹具 */
    private String equipmentType;
    
    /** 规格型号 */
    private String spec;
    
    /** 所属车间 */
    private String workshopCode;
    
    /** 所属工作中心 */
    private String workCenterCode;
    
    /** 状态：IDLE-空闲/RUNNING-运行/MAINTENANCE-维护/BROKEN-故障 */
    private String status;
    
    /** 产能(件/小时) */
    private BigDecimal capacity;
    
    /** 已用产能 */
    private BigDecimal usedCapacity;
    
    /** 购置日期 */
    private LocalDateTime purchaseDate;
    
    /** 使用年限 */
    private Integer usefulLife;
    
    /** 负责人 */
    private String responsible;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
