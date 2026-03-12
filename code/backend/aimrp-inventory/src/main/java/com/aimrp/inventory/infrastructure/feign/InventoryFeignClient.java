package com.aimrp.inventory.infrastructure.feign;

import com.aimrp.common.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存服务 Feign 客户端
 * 用于其他模块调用库存模块
 */
@FeignClient(name = "aimrp-inventory", path = "/api/inventory")
public interface InventoryFeignClient {

    /**
     * 更新安全库存
     */
    @PutMapping("/update-safety-stock/{itemCode}")
    ApiResponse<Integer> updateSafetyStockByItemCode(
            @PathVariable("itemCode") String itemCode,
            @RequestParam("safetyStock") BigDecimal safetyStock);

    /**
     * 更新最大库存
     */
    @PutMapping("/update-max-stock/{itemCode}")
    ApiResponse<Integer> updateMaxStockByItemCode(
            @PathVariable("itemCode") String itemCode,
            @RequestParam("maxStock") BigDecimal maxStock);

    /**
     * 更新现有量（盘点调整）
     */
    @PutMapping("/update-on-hand/{itemCode}")
    ApiResponse<Integer> updateOnHandQtyByItemCode(
            @PathVariable("itemCode") String itemCode,
            @RequestParam("quantity") BigDecimal quantity);

    /**
     * 更新 MRP 参数
     */
    @PutMapping("/update-mrp-param/{itemCode}")
    ApiResponse<Integer> updateMrpParameterByItemCode(
            @PathVariable("itemCode") String itemCode,
            @RequestParam("paramName") String paramName,
            @RequestParam("paramValue") String paramValue);

    /**
     * 根据物料编码查询库存
     */
    @GetMapping("/item/{itemCode}")
    ApiResponse<Map<String, Object>> getInventoryByItemCode(@PathVariable("itemCode") String itemCode);

    /**
     * 查询库存列表
     */
    @GetMapping("/list")
    ApiResponse<List<Map<String, Object>>> listInventory();
}
