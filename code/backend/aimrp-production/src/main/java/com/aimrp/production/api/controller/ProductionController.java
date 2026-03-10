package com.aimrp.production.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.production.api.dto.ProductionOrderCreateRequest;
import com.aimrp.production.domain.entity.ProductionOrder;
import com.aimrp.production.infrastructure.persistence.mapper.ProductionOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 生产工单 Controller
 */
@RestController
@RequestMapping("/api/production-orders")
@RequiredArgsConstructor
public class ProductionOrderController {
    
    private final ProductionOrderMapper productionOrderMapper;
    
    /**
     * 分页查询生产工单
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status) {
        
        var list = productionOrderMapper.selectList(status, itemCode);
        
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        var pageList = fromIndex < total ? list.subList(fromIndex, toIndex) : list;
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取生产工单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> order = productionOrderMapper.selectById(id);
        
        if (order == null) {
            return ApiResponse.fail("工单不存在");
        }
        
        return ApiResponse.ok(order);
    }
    
    /**
     * 创建生产工单
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Validated @RequestBody ProductionOrderCreateRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("mo_no", "MO" + System.currentTimeMillis());
        data.put("item_code", request.getItemCode());
        data.put("item_name", request.getItemName());
        data.put("plan_qty", request.getPlanQty());
        data.put("status", "DRAFT");
        
        Long id = productionOrderMapper.insert(data);
        
        data.put("id", id);
        
        return ApiResponse.ok(data);
    }
    
    /**
     * 更新生产工单
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody ProductionOrderCreateRequest request) {
        return ApiResponse.ok();
    }
    
    /**
     * 下达生产工单
     */
    @PostMapping("/{id}/release")
    public ApiResponse<Void> release(@PathVariable Long id) {
        productionOrderMapper.updateStatus(id, "RELEASED");
        productionOrderMapper.updateDates(id, java.time.LocalDate.now().toString(), null);
        return ApiResponse.ok();
    }
    
    /**
     * 开始生产
     */
    @PostMapping("/{id}/start")
    public ApiResponse<Void> start(@PathVariable Long id) {
        productionOrderMapper.updateStatus(id, "PROCESSING");
        return ApiResponse.ok();
    }
    
    /**
     * 完工
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        productionOrderMapper.updateStatus(id, "COMPLETED");
        return ApiResponse.ok();
    }
    
    /**
     * 取消生产工单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        productionOrderMapper.updateStatus(id, "CANCELLED");
        return ApiResponse.ok();
    }
}
