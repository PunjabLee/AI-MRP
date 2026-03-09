package com.aimrp.item.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 物料分类实体
 */
@Data
@TableName("m_item_category")
public class ItemCategory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String categoryCode;    // 分类编码
    
    private String categoryName;    // 分类名称
    
    private Long parentId;          // 父分类ID
    
    private Integer level;          // 层级
    
    private Integer sortOrder;      // 排序
    
    private String status;          // 状态
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
