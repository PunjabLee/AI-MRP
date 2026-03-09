package com.aimrp.whatif.application;

import com.aimrp.whatif.domain.model.WhatIfScenario;
import com.aimrp.whatif.domain.model.WhatIfResult;
import com.aimrp.whatif.domain.service.WhatIfSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * What-if 模拟 API
 */
@Slf4j
@RestController
@RequestMapping("/api/whatif")
@RequiredArgsConstructor
public class WhatIfController {
    
    private final WhatIfSimulationService simulationService;
    
    /**
     * 创建场景
     * 
     * POST /api/whatif/scenario
     * {
     *   "scenarioName": "订单插单测试",
     *   "description": "测试插入紧急订单的影响",
     *   "scenarioType": "DEMAND_CHANGE",
     *   "baselineId": 123,
     *   "changes": [
     *     {
     *       "changeType": "DEMAND_CHANGE",
     *       "targetType": "ORDER",
     *       "targetCode": "SO001",
     *       "fieldName": "priority",
     *       "originalValue": "5",
     *       "newValue": "10",
     *       "description": "提高优先级"
     *     }
     *   ]
     * }
     */
    @PostMapping("/scenario")
    public WhatIfScenario createScenario(@RequestBody WhatIfScenario request) {
        log.info("创建 What-if 场景: {}", request.getScenarioName());
        
        WhatIfScenario scenario = new WhatIfScenario();
        scenario.setScenarioId(System.currentTimeMillis());
        scenario.setScenarioName(request.getScenarioName());
        scenario.setDescription(request.getDescription());
        scenario.setScenarioType(request.getScenarioType());
        scenario.setBaselineId(request.getBaselineId());
        scenario.setChanges(request.getChanges());
        scenario.setStatus(WhatIfScenario.ScenarioStatus.DRAFT);
        scenario.setCreatedAt(java.time.LocalDate.now());
        
        return scenario;
    }
    
    /**
     * 运行模拟
     * 
     * POST /api/whatif/simulate
     */
    @PostMapping("/simulate")
    public WhatIfResult simulate(@RequestBody WhatIfScenario scenario) {
        log.info("执行 What-if 模拟: {}", scenario.getScenarioName());
        
        scenario.setStatus(WhatIfScenario.ScenarioStatus.RUNNING);
        
        WhatIfResult result = simulationService.simulate(scenario);
        
        scenario.setStatus(WhatIfScenario.ScenarioStatus.COMPLETED);
        
        return result;
    }
    
    /**
     * 对比场景
     * 
     * POST /api/whatif/compare
     */
    @PostMapping("/compare")
    public List<WhatIfResult> compare(@RequestBody List<WhatIfScenario> scenarios) {
        log.info("对比 What-if 场景: {} 个", scenarios.size());
        
        // 设置场景状态
        scenarios.forEach(s -> s.setStatus(WhatIfScenario.ScenarioStatus.RUNNING));
        
        List<WhatIfResult> results = simulationService.compareScenarios(scenarios);
        
        // 更新状态
        scenarios.forEach(s -> s.setStatus(WhatIfScenario.ScenarioStatus.COMPARED));
        
        return results;
    }
    
    /**
     * 应用场景
     * 
     * POST /api/whatif/apply/{scenarioId}
     */
    @PostMapping("/apply/{scenarioId}")
    public String applyScenario(@PathVariable Long scenarioId) {
        log.info("应用 What-if 场景: {}", scenarioId);
        
        // TODO: 实际应用场景变更到生产数据
        return "场景已应用到生产系统";
    }
    
    /**
     * 获取场景列表
     * 
     * GET /api/whatif/scenarios
     */
    @GetMapping("/scenarios")
    public List<WhatIfScenario> listScenarios() {
        // TODO: 从数据库查询
        return List.of();
    }
    
    /**
     * 获取场景详情
     * 
     * GET /api/whatif/scenario/{id}
     */
    @GetMapping("/scenario/{id}")
    public WhatIfScenario getScenario(@PathVariable Long id) {
        // TODO: 从数据库查询
        return null;
    }
}
