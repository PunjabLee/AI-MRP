package com.aimrp.purchase.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.purchase.domain.entity.PurchaseOrder;
import com.aimrp.purchase.domain.entity.PurchaseOrderLine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购订单 Controller
 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    /**
     * 分页查询采购订单
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String status) {
        
        // TODO: 从数据库查询
        List<PurchaseOrder> list = new ArrayList<>();
        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setPoNo("PO20260309001");
        order.setSupplierCode("SUP001");
        order.setSupplierName("供应商A");
        order.setOrderDate(LocalDate.now());
        order.setExpectDate(LocalDate.now().plusDays(7));
        order.setStatus("CONFIRMED");
        order.setTotalAmount(new BigDecimal("10000"));
        list.add(order);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", 1);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取采购订单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        // TODO: 从数据库查询
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("poNo", "PO20260309001");
        order.put("supplierCode", "SUP001");
        order.put("supplierName", "供应商A");
        
        List<Map<String, Object>> lines = new ArrayList<>();
        Map<String, Object> line = new HashMap<>();
        line.put("itemCode", "A001");
        line.put("itemName", "物料A");
        line.put("orderQty", 100);
        line.put("receivedQty", 0);
        lines.add(line);
        order.put("lines", lines);
        
        return ApiResponse.ok(order);
    }
    
    /**
     * 创建采购订单
     */
    @PostMapping
    public ApiResponse<PurchaseOrder> create(@RequestBody PurchaseOrder order) {
        order.setId(1L);
        order.setPoNo("PO" + System.currentTimeMillis());
        order.setStatus("DRAFT");
        return ApiResponse.ok(order);
    }
    
    /**
     * 更新采购订单
     */
    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrder> update(@PathVariable Long id, @RequestBody PurchaseOrder order) {
        order.setId(id);
        return ApiResponse.ok(order);
    }
    
    /**
     * 删除采购订单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return ApiResponse.ok();
    }
    
    /**
     * 确认采购订单
     */
    @PostMapping("/{id}/confirm")
    public ApiResponse<PurchaseOrder> confirm(@PathVariable Long id) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setStatus("CONFIRMED");
        return ApiResponse.ok(order);
    }
    
    /**
     * 采购入库
     */
    @PostMapping("/{id}/receive")
    public ApiResponse<Map<String, Object>> receive(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        // TODO: 创建入库单，更新库存
        Map<String, Object> result = new HashMap<>();
        result.put("receiveNo", "RCV" + System.currentTimeMillis());
        result.put("status", "COMPLETED");
        return ApiResponse.ok(result);
    }
    
    /**
     * 取消采购订单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseOrder> cancel(@PathVariable Long id) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setStatus("CANCELLED");
        return ApiResponse.ok(order);
    }
}
