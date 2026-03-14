package com.aimrp.bom.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * BOM 领域服务
 * 
 * 处理 BOM 相关的核心业务逻辑
 */
@Slf4j
@Service
public class BomDomainService {
    
    /**
     * 展开 BOM
     * 
     * @param parentItemCode 父件编码
     * @param requiredQty 需求数量
     * @param bomMap BOM数据映射
     * @param maxLevel 最大展开层级
     * @return 展开结果列表
     */
    public List<BomExpandResult> expandBom(String parentItemCode, BigDecimal requiredQty,
                                          Map<String, List<BomLine>> bomMap, int maxLevel) {
        List<BomExpandResult> results = new ArrayList<>();
        expandRecursive(parentItemCode, requiredQty, bomMap, 1, maxLevel, results);
        return results;
    }
    
    /**
     * 递归展开
     */
    private void expandRecursive(String parentCode, BigDecimal parentQty,
                                Map<String, List<BomLine>> bomMap,
                                int currentLevel, int maxLevel,
                                List<BomExpandResult> results) {
        if (currentLevel > maxLevel) {
            log.warn("BOM 展开超过最大层级: {}", maxLevel);
            return;
        }
        
        List<BomLine> bomLines = bomMap.get(parentCode);
        if (bomLines == null || bomLines.isEmpty()) {
            return;
        }
        
        for (BomLine line : bomLines) {
            BigDecimal lossRate = line.getLossRate() != null ? line.getLossRate() : BigDecimal.ZERO;
            BigDecimal childQty = parentQty.multiply(line.getUsageQty())
                    .multiply(BigDecimal.ONE.add(lossRate));
            
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
            
            expandRecursive(line.getChildItemCode(), childQty, bomMap, currentLevel + 1, maxLevel, results);
        }
    }
    
    /**
     * 汇总子件需求
     */
    public Map<String, BigDecimal> aggregate(List<BomExpandResult> expandResults) {
        Map<String, BigDecimal> aggregated = new HashMap<>();
        for (BomExpandResult result : expandResults) {
            aggregated.merge(result.getChildItemCode(), result.getRequiredQty(), BigDecimal::add);
        }
        return aggregated;
    }
    
    /**
     * BOM 行数据
     */
    @Data
    @Builder
    public static class BomLine {
        private Long bomId;
        private String parentItemCode;
        private String childItemCode;
        private String childItemName;
        private BigDecimal usageQty;
        private BigDecimal lossRate;
        private Integer level;
    }
    
    /**
     * BOM 展开结果
     */
    @Data
    @Builder
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
