package com.aimrp.supplierportal.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 供应商门户接口
 */
@Slf4j
@RestController
@RequestMapping("/api/supplier-portal")
@RequiredArgsConstructor
public class SupplierPortalController {
    
    /**
     * 供应商登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        log.info("供应商登录: {}", credentials.get("supplierCode"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", "mock-jwt-token");
        result.put("supplierId", 1L);
        result.put("supplierCode", credentials.get("supplierCode"));
        
        return Map.of("code", 200, "data", result);
    }
    
    /**
     * 获取采购订单列表
     */
    @GetMapping("/orders")
    public Map<String, Object> getOrders(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String status) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 1L);
        order1.put("orderNo", "PO20240309001");
        order1.put("itemName", "物料A");
        order1.put("qty", 1000);
        order1.put("deliveryDate", "2024-03-20");
        order1.put("status", "PENDING");
        list.add(order1);
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 确认订单
     */
    @PostMapping("/orders/{id}/confirm")
    public Map<String, Object> confirmOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        log.info("供应商确认订单: {}", id);
        
        return Map.of("code", 200, "message", "确认成功");
    }
    
    /**
     * 提交送货通知
     */
    @PostMapping("/delivery-notices")
    public Map<String, Object> submitDeliveryNotice(@RequestBody Map<String, Object> data) {
        log.info("提交送货通知: {}", data);
        
        data.put("id", System.currentTimeMillis());
        
        return Map.of("code", 200, "data", data, "message", "提交成功");
    }
    
    /**
     * 获取对账单
     */
    @GetMapping("/statements")
    public Map<String, Object> getStatements(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> stmt = new HashMap<>();
        stmt.put("id", 1L);
        stmt.put("statementNo", "ST20240301");
        stmt.put("amount", 50000);
        stmt.put("status", "CONFIRMED");
        list.add(stmt);
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 送货通知列表
     */
    @GetMapping("/delivery-notices")
    public Map<String, Object> getDeliveryNotices(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        return Map.of("code", 200, "data", new ArrayList<>());
    }
}
