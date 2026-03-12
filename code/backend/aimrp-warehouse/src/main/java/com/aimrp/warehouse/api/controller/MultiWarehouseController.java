package com.aimrp.warehouse.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.warehouse.domain.entity.Warehouse;
import com.aimrp.warehouse.infrastructure.persistence.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多仓库管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class MultiWarehouseController {
    
    private final WarehouseMapper warehouseMapper;
    
    /**
     * 获取所有仓库（下拉选择用）
     */
    @GetMapping("/options")
    public ApiResponse<List<Map<String, Object>>> getWarehouseOptions() {
        List<Warehouse> list = warehouseMapper.selectList(null, "ENABLED");
        
        List<Map<String, Object>> options = new java.util.ArrayList<>();
        for (Warehouse wh : list) {
            Map<String, Object> option = new HashMap<>();
            option.put("value", wh.getWarehouseCode());
            option.put("label", wh.getWarehouseName());
            option.put("type", wh.getWarehouseType());
            options.add(option);
        }
        
        return ApiResponse.ok(options);
    }
    
    /**
     * 根据组织获取仓库
     */
    @GetMapping("/by-org/{orgId}")
    public ApiResponse<List<Warehouse>> getByOrg(@PathVariable Long orgId) {
        // 根据组织ID查询仓库 - Enterprise多组织功能
        List<Warehouse> warehouses = queryWarehousesByOrg(orgId);
        return ApiResponse.ok(warehouses);
    }

    /**
     * 根据组织查询仓库
     */
    private List<Warehouse> queryWarehousesByOrg(Long orgId) {
        // TODO: 实现按组织查询仓库逻辑
        // Enterprise功能：需要与组织模块集成
        // 1. 查询组织关联的仓库
        // 2. 返回仓库列表
        return warehouseMapper.selectList(null, "ENABLED");
    }
    
    /**
     * 设置默认仓库
     */
    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        // 取消其他默认
        List<Warehouse> all = warehouseMapper.selectList(null, null);
        for (Warehouse wh : all) {
            if ("DEFAULT".equals(wh.getRemark())) {
                wh.setRemark(null);
                warehouseMapper.updateById(wh);
            }
        }
        
        // 设置当前为默认
        Warehouse wh = warehouseMapper.selectById(id);
        wh.setRemark("DEFAULT");
        warehouseMapper.updateById(wh);
        
        return ApiResponse.ok();
    }
    
    /**
     * 仓库分配规则
     */
    @GetMapping("/allocation-rules")
    public ApiResponse<Map<String, Object>> getAllocationRules() {
        // 从配置获取仓库分配规则
        Map<String, Object> rules = queryAllocationRules();
        return ApiResponse.ok(rules);
    }

    /**
     * 查询仓库分配规则
     */
    private Map<String, Object> queryAllocationRules() {
        // TODO: 从数据库或配置中心获取规则
        Map<String, Object> rules = new HashMap<>();
        rules.put("strategy", "NEAREST");
        rules.put("priorityWarehouse", "WH01");
        return rules;
    }

    /**
     * 设置仓库分配规则
     */
    @PutMapping("/allocation-rules")
    public ApiResponse<Void> setAllocationRules(@RequestBody Map<String, Object> rules) {
        // 保存到配置
        saveAllocationRules(rules);
        return ApiResponse.ok();
    }

    /**
     * 保存仓库分配规则
     */
    private void saveAllocationRules(Map<String, Object> rules) {
        // TODO: 保存规则到数据库或配置中心
        log.info("保存仓库分配规则: {}", rules);
    }
}
