package com.aimrp.mrp.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.mrp.domain.service.CostImpactAnalysisService;
import com.aimrp.mrp.domain.service.CostImpactAnalysisService.*;
import com.aimrp.mrp.infrastructure.persistence.mapper.CostAnalysisHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 成本影响分析 API
 */
@Slf4j
@RestController
@RequestMapping("/api/cost-analysis")
@RequiredArgsConstructor
public class CostAnalysisController {
    
    private final CostImpactAnalysisService costAnalysisService;
    private final CostAnalysisHistoryMapper costAnalysisHistoryMapper;
    
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
        
        // 保存到历史记录
        saveToHistory(request, result);
        
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
                .map(scenario -> {
                    CostImpactResult result = costAnalysisService.analyze(scenario);
                    // 保存每个方案到历史
                    saveToHistory(scenario, result);
                    return result;
                })
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
    public ApiResponse<List<Map<String, Object>>> getHistory(
            @RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> history = costAnalysisHistoryMapper.selectRecent(limit);
        return ApiResponse.ok(history);
    }
    
    /**
     * 获取单条分析详情
     * 
     * GET /api/cost-analysis/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> record = costAnalysisHistoryMapper.selectById(id);
        if (record == null) {
            return ApiResponse.fail("记录不存在");
        }
        return ApiResponse.ok(record);
    }
    
    /**
     * 删除历史记录
     * 
     * DELETE /api/cost-analysis/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        costAnalysisHistoryMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 保存到历史记录
     */
    private void saveToHistory(CostAnalysisScenario scenario, CostImpactResult result) {
        try {
            Map<String, Object> record = new HashMap<>();
            record.put("scenarioName", scenario.getScenarioName());
            record.put("itemCostChange", result.getItemCostChange());
            record.put("laborCostChange", result.getLaborCostChange());
            record.put("emergencyCostChange", result.getEmergencyCostChange());
            record.put("totalCostChange", result.getTotalCostChange());
            record.put("costChangeRate", result.getCostChangeRate());
            record.put("analysisDetails", result.getAnalysisDetails() != null ? 
                    result.getAnalysisDetails().toString() : "");
            record.put("createdBy", "system");
            
            costAnalysisHistoryMapper.insert(record);
        } catch (Exception e) {
            log.warn("保存成本分析历史失败: {}", e.getMessage());
        }
    }
}
