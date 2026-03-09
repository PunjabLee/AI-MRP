package com.aimrp.bom.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("m_bom")
public class Bom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bomNo;
    private String itemCode;
    private String itemName;
    private String version;
    private String status;
    private BigDecimal yield;
    private String memo;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
