package com.aimrp.org.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 组织管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrganizationController {
    
    /**
     * 获取组织树
     */
    @GetMapping("/tree")
    public Map<String, Object> getTree() {
        return Map.of("code", 200, "data", new ArrayList<>());
    }
    
    /**
     * 获取组织列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String orgType,
            @RequestParam(required = false) String status) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 模拟数据
        list.add(Map.of("id", 1L, "orgCode", "GROUP", "orgName", "集团", "orgType", "GROUP", "level", 1));
        list.add(Map.of("id", 2L, "orgCode", "COMPANY", "orgName", "母公司", "orgType", "COMPANY", "level", 2));
        list.add(Map.of("id", 3L, "orgCode", "FACTORY01", "orgName", "工厂一", "orgType", "FACTORY", "level", 3));
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 获取组织详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Map<String, Object> org = new HashMap<>();
        org.put("id", id);
        org.put("orgCode", "FACTORY01");
        org.put("orgName", "工厂一");
        org.put("orgType", "FACTORY");
        org.put("status", "ENABLED");
        
        return Map.of("code", 200, "data", org);
    }
    
    /**
     * 创建组织
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        log.info("创建组织: {}", data);
        data.put("id", System.currentTimeMillis());
        
        return Map.of("code", 200, "data", data, "message", "创建成功");
    }
    
    /**
     * 更新组织
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        log.info("更新组织: {}", id);
        
        return Map.of("code", 200, "message", "更新成功");
    }
    
    /**
     * 删除组织
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        log.info("删除组织: {}", id);
        
        return Map.of("code", 200, "message", "删除成功");
    }
}
