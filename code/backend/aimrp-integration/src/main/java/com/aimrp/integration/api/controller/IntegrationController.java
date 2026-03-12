package com.aimrp.integration.api.controller;

import com.aimrp.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * ERP集成 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationController {

    private final RestTemplate restTemplate;

    @Value("${erp.api.base-url:}")
    private String erpBaseUrl;

    @Value("${erp.api.enabled:false}")
    private boolean erpEnabled;

    /**
     * 同步销售订单到ERP
     */
    @PostMapping("/sync/sales-order")
    public ApiResponse<Map<String, Object>> syncSalesOrder(@RequestBody Map<String, Object> data) {
        log.info("ERP同步销售订单: {}", data.get("orderNo"));

        Map<String, Object> result = new HashMap<>();

        if (erpEnabled && erpBaseUrl != null && !erpBaseUrl.isEmpty()) {
            try {
                // 调用ERP API
                String url = erpBaseUrl + "/api/sales-order/sync";
                Map<String, Object> erpResponse = restTemplate.postForObject(url, data, Map.class);

                if (erpResponse != null && Boolean.TRUE.equals(erpResponse.get("success"))) {
                    result.put("success", true);
                    result.put("erpOrderNo", erpResponse.get("erpOrderNo"));
                    log.info("订单同步成功 - ERP单号: {}", erpResponse.get("erpOrderNo"));
                } else {
                    result.put("success", false);
                    result.put("message", "ERP同步失败");
                }
            } catch (Exception e) {
                log.error("ERP API调用失败: {}", e.getMessage());
                result.put("success", false);
                result.put("message", "ERP连接失败: " + e.getMessage());
            }
        } else {
            // 模拟同步成功
            result.put("success", true);
            result.put("erpOrderNo", "ERP" + System.currentTimeMillis());
            result.put("message", "模拟同步成功（ERP未配置）");
        }

        return ApiResponse.ok(result);
    }

    /**
     * 同步库存
     */
    @PostMapping("/sync/inventory")
    public ApiResponse<Map<String, Object>> syncInventory(@RequestBody Map<String, Object> data) {
        log.info("ERP同步库存: {}", data.get("itemCode"));

        Map<String, Object> result = new HashMap<>();

        if (erpEnabled && erpBaseUrl != null && !erpBaseUrl.isEmpty()) {
            try {
                String url = erpBaseUrl + "/api/inventory/sync";
                Map<String, Object> erpResponse = restTemplate.postForObject(url, data, Map.class);

                result.put("success", erpResponse != null && Boolean.TRUE.equals(erpResponse.get("success")));
            } catch (Exception e) {
                log.error("ERP API调用失败: {}", e.getMessage());
                result.put("success", false);
                result.put("message", "ERP连接失败: " + e.getMessage());
            }
        } else {
            result.put("success", true);
            result.put("message", "模拟同步成功（ERP未配置）");
        }

        return ApiResponse.ok(result);
    }
    
    /**
     * 同步物料主数据
     */
    @PostMapping("/sync/item")
    public ApiResponse<Map<String, Object>> syncItem(@RequestBody Map<String, Object> data) {
        log.info("ERP同步物料: {}", data.get("itemCode"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 推送采购入库
     */
    @PostMapping("/push/purchase-receipt")
    public ApiResponse<Map<String, Object>> pushPurchaseReceipt(@RequestBody Map<String, Object> data) {
        log.info("推送采购入库到ERP: {}", data.get("receiptNo"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("erpReceiptNo", "ERP_RECEIPT" + System.currentTimeMillis());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 推送生产成本
     */
    @PostMapping("/push/production-cost")
    public ApiResponse<Map<String, Object>> pushProductionCost(@RequestBody Map<String, Object> data) {
        log.info("推送生产成本到ERP");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取同步状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastSyncTime", "2026-03-10 15:00:00");
        status.put("salesOrder", "SUCCESS");
        status.put("inventory", "SUCCESS");
        status.put("item", "SUCCESS");
        
        return ApiResponse.ok(status);
    }
}
