package com.aimrp.whatif.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What-if 模拟服务（领域服务）
 */
@Slf4j
@Service
public class WhatIfSimulationService {
    
    /**
     * 场景存储（内存 + TODO: 数据库持久化）
     */
    private final Map<String, Scenario> scenarios = new ConcurrentHashMap<>();
    
    /**
     * 执行模拟
     */
    public SimulationResult simulate(String scenarioName, Map<String, Object> parameters) {
        log.info("执行What-if模拟 - scenario: {}", scenarioName);
        
        SimulationResult result = new SimulationResult();
        result.setScenarioName(scenarioName);
        
        // 模拟计算（简化版）
        Map<String, Object> results = new HashMap<>();
        results.put("input", parameters);
        results.put("output", calculate(parameters));
        results.put("timestamp", System.currentTimeMillis());
        
        result.setResults(results);
        
        // 对比基线
        result.setComparison(compareWithBaseline(parameters));
        
        // 保存场景
        saveScenario(scenarioName, parameters);
        
        return result;
    }
    
    /**
     * 计算（简化版）
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
        
        Map<String, Object> current = parameters;
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("baseline", baseline);
        comparison.put("current", current);
        comparison.put("delta", "需要计算");
        
        return comparison;
    }
    
    /**
     * 保存场景
     */
    private void saveScenario(String name, Map<String, Object> parameters) {
        String id = UUID.randomUUID().toString();
        Scenario scenario = new Scenario();
        scenario.setId(id);
        scenario.setName(name);
        scenario.setParameters(parameters);
        scenario.setCreatedAt(new Date());
        
        scenarios.put(id, scenario);
        log.info("保存What-if场景: {}, id: {}", name, id);
        
        // TODO: 持久化到数据库
    }
    
    /**
     * 对比场景
     */
    public Map<String, Object> compare(String scenarioId1, String scenarioId2) {
        Scenario s1 = scenarios.get(scenarioId1);
        Scenario s2 = scenarios.get(scenarioId2);
        
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("场景不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("scenario1", s1.getResults());
        result.put("scenario2", s2.getResults());
        
        return result;
    }
    
    /**
     * 获取所有场景
     */
    public List<Scenario> listScenarios() {
        return new ArrayList<>(scenarios.values());
    }
    
    // 内部类
    @lombok.Data
    public static class SimulationResult {
        private String scenarioName;
        private Map<String, Object> results;
        private Map<String, Object> comparison;
    }
    
    @lombok.Data
    public static class Scenario {
        private String id;
        private String name;
        private Map<String, Object> parameters;
        private Map<String, Object> results;
        private Date createdAt;
    }
}
