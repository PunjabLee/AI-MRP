package com.aimrp.mrp.domain.service;

import com.aimrp.mrp.domain.valueobject.MrpContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * BOM 展开服务
 * 
 * 负责将父件展开为子件需求
 */
@Slf4j
@Service
public class BomExpander {
    
    /**
     * 展开 BOM
     * 
     * @param itemCode 父件编码
     * @param requiredQty 需求数量
     * @param bomMap BOM数据
     * @param maxLevel 最大层级
     * @return 子件需求列表
     */
    public List<BomExpandResult> expand(String itemCode, BigDecimal requiredQty, 
                                        Map<String, List<MrpContext.BomLineVO>> bomMap, 
                                        int maxLevel) {
        List<BomExpandResult> results = new ArrayList<>();
        expandRecursive(itemCode, requiredQty, bomMap, 1, maxLevel, results);
        return results;
    }
    
    /**
     * 递归展开
     */
    private void expandRecursive(String parentCode, BigDecimal parentQty,
                                  Map<String, List<MrpContext.BomLineVO>> bomMap,
                                  int currentLevel, int maxLevel,
                                  List<BomExpandResult> results) {
        
        if (currentLevel > maxLevel) {
            log.warn("BOM 展开超过最大层级: {}", maxLevel);
            return;
        }
        
        List<MrpContext.BomLineVO> bomLines = bomMap.get(parentCode);
        if (bomLines == null || bomLines.isEmpty()) {
            // 无子 BOM，可能是原材料
            return;
        }
        
        for (MrpContext.BomLineVO line : bomLines) {
            // 计算子件需求 = 父件需求 × 用量 × (1 + 损耗率)
            BigDecimal lossRate = line.getLossRate() != null ? line.getLossRate() : BigDecimal.ZERO;
            BigDecimal childQty = parentQty.multiply(line.getUsageQty())
                    .multiply(BigDecimal.ONE.add(lossRate));
            
            // 添加到结果
            BomExpandResult result = BomExpandResult.builder()
                    .childItemCode(line.getChildItemCode())
                    .childItemName(line.getChildItemName())
                    .parentItemCode(parentCode)
                    .usageQty(line.getUsageQty())
                    .requiredQty(childQty)
                    .level(currentLevel)
                    .lossRate(lossRate)
                    .build();
            results.add(result);
            
            // 递归展开子件的 BOM
            expandRecursive(line.getChildItemCode(), childQty, bomMap, currentLevel + 1, maxLevel, results);
        }
    }
    
    /**
     * 汇总子件需求
     * 将相同子件的需求合并
     */
    public Map<String, BigDecimal> aggregate(List<BomExpandResult> expandResults) {
        Map<String, BigDecimal> aggregated = new HashMap<>();
        
        for (BomExpandResult result : expandResults) {
            String itemCode = result.getChildItemCode();
            BigDecimal qty = result.getRequiredQty();
            
            aggregated.merge(itemCode, qty, BigDecimal::add);
        }
        
        return aggregated;
    }
    
    /**
     * BOM 展开结果
     */
    @lombok.Data
    @lombok.Builder
    public static class BomExpandResult {
        private String childItemCode;
        private String childItemName;
        private String parentItemCode;
        private BigDecimal usageQty;
        private BigDecimal requiredQty;
        private int level;
        private BigDecimal lossRate;
    }
}
