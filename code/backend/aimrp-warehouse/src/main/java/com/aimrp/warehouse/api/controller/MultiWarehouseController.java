package com.aimrp.warehouse.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.warehouse.domain.entity.Warehouse;
import com.aimrp.warehouse.infrastructure.persistence.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多仓库管理 Controller
 */
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
        // TODO: 根据组织ID查询仓库
        return ApiResponse.ok(warehouseMapper.selectList(null, "ENABLED"));
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
        // TODO: 从配置获取仓库分配规则
        Map<String, Object> rules = new HashMap<>();
        rules.put("strategy", "NEAREST"); // 就近分配
        rules.put("priorityWarehouse", "WH01"); // 优先仓库
        return ApiResponse.ok(rules);
    }
    
    /**
     * 设置仓库分配规则
     */
    @PutMapping("/allocation-rules")
    public ApiResponse<Void> setAllocationRules(@RequestBody Map<String, Object> rules) {
        // TODO: 保存到配置
        return ApiResponse.ok();
    }
}
