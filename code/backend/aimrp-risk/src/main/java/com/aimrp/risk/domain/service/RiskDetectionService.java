package com.aimrp.risk.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 风险检测服务 - 物流/产能预警
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskDetectionService {
    
    /**
     * 检测物流异常
     * 
     * @return 物流风险列表
     */
    public List<RiskItem> detectLogisticsRisks() {
        log.info("执行物流风险检测");
        
        List<RiskItem> risks = new ArrayList<>();
        
        // TODO: 从物流系统或采购订单获取数据
        
        // 示例：检测在途订单延迟风险
        // List<PurchaseOrder> orders = purchaseOrderMapper.selectInTransit();
        // for (Order order : orders) {
        //     if (isDelayed(order)) {
        //         RiskItem risk = new RiskItem();
        //         risk.setRiskType("LOGISTICS");
        //         risk.setRiskLevel("HIGH");
        //         risk.setTitle("物流延迟: " + order.getPoNo());
        //         risk.setDescription("预计延迟 " + calculateDelayDays(order) + " 天");
        //         risks.add(risk);
        //     }
        // }
        
        // 模拟检测结果
        risks.add(createLogisticsRisk("物流延迟", "HIGH", "订单PO001预计延迟3天"));
        risks.add(createLogisticsRisk("物流异常", "MEDIUM", "订单PO002物流信息停滞"));
        
        return risks;
    }
    
    /**
     * 检测产能风险
     * 
     * @return 产能风险列表
     */
    public List<RiskItem> detectCapacityRisks() {
        log.info("执行产能风险检测");
        
        List<RiskItem> risks = new ArrayList<>();
        
        // TODO: 从生产系统获取数据
        
        // 1. 检查工作中心利用率
        // List<WorkCenter> workCenters = workCenterMapper.selectAll();
        // for (WorkCenter wc : workCenters) {
        //     if (wc.getUtilizationRate() > 100) {
        //         RiskItem risk = new RiskItem();
        //         risk.setRiskType("CAPACITY");
        //         risk.setRiskLevel("HIGH");
        //         risk.setTitle("产能不足: " + wc.getName());
        //         risk.setDescription("利用率" + wc.getUtilizationRate() + "%，建议调整生产计划");
        //         risks.add(risk);
        //     }
        // }
        
        // 2. 检查工单积压
        // long pendingOrders = productionOrderMapper.countByStatus("PROCESSING");
        // if (pendingOrders > threshold) {
        //     // 添加风险
        // }
        
        // 3. 检查关键设备故障风险
        // List<Equipment> criticalEquipments = equipmentMapper.selectCritical();
        // for (Equipment eq : criticalEquipments) {
        //     if (eq.getStatus().equals("MAINTENANCE")) {
        //         RiskItem risk = new RiskItem();
        //         risk.setRiskType("CAPACITY");
        //         risk.setTitle("关键设备停机: " + eq.getName());
        //         risks.add(risk);
        //     }
        // }
        
        // 模拟检测结果
        risks.add(createCapacityRisk("产能不足", "HIGH", "工作中心WC01利用率120%"));
        risks.add(createCapacityRisk("工单积压", "MEDIUM", "当前有45个工单等待排程"));
        
        return risks;
    }
    
    /**
     * 综合风险评估
     */
    public RiskAssessment assessOverallRisk() {
        log.info("执行综合风险评估");
        
        RiskAssessment assessment = new RiskAssessment();
        
        // 库存风险
        assessment.setInventoryRiskLevel("MEDIUM");
        assessment.setInventoryRiskCount(5);
        
        // 供应商风险
        assessment.setSupplierRiskLevel("LOW");
        assessment.setSupplierRiskCount(2);
        
        // 物流风险
        assessment.setLogisticsRiskLevel("MEDIUM");
        assessment.setLogisticsRiskCount(3);
        
        // 产能风险
        assessment.setCapacityRiskLevel("HIGH");
        assessment.setCapacityRiskCount(2);
        
        // 综合风险值 (0-100)
        int overallScore = calculateOverallScore(assessment);
        assessment.setOverallRiskValue(overallScore);
        
        if (overallScore >= 70) {
            assessment.setOverallRiskLevel("CRITICAL");
        } else if (overallScore >= 50) {
            assessment.setOverallRiskLevel("HIGH");
        } else if (overallScore >= 30) {
            assessment.setOverallRiskLevel("MEDIUM");
        } else {
            assessment.setOverallRiskLevel("LOW");
        }
        
        return assessment;
    }
    
    private int calculateOverallScore(RiskAssessment a) {
        int score = 0;
        
        // 各风险加权计算
        if ("HIGH".equals(a.getInventoryRiskLevel())) score += 25;
        else if ("MEDIUM".equals(a.getInventoryRiskLevel())) score += 15;
        
        if ("HIGH".equals(a.getSupplierRiskLevel())) score += 20;
        else if ("MEDIUM".equals(a.getSupplierRiskLevel())) score += 10;
        
        if ("HIGH".equals(a.getLogisticsRiskLevel())) score += 25;
        else if ("MEDIUM".equals(a.getLogisticsRiskLevel())) score += 15;
        
        if ("HIGH".equals(a.getCapacityRiskLevel())) score += 30;
        else if ("MEDIUM".equals(a.getCapacityRiskLevel())) score += 20;
        
        return Math.min(score, 100);
    }
    
    private RiskItem createLogisticsRisk(String title, String level, String description) {
        RiskItem risk = new RiskItem();
        risk.setRiskType("LOGISTICS");
        risk.setRiskLevel(level);
        risk.setTitle(title);
        risk.setDescription(description);
        risk.setStatus("ACTIVE");
        risk.setCreatedAt(LocalDateTime.now());
        return risk;
    }
    
    private RiskItem createCapacityRisk(String title, String level, String description) {
        RiskItem risk = new RiskItem();
        risk.setRiskType("CAPACITY");
        risk.setRiskLevel(level);
        risk.setTitle(title);
        risk.setDescription(description);
        risk.setStatus("ACTIVE");
        risk.setCreatedAt(LocalDateTime.now());
        return risk;
    }
    
    /**
     * 风险评估结果
     */
    @lombok.Data
    public static class RiskAssessment {
        private String overallRiskLevel;
        private Integer overallRiskValue;
        private String inventoryRiskLevel;
        private Integer inventoryRiskCount;
        private String supplierRiskLevel;
        private Integer supplierRiskCount;
        private String logisticsRiskLevel;
        private Integer logisticsRiskCount;
        private String capacityRiskLevel;
        private Integer capacityRiskCount;
    }
    
    // RiskItem简略定义（实际应引用domain model）
    @lombok.Data
    public static class RiskItem {
        private String riskType;
        private String riskLevel;
        private String title;
        private String description;
        private String status;
        private LocalDateTime createdAt;
    }
}
