package com.aimrp.warehouse.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.warehouse.domain.entity.WarehouseLocation;
import com.aimrp.warehouse.infrastructure.persistence.mapper.WarehouseLocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库位管理 Controller
 */
@RestController
@RequestMapping("/api/warehouse/locations")
@RequiredArgsConstructor
public class WarehouseLocationController {
    
    private final WarehouseLocationMapper locationMapper;
    
    /**
     * 查询库位列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        List<WarehouseLocation> list = locationMapper.selectList(
                warehouseCode, areaCode, status, keyword);
        
        // 分页
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<WarehouseLocation> pageList = fromIndex < total ?
                list.subList(fromIndex, toIndex) : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 查询库位详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WarehouseLocation> getById(@PathVariable Long id) {
        return ApiResponse.ok(locationMapper.selectById(id));
    }
    
    /**
     * 创建库位
     */
    @PostMapping
    public ApiResponse<WarehouseLocation> create(@RequestBody WarehouseLocation location) {
        // 检查编码是否重复
        WarehouseLocation exist = locationMapper.selectByCode(location.getLocationCode());
        if (exist != null) {
            return ApiResponse.fail("库位编码已存在");
        }
        
        location.setStatus("ACTIVE");
        locationMapper.insert(location);
        
        return ApiResponse.ok(location);
    }
    
    /**
     * 更新库位
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody WarehouseLocation location) {
        location.setId(id);
        locationMapper.updateById(location);
        
        return ApiResponse.ok();
    }
    
    /**
     * 禁用/启用库位
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        WarehouseLocation location = new WarehouseLocation();
        location.setId(id);
        location.setStatus(status);
        locationMapper.updateById(location);
        
        return ApiResponse.ok();
    }
    
    /**
     * 删除库位
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        locationMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 根据物料查询库位
     */
    @GetMapping("/by-item/{itemCode}")
    public ApiResponse<List<WarehouseLocation>> getByItemCode(@PathVariable String itemCode) {
        // TODO: 根据物料查询存放的库位
        return ApiResponse.ok(List.of());
    }
}
