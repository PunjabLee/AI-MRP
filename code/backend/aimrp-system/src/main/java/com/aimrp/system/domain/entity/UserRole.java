package com.aimrp.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联
 */
@Data
@TableName("m_user_role")
public class UserRole {
    
    private Long userId;
    
    private Long roleId;
}
