package com.aimrp.quality.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 不良品记录实体
 */
@Data
@TableName("t_quality_defect")
public class QualityDefect {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 不良品单号 */
    private String defectNo;
    
    /** 关联检验单ID */
    private Long inspectionId;
    
    /** 关联检验单号 */
    private String inspectionNo;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 不良品数量 */
    private BigDecimal defectQty;
    
    /** 不良品类型：MISSING-缺件/DAMAGED-损坏/DEFECTIVE-不良 */
    private String defectType;
    
    /** 不良品原因 */
    private String reason;
    
    /** 处理方式：SCRAP-报废/REWORK-返工/RETURN-退货/SPECIAL-特采 */
    private String handlingMethod;
    
    /** 处理数量 */
    private BigDecimal handlingQty;
    
    /** 处理人 */
    private String handler;
    
    /** 处理日期 */
    private LocalDate handlingDate;
    
    /** 状态：PENDING-待处理/COMPLETED-已处理 */
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
