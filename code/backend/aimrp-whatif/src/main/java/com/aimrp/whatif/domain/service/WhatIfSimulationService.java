package com.aimrp.whatif.domain.service;

import com.aimrp.whatif.domain.model.WhatIfScenario;
import com.aimrp.whatif.domain.model.WhatIfResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * What-if 模拟服务
 * 
 * 负责：
 * 1. 场景创建与管理
 * 2. 影响分析计算
 * 3. 方案对比
 */
@Slf4j
@Service
public class WhatIfSimulationService {
    
    /**
     * 执行 What-if 模拟
     * 
     * @param scenario 场景
     * @return 分析结果
     */
    public WhatIfResult simulate(WhatIfScenario scenario) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始 What-if 模拟 - 场景: {}, 类型: {}", 
                scenario.getScenarioName(), scenario.getScenarioType());
        
        WhatIfResult result = new WhatIfResult();
        result.setResultId(System.currentTimeMillis());
        result.setScenarioId(scenario.getScenarioId());
        result.setScenarioName(scenario.getScenarioName());
        result.setAnalyzedAt(LocalDate.now().toString());
        
        try {
            // 1. 分析影响
            WhatIfResult.ImpactAnalysis impactAnalysis = analyzeImpact(scenario);
            result.setImpactAnalysis(impactAnalysis);
            
            // 2. 生成建议
            List<String> recommendations = generateRecommendations(impactAnalysis, scenario);
            result.setRecommendations(recommendations);
            
            // 3. 判断是否可应用
            result.setApplicable(isApplicable(impactAnalysis));
            
            // 4. 设置计算完成
            result.setElapsedMs(System.currentTimeMillis() - startTime);
            result.setComparison(compareWithBaseline(scenario, impactAnalysis));
            
            log.info("What-if 模拟完成 - 场景: {}, 影响订单: {}, 影响物料: {}", 
                    scenario.getScenarioName(), 
                    impactAnalysis.getAffectedOrders(),
                    impactAnalysis.getAffectedItems());
            
        } catch (Exception e) {
            log.error("What-if 模拟失败", e);
            result.setApplicable(false);
        }
        
        return result;
    }
    
    /**
     * 分析影响
     */
    private WhatIfResult.ImpactAnalysis analyzeImpact(WhatIfScenario scenario) {
        WhatIfResult.ImpactAnalysis analysis = new WhatIfResult.ImpactAnalysis();
        List<WhatIfResult.ImpactAnalysis.ImpactDetail> details = new ArrayList<>();
        
        int affectedOrders = 0;
        int affectedItems = 0;
        
        // 模拟分析每个变更项
        for (WhatIfScenario.ChangeItem change : scenario.getChanges()) {
            WhatIfResult.ImpactAnalysis.ImpactDetail detail = new WhatIfResult.ImpactAnalysis.ImpactDetail();
            detail.setTargetType(change.getTargetType());
            detail.setTargetCode(change.getTargetCode());
            detail.setImpactType(change.getChangeType());
            detail.setOriginalValue(change.getOriginalValue());
            detail.setNewValue(change.getNewValue());
            
            // 根据变更类型计算影响
            String impactDesc = calculateImpact(change);
            detail.setImpactDescription(impactDesc);
            details.add(detail);
            
            // 统计
            if ("ORDER".equals(change.getTargetType())) {
                affectedOrders++;
            } else if ("ITEM".equals(change.getTargetType())) {
                affectedItems++;
            }
        }
        
        analysis.setAffectedOrders(affectedOrders);
        analysis.setAffectedItems(affectedItems);
        analysis.setDetails(details);
        
        // 模拟计算关键指标变化
        analysis.setCompletionDateChange(calculateCompletionChange(scenario));
        analysis.setTotalCostChange(calculateCostChange(scenario));
        analysis.setResourceUtilizationChange(
                BigDecimal.valueOf(Math.random() * 20 - 5)  // -5% ~ +15%
                        .setScale(2, RoundingMode.HALF_UP));
        
        return analysis;
    }
    
    /**
     * 计算单个变更的影响
     */
    private String calculateImpact(WhatIfScenario.ChangeItem change) {
        switch (change.getChangeType()) {
            case "DEMAND_CHANGE":
                return String.format("需求变更: %s -> %s，预计影响%s的供应计划", 
                        change.getOriginalValue(), change.getNewValue(), change.getTargetCode());
                
            case "ORDER_PRIORITY":
                return String.format("优先级调整: %s -> %s，可能导致其他订单延期", 
                        change.getOriginalValue(), change.getNewValue());
                
            case "LEAD_TIME_CHANGE":
                return String.format("交期变更: %s -> %s，需要重新计算MRP", 
                        change.getOriginalValue(), change.getNewValue());
                
            case "INVENTORY_CHANGE":
                return String.format("库存变化: %s -> %s，影响可用量", 
                        change.getOriginalValue(), change.getNewValue());
                
            default:
                return "其他变更类型";
        }
    }
    
    /**
     * 计算完工时间变化
     */
    private String calculateCompletionChange(WhatIfScenario scenario) {
        // 模拟计算
        int days = (int) (Math.random() * 10 - 3); // -3 ~ +7天
        if (days > 0) {
            return String.format("+%d 天", days);
        } else if (days < 0) {
            return String.format("%d 天", days);
        }
        return "无变化";
    }
    
    /**
     * 计算成本变化
     */
    private BigDecimal calculateCostChange(WhatIfScenario scenario) {
        // 模拟计算成本变化
        return BigDecimal.valueOf(Math.random() * 10000 - 2000)  // -2000 ~ +8000
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 生成建议
     */
    private List<String> generateRecommendations(WhatIfResult.ImpactAnalysis analysis, WhatIfScenario scenario) {
        List<String> recommendations = new ArrayList<>();
        
        // 基于影响生成建议
        if (analysis.getAffectedOrders() > 0) {
            recommendations.add(String.format("建议重新运行MRP以更新%d个受影响订单的计划", 
                    analysis.getAffectedOrders()));
        }
        
        if (analysis.getTotalCostChange().compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add("预计成本增加，建议评估是否接受当前方案");
        } else {
            recommendations.add("方案成本优化，建议采纳");
        }
        
        if (analysis.getResourceUtilizationChange().compareTo(BigDecimal.valueOf(10)) > 0) {
            recommendations.add("资源利用率显著提升，建议确认产能安排");
        }
        
        // 场景特定建议
        if (scenario.getScenarioType() == WhatIfScenario.ScenarioType.ORDER_PRIORITY) {
            recommendations.add("优先级调整可能引发冲突，建议与相关部门确认");
        }
        
        return recommendations;
    }
    
    /**
     * 判断是否可应用
     */
    private boolean isApplicable(WhatIfResult.ImpactAnalysis analysis) {
        // 简单规则：影响在可接受范围内即可应用
        return analysis.getTotalCostChange().compareTo(BigDecimal.valueOf(50000)) < 0;
    }
    
    /**
     * 与基准对比
     */
    private WhatIfResult.ComparisonResult compareWithBaseline(WhatIfScenario scenario, 
                                                               WhatIfResult.ImpactAnalysis analysis) {
        WhatIfResult.ComparisonResult comparison = new WhatIfResult.ComparisonResult();
        
        // 模拟对比结果
        boolean improved = analysis.getTotalCostChange().compareTo(BigDecimal.ZERO) < 0;
        comparison.setImproved(improved);
        
        // 改善百分比
        BigDecimal improvement = improved 
                ? BigDecimal.valueOf(Math.random() * 15 + 5)
                : BigDecimal.valueOf(Math.random() * 10);
        comparison.setImprovementPercent(improvement.setScale(2, RoundingMode.HALF_UP));
        
        // 关键指标
        Map<String, String> metrics = new HashMap<>();
        metrics.put("完工时间", analysis.getCompletionDateChange());
        metrics.put("总成本", analysis.getTotalCostChange().toString());
        metrics.put("资源利用率", analysis.getResourceUtilizationChange().toString() + "%");
        comparison.setKeyMetrics(metrics);
        
        // 主要变化
        List<String> changes = new ArrayList<>();
        if (analysis.getAffectedOrders() > 0) {
            changes.add(String.format("影响订单数: %d", analysis.getAffectedOrders()));
        }
        if (analysis.getAffectedItems() > 0) {
            changes.add(String.format("影响物料数: %d", analysis.getAffectedItems()));
        }
        comparison.setMainChanges(changes);
        
        return comparison;
    }
    
    /**
     * 方案对比（多场景对比）
     * 
     * @param scenarios 场景列表
     * @return 对比结果
     */
    public List<WhatIfResult> compareScenarios(List<WhatIfScenario> scenarios) {
        log.info("开始方案对比 - 场景数: {}", scenarios.size());
        
        List<WhatIfResult> results = new ArrayList<>();
        
        for (WhatIfScenario scenario : scenarios) {
            WhatIfResult result = simulate(scenario);
            results.add(result);
        }
        
        // 按成本排序
        results.sort((r1, r2) -> r1.getImpactAnalysis().getTotalCostChange()
                .compareTo(r2.getImpactAnalysis().getTotalCostChange()));
        
        return results;
    }
}
