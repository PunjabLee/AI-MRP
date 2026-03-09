package com.aimrp.warehouse.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 仓库管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    
    /**
     * 获取仓库列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String warehouseType,
            @RequestParam(required = false) String status) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 模拟数据
        list.add(Map.of("id", 1L, "warehouseCode", "WH01", "warehouseName", "主仓库", "warehouseType", "MAIN", "status", "ENABLED"));
        list.add(Map.of("id", 2L, "warehouseCode", "WH02", "warehouseName", "原料仓", "warehouseType", "RAW", "status", "ENABLED"));
        list.add(Map.of("id", 3L, "warehouseCode", "WH03", "warehouseName", "成品仓", "warehouseType", "FINISHED", "status", "ENABLED"));
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 获取仓库详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Map<String, Object> warehouse = new HashMap<>();
        warehouse.put("id", id);
        warehouse.put("warehouseCode", "WH01");
        warehouse.put("warehouseName", "主仓库");
        warehouse.put("warehouseType", "MAIN");
        warehouse.put("orgId", 1L);
        warehouse.put("status", "ENABLED");
        
        return Map.of("code", 200, "data", warehouse);
    }
    
    /**
     * 创建仓库
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        log.info("创建仓库: {}", data);
        data.put("id", System.currentTimeMillis());
        
        return Map.of("code", 200, "data", data, "message", "创建成功");
    }
    
    /**
     * 更新仓库
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        log.info("更新仓库: {}", id);
        
        return Map.of("code", 200, "message", "更新成功");
    }
    
    /**
     * 删除仓库
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        log.info("删除仓库: {}", id);
        
        return Map.of("code", 200, "message", "删除成功");
    }
}
