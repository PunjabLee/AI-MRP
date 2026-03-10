package com.aimrp.org.application.service;

import com.aimrp.org.domain.entity.Organization;
import com.aimrp.org.infrastructure.persistence.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织权限服务
 */
@Service
@RequiredArgsConstructor
public class OrgPermissionService {
    
    private final OrganizationMapper orgMapper;
    
    /**
     * 绑定组织与用户权限
     */
    public void bindUserPermission(Long orgId, Long userId, List<String> permissions) {
        // 1. 获取组织权限key
        Organization org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new RuntimeException("组织不存在");
        }
        
        // 2. 继承上级组织权限
        List<String> inheritedPermissions = getInheritedPermissions(org.getParentId());
        
        // 3. 合并自有权限
        List<String> allPermissions = permissions != null ? permissions : List.of();
        allPermissions = Stream.concat(inheritedPermissions.stream(), allPermissions.stream())
            .distinct()
            .collect(Collectors.toList());
        
        // 4. 保存用户-组织权限关系（此处应调用system模块）
        saveUserOrgPermissions(userId, orgId, allPermissions);
    }
    
    /**
     * 获取继承的权限
     */
    private List<String> getInheritedPermissions(Long parentId) {
        if (parentId == null) {
            return List.of();
        }
        
        Organization parent = orgMapper.selectById(parentId);
        if (parent == null || parent.getPermissionKey() == null) {
            return getInheritedPermissions(parent.getParentId());
        }
        
        return List.of(parent.getPermissionKey().split(","));
    }
    
    /**
     * 保存用户组织权限
     */
    private void saveUserOrgPermissions(Long userId, Long orgId, List<String> permissions) {
        // TODO: 调用system模块保存用户组织权限关系
        // 实际实现需要system模块提供接口
        System.out.println("绑定用户:" + userId + " 到组织:" + orgId + " 权限:" + permissions);
    }
    
    /**
     * 获取用户的组织权限
     */
    public List<String> getUserPermissions(Long userId, Long orgId) {
        // 1. 获取组织权限key
        Organization org = orgMapper.selectById(orgId);
        
        // 2. 获取继承权限
        List<String> inherited = getInheritedPermissions(org.getParentId());
        
        // 3. 获取用户自有权限
        List<String> userPermissions = getUserOwnPermissions(userId, orgId);
        
        // 4. 合并返回
        return Stream.concat(inherited.stream(), userPermissions.stream())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取用户自有权限
     */
    private List<String> getUserOwnPermissions(Long userId, Long orgId) {
        // TODO: 从system模块获取
        return List.of();
    }
    
    /**
     * 解除用户组织绑定
     */
    public void unbindUserPermission(Long userId, Long orgId) {
        // TODO: 调用system模块解除绑定
    }
}
