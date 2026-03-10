package com.aimrp.supplierportal.api.controller;

import com.aimrp.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 供应商门户 Controller
 */
@RestController
@RequestMapping("/api/supplier-portal")
@RequiredArgsConstructor
public class SupplierPortalController {
    
    /**
     * 供应商登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        // TODO: 验证用户名密码
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", "mock_token_" + System.currentTimeMillis());
        result.put("supplierId", 1L);
        result.put("supplierName", "测试供应商");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取供应商待确认订单
     */
    @GetMapping("/orders/pending")
    public ApiResponse<Map<String, Object>> getPendingOrders(@RequestParam Long supplierId) {
        List<Map<String, Object>> orders = new ArrayList<>();
        
        // 示例数据
        Map<String, Object> order = new HashMap<>();
        order.put("poNo", "PO001");
        order.put("itemCode", "A001");
        order.put("itemName", "物料A");
        order.put("qty", 100);
        order.put("deliveryDate", "2026-03-20");
        order.put("status", "PENDING_CONFIRM");
        orders.add(order);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", orders);
        result.put("total", orders.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 供应商确认订单
     */
    @PostMapping("/orders/{id}/confirm")
    public ApiResponse<Void> confirmOrder(@PathVariable Long id) {
        // TODO: 更新订单状态
        return ApiResponse.ok();
    }
    
    /**
     * 供应商报价
     */
    @PostMapping("/quotes")
    public ApiResponse<Map<String, Object>> submitQuote(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("quoteNo", "Q" + System.currentTimeMillis());
        result.put("status", "SUBMITTED");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 供应商发货通知
     */
    @PostMapping("/shipments")
    public ApiResponse<Map<String, Object>> submitShipment(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("shipmentNo", "SH" + System.currentTimeMillis());
        result.put("status", "SHIPPED");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取对账单
     */
    @GetMapping("/statements")
    public ApiResponse<Map<String, Object>> getStatements(@RequestParam Long supplierId) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        
        return ApiResponse.ok(result);
    }
}
