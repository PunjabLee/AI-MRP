package com.aimrp.mrp.domain.service;

import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MRP 计算核心服务
 * 
 * 负责：
 * 1. 需求合并
 * 2. BOM 展开
 * 3. 净需求计算
 * 4. 计划订单生成
 */
@Slf4j
@Service
public class MrpCalculator {
    
    /**
     * 执行 MRP 计算
     * 
     * 流程：
     * 1. 准备计算上下文
     * 2. 遍历所有需求物料
     * 3. 对每个物料进行 BOM 展开
     * 4. 计算净需求
     * 5. 生成采购/生产建议
     * 6. 检测风险
     */
    public MrpResult calculate(MrpContext context) {
        long startTime = System.currentTimeMillis();
        
        MrpResult.MrpResultBuilder resultBuilder = MrpResult.builder()
                .runId(context.getRunId())
                .status("RUNNING");
        
        try {
            log.info("开始 MRP 计算，runId: {}", context.getRunId());
            
            // 1. 按物料汇总需求
            Map<String, List<MrpContext.DemandVO>> demandMap = mergeDemands(context);
            
            // 2. 对每个物料计算净需求并生成建议
            List<MrpResult.Suggestion> allSuggestions = new ArrayList<>();
            List<MrpResult.RiskAlert> riskAlerts = new ArrayList<>();
            
            for (String itemCode : demandMap.keySet()) {
                MrpContext.ItemVO item = context.getItems().get(itemCode);
                if (item == null) {
                    log.warn("物料 {} 不存在，跳过", itemCode);
                    continue;
                }
                
                List<MrpContext.DemandVO> demands = demandMap.get(itemCode);
                
                // 净需求计算
                NetRequirement netReq = calculateNetRequirement(context, item, demands);
                
                // 生成建议
                List<MrpResult.Suggestion> suggestions = generateSuggestions(context, item, netReq);
                allSuggestions.addAll(suggestions);
                
                // 风险检测
                List<MrpResult.RiskAlert> alerts = detectRisks(context, item, netReq);
                riskAlerts.addAll(alerts);
            }
            
            // 3. 分类建议
            List<MrpResult.Suggestion> purchaseSuggestions = allSuggestions.stream()
                    .filter(s -> "BUY".equals(s.getSuggestionType()) || "PURCHASE".equals(s.getSuggestionType()))
                    .collect(Collectors.toList());
            
            List<MrpResult.Suggestion> productionSuggestions = allSuggestions.stream()
                    .filter(s -> "MAKE".equals(s.getSuggestionType()) || "PRODUCTION".equals(s.getSuggestionType()))
                    .collect(Collectors.toList());
            
            // 4. 构建结果
            long runTime = System.currentTimeMillis() - startTime;
            
            resultBuilder
                    .status("COMPLETED")
                    .runTimeMs(runTime)
                    .purchaseSuggestions(purchaseSuggestions)
                    .productionSuggestions(productionSuggestions)
                    .riskAlerts(riskAlerts)
                    .statistics(MrpResult.Statistics.builder()
                            .totalItems(demandMap.size())
                            .totalDemands(demandMap.values().stream().mapToInt(List::size).sum())
                            .totalSuggestions(allSuggestions.size())
                            .purchaseSuggestions(purchaseSuggestions.size())
                            .productionSuggestions(productionSuggestions.size())
                            .riskCount(riskAlerts.size())
                            .build());
            
            log.info("MRP 计算完成，runId: {}, 耗时: {}ms, 建议数: {}", 
                    context.getRunId(), runTime, allSuggestions.size());
            
        } catch (Exception e) {
            log.error("MRP 计算失败，runId: {}", context.getRunId(), e);
            resultBuilder
                    .status("FAILED")
                    .errorMessage(e.getMessage());
        }
        
        return resultBuilder.build();
    }
    
    /**
     * 合并需求
     * 
     * 将销售订单、销售预测等需求按物料汇总
     */
    private Map<String, List<MrpContext.DemandVO>> mergeDemands(MrpContext context) {
        Map<String, List<MrpContext.DemandVO>> demandMap = new HashMap<>();
        
        // 合并销售订单需求
        if (context.getSalesDemandMap() != null) {
            for (Map.Entry<String, List<MrpContext.DemandVO>> entry : context.getSalesDemandMap().entrySet()) {
                demandMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .addAll(entry.getValue());
            }
        }
        
        // 按日期排序
        for (String itemCode : demandMap.keySet()) {
            demandMap.get(itemCode).sort(Comparator.comparing(MrpContext.DemandVO::getDueDate));
        }
        
        return demandMap;
    }
    
