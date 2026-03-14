package com.aimrp.report.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报表配置实体
 */
@Data
@TableName("t_report_config")
public class ReportConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 报表编码 */
    private String reportCode;
    
    /** 报表名称 */
    private String reportName;
    
    /** 报表类型：INVENTORY库存/SALES销售/PRODUCTION生产/PURCHASE采购/MRP */
    private String reportType;
    
    /** 数据源SQL或存储过程 */
    private String dataSource;
    
    /** 状态：ENABLED-启用/DISABLED-停用 */
    private String status;
    
    /** 说明 */
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
