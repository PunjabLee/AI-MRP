package com.aimrp.mrp.domain.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 成本影响分析服务
 * 
 * 分析计划变更对成本的影响
 */
@Slf4j
@Service
public class CostImpactAnalysisService {
    
    /**
     * 分析成本影响
     * 
     * @param scenario 场景（订单变更、物料变更等）
     * @return 成本影响分析结果
     */
    public CostImpactResult analyze(CostAnalysisScenario scenario) {
        log.info("开始成本影响分析 - 场景: {}", scenario.getScenarioName());
        
        CostImpactResult result = new CostImpactResult();
        result.setScenarioName(scenario.getScenarioName());
        result.setAnalyzedDate(LocalDate.now().toString());
        
        // 1. 计算直接成本变化
        BigDecimal directCostChange = calculateDirectCostChange(scenario);
        result.setDirectCostChange(directCostChange);
        
        // 2. 计算间接成本变化
        BigDecimal indirectCostChange = calculateIndirectCostChange(scenario);
        result.setIndirectCostChange(indirectCostChange);
        
        // 3. 计算总成本变化
        BigDecimal totalCostChange = directCostChange.add(indirectCostChange);
        result.setTotalCostChange(totalCostChange);
        
        // 4. 成本明细
        result.setCostDetails(generateCostDetails(scenario));
        
        // 5. 成本优化建议
        result.setRecommendations(generateRecommendations(result));
        
        // 6. 投资回报分析
        result.setRoiAnalysis(calculateRoi(scenario, totalCostChange));
        
        log.info("成本影响分析完成 - 总成本变化: {}", totalCostChange);
        
        return result;
    }
    
