package com.aimrp.mrp.domain.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 插单影响分析服务
 * 
 * 评估插入新订单对现有计划的影响
 */
@Slf4j
@Service
public class ImpactAnalysisService {
    
    /**
     * 分析插入订单的影响
     * 
     * @param newOrder 新订单
     * @param existingPlans 现有计划
     * @return 影响分析结果
     */
    public ImpactResult analyzeOrderImpact(NewOrder newOrder, List<ExistingPlan> existingPlans) {
        log.info("分析插单影响 - 新订单: {}, 现有计划数: {}", newOrder.getOrderNo(), existingPlans.size());
        
        ImpactResult result = new ImpactResult();
        result.setNewOrderNo(newOrder.getOrderNo());
        
        List<ImpactDetail> details = new ArrayList<>();
        
        for (ExistingPlan plan : existingPlans) {
            ImpactDetail detail = new ImpactDetail();
            detail.setAffectedPlanNo(plan.getPlanNo());
            
            // 1. 检查资源冲突
            boolean hasConflict = checkResourceConflict(newOrder, plan);
            detail.setHasResourceConflict(hasConflict);
            
            // 2. 计算交期影响
            int delayDays = calculateDelayImpact(newOrder, plan);
            detail.setDelayDays(delayDays);
            
            // 3. 计算成本影响
            BigDecimal costImpact = calculateCostImpact(newOrder, plan);
            detail.setCostImpact(costImpact);
            
            // 4. 检查库存影响
            boolean inventoryImpact = checkInventoryImpact(newOrder, plan);
            detail.setHasInventoryImpact(inventoryImpact);
            
            // 5. 风险评估
            String riskLevel = assessRisk(detail);
            detail.setRiskLevel(riskLevel);
            
            details.add(detail);
        }
        
        // 汇总结果
        result.setDetails(details);
        result.setTotalAffected(details.size());
        result.setHighRiskCount((int) details.stream().filter(d -> "HIGH".equals(d.getRiskLevel())).count());
        result.setDelayDays(details.stream().mapToInt(ImpactDetail::getDelayDays).max().orElse(0));
        result.setTotalCostImpact(details.stream().map(ImpactDetail::getCostImpact).reduce(BigDecimal.ZERO, BigDecimal::add));
        
        log.info("影响分析完成 - 影响计划数: {}, 高风险: {}, 最大延期: {}天", 
                result.getTotalAffected(), result.getHighRiskCount(), result.getDelayDays());
        
        return result;
    }
    
    /**
     * 检查资源冲突
     */
    private boolean checkResourceConflict(NewOrder newOrder, ExistingPlan plan) {
        // 检查工作中心是否冲突
        if (newOrder.getWorkCenterCode() != null && 
            newOrder.getWorkCenterCode().equals(plan.getWorkCenterCode())) {
            // 检查时间是否重叠
            if (newOrder.getStartDate().isBefore(plan.getEndDate()) &&
                newOrder.getEndDate().isAfter(plan.getStartDate())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 计算延期影响
     */
    private int calculateDelayImpact(NewOrder newOrder, ExistingPlan plan) {
        // 如果有资源冲突，可能导致延期
        if (checkResourceConflict(newOrder, plan)) {
            // 简单计算：基于订单数量和产能
            BigDecimal hoursNeeded = newOrder.getQty().multiply(newOrder.getStdHoursPerUnit());
            BigDecimal availableHours = plan.getAvailableHoursPerDay();
            
            int additionalDays = hoursNeeded.divide(availableHours, 0, BigDecimal.ROUND_UP).intValue();
            return additionalDays;
        }
        return 0;
    }
    
    /**
     * 计算成本影响
     */
    private BigDecimal calculateCostImpact(NewOrder newOrder, ExistingPlan plan) {
        BigDecimal baseCost = newOrder.getQty().multiply(newOrder.getUnitCost());
        
        // 加班成本
        if (calculateDelayImpact(newOrder, plan) > 0) {
            baseCost = baseCost.multiply(new BigDecimal("1.2")); // 20% 加班费
        }
        
        // 急单加急费
        baseCost = baseCost.multiply(new BigDecimal("1.1")); // 10% 加急费
        
        return baseCost.subtract(newOrder.getQty().multiply(newOrder.getUnitCost()));
    }
    
    /**
     * 检查库存影响
     */
    private boolean checkInventoryImpact(NewOrder newOrder, ExistingPlan plan) {
        // 检查新订单所需的物料是否足够
        // 简化：假设需要检查物料库存
        return true;
    }
    
    /**
     * 风险评估
     */
    private String assessRisk(ImpactDetail detail) {
        int riskScore = 0;
        
        if (detail.isHasResourceConflict()) riskScore += 40;
        if (detail.getDelayDays() > 5) riskScore += 30;
        else if (detail.getDelayDays() > 0) riskScore += 15;
        
        if (detail.getCostImpact().compareTo(BigDecimal.valueOf(10000)) > 0) riskScore += 20;
        if (detail.isHasInventoryImpact()) riskScore += 10;
        
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 30) return "MEDIUM";
        return "LOW";
    }
    
    // ==================== 模型类 ====================
    
    @Data
    public static class NewOrder {
        private String orderNo;
        private String itemCode;
        private BigDecimal qty;
        private LocalDate requiredDate;
        private LocalDate startDate;
        private LocalDate endDate;
        private String workCenterCode;
        private BigDecimal stdHoursPerUnit;
        private BigDecimal unitCost;
    }
    
    @Data
    public static class ExistingPlan {
        private String planNo;
        private String orderNo;
        private String workCenterCode;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal availableHoursPerDay;
    }
    
    @Data
    public static class ImpactResult {
        private String newOrderNo;
        private int totalAffected;
        private int highRiskCount;
        private int delayDays;
        private BigDecimal totalCostImpact;
        private List<ImpactDetail> details;
    }
    
    @Data
    public static class ImpactDetail {
        private String affectedPlanNo;
        private boolean hasResourceConflict;
        private int delayDays;
        private BigDecimal costImpact;
        private boolean hasInventoryImpact;
        private String riskLevel;
    }
}
