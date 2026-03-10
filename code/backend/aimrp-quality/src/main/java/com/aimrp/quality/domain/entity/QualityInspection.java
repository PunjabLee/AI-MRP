package com.aimrp.quality.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检验单实体
 */
@Data
@TableName("t_quality_inspection")
public class QualityInspection {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 检验单号 */
    private String inspectionNo;
    
    /** 检验类型：INCOMING-来料/PROCESS-过程/FINAL-成品 */
    private String inspectionType;
    
    /** 来源单号(采购单/工单) */
    private String sourceNo;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 检验数量 */
    private BigDecimal inspectionQty;
    
    /** 合格数量 */
    private BigDecimal qualifiedQty;
    
    /** 不良数量 */
    private BigDecimal defectiveQty;
    
    /** 报废数量 */
    private BigDecimal scrappedQty;
    
    /** 合格率 */
    private BigDecimal qualifiedRate;
    
    /** 检验结果：QUALIFIED-合格/UNQUALIFIED-不合格 */
    private String result;
    
    /** 检验员 */
    private String inspector;
    
    /** 检验日期 */
    private LocalDateTime inspectionDate;
    
    /** 状态：DRAFT-草稿/PENDING-待检验/COMPLETED-已完成 */
    private String status;
    
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
