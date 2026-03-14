package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.inventory.domain.entity.InventoryCheck;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存盘点 Controller
 */
@RestController
@RequestMapping("/api/inventory/check")
@RequiredArgsConstructor
public class InventoryCheckController {
    
    private final InventoryCheckMapper checkMapper;
    
    /**
     * 创建盘点单
     */
    @PostMapping
    public ApiResponse<InventoryCheck> create(@RequestBody InventoryCheck check) {
        check.setCheckNo("IC" + System.currentTimeMillis());
        check.setStatus("DRAFT");
        check.setCheckDate(LocalDate.now());
        
        checkMapper.insert(check);
        
        return ApiResponse.ok(check);
    }
    
    /**
     * 审核盘点单
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        checkMapper.updateStatus(id, "APPROVED");
        return ApiResponse.ok();
    }
    
    /**
     * 开始盘点
     */
    @PostMapping("/{id}/start")
    public ApiResponse<Void> start(@PathVariable Long id) {
        checkMapper.updateStatus(id, "EXECUTING");
        return ApiResponse.ok();
    }
    
    /**
     * 完成盘点
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        checkMapper.updateStatus(id, "COMPLETED");
        return ApiResponse.ok();
    }
    
    /**
     * 查询盘点单列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(required = false) String status) {
        List<InventoryCheck> list = status != null ? 
            checkMapper.selectByStatus(status) : checkMapper.selectAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 查询盘点单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<InventoryCheck> getById(@PathVariable Long id) {
        return ApiResponse.ok(checkMapper.selectById(id));
    }
}
