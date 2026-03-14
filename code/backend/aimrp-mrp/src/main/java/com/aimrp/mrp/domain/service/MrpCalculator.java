package com.aimrp.mrp.domain.service;

import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;
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
                // 根据成本和产能选择：采购成本 vs 生产成本
                BigDecimal purchaseCost = estimatePurchaseCost(netReq.getNetRequirement(), item);
                BigDecimal productionCost = estimateProductionCost(netReq.getNetRequirement(), item);
                suggestionType = purchaseCost.compareTo(productionCost) <= 0 ? "PURCHASE" : "PRODUCTION";
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
                // 固定批量：按最小批量下单，不足则补齐
                if (item.getMinLotSize() != null && orderQty.compareTo(item.getMinLotSize()) < 0) {
                    orderQty = item.getMinLotSize();
                }
                break;
                
            case "EOQ":
                // 经济批量：EOQ = √(2 × 年需求 × 订货成本 / 单位持有成本)
                orderQty = calculateEOQ(netRequirement, item);
                break;
                
            case "PERIOD":
                // 期间批量：将N天的需求合并为一次订单
                orderQty = calculatePeriodLot(netRequirement, item);
                break;
                
            case "MULTIPLE":
                // 倍批量：按最小批量的倍数下单
                orderQty = calculateMultipleLot(netRequirement, item);
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
        
        // 确保不低于最小批量
        if (item.getMinLotSize() != null && orderQty.compareTo(item.getMinLotSize()) < 0) {
            orderQty = item.getMinLotSize();
        }
        
        return orderQty;
    }
    
    /**
     * 计算经济批量 (EOQ)
     * 公式：EOQ = √(2 × D × S / H)
     * D: 年需求量 (Demand)
     * S: 订货成本 (Ordering Cost)
     * H: 单位持有成本 (Holding Cost) = 单位成本 × 持有成本率
     */
    private BigDecimal calculateEOQ(BigDecimal netRequirement, MrpContext.ItemVO item) {
        // 默认年需求 = 净需求 × 12（假设月需求）
        BigDecimal annualDemand = netRequirement.multiply(new BigDecimal("12"));
        
        // 订货成本：默认值100，可从物料主数据获取
        BigDecimal orderingCost = new BigDecimal("100");
        if (item.getOrderingCost() != null) {
            orderingCost = item.getOrderingCost();
        }
        
        // 单位成本：默认50，可从物料主数据获取
        BigDecimal unitCost = new BigDecimal("50");
        if (item.getUnitCost() != null) {
            unitCost = item.getUnitCost();
        }
        
        // 持有成本率：默认20%
        BigDecimal holdingRate = new BigDecimal("0.2");
        if (item.getHoldingRate() != null) {
            holdingRate = item.getHoldingRate();
        }
        
        // 持有成本 H = 单位成本 × 持有成本率
        BigDecimal holdingCost = unitCost.multiply(holdingRate);
        
        if (holdingCost.compareTo(BigDecimal.ZERO) == 0) {
            return netRequirement;
        }
        
        // EOQ = √(2 × D × S / H)
        BigDecimal eoq = annualDemand.multiply(orderingCost)
                .multiply(new BigDecimal("2"))
                .divide(holdingCost, 2, java.math.RoundingMode.HALF_UP);
        
        // 开平方
        double eoqValue = Math.sqrt(eoq.doubleValue());
        eoq = BigDecimal.valueOf(eoqValue).setScale(2, java.math.RoundingMode.HALF_UP);
        
        log.info("EOQ计算 - 物料: {}, 年需求: {}, 订货成本: {}, 持有成本: {}, EOQ: {}", 
                item.getItemCode(), annualDemand, orderingCost, holdingCost, eoq);
        
        // EOQ不应超过年需求
        return eoq.compareTo(annualDemand) > 0 ? annualDemand : eoq;
    }
    
    /**
     * 计算期间批量
     * 将指定期间的需求合并为一次订单
     */
    private BigDecimal calculatePeriodLot(BigDecimal netRequirement, MrpContext.ItemVO item) {
        // 期间天数：默认30天，可从物料主数据获取
        int periodDays = 30;
        if (item.getLotPeriodDays() != null) {
            periodDays = item.getLotPeriodDays();
        }
        
        // 期间需求 = 日均需求 × 期间天数
        // 假设netRequirement是日需求
        BigDecimal periodQty = netRequirement.multiply(new BigDecimal(periodDays));
        
        // 调整到批量倍数
        periodQty = roundToLotMultiplier(periodQty, item);
        
        log.info("期间批量计算 - 物料: {}, 期间: {}天, 批量: {}", 
                item.getItemCode(), periodDays, periodQty);
        
        return periodQty;
    }
    
    /**
     * 计算倍批量
     * 调整到最小批量的整数倍
     */
    private BigDecimal calculateMultipleLot(BigDecimal netRequirement, MrpContext.ItemVO item) {
        BigDecimal minLot = item.getMinLotSize() != null ? item.getMinLotSize() : BigDecimal.ONE;
        
        // 向上取整到最小批量的倍数
        double multiple = Math.ceil(netRequirement.doubleValue() / minLot.doubleValue());
        BigDecimal orderQty = minLot.multiply(BigDecimal.valueOf(multiple));
        
        return orderQty;
    }
    
    /**
     * 调整到批量倍数
     */
    private BigDecimal roundToLotMultiplier(BigDecimal qty, MrpContext.ItemVO item) {
        BigDecimal minLot = item.getMinLotSize() != null ? item.getMinLotSize() : BigDecimal.ONE;
        
        double multiple = Math.ceil(qty.doubleValue() / minLot.doubleValue());
        return minLot.multiply(BigDecimal.valueOf(multiple));
    }
    
    /**
     * 估算采购成本
     */
    private BigDecimal estimatePurchaseCost(BigDecimal qty, MrpContext.ItemVO item) {
        BigDecimal unitCost = item.getUnitCost() != null ? item.getUnitCost() : new BigDecimal("50");
        BigDecimal orderingCost = item.getOrderingCost() != null ? item.getOrderingCost() : new BigDecimal("100");
        
        // 采购成本 = 物料成本 + 订货成本
        return qty.multiply(unitCost).add(orderingCost);
    }
    
    /**
     * 估算生产成本
     */
    private BigDecimal estimateProductionCost(BigDecimal qty, MrpContext.ItemVO item) {
        BigDecimal unitCost = item.getUnitCost() != null ? item.getUnitCost() : new BigDecimal("30");
        BigDecimal setupCost = item.getSetupCost() != null ? item.getSetupCost() : new BigDecimal("200");
        
        // 生产成本 = 物料成本 + 换线成本
        return qty.multiply(unitCost).add(setupCost);
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
    @Data
    @Builder
    private static class NetRequirement {
        private BigDecimal grossRequirement;
        private BigDecimal availableQty;
        private BigDecimal onWayQty;
        private BigDecimal netRequirement;
        private List<MrpContext.DemandVO> demands;
    }
}
