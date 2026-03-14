package com.aimrp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工艺路线工序
 */
@Data
@TableName("m_process_route_line")
public class ProcessRouteLine {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 路线ID */
    private Long routeId;
    
    /** 工序号 */
    private Integer operationNo;
    
    /** 工序名称 */
    private String operationName;
    
    /** 工作中心编码 */
    private String workCenterCode;
    
    /** 标准工时（小时） */
    private BigDecimal stdHours;
    
    /** 准备时间（分钟） */
    private Integer setupTime;
    
    /** 排队时间（小时） */
    private BigDecimal queueTime;
    
    /** 优先级 */
    private Integer priority;
    
    /** 是否关键工序 */
    private Boolean isCritical;
    
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
