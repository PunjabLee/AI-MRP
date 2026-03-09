package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工艺路线
 */
@Data
@TableName("m_process_route")
public class ProcessRoute {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 路线编码 */
    private String routeCode;
    
    /** 物料编码 */
    private String itemCode;
    
    /** 物料名称 */
    private String itemName;
    
    /** 路线名称 */
    private String routeName;
    
    /** 版本 */
    private String version;
    
    /** 状态：DRAFT/ACTIVE/OBSOLETE */
    private String status;
    
    /** 生效日期 */
    private LocalDateTime effectiveDate;
    
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
