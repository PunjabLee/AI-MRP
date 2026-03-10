package com.aimrp.risk.domain.service;

import com.aimrp.notification.domain.service.NotificationService;
import com.aimrp.risk.domain.model.RiskItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 风险监控服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskMonitorService {
    
    private final NotificationService notificationService;
    private final RiskConfigService configService;
    
    /**
     * 扫描所有风险
     * 
     * @return 风险列表
     */
    public List<RiskItem> scanAllRisks() {
        log.info("开始风险扫描...");
        
        List<RiskItem> allRisks = new ArrayList<>();
        
        // 1. 库存风险检测
        allRisks.addAll(scanInventoryRisks());
        
        // 2. 供应商风险检测
        allRisks.addAll(scanSupplierRisks());
        
        // 3. 需求风险检测
        allRisks.addAll(scanDemandRisks());
        
        // 4. 生产风险检测
        allRisks.addAll(scanProductionRisks());
        
        // 按风险等级排序
        allRisks.sort((r1, r2) -> r2.getRiskLevel().compareTo(r1.getRiskLevel()));
        
        log.info("风险扫描完成，发现 {} 个风险", allRisks.size());
        
        return allRisks;
    }
    
    /**
     * 扫描库存风险
     */
    private List<RiskItem> scanInventoryRisks() {
        List<RiskItem> risks = new ArrayList<>();
        
        // 模拟库存数据
        List<Map<String, Object>> inventoryData = getMockInventoryData();
        
        for (Map<String, Object> item : inventoryData) {
            String itemCode = (String) item.get("itemCode");
            BigDecimal onHandQty = new BigDecimal(item.get("onHandQty").toString());
            BigDecimal safetyStock = new BigDecimal(item.get("safetyStock").toString());
            BigDecimal avgDemand = new BigDecimal(item.get("avgDailyDemand").toString());
            
            // 计算可用天数
            BigDecimal availableDays = onHandQty.divide(avgDemand, 2, RoundingMode.HALF_UP);
            
            // 库存低于安全库存
            if (onHandQty.compareTo(safetyStock) < 0) {
                RiskItem risk = new RiskItem();
                risk.setRiskId(System.currentTimeMillis() + risks.size());
                risk.setRiskCode("INV-" + itemCode);
                risk.setRiskType(RiskItem.RiskType.INVENTORY_SHORTAGE);
                risk.setTitle("库存不足：" + itemCode);
                risk.setDescription(String.format("物料 %s 库存 %.0f 低于安全库存 %.0f", 
                        itemCode, onHandQty, safetyStock));
                risk.setRelatedType("ITEM");
                risk.setRelatedCode(itemCode);
                
                // 计算风险值
                BigDecimal riskValue = safetyStock.subtract(onHandQty)
                        .divide(safetyStock, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                risk.setRiskValue(riskValue);
                
                // 设置风险等级
                if (riskValue.compareTo(BigDecimal.valueOf(80)) > 0) {
                    risk.setRiskLevel(RiskItem.RiskLevel.CRITICAL);
                } else if (riskValue.compareTo(BigDecimal.valueOf(50)) > 0) {
                    risk.setRiskLevel(RiskItem.RiskLevel.HIGH);
                } else {
                    risk.setRiskLevel(RiskItem.RiskLevel.MEDIUM);
                }
                
                risk.setSuggestedAction("建议立即采购或调整生产计划");
                risk.setStatus(RiskItem.RiskStatus.ACTIVE);
                risk.setDetectedAt(LocalDateTime.now());
                
                risks.add(risk);
            }
            
            // 可用天数低于阈值
            if (availableDays.compareTo(BigDecimal.valueOf(7)) < 0) {
                RiskItem risk = new RiskItem();
                risk.setRiskId(System.currentTimeMillis() + risks.size());
                risk.setRiskCode("INV-DAYS-" + itemCode);
                risk.setRiskType(RiskItem.RiskType.INVENTORY_SHORTAGE);
                risk.setTitle("库存可用天数不足：" + itemCode);
                risk.setDescription(String.format("物料 %s 可用天数仅 %.1f 天", itemCode, availableDays));
                risk.setRelatedType("ITEM");
                risk.setRelatedCode(itemCode);
                risk.setRiskValue(BigDecimal.valueOf(100).subtract(availableDays.multiply(BigDecimal.valueOf(10))));
                risk.setRiskLevel(RiskItem.RiskLevel.HIGH);
                risk.setSuggestedAction("建议补充库存");
                risk.setStatus(RiskItem.RiskStatus.MONITORING);
                risk.setDetectedAt(LocalDateTime.now());
                
                risks.add(risk);
            }
        }
        
        return risks;
    }
    
    /**
     * 扫描供应商风险
     */
    private List<RiskItem> scanSupplierRisks() {
        List<RiskItem> risks = new ArrayList<>();
        
        // 模拟供应商数据
        List<Map<String, Object>> supplierData = getMockSupplierData();
        
        for (Map<String, Object> supplier : supplierData) {
            String supplierCode = (String) supplier.get("supplierCode");
            Integer delayDays = (Integer) supplier.get("avgDelayDays");
            Double onTimeRate = (Double) supplier.get("onTimeRate");
            
            // 延迟天数超过阈值
            if (delayDays > 3) {
                RiskItem risk = new RiskItem();
                risk.setRiskId(System.currentTimeMillis() + risks.size());
                risk.setRiskCode("SUP-" + supplierCode);
                risk.setRiskType(RiskItem.RiskType.SUPPLIER_DELAY);
                risk.setTitle("供应商交期延迟：" + supplierCode);
                risk.setDescription(String.format("供应商 %s 平均延迟 %d 天", supplierCode, delayDays));
                risk.setRelatedType("SUPPLIER");
                risk.setRelatedCode(supplierCode);
                risk.setRiskValue(BigDecimal.valueOf(delayDays * 10).min(BigDecimal.valueOf(100)));
                
                if (delayDays > 7) {
                    risk.setRiskLevel(RiskItem.RiskLevel.CRITICAL);
                } else if (delayDays > 5) {
                    risk.setRiskLevel(RiskItem.RiskLevel.HIGH);
                } else {
                    risk.setRiskLevel(RiskItem.RiskLevel.MEDIUM);
                }
                
                risk.setSuggestedAction("建议寻找替代供应商或提前备货");
                risk.setStatus(RiskItem.RiskStatus.ACTIVE);
                risk.setDetectedAt(LocalDateTime.now());
                
                risks.add(risk);
            }
            
            // 准时率低于阈值
            if (onTimeRate < 0.85) {
                RiskItem risk = new RiskItem();
                risk.setRiskId(System.currentTimeMillis() + risks.size());
                risk.setRiskCode("SUP-RATE-" + supplierCode);
                risk.setRiskType(RiskItem.RiskType.SUPPLIER_DELAY);
                risk.setTitle("供应商准时率低：" + supplierCode);
                risk.setDescription(String.format("供应商 %s 准时率仅 %.1f%%", supplierCode, onTimeRate * 100));
                risk.setRelatedType("SUPPLIER");
                risk.setRelatedCode(supplierCode);
                risk.setRiskValue(BigDecimal.valueOf((0.85 - onTimeRate) * 500));
                risk.setRiskLevel(RiskItem.RiskLevel.MEDIUM);
                risk.setSuggestedAction("建议与供应商沟通改善");
                risk.setStatus(RiskItem.RiskStatus.MONITORING);
                risk.setDetectedAt(LocalDateTime.now());
                
                risks.add(risk);
            }
        }
        
        return risks;
    }
    
    /**
     * 扫描需求风险
     */
    private List<RiskItem> scanDemandRisks() {
        List<RiskItem> risks = new ArrayList<>();
        
        // 模拟需求突变检测
        // 实际应从销售数据计算
        
        RiskItem risk = new RiskItem();
        risk.setRiskId(System.currentTimeMillis());
        risk.setRiskCode("DEM-SURGE-001");
        risk.setRiskType(RiskItem.RiskType.DEMAND_SURGE);
        risk.setRiskLevel(RiskItem.RiskLevel.MEDIUM);
        risk.setTitle("需求突变预警");
        risk.setDescription("检测到某些物料需求突然增长，可能导致库存不足");
        risk.setRiskValue(BigDecimal.valueOf(60));
        risk.setSuggestedAction("建议密切关注库存水平，必要时启动紧急采购");
        risk.setStatus(RiskItem.RiskStatus.MONITORING);
        risk.setDetectedAt(LocalDateTime.now());
        
        // 只在模拟模式下添加
        // risks.add(risk);
        
        return risks;
    }
    
    /**
     * 扫描生产风险
     */
    private List<RiskItem> scanProductionRisks() {
        List<RiskItem> risks = new ArrayList<>();
        
        // 模拟产能风险
        RiskItem risk = new RiskItem();
        risk.setRiskId(System.currentTimeMillis());
        risk.setRiskCode("PROD-CAP-001");
        risk.setRiskType(RiskItem.RiskType.CAPACITY_SHORTAGE);
        risk.setRiskLevel(RiskItem.RiskLevel.MEDIUM);
        risk.setTitle("产能利用率过高");
        risk.setDescription("部分工作中心产能利用率超过90%，可能影响紧急订单");
        risk.setRiskValue(BigDecimal.valueOf(70));
        risk.setSuggestedAction("建议提前安排加班或外协");
        risk.setStatus(RiskItem.RiskStatus.MONITORING);
        risk.setDetectedAt(LocalDateTime.now());
        
        // risks.add(risk);
        
        return risks;
    }
    
    /**
     * 获取风险统计
     */
    public RiskStatistics getStatistics(List<RiskItem> risks) {
        RiskStatistics stats = new RiskStatistics();
        stats.setTotalCount(risks.size());
        
        // 按类型统计
        Map<RiskItem.RiskType, Long> typeCount = new HashMap<>();
        for (RiskItem risk : risks) {
            typeCount.merge(risk.getRiskType(), 1L, Long::sum);
        }
        stats.setByType(typeCount);
        
        // 按等级统计
        Map<RiskItem.RiskLevel, Long> levelCount = new HashMap<>();
        for (RiskItem risk : risks) {
            levelCount.merge(risk.getRiskLevel(), 1L, Long::sum);
        }
        stats.setByLevel(levelCount);
        
        // 计算整体风险值
        if (!risks.isEmpty()) {
            BigDecimal total = risks.stream()
                    .map(RiskItem::getRiskValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setOverallRiskValue(total.divide(
                    BigDecimal.valueOf(risks.size()), 2, RoundingMode.HALF_UP));
        }
        
        return stats;
    }
    
    /**
     * 风险统计
     */
    @lombok.Data
    public static class RiskStatistics {
        private int totalCount;
        private Map<RiskItem.RiskType, Long> byType;
        private Map<RiskItem.RiskLevel, Long> byLevel;
        private BigDecimal overallRiskValue;
    }
    
    // ==================== 模拟数据 ====================
    
    private List<Map<String, Object>> getMockInventoryData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("itemCode", "A001");
        item1.put("onHandQty", 50);
        item1.put("safetyStock", 100);
        item1.put("avgDailyDemand", 20);
        data.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("itemCode", "B002");
        item2.put("onHandQty", 200);
        item2.put("safetyStock", 80);
        item2.put("avgDailyDemand", 15);
        data.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("itemCode", "C003");
        item3.put("onHandQty", 30);
        item3.put("safetyStock", 50);
        item3.put("avgDailyDemand", 10);
        data.add(item3);
        
        return data;
    }
    
    private List<Map<String, Object>> getMockSupplierData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        Map<String, Object> sup1 = new HashMap<>();
        sup1.put("supplierCode", "SUP001");
        sup1.put("avgDelayDays", 5);
        sup1.put("onTimeRate", 0.80);
        data.add(sup1);
        
        Map<String, Object> sup2 = new HashMap<>();
        sup2.put("supplierCode", "SUP002");
        sup2.put("avgDelayDays", 2);
        sup2.put("onTimeRate", 0.95);
        data.add(sup2);
        
        return data;
    }
    
    /**
     * 发送风险预警
     */
    public void sendWarning(Long riskId) {
        log.warn("发送风险预警 - riskId: {}", riskId);
        
        // 从配置获取接收人
        List<String> recipients = configService.getWarningRecipients(null);
        String level = "HIGH";
        
        // 集成通知服务
        if (notificationService != null) {
            for (String receiver : recipients) {
                notificationService.sendRiskWarning(
                    riskId.toString(),
                    "风险预警",
                    level,
                    receiver
                );
            }
        }
        
        log.info("风险预警已发送 - riskId: {}, 接收人: {}", riskId, recipients);
    }
    
    /**
     * 批量发送风险预警
     */
    public void sendWarnings(List<Long> riskIds) {
        for (Long riskId : riskIds) {
            sendWarning(riskId);
        }
        log.info("批量发送风险预警完成，共 {} 条", riskIds.size());
    }
}
