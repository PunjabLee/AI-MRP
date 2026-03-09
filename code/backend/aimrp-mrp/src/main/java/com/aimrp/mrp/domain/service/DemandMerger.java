package com.aimrp.mrp.domain.service;

import com.aimrp.mrp.domain.valueobject.MrpContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求合并服务
 * 
 * 负责将多种需求来源合并为统一需求
 */
@Slf4j
@Service
public class DemandMerger {
    
    /**
     * 合并需求
     * 
     * @param salesOrders 销售订单需求
     * @param forecasts 预测需求
     * @param demandPool 需求池
     * @return 按物料分组的需求列表
     */
    public Map<String, List<MrpContext.DemandVO>> merge(
            List<MrpContext.DemandVO> salesOrders,
            List<MrpContext.DemandVO> forecasts,
            List<MrpContext.DemandVO> demandPool) {
        
        Map<String, List<MrpContext.DemandVO>> merged = new HashMap<>();
        
        // 1. 合并销售订单需求（优先级最高）
        if (salesOrders != null) {
            for (MrpContext.DemandVO demand : salesOrders) {
                merged.computeIfAbsent(demand.getItemCode(), k -> new ArrayList<>())
                        .add(demand);
            }
        }
        
        // 2. 合并预测需求
        if (forecasts != null) {
            for (MrpContext.DemandVO forecast : forecasts) {
                merged.computeIfAbsent(forecast.getItemCode(), k -> new ArrayList<>())
                        .add(forecast);
            }
        }
        
        // 3. 合并需求池
        if (demandPool != null) {
            for (MrpContext.DemandVO pool : demandPool) {
                merged.computeIfAbsent(pool.getItemCode(), k -> new ArrayList<>())
                        .add(pool);
            }
        }
        
        // 4. 按日期排序
        for (String itemCode : merged.keySet()) {
            merged.get(itemCode).sort(Comparator
                    .comparing(MrpContext.DemandVO::getDueDate)
                    .thenComparing(Comparator.comparing(MrpContext.DemandVO::getPriority, Comparator.reverseOrder())));
        }
        
        log.info("需求合并完成，物料数: {}", merged.size());
        
        return merged;
    }
    
    /**
     * 按时间段汇总需求
     * 
     * @param demands 需求列表
     * @param bucketDays 时间段天数
     * @return 按时间段汇总的需求
     */
    public Map<LocalDate, BigDecimal> aggregateByPeriod(List<MrpContext.DemandVO> demands, int bucketDays) {
        Map<LocalDate, BigDecimal> aggregated = new TreeMap<>();
        
        for (MrpContext.DemandVO demand : demands) {
            LocalDate periodStart = getPeriodStart(demand.getDueDate(), bucketDays);
            aggregated.merge(periodStart, demand.getQty(), BigDecimal::add);
        }
        
        return aggregated;
    }
    
    /**
     * 获取时间段起点
     */
    private LocalDate getPeriodStart(LocalDate date, int bucketDays) {
        int dayOfYear = date.getDayOfYear();
        int periodStartDay = ((dayOfYear - 1) / bucketDays) * bucketDays + 1;
        return date.withDayOfYear(periodStartDay);
    }
    
    /**
     * 按优先级排序需求
     * 
     * @param demands 需求列表
     * @return 排序后的需求
     */
    public List<MrpContext.DemandVO> sortByPriority(List<MrpContext.DemandVO> demands) {
        return demands.stream()
                .sorted(Comparator
                        .comparing(MrpContext.DemandVO::getDueDate)
                        .thenComparing(Comparator.comparing(
                                MrpContext.DemandVO::getPriority, 
                                Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}
