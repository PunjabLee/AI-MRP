package com.aimrp.whatif.domain.service;

import com.aimrp.whatif.infrastructure.persistence.mapper.WhatIfScenarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * What-if 模拟服务（领域服务）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatIfSimulationService {
    
    private final WhatIfScenarioMapper scenarioMapper;
    
    /**
     * 执行模拟
     */
    public SimulationResult simulate(String scenarioName, Map<String, Object> parameters) {
        log.info("执行What-if模拟 - scenario: {}", scenarioName);
        
        SimulationResult result = new SimulationResult();
        result.setScenarioName(scenarioName);
        
        // 模拟计算
        Map<String, Object> results = calculate(parameters);
        result.setResults(results);
        
        // 对比基线
        result.setComparison(compareWithBaseline(parameters));
        
        // 持久化场景
        saveScenario(scenarioName, parameters, results);
        
        return result;
    }
    
    /**
     * 计算
     */
    private Map<String, Object> calculate(Map<String, Object> parameters) {
        Map<String, Object> output = new HashMap<>();
        
        Object demand = parameters.get("demand");
        Object price = parameters.get("price");
        
        if (demand != null && price != null) {
            double d = ((Number) demand).doubleValue();
            double p = ((Number) price).doubleValue();
            output.put("revenue", d * p);
            output.put("profit", d * p * 0.2);
        }
        
        return output;
    }
    
    /**
     * 对比基线
     */
    private Map<String, Object> compareWithBaseline(Map<String, Object> parameters) {
        Map<String, Object> baseline = new HashMap<>();
        baseline.put("demand", 100);
        baseline.put("price", 10);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("baseline", baseline);
        comparison.put("current", parameters);
        
        return comparison;
    }
    
    /**
     * 保存场景到数据库
     */
    private void saveScenario(String name, Map<String, Object> parameters, Map<String, Object> results) {
        try {
            String paramsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(parameters);
            String resultsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(results);
            
            Long id = scenarioMapper.insertScenario(name, "system", "", paramsJson);
            scenarioMapper.updateScenario(id, paramsJson, resultsJson);
            
            log.info("保存What-if场景到数据库: {}, id: {}", name, id);
        } catch (Exception e) {
            log.error("保存场景失败: {}", e.getMessage());
        }
    }
    
    /**
     * 对比场景
     */
    public Map<String, Object> compare(String scenarioId1, String scenarioId2) {
        var s1 = scenarioMapper.selectScenarioById(Long.parseLong(scenarioId1));
        var s2 = scenarioMapper.selectScenarioById(Long.parseLong(scenarioId2));
        
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("场景不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("scenario1", s1.get("parameters"));
        result.put("scenario2", s2.get("parameters"));
        
        return result;
    }
    
    /**
     * 获取所有场景
     */
    public List<Scenario> listScenarios() {
        var scenarios = scenarioMapper.selectScenarios(null, null);
        List<Scenario> result = new ArrayList<>();
        
        for (var row : scenarios) {
            Scenario s = new Scenario();
            s.setId(((Number) row.get("id")).longValue());
            s.setName((String) row.get("scenario_name"));
            s.setCreatedAt((java.util.Date) row.get("created_at"));
            result.add(s);
        }
        
        return result;
    }
    
    @Data
    public static class SimulationResult {
        private String scenarioName;
        private Map<String, Object> results;
        private Map<String, Object> comparison;
    }
    
    @Data
    public static class Scenario {
        private Long id;
        private String name;
        private Map<String, Object> parameters;
        private Map<String, Object> results;
        private Date createdAt;
    }
}
