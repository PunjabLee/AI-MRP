package com.aimrp.org.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.org.api.dto.OrganizationCreateRequest;
import com.aimrp.org.domain.entity.Organization;
import com.aimrp.org.infrastructure.persistence.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 组织管理 Controller
 */
@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrganizationController {
    
    private final OrganizationMapper orgMapper;
    
    /**
     * 获取组织树
     */
    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> getOrgTree() {
        List<Organization> all = orgMapper.selectList(null, null, null);
        List<Map<String, Object>> tree = buildTree(all, null);
        return ApiResponse.ok(tree);
    }
    
    /**
     * 获取组织列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String orgType,
            @RequestParam(required = false) String status) {
        
        List<Organization> list = orgMapper.selectList(parentId, orgType, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取组织详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Organization> getById(@PathVariable Long id) {
        return ApiResponse.ok(orgMapper.selectById(id));
    }
    
    /**
     * 创建组织
     */
    @PostMapping
    public ApiResponse<Organization> create(@Validated @RequestBody OrganizationCreateRequest request) {
        Organization exist = orgMapper.selectByCode(request.getOrgCode());
        if (exist != null) {
            return ApiResponse.fail("组织编码已存在");
        }
        
        Organization org = new Organization();
        org.setOrgCode(request.getOrgCode());
        org.setOrgName(request.getOrgName());
        org.setParentId(request.getParentId());
        org.setOrgType(request.getOrgType());
        org.setManager(request.getManager());
        org.setRemark(request.getRemark());
        
        if (request.getParentId() != null) {
            Organization parent = orgMapper.selectById(request.getParentId());
            if (parent != null) {
                org.setLevel(parent.getLevel() + 1);
            }
        } else {
            org.setLevel(1);
        }
        
        org.setStatus("ENABLED");
        orgMapper.insert(org);
        
        return ApiResponse.ok(org);
    }
    
    /**
     * 更新组织
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody OrganizationCreateRequest request) {
        Organization org = new Organization();
        org.setId(id);
        org.setOrgCode(request.getOrgCode());
        org.setOrgName(request.getOrgName());
        org.setOrgType(request.getOrgType());
        org.setManager(request.getManager());
        org.setRemark(request.getRemark());
        
        orgMapper.updateById(org);
        return ApiResponse.ok();
    }
    
    /**
     * 删除组织
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        List<Organization> children = orgMapper.selectList(id, null, null);
        if (!children.isEmpty()) {
            return ApiResponse.fail("请先删除子组织");
        }
        
        orgMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 启用/禁用组织
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Organization org = new Organization();
        org.setId(id);
        org.setStatus(status);
        orgMapper.updateById(org);
        
        return ApiResponse.ok();
    }
    
    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<Organization> all, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (Organization org : all) {
            boolean isRoot = (parentId == null && org.getParentId() == null);
            boolean isChild = (parentId != null && org.getParentId() != null && org.getParentId().equals(parentId));
            
            if (isRoot || isChild) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", org.getId());
                node.put("orgCode", org.getOrgCode());
                node.put("orgName", org.getOrgName());
                node.put("orgType", org.getOrgType());
                node.put("level", org.getLevel());
                node.put("children", buildTree(all, org.getId()));
                
                tree.add(node);
            }
        }
        
        return tree;
    }
}
