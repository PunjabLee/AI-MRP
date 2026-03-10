package com.aimrp.whatif.application;

import com.aimrp.whatif.domain.model.WhatIfScenario;
import com.aimrp.whatif.domain.model.WhatIfResult;
import com.aimrp.whatif.domain.service.WhatIfSimulationService;
import com.aimrp.whatif.domain.service.WhatIfApplicationService;
import com.aimrp.whatif.infrastructure.persistence.mapper.WhatIfScenarioMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What-if 模拟 API
 */
@Slf4j
@RestController
@RequestMapping("/api/whatif")
@RequiredArgsConstructor
public class WhatIfController {
    
    private final WhatIfSimulationService simulationService;
    private final WhatIfScenarioMapper scenarioMapper;
    private final WhatIfApplicationService applicationService;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建场景
     * 
     * POST /api/whatif/scenario
     */
    @PostMapping("/scenario")
    public Map<String, Object> createScenario(@RequestBody WhatIfScenario request) {
        log.info("创建 What-if 场景: {}", request.getScenarioName());
        
        Long scenarioId = System.currentTimeMillis();
        
        WhatIfScenario scenario = new WhatIfScenario();
        scenario.setScenarioId(scenarioId);
        scenario.setScenarioName(request.getScenarioName());
        scenario.setDescription(request.getDescription());
        scenario.setScenarioType(request.getScenarioType());
        scenario.setBaselineId(request.getBaselineId());
        scenario.setChanges(request.getChanges());
        scenario.setStatus(WhatIfScenario.ScenarioStatus.DRAFT);
        scenario.setCreatedAt(java.time.LocalDate.now());
        
        // 保存到数据库
        try {
            scenario.setChangesJson(objectMapper.writeValueAsString(request.getChanges()));
        } catch (JsonProcessingException e) {
            log.warn("序列化changes失败", e);
        }
        
        scenarioMapper.insertScenario(scenario);
        
        Map<String, Object> result = new HashMap<>();
        result.put("scenarioId", scenarioId);
        result.put("message", "场景创建成功");
        
        return result;
    }
    
    /**
     * 运行模拟
     * 
     * POST /api/whatif/simulate
     */
    @PostMapping("/simulate")
    public Map<String, Object> simulate(@RequestBody WhatIfScenario scenario) {
        log.info("执行 What-if 模拟: {}", scenario.getScenarioName());
        
        // 更新状态为运行中
        scenarioMapper.updateStatus(scenario.getScenarioId(), "RUNNING");
        
        // 执行模拟
        WhatIfResult result = simulationService.simulate(scenario);
        
        // 更新状态为完成
        scenarioMapper.updateStatus(scenario.getScenarioId(), "COMPLETED");
        
        // 保存结果到数据库（可选）
        
        Map<String, Object> response = new HashMap<>();
        response.put("scenarioId", scenario.getScenarioId());
        response.put("status", "COMPLETED");
        response.put("result", result);
        
        return response;
    }
    
