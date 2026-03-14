package com.aimrp.supplierportal.api.controller;

import com.aimrp.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        
        Map<String, Object> order = new HashMap<>();
        order.put("id", 1L);
        order.put("poNo", "PO20260310001");
        order.put("itemCode", "ITEM001");
        order.put("itemName", "测试物料");
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
     * 确认订单
     */
    @PostMapping("/orders/{id}/confirm")
    public ApiResponse<Void> confirmOrder(@PathVariable Long id) {
        // TODO: 更新订单状态为已确认
        return ApiResponse.ok();
    }
    
    /**
     * 提交报价
     */
    @PostMapping("/quotes")
    public ApiResponse<Map<String, Object>> submitQuote(@RequestBody Map<String, Object> params) {
        Map<String, Object> quote = new HashMap<>();
        quote.put("quoteNo", "QT" + System.currentTimeMillis());
        quote.put("poNo", params.get("poNo"));
        quote.put("unitPrice", params.get("unitPrice"));
        quote.put("deliveryDate", params.get("deliveryDate"));
        quote.put("remark", params.get("remark"));
        quote.put("status", "PENDING"); // 待审核
        
        // TODO: 保存报价到数据库
        
        return ApiResponse.ok(quote);
    }
    
    /**
     * 获取报价列表
     */
    @GetMapping("/quotes")
    public ApiResponse<Map<String, Object>> getQuotes(@RequestParam Long supplierId) {
        List<Map<String, Object>> quotes = new ArrayList<>();
        
        Map<String, Object> quote = new HashMap<>();
        quote.put("id", 1L);
        quote.put("quoteNo", "QT20260310001");
        quote.put("poNo", "PO20260310001");
        quote.put("unitPrice", 99.00);
        quote.put("deliveryDate", "2026-03-20");
        quote.put("status", "PENDING");
        quotes.add(quote);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", quotes);
        result.put("total", quotes.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 确认报价
     */
    @PostMapping("/quotes/{id}/accept")
    public ApiResponse<Void> acceptQuote(@PathVariable Long id) {
        // TODO: 更新报价状态为已接受
        return ApiResponse.ok();
    }
    
    /**
     * 创建发货通知
     */
    @PostMapping("/shipments")
    public ApiResponse<Map<String, Object>> createShipment(@RequestBody Map<String, Object> params) {
        Map<String, Object> shipment = new HashMap<>();
        shipment.put("shipmentNo", "SH" + System.currentTimeMillis());
        shipment.put("poNo", params.get("poNo"));
        shipment.put("itemCode", params.get("itemCode"));
        shipment.put("shipmentQty", params.get("shipmentQty"));
        shipment.put("carrier", params.get("carrier"));
        shipment.put("trackingNo", params.get("trackingNo"));
        shipment.put("shipmentDate", LocalDate.now());
        shipment.put("status", "SHIPPED");
        
        // TODO: 保存发货通知到数据库
        
        return ApiResponse.ok(shipment);
    }
    
    /**
     * 获取发货列表
     */
    @GetMapping("/shipments")
    public ApiResponse<Map<String, Object>> getShipments(@RequestParam Long supplierId) {
        List<Map<String, Object>> shipments = new ArrayList<>();
        
        Map<String, Object> shipment = new HashMap<>();
        shipment.put("id", 1L);
        shipment.put("shipmentNo", "SH20260310001");
        shipment.put("poNo", "PO20260310001");
        shipment.put("shipmentQty", 100);
        shipment.put("shipmentDate", "2026-03-10");
        shipment.put("status", "SHIPPED");
        shipments.add(shipment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", shipments);
        result.put("total", shipments.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取对账账单
     */
    @GetMapping("/invoices")
    public ApiResponse<Map<String, Object>> getInvoices(@RequestParam Long supplierId) {
        List<Map<String, Object>> invoices = new ArrayList<>();
        
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("id", 1L);
        invoice.put("invoiceNo", "INV202603001");
        invoice.put("poNo", "PO20260310001");
        invoice.put("amount", 9900.00);
        invoice.put("invoiceDate", "2026-03-10");
        invoice.put("status", "PENDING");
        invoices.add(invoice);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", invoices);
        result.put("total", invoices.size());
        
        return ApiResponse.ok(result);
    }
}
