package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.inventory.domain.service.InventoryAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存分析 Controller
 */
@RestController
@RequestMapping("/api/inventory/analysis")
@RequiredArgsConstructor
public class InventoryAnalysisController {
    
    private final InventoryAnalysisService analysisService;
    
    /**
     * 库龄分析
     */
    @GetMapping("/ageing")
    public ApiResponse<Map<String, Object>> analyseAgeing(
            @RequestParam(required = false) String warehouseCode) {
        
        List<InventoryAnalysisService.InventoryAgeing> results = analysisService.analyseAgeing(warehouseCode);
        
        // 统计各等级数量
        Map<String, Integer> levelCount = new HashMap<>();
        levelCount.put("NORMAL", 0);
        levelCount.put("WARNING", 0);
        levelCount.put("SLOW", 0);
        levelCount.put("DEAD", 0);
        
        for (InventoryAnalysisService.InventoryAgeing item : results) {
            String level = item.getAgeLevel();
            levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", results);
        response.put("total", results.size());
        response.put("levelCount", levelCount);
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 呆滞分析
     */
    @GetMapping("/slow-moving")
    public ApiResponse<Map<String, Object>> analyseSlowMoving(
            @RequestParam(defaultValue = "90") int slowDays) {
        
        List<InventoryAnalysisService.InventoryAgeing> results = analysisService.analyseSlowMoving(slowDays);
        
        // 统计呆滞总金额
        java.math.BigDecimal totalAmount = results.stream()
                .filter(r -> r.getIdleAmount() != null)
                .map(InventoryAnalysisService.InventoryAgeing::getIdleAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", results);
        response.put("total", results.size());
        response.put("totalIdleAmount", totalAmount);
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 库存周转分析
     */
    @GetMapping("/turnover")
    public ApiResponse<Map<String, Object>> analyseTurnover(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "30") int days) {
        
        // TODO: 完整实现库存周转率计算
        // 周转率 = 出库数量 / 平均库存
        
        Map<String, Object> response = new HashMap<>();
        response.put("turnoverRate", 0);
        response.put("avgInventory", 0);
        response.put("outboundQty", 0);
        
        return ApiResponse.ok(response);
    }
}
