package com.aimrp.purchase.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.purchase.domain.entity.PurchaseOrder;
import com.aimrp.purchase.domain.entity.PurchaseOrderLine;
import com.aimrp.purchase.infrastructure.persistence.mapper.PurchaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        
        // 从数据库查询
        var list = purchaseMapper.selectList(supplierCode, status);
        
        // 分页
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
    public ApiResponse<Map<String, Object>> create(@RequestBody PurchaseOrder order) {
        Long id = purchaseMapper.insertPurchaseOrder(
            order.getSupplierCode(),
            order.getItemCode(),
            order.getOrderQty(),
            order.getUnitPrice(),
            order.getTotalAmount()
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
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PurchaseOrder order) {
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
        // 更新状态为已确认
        return ApiResponse.ok();
    }
    
    /**
     * 采购入库
     */
    @PostMapping("/{id}/receive")
    public ApiResponse<Map<String, Object>> receive(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        // 获取入库数量
        BigDecimal qty = new BigDecimal(params.get("qty").toString());
        
        // 更新已入库数量
        purchaseMapper.updateReceivedQty(id, qty);
        
        // 创建入库单（调用库存模块）
        
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
        // 更新状态为已取消
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