    /**
     * 计算净需求
     * 
     * 净需求 = 毛需求 - 现有量 - 在途量 - 预留量
     */
    private NetRequirement calculateNetRequirement(MrpContext context, 
                                                   MrpContext.ItemVO item, 
                                                   List<MrpContext.DemandVO> demands) {
        
        BigDecimal grossRequirement = demands.stream()
                .map(MrpContext.DemandVO::getQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 现有可用量
        BigDecimal availableQty = BigDecimal.ZERO;
        if (context.getInventoryMap() != null && context.getInventoryMap().containsKey(item.getItemCode())) {
            MrpContext.InventoryVO inv = context.getInventoryMap().get(item.getItemCode());
            availableQty = inv.getAvailableQty() != null ? inv.getAvailableQty() : BigDecimal.ZERO;
        }
        
        // 在途量
        BigDecimal onWayQty = BigDecimal.ZERO;
        if (context.getPurchaseOnWayMap() != null) {
            List<MrpContext.PurchaseOnWayVO> onWayList = context.getPurchaseOnWayMap().get(item.getItemCode());
            if (onWayList != null) {
                onWayQty = onWayList.stream()
                        .filter(po -> po.getExpectDate().isAfter(LocalDate.now()))
                        .map(MrpContext.PurchaseOnWayVO::getQty)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }
        
        // 净需求
        BigDecimal netRequirement = grossRequirement.subtract(availableQty).subtract(onWayQty);
        if (netRequirement.compareTo(BigDecimal.ZERO) < 0) {
            netRequirement = BigDecimal.ZERO;
        }
        
        return NetRequirement.builder()
                .grossRequirement(grossRequirement)
                .availableQty(availableQty)
                .onWayQty(onWayQty)
                .netRequirement(netRequirement)
                .demands(demands)
                .build();
    }
    
    /**
     * 生成采购/生产建议
     */
    private List<MrpResult.Suggestion> generateSuggestions(MrpContext context, 
                                                          MrpContext.ItemVO item, 
                                                          NetRequirement netReq) {
        List<MrpResult.Suggestion> suggestions = new ArrayList<>();
        
        if (netReq.getNetRequirement().compareTo(BigDecimal.ZERO) <= 0) {
            return suggestions;
        }
        
        // 根据物料类型决定建议类型
        String suggestionType;
        switch (item.getSource()) {
            case "BUY":
                suggestionType = "PURCHASE";
                break;
            case "MAKE":
                suggestionType = "PRODUCTION";
                break;
            case "BOTH":
                // TODO: 需要根据成本/产能等因素选择
                suggestionType = "PURCHASE";
                break;
            default:
                suggestionType = "PURCHASE";
        }
        
        // 应用批量规则
        BigDecimal orderQty = applyLotSizeRule(netReq.getNetRequirement(), item);
        
        // 计算建议日期
        LocalDate needDate = netReq.getDemands().stream()
                .map(MrpContext.DemandVO::getDueDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        int leadTime = item.getLeadTime() != null ? item.getLeadTime() : 7;
        LocalDate suggestOrderDate = needDate.minusDays(leadTime);
        
        // 创建建议
        MrpResult.Suggestion suggestion = MrpResult.Suggestion.builder()
                .suggestionType(suggestionType)
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .suggestQty(orderQty)
                .needDate(needDate)
                .suggestOrderDate(suggestOrderDate)
                .suggestFinishDate(needDate)
                .demandSource("ORDER")
                .priority(5)
                .reason("净需求: " + netReq.getNetRequirement())
                .build();
        
        suggestions.add(suggestion);
        
        return suggestions;
    }
    
    /**
     * 应用批量规则
     */
    private BigDecimal applyLotSizeRule(BigDecimal netRequirement, MrpContext.ItemVO item) {
        String rule = item.getLotSizeRule();
        if (rule == null) {
            rule = "LOT_FOR_LOT";
        }
        
        BigDecimal orderQty = netRequirement;
        
        switch (rule) {
            case "FIXED":
                // 固定批量
                if (item.getMinLotSize() != null && orderQty.compareTo(item.getMinLotSize()) < 0) {
                    orderQty = item.getMinLotSize();
                }
                break;
            case "EOQ":
                // 经济批量（简化版）
                // TODO: 实现完整 EOQ 计算
                break;
            case "PERIOD":
                // 期间批量
                // TODO: 实现期间批量
                break;
            case "LOT_FOR_LOT":
            default:
                // 按需批量
                break;
        }
        
        // 确保不超过最大批量
        if (item.getMaxLotSize() != null && orderQty.compareTo(item.getMaxLotSize()) > 0) {
            orderQty = item.getMaxLotSize();
        }
        
        return orderQty;
    }
    
    /**
     * 检测风险
     */
    private List<MrpResult.RiskAlert> detectRisks(MrpContext context, 
                                                   MrpContext.ItemVO item, 
                                                   NetRequirement netReq) {
        List<MrpResult.RiskAlert> alerts = new ArrayList<>();
        
        // 检查是否低于安全库存
        BigDecimal safetyStock = item.getSafetyStock() != null ? item.getSafetyStock() : BigDecimal.ZERO;
        BigDecimal availableQty = netReq.getAvailableQty();
        
        if (availableQty.compareTo(safetyStock) < 0) {
            MrpResult.RiskAlert alert = MrpResult.RiskAlert.builder()
                    .riskType("STOCKOUT")
                    .itemCode(item.getItemCode())
                    .message("物料 " + item.getItemCode() + " 库存 " + availableQty + " 低于安全库存 " + safetyStock)
                    .severity(availableQty.compareTo(safetyStock.multiply(BigDecimal.valueOf(0.5))) < 0 ? "HIGH" : "MEDIUM")
                    .build();
            alerts.add(alert);
        }
        
        return alerts;
    }
    
    /**
     * 净需求内部类
     */
    @lombok.Data
    @lombok.Builder
    private static class NetRequirement {
        private BigDecimal grossRequirement;
        private BigDecimal availableQty;
        private BigDecimal onWayQty;
        private BigDecimal netRequirement;
        private List<MrpContext.DemandVO> demands;
    }
}
