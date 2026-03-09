package com.aimrp.mrp.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.mrp.domain.service.CostImpactAnalysisService;
import com.aimrp.mrp.domain.service.CostImpactAnalysisService.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成本影响分析 API
 */
@Slf4j
@RestController
@RequestMapping("/api/cost-analysis")
@RequiredArgsConstructor
public class CostAnalysisController {
    
    private final CostImpactAnalysisService costAnalysisService;
    
    /**
     * 分析成本影响
     * 
     * POST /api/cost-analysis/analyze
     * {
     *   "scenarioName": "订单数量变更",
     *   "itemChanges": [
     *     {"itemCode": "A001", "qtyChange": "100", "unitCost": 50}
     *   ],
     *   "laborHoursChange": 20,
     *   "hourlyLaborRate": 50,
     *   "emergencyPurchase": false
     * }
     */
    @PostMapping("/analyze")
    public ApiResponse<CostImpactResult> analyze(@RequestBody CostAnalysisScenario request) {
        log.info("接收成本影响分析请求 - 场景: {}", request.getScenarioName());
        
        CostImpactResult result = costAnalysisService.analyze(request);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 批量对比方案
     * 
     * POST /api/cost-analysis/compare
     */
    @PostMapping("/compare")
    public ApiResponse<List<CostImpactResult>> compare(@RequestBody List<CostAnalysisScenario> scenarios) {
        log.info("成本方案对比 - 场景数: {}", scenarios.size());
        
        List<CostImpactResult> results = scenarios.stream()
                .map(costAnalysisService::analyze)
                .sorted((r1, r2) -> r1.getTotalCostChange().compareTo(r2.getTotalCostChange()))
                .toList();
        
        return ApiResponse.ok(results);
    }
    
    /**
     * 获取成本分析历史
     * 
     * GET /api/cost-analysis/history
     */
    @GetMapping("/history")
    public ApiResponse<List<CostImpactResult>> getHistory() {
        // TODO: 从数据库查询历史
        return ApiResponse.ok(List.of());
    }
}
