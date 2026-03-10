package com.aimrp.purchase.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.purchase.api.dto.PurchaseOrderCreateRequest;
import com.aimrp.purchase.domain.entity.PurchaseOrder;
import com.aimrp.purchase.infrastructure.persistence.mapper.PurchaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 采购订单 Controller
 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    private final PurchaseMapper purchaseMapper;
    
    /**
     * 分页查询采购订单
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String status) {
        
        var list = purchaseMapper.selectList(supplierCode, status);
        
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        var pageList = fromIndex < total ? list.subList(fromIndex, toIndex) : list;
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取采购订单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> order = purchaseMapper.selectById(id);
        
        if (order == null) {
            return ApiResponse.fail("订单不存在");
        }
        
        return ApiResponse.ok(order);
    }
    
    /**
     * 创建采购订单
     */
    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Validated @RequestBody PurchaseOrderCreateRequest request) {
        // 计算金额
        BigDecimal amount = BigDecimal.ZERO;
        if (request.getOrderQty() != null && request.getUnitPrice() != null) {
            amount = request.getOrderQty().multiply(request.getUnitPrice());
        }
        
        Long id = purchaseMapper.insertPurchaseOrder(
            request.getSupplierCode(),
            request.getItemCode(), 
            request.getOrderQty(), 
            request.getUnitPrice(),
            amount
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("poNo", "PO" + System.currentTimeMillis());
        result.put("status", "DRAFT");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 更新采购订单
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody PurchaseOrderCreateRequest request) {
        // 更新逻辑
        return ApiResponse.ok();
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
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        return ApiResponse.ok();
    }
    
    /**
     * 采购入库
     */
    @PostMapping("/{id}/receive")
    public ApiResponse<Map<String, Object>> receive(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        BigDecimal qty = new BigDecimal(params.get("qty").toString());
        
        purchaseMapper.updateReceivedQty(id, qty);
        
        Map<String, Object> result = new HashMap<>();
        result.put("receiveNo", "RCV" + System.currentTimeMillis());
        result.put("status", "COMPLETED");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 取消采购订单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        return ApiResponse.ok();
    }
    
    /**
     * 获取待入库订单
     */
    @GetMapping("/pending-receive")
    public ApiResponse<Map<String, Object>> getPendingReceive() {
        var list = purchaseMapper.selectList(null, "CONFIRMED");
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        
        return ApiResponse.ok(result);
    }
}
