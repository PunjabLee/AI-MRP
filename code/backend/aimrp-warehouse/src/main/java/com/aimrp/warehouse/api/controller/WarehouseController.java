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
 * 仓库管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    
    private final WarehouseMapper warehouseMapper;
    
    /**
     * 获取仓库列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String warehouseType,
            @RequestParam(required = false) String status) {
        
        List<Warehouse> list = warehouseMapper.selectList(warehouseType, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取仓库详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Warehouse> get(@PathVariable Long id) {
        Warehouse warehouse = warehouseMapper.selectById(id);
        return ApiResponse.ok(warehouse);
    }
    
    /**
     * 创建仓库
     */
    @PostMapping
    public ApiResponse<Warehouse> create(@RequestBody Warehouse warehouse) {
        // 检查编码是否重复
        Warehouse exist = warehouseMapper.selectByCode(warehouse.getWarehouseCode());
        if (exist != null) {
            return ApiResponse.fail("仓库编码已存在");
        }
        
        warehouse.setStatus("ENABLED");
        warehouseMapper.insert(warehouse);
        
        return ApiResponse.ok(warehouse);
    }
    
    /**
     * 更新仓库
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseMapper.updateById(warehouse);
        
        return ApiResponse.ok();
    }
    
    /**
     * 删除仓库
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        warehouseMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 启用/禁用仓库
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setStatus(status);
        warehouseMapper.updateById(warehouse);
        
        return ApiResponse.ok();
    }
}
