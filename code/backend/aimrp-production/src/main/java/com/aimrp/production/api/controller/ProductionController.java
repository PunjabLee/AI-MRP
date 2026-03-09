package com.aimrp.production.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.production.domain.entity.ProductionOrder;
import com.aimrp.production.domain.entity.ProductionReport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产工单 Controller
 */
@RestController
@RequestMapping("/api/production-orders")
@RequiredArgsConstructor
public class ProductionOrderController {
    
    /**
     * 分页查询生产工单
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String status) {
        
        // TODO: 从数据库查询
        List<ProductionOrder> list = new ArrayList<>();
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        order.setMoNo("MO20260309001");
        order.setItemCode("A001");
        order.setItemName("产品A");
        order.setPlanQty(new BigDecimal("1000"));
        order.setCompletedQty(BigDecimal.ZERO);
        order.setStatus("RELEASED");
        order.setStartDate(LocalDate.now());
        order.setEndDate(LocalDate.now().plusDays(7));
        list.add(order);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", 1);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取生产工单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("moNo", "MO20260309001");
        order.put("itemCode", "A001");
        order.put("planQty", 1000);
        return ApiResponse.ok(order);
    }
    
    /**
     * 创建生产工单
     */
    @PostMapping
    public ApiResponse<ProductionOrder> create(@RequestBody ProductionOrder order) {
        order.setId(1L);
        order.setMoNo("MO" + System.currentTimeMillis());
        order.setStatus("DRAFT");
        return ApiResponse.ok(order);
    }
    
    /**
     * 更新生产工单
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductionOrder> update(@PathVariable Long id, @RequestBody ProductionOrder order) {
        order.setId(id);
        return ApiResponse.ok(order);
    }
    
    /**
     * 下达生产工单
     */
    @PostMapping("/{id}/release")
    public ApiResponse<ProductionOrder> release(@PathVariable Long id) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setStatus("RELEASED");
        order.setActualStartDate(java.time.LocalDate.now());
        return ApiResponse.ok(order);
    }
    
    /**
     * 开始生产
     */
    @PostMapping("/{id}/start")
    public ApiResponse<ProductionOrder> start(@PathVariable Long id) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setStatus("PROCESSING");
        return ApiResponse.ok(order);
    }
    
    /**
     * 完工
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<ProductionOrder> complete(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setStatus("COMPLETED");
        order.setCompletedQty(new BigDecimal(params.get("completedQty").toString()));
        return ApiResponse.ok(order);
    }
    
    /**
     * 取消生产工单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<ProductionOrder> cancel(@PathVariable Long id) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setStatus("CANCELLED");
        return ApiResponse.ok(order);
    }
}

/**
 * 生产报工 Controller
 */
@RestController
@RequestMapping("/api/production-reports")
@RequiredArgsConstructor
class ProductionReportController {
    
    /**
     * 创建报工记录
     */
    @PostMapping
    public ApiResponse<ProductionReport> create(@RequestBody ProductionReport report) {
        report.setId(1L);
        report.setReportNo("RPT" + System.currentTimeMillis());
        report.setStatus("PENDING");
        return ApiResponse.ok(report);
    }
    
    /**
     * 报工审核
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<ProductionReport> approve(@PathVariable Long id) {
        ProductionReport report = new ProductionReport();
        report.setId(id);
        report.setStatus("APPROVED");
        return ApiResponse.ok(report);
    }
}
