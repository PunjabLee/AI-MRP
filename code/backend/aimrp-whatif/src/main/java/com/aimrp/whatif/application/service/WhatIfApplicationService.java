package com.aimrp.whatif.application.service;

import com.aimrp.whatif.domain.service.WhatIfSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * What-if 应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatIfApplicationService {
    
    private final WhatIfSimulationService simulationService;
    
    /**
     * 执行模拟
     */
    @Transactional(readOnly = true)
    public Map<String, Object> simulate(SimulateRequest request) {
        log.info("执行What-if模拟 - scenario: {}", request.getScenarioName());
        
        var result = simulationService.simulate(
                request.getScenarioName(),
                request.getParameters());
        
        return Map.of(
                "scenarioName", result.getScenarioName(),
                "results", result.getResults(),
                "comparison", result.getComparison());
    }
    
    /**
     * 对比场景
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compare(String scenarioId1, String scenarioId2) {
        log.info("对比场景 - {} vs {}", scenarioId1, scenarioId2);
        return simulationService.compare(scenarioId1, scenarioId2);
    }
    
    @lombok.Data
    public static class SimulateRequest {
        private String scenarioName;
        private Map<String, Object> parameters;
    }
}
