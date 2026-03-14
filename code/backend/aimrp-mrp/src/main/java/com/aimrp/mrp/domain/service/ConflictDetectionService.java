package com.aimrp.mrp.domain.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 冲突检测服务
 * 
 * 检测生产计划中的资源冲突
 */
@Slf4j
@Service
public class ConflictDetectionService {
    
    /**
     * 检测所有冲突
     * 
     * @param plans 计划列表
     * @return 冲突列表
     */
    public List<Conflict> detectConflicts(List<ProductionPlan> plans) {
        log.info("开始冲突检测 - 计划数: {}", plans.size());
        
        List<Conflict> allConflicts = new ArrayList<>();
        
        // 1. 资源冲突检测
        allConflicts.addAll(detectResourceConflicts(plans));
        
        // 2. 物料冲突检测
        allConflicts.addAll(detectMaterialConflicts(plans));
        
        // 3. 产能冲突检测
        allConflicts.addAll(detectCapacityConflicts(plans));
        
        // 按严重程度排序
        allConflicts.sort((c1, c2) -> c2.getSeverity().compareTo(c1.getSeverity()));
        
        log.info("冲突检测完成 - 发现 {} 个冲突", allConflicts.size());
        
        return allConflicts;
    }
    
    /**
     * 资源冲突检测
     */
    private List<Conflict> detectResourceConflicts(List<ProductionPlan> plans) {
        List<Conflict> conflicts = new ArrayList<>();
        
        // 按工作中心分组
        Map<String, List<ProductionPlan>> byWorkCenter = plans.stream()
                .collect(Collectors.groupingBy(ProductionPlan::getWorkCenterCode));
        
        for (Map.Entry<String, List<ProductionPlan>> entry : byWorkCenter.entrySet()) {
            String wcCode = entry.getKey();
            List<ProductionPlan> wcPlans = entry.getValue();
            
            // 检查时间重叠
            for (int i = 0; i < wcPlans.size(); i++) {
                for (int j = i + 1; j < wcPlans.size(); j++) {
                    ProductionPlan p1 = wcPlans.get(i);
                    ProductionPlan p2 = wcPlans.get(j);
                    
                    if (isTimeOverlap(p1, p2)) {
                        Conflict conflict = new Conflict();
                        conflict.setConflictId(System.currentTimeMillis() + conflicts.size());
                        conflict.setConflictType(ConflictType.RESOURCE_CONFLICT);
                        conflict.setSeverity(calculateSeverity(p1, p2));
                        conflict.setTitle("工作中心 " + wcCode + " 存在资源冲突");
                        conflict.setDescription(String.format(
                                "计划 %s 与 %s 在 %s 时间重叠", 
                                p1.getPlanNo(), p2.getPlanNo(), wcCode));
                        conflict.setAffectedPlans(Arrays.asList(p1.getPlanNo(), p2.getPlanNo()));
                        conflict.setWorkCenterCode(wcCode);
                        
                        conflicts.add(conflict);
                    }
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 物料冲突检测
     */
    private List<Conflict> detectMaterialConflicts(List<ProductionPlan> plans) {
        List<Conflict> conflicts = new ArrayList<>();
        
        // 按物料分组
        Map<String, List<ProductionPlan>> byItem = plans.stream()
                .collect(Collectors.groupingBy(ProductionPlan::getItemCode));
        
        for (Map.Entry<String, List<ProductionPlan>> entry : byItem.entrySet()) {
            String itemCode = entry.getKey();
            List<ProductionPlan> itemPlans = entry.getValue();
            
            // 计算总需求
            BigDecimal totalQty = itemPlans.stream()
                    .map(ProductionPlan::getPlanQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 简化：假设库存不足
            BigDecimal inventory = new BigDecimal("500"); // 模拟库存
            if (totalQty.compareTo(inventory) > 0) {
                Conflict conflict = new Conflict();
                conflict.setConflictId(System.currentTimeMillis() + conflicts.size());
                conflict.setConflictType(ConflictType.MATERIAL_CONFLICT);
                conflict.setSeverity(totalQty.compareTo(inventory.multiply(BigDecimal.valueOf(2))) > 0 ? "HIGH" : "MEDIUM");
                conflict.setTitle("物料 " + itemCode + " 库存不足");
                conflict.setDescription(String.format(
                        "总需求 %s > 库存 %s", totalQty, inventory));
                conflict.setAffectedPlans(itemPlans.stream().map(ProductionPlan::getPlanNo).collect(Collectors.toList()));
                conflict.setItemCode(itemCode);
                
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    /**
     * 产能冲突检测
     */
    private List<Conflict> detectCapacityConflicts(List<ProductionPlan> plans) {
        List<Conflict> conflicts = new ArrayList<>();
        
        // 按工作中心统计产能使用
        Map<String, BigDecimal> wcUsage = new HashMap<>();
        
        for (ProductionPlan plan : plans) {
            BigDecimal hours = plan.getPlanHours() != null ? plan.getPlanHours() : BigDecimal.ZERO;
            wcUsage.merge(plan.getWorkCenterCode(), hours, BigDecimal::add);
        }
        
        // 模拟产能上限
        Map<String, BigDecimal> wcCapacity = Map.of(
                "WC01", new BigDecimal("80"),
                "WC02", new BigDecimal("80"),
                "WC03", new BigDecimal("60")
        );
        
        for (Map.Entry<String, BigDecimal> entry : wcUsage.entrySet()) {
            String wcCode = entry.getKey();
            BigDecimal usage = entry.getValue();
            BigDecimal capacity = wcCapacity.getOrDefault(wcCode, new BigDecimal("80"));
            
            if (usage.compareTo(capacity) > 0) {
                BigDecimal overload = usage.subtract(capacity);
                Conflict conflict = new Conflict();
                conflict.setConflictId(System.currentTimeMillis() + conflicts.size());
                conflict.setConflictType(ConflictType.CAPACITY_CONFLICT);
                conflict.setSeverity(overload.compareTo(capacity.multiply(BigDecimal.valueOf(0.2))) > 0 ? "HIGH" : "MEDIUM");
                conflict.setTitle("工作中心 " + wcCode + " 产能不足");
                conflict.setDescription(String.format(
                        "使用 %s 小时 > 产能 %s 小时，超负荷 %.0f%%", 
                        usage, capacity, 
                        overload.multiply(BigDecimal.valueOf(100)).divide(capacity, 0, BigDecimal.ROUND_UP)));
                conflict.setWorkCenterCode(wcCode);
                
                // 找出超负荷的计划
                List<String> affected = plans.stream()
                        .filter(p -> wcCode.equals(p.getWorkCenterCode()))
                        .map(ProductionPlan::getPlanNo)
                        .collect(Collectors.toList());
                conflict.setAffectedPlans(affected);
                
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检查时间是否重叠
     */
    private boolean isTimeOverlap(ProductionPlan p1, ProductionPlan p2) {
        if (p1.getStartDate() == null || p2.getStartDate() == null) return false;
        return p1.getStartDate().isBefore(p2.getEndDate()) && 
               p1.getEndDate().isAfter(p2.getStartDate());
    }
    
    /**
     * 计算严重程度
     */
    private String calculateSeverity(ProductionPlan p1, ProductionPlan p2) {
        // 简单计算
        return "MEDIUM";
    }
    
    // ==================== 模型类 ====================
    
    @Data
    public static class ProductionPlan {
        private String planNo;
        private String orderNo;
        private String itemCode;
        private String workCenterCode;
        private BigDecimal planQty;
        private BigDecimal planHours;
        private LocalDate startDate;
        private LocalDate endDate;
    }
    
    @Data
    public static class Conflict {
        private Long conflictId;
        private ConflictType conflictType;
        private String severity;
        private String title;
        private String description;
        private List<String> affectedPlans;
        private String workCenterCode;
        private String itemCode;
    }
    
    public enum ConflictType {
        RESOURCE_CONFLICT,
        MATERIAL_CONFLICT,
        CAPACITY_CONFLICT
    }
}