    /**
     * 计算直接成本变化
     */
    private BigDecimal calculateDirectCostChange(CostAnalysisScenario scenario) {
        BigDecimal total = BigDecimal.ZERO;
        
        // 物料成本变化
        for (CostAnalysisScenario.ItemChange item : scenario.getItemChanges()) {
            BigDecimal qtyChange = new BigDecimal(item.getQtyChange());
            BigDecimal unitCost = item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO;
            total = total.add(qtyChange.multiply(unitCost));
        }
        
        // 人工成本变化
        if (scenario.getLaborHoursChange() != null) {
            BigDecimal laborCost = scenario.getLaborHoursChange()
                    .multiply(scenario.getHourlyLaborRate() != null ? scenario.getHourlyLaborRate() : BigDecimal.valueOf(50));
            total = total.add(laborCost);
        }
        
        // 设备成本变化
        if (scenario.getMachineHoursChange() != null) {
            BigDecimal machineCost = scenario.getMachineHoursChange()
                    .multiply(scenario.getHourlyMachineRate() != null ? scenario.getHourlyMachineRate() : BigDecimal.valueOf(100));
            total = total.add(machineCost);
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算间接成本变化
     */
    private BigDecimal calculateIndirectCostChange(CostAnalysisScenario scenario) {
        BigDecimal total = BigDecimal.ZERO;
        
        // 运输成本
        if (scenario.getShippingCostChange() != null) {
            total = total.add(scenario.getShippingCostChange());
        }
        
        // 库存持有成本（基于库存变化）
        if (scenario.getInventoryValueChange() != null) {
            // 假设库存持有成本率 20%/年
            BigDecimal holdingCostRate = new BigDecimal("0.20");
            BigDecimal holdingCost = scenario.getInventoryValueChange()
                    .multiply(holdingCostRate)
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP); // 月度
            total = total.add(holdingCost);
        }
        
        // 紧急采购加急费
        if (scenario.isEmergencyPurchase()) {
            BigDecimal urgentFee = calculateDirectCostChange(scenario).multiply(new BigDecimal("0.1"));
            total = total.add(urgentFee);
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 生成成本明细
     */
    private List<CostDetail> generateCostDetails(CostAnalysisScenario scenario) {
        List<CostDetail> details = new ArrayList<>();
        
        // 物料成本明细
        for (CostAnalysisScenario.ItemChange item : scenario.getItemChanges()) {
            CostDetail detail = new CostDetail();
            detail.setCategory("物料成本");
            detail.setItem(item.getItemCode());
            detail.setQuantityChange(new BigDecimal(item.getQtyChange()));
            detail.setUnitCost(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
            detail.setCostChange(new BigDecimal(item.getQtyChange())
                    .multiply(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP));
            details.add(detail);
        }
        
        // 人工成本
        if (scenario.getLaborHoursChange() != null) {
            CostDetail detail = new CostDetail();
            detail.setCategory("人工成本");
            detail.setItem("直接人工");
            detail.setQuantityChange(scenario.getLaborHoursChange());
            detail.setUnitCost(scenario.getHourlyLaborRate() != null ? scenario.getHourlyLaborRate() : BigDecimal.valueOf(50));
            detail.setCostChange(scenario.getLaborHoursChange()
                    .multiply(scenario.getHourlyLaborRate() != null ? scenario.getHourlyLaborRate() : BigDecimal.valueOf(50))
                    .setScale(2, RoundingMode.HALF_UP));
            details.add(detail);
        }
        
        // 设备成本
        if (scenario.getMachineHoursChange() != null) {
            CostDetail detail = new CostDetail();
            detail.setCategory("设备成本");
            detail.setItem("设备折旧");
            detail.setQuantityChange(scenario.getMachineHoursChange());
            detail.setUnitCost(scenario.getHourlyMachineRate() != null ? scenario.getHourlyMachineRate() : BigDecimal.valueOf(100));
            detail.setCostChange(scenario.getMachineHoursChange()
                    .multiply(scenario.getHourlyMachineRate() != null ? scenario.getHourlyMachineRate() : BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP));
            details.add(detail);
        }
        
        // 运输成本
        if (scenario.getShippingCostChange() != null) {
            CostDetail detail = new CostDetail();
            detail.setCategory("运输成本");
            detail.setItem("物流运输");
            detail.setCostChange(scenario.getShippingCostChange());
            details.add(detail);
        }
        
        return details;
    }
    
    /**
     * 生成成本优化建议
     */
    private List<String> generateRecommendations(CostImpactResult result) {
        List<String> recommendations = new ArrayList<>();
        
        if (result.getTotalCostChange().compareTo(BigDecimal.ZERO) > 0) {
            // 成本增加
            recommendations.add("成本增加 " + result.getTotalCostChange() + " 元，建议评估是否必要");
            
            if (result.getIndirectCostChange().compareTo(result.getDirectCostChange().multiply(new BigDecimal("0.3"))) > 0) {
                recommendations.add("间接成本占比较高，建议优化库存和物流");
            }
            
            if (result.getDirectCostChange().compareTo(BigDecimal.ZERO) > 0) {
                recommendations.add("可直接与供应商协商降低采购成本");
            }
        } else {
            // 成本降低
            recommendations.add("成本降低 " + result.getTotalCostChange().abs() + " 元，方案可行");
        }
        
        return recommendations;
    }
    
    /**
     * 计算投资回报
     */
    private RoiAnalysis calculateRoi(CostAnalysisScenario scenario, BigDecimal totalCostChange) {
        RoiAnalysis roi = new RoiAnalysis();
        
        // 假设投资回报基于节省
        BigDecimal benefit = scenario.getExpectedBenefit() != null ? scenario.getExpectedBenefit() : BigDecimal.ZERO;
        
        if (benefit.compareTo(BigDecimal.ZERO) > 0 && totalCostChange.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal roiPercent = benefit.subtract(totalCostChange)
                    .divide(totalCostChange.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            roi.setRoiPercent(roiPercent);
        } else {
            roi.setRoiPercent(BigDecimal.ZERO);
        }
        
        roi.setPaybackPeriodMonths(calculatePaybackPeriod(benefit, totalCostChange));
        roi.setBenefit(benefit);
        roi.setCost(totalCostChange);
        
        return roi;
    }
    
    private int calculatePaybackPeriod(BigDecimal benefit, BigDecimal cost) {
        if (benefit.compareTo(BigDecimal.ZERO) <= 0) return -1;
        // 假设月度节省
        return cost.divide(benefit, 0, BigDecimal.ROUND_UP).intValue();
    }
    
    // ==================== 模型类 ====================
    
    @Data
    public static class CostAnalysisScenario {
        private String scenarioName;
        private String description;
        private List<ItemChange> itemChanges;
        private BigDecimal laborHoursChange;
        private BigDecimal hourlyLaborRate;
        private BigDecimal machineHoursChange;
        private BigDecimal hourlyMachineRate;
        private BigDecimal shippingCostChange;
        private BigDecimal inventoryValueChange;
        private boolean emergencyPurchase;
        private BigDecimal expectedBenefit;
        
        @Data
        public static class ItemChange {
            private String itemCode;
            private String qtyChange; // 变化数量
            private BigDecimal unitCost;
        }
    }
    
    @Data
    public static class CostImpactResult {
        private String scenarioName;
        private String analyzedDate;
        private BigDecimal directCostChange;
        private BigDecimal indirectCostChange;
        private BigDecimal totalCostChange;
        private List<CostDetail> costDetails;
        private List<String> recommendations;
        private RoiAnalysis roiAnalysis;
        
        @Data
        public static class CostDetail {
            private String category;
            private String item;
            private BigDecimal quantityChange;
            private BigDecimal unitCost;
            private BigDecimal costChange;
        }
        
        @Data
        public static class RoiAnalysis {
            private BigDecimal roiPercent;
            private Integer paybackPeriodMonths;
            private BigDecimal benefit;
            private BigDecimal cost;
        }
    }
}
