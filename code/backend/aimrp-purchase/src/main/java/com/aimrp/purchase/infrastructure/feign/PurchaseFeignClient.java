package com.aimrp.purchase.infrastructure.feign;

import com.aimrp.common.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 采购服务 Feign 客户端
 * 用于其他模块调用采购模块
 */
@FeignClient(name = "aimrp-purchase", path = "/api/purchase-orders")
public interface PurchaseFeignClient {

    /**
     * 更新采购数量
     */
    @PutMapping("/update-quantity/{purchaseNo}")
    ApiResponse<Integer> updateQuantityByPurchaseNo(
            @PathVariable("purchaseNo") String purchaseNo,
            @RequestParam("quantity") BigDecimal quantity);

    /**
     * 更新采购交期
     */
    @PutMapping("/update-due-date/{purchaseNo}")
    ApiResponse<Integer> updateDueDateByPurchaseNo(
            @PathVariable("purchaseNo") String purchaseNo,
            @RequestParam("dueDate") String dueDate);

    /**
     * 更新采购状态
     */
    @PutMapping("/update-status/{purchaseNo}")
    ApiResponse<Integer> updateStatusByPurchaseNo(
            @PathVariable("purchaseNo") String purchaseNo,
            @RequestParam("status") String status);

    /**
     * 查询在途采购订单
     */
    @GetMapping("/in-transit")
    ApiResponse<List<Map<String, Object>>> selectInTransitOrders();

    /**
     * 根据采购单号查询
     */
    @GetMapping("/{purchaseNo}")
    ApiResponse<Map<String, Object>> getPurchaseByNo(@PathVariable("purchaseNo") String purchaseNo);
}