    /**
     * 对比场景
     * 
     * POST /api/whatif/compare
     */
    @PostMapping("/compare")
    public Map<String, Object> compare(@RequestBody List<WhatIfScenario> scenarios) {
        log.info("对比 What-if 场景: {} 个", scenarios.size());
        
        // 更新所有场景状态
        scenarios.forEach(s -> scenarioMapper.updateStatus(s.getScenarioId(), "COMPARING"));
        
        // 执行对比
        List<WhatIfResult> results = simulationService.compareScenarios(scenarios);
        
        // 更新状态
        scenarios.forEach(s -> scenarioMapper.updateStatus(s.getScenarioId(), "COMPARED"));
        
        // 保存场景列表
        for (WhatIfScenario scenario : scenarios) {
            try {
                scenario.setChangesJson(objectMapper.writeValueAsString(scenario.getChanges()));
            } catch (JsonProcessingException e) {
                log.warn("序列化失败", e);
            }
            scenarioMapper.insertScenario(scenario);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("scenarios", scenarios);
        response.put("results", results);
        response.put("comparison", buildComparison(results));
        
        return response;
    }
    
    /**
     * 构建对比分析
     */
    private Map<String, Object> buildComparison(List<WhatIfResult> results) {
        Map<String, Object> comparison = new HashMap<>();
        
        if (results.isEmpty()) {
            return comparison;
        }
        
        // 找出最优方案
        WhatIfResult best = results.stream()
                .min((r1, r2) -> r1.getTotalImpactScore().compareTo(r2.getTotalImpactScore()))
                .orElse(results.get(0));
        
        comparison.put("recommendedScenarioId", best.getScenarioId());
        comparison.put("recommendedScenarioName", best.getScenarioName());
        
        return comparison;
    }
    
    /**
     * 应用场景
     * 
     * POST /api/whatif/apply/{scenarioId}
     */
    @PostMapping("/apply/{scenarioId}")
    public Map<String, Object> applyScenario(@PathVariable Long scenarioId) {
        log.info("应用 What-if 场景: {}", scenarioId);
        
        // 查询场景
        WhatIfScenario scenario = scenarioMapper.selectByScenarioId(scenarioId);
        if (scenario == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "场景不存在");
            return error;
        }
        
        // 检查场景状态
        if ("APPLIED".equals(scenario.getStatus())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "场景已应用，不能重复应用");
            return error;
        }
        
        // 应用场景变更到生产系统
        WhatIfApplicationService.ApplyResult result = applicationService.applyToProduction(
                scenarioId, scenario.getChanges());
        
        // 更新状态
        if (result.isSuccess()) {
            scenarioMapper.updateStatus(scenarioId, "APPLIED");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("appliedCount", result.getAppliedCount());
        response.put("failedCount", result.getFailedCount());
        response.put("details", result.getDetails());
        
        return response;
    }
    
    /**
     * 预览应用效果
     * 
     * POST /api/whatif/preview/{scenarioId}
     */
    @PostMapping("/preview/{scenarioId}")
    public Map<String, Object> previewScenario(@PathVariable Long scenarioId) {
        log.info("预览 What-if 场景: {}", scenarioId);
        
        // 查询场景
        WhatIfScenario scenario = scenarioMapper.selectByScenarioId(scenarioId);
        if (scenario == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "场景不存在");
            return error;
        }
        
        // 预览影响
        WhatIfApplicationService.PreviewResult preview = applicationService.preview(
                scenarioId, scenario.getChanges());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("scenarioId", preview.getScenarioId());
        response.put("changeCount", preview.getChangeCount());
        response.put("impacts", preview.getImpacts());
        response.put("estimatedTime", preview.getEstimatedTime());
        
        return response;
    }
    
    /**
     * 获取场景列表
     * 
     * GET /api/whatif/scenarios
     */
    @GetMapping("/scenarios")
    public Map<String, Object> listScenarios(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<WhatIfScenario> scenarios;
        
        if (status != null) {
            scenarios = scenarioMapper.selectByStatus(status);
        } else {
            scenarios = scenarioMapper.selectRecent(limit);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("scenarios", scenarios);
        result.put("total", scenarios.size());
        
        return result;
    }
    
    /**
     * 获取场景详情
     * 
     * GET /api/whatif/scenario/{id}
     */
    @GetMapping("/scenario/{id}")
    public Map<String, Object> getScenario(@PathVariable Long id) {
        WhatIfScenario scenario = scenarioMapper.selectByScenarioId(id);
        
        Map<String, Object> result = new HashMap<>();
        
        if (scenario == null) {
            result.put("success", false);
            result.put("message", "场景不存在");
            return result;
        }
        
        result.put("success", true);
        result.put("scenario", scenario);
        
        return result;
    }
    
    /**
     * 删除场景
     * 
     * DELETE /api/whatif/scenario/{id}
     */
    @DeleteMapping("/scenario/{id}")
    public Map<String, Object> deleteScenario(@PathVariable Long id) {
        int rows = scenarioMapper.deleteByScenarioId(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        
        return result;
    }
}
