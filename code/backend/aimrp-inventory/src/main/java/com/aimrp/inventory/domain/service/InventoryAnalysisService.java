package com.aimrp.inventory.domain.service;

import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryMapper;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存分析服务 - 库龄/呆滞分析
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAnalysisService {
    
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;
    
    /**
     * 库龄分析
     * 
     * @param warehouseCode 仓库编码(可选)
     * @return 库龄分析结果
     */
    public List<InventoryAgeing> analyseAgeing(String warehouseCode) {
        log.info("执行库龄分析 - warehouse: {}", warehouseCode);
        
        // 查询所有库存
        List<Map<String, Object>> inventories = inventoryMapper.selectList(null, warehouseCode);
        
        List<InventoryAgeing> results = new ArrayList<>();
        
        for (Map<String, Object> inv : inventories) {
            String itemCode = (String) inv.get("item_code");
            String warehouseCode = (String) inv.get("warehouse_code");
            
            // 查询该物料的最早入库时间
            LocalDateTime earliestIn = getEarliestInDate(itemCode, warehouseCode);
            
            if (earliestIn != null) {
                long days = java.time.Duration.between(earliestIn, LocalDateTime.now()).toDays();
                
                InventoryAgeing ageing = new InventoryAgeing();
                ageing.setItemCode(itemCode);
                ageing.setWarehouseCode(warehouseCode);
                ageing.setOnHandQty(new BigDecimal(inv.get("on_hand_qty").toString()));
                ageing.setInDate(earliestIn.toLocalDate());
                ageing.setAgeDays((int) days);
                ageing.setAgeLevel(getAgeLevel((int) days));
                
                results.add(ageing);
            }
        }
        
        // 按库龄排序
        results.sort((a, b) -> b.getAgeDays() - a.getAgeDays());
        
        return results;
    }
    
    /**
     * 呆滞分析
     * 
     * @param slowDays 超过多少天未动货视为呆滞(默认90天)
     * @return 呆滞物料列表
     */
    public List<InventoryAgeing> analyseSlowMoving(int slowDays) {
        log.info("执行呆滞分析 - slowDays: {}", slowDays);
        
        // 查询所有库存
        List<Map<String, Object>> inventories = inventoryMapper.selectList(null, null);
        
        List<InventoryAgeing> results = new ArrayList<>();
        
        for (Map<String, Object> inv : inventories) {
            String itemCode = (String) inv.get("item_code");
            String warehouseCode = (String) inv.get("warehouse_code");
            
            // 查询该物料的最后出库时间
            LocalDateTime lastOutDate = getLastOutDate(itemCode, warehouseCode);
            
            int days;
            if (lastOutDate != null) {
                days = (int) java.time.Duration.between(lastOutDate, LocalDateTime.now()).toDays();
            } else {
                // 如果没有出库记录，查看入库时间
                LocalDateTime earliestIn = getEarliestInDate(itemCode, warehouseCode);
                if (earliestIn != null) {
                    days = (int) java.time.Duration.between(earliestIn, LocalDateTime.now()).toDays();
                } else {
                    days = 0;
                }
            }
            
            // 超过指定天数视为呆滞
            if (days >= slowDays) {
                InventoryAgeing ageing = new InventoryAgeing();
                ageing.setItemCode(itemCode);
                ageing.setWarehouseCode(warehouseCode);
                ageing.setOnHandQty(new BigDecimal(inv.get("on_hand_qty").toString()));
                ageing.setLastOutDate(lastOutDate != null ? lastOutDate.toLocalDate() : null);
                ageing.setIdleDays(days);
                ageing.setAgeLevel(getAgeLevel(days));
                
                // 估算呆滞金额
                BigDecimal unitCost = inv.get("unit_cost") != null ? 
                    new BigDecimal(inv.get("unit_cost").toString()) : BigDecimal.ZERO;
                ageing.setIdleAmount(ageing.getOnHandQty().multiply(unitCost));
                
                results.add(ageing);
            }
        }
        
        // 按呆滞金额排序
        results.sort((a, b) -> b.getIdleAmount().compareTo(a.getIdleAmount()));
        
        return results;
    }
    
    /**
     * 获取最早入库日期
     */
    private LocalDateTime getEarliestInDate(String itemCode, String warehouseCode) {
        // TODO: 从交易记录查询
        // 简化实现：返回固定日期
        return LocalDateTime.now().minusDays(30);
    }
    
    /**
     * 获取最后出库日期
     */
    private LocalDateTime getLastOutDate(String itemCode, String warehouseCode) {
        // TODO: 从交易记录查询
        return LocalDateTime.now().minusDays(100);
    }
    
    /**
     * 获取库龄等级
     */
    private String getAgeLevel(int days) {
        if (days <= 30) return "NORMAL";
        if (days <= 90) return "WARNING";
        if (days <= 180) return "SLOW";
        return "DEAD";
    }
    
    /**
     * 库龄分析结果
     */
    @lombok.Data
    public static class InventoryAgeing {
        private String itemCode;
        private String warehouseCode;
        private BigDecimal onHandQty;
        private LocalDate inDate;
        private LocalDate lastOutDate;
        private Integer ageDays;
        private Integer idleDays;
        private String ageLevel;
        private BigDecimal idleAmount;
    }
}
