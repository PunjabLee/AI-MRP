package com.aimrp.bom.application.service;

import com.aimrp.bom.domain.service.BomDomainService;
import com.aimrp.bom.domain.service.BomDomainService.*;
import com.aimrp.bom.infrastructure.persistence.mapper.BomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * BOM 应用服务
 * 
 * 处理 BOM 相关的业务流程和事务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BomApplicationService {
    
    private final BomDomainService bomDomainService;
    private final BomMapper bomMapper;
    
    /**
     * 展开 BOM
     * 
     * @param itemCode 物料编码
     * @param qty 需求数量
     * @param level 展开层级
     * @return 展开结果
     */
    @Transactional(readOnly = true)
    public List<BomExpandResult> expand(String itemCode, Integer qty, Integer level) {
        log.info("展开BOM - itemCode: {}, qty: {}, level: {}", itemCode, qty, level);
        
        // 1. 查询 BOM 数据
        Map<String, List<BomDomainService.BomLine>> bomMap = bomMapper.selectBomMap(itemCode);
        
        if (bomMap == null || bomMap.isEmpty()) {
            log.warn("物料 {} 无BOM数据", itemCode);
            return List.of();
        }
        
        // 2. 调用领域服务展开
        List<BomExpandResult> results = bomDomainService.expandBom(
                itemCode, 
                new java.math.BigDecimal(qty), 
                bomMap, 
                level != null ? level : 5);
        
        // 3. 汇总
        Map<String, java.math.BigDecimal> aggregated = bomDomainService.aggregate(results);
        
        log.info("BOM展开完成 - 物料: {}, 子件: {}", itemCode, aggregated.size());
        
        return results;
    }
    
    /**
     * 获取 BOM 详情
     */
    @Transactional(readOnly = true)
    public BomDetail getDetail(String itemCode) {
        log.info("获取BOM详情 - itemCode: {}", itemCode);
        
        // 查询 BOM 主数据
        var bom = bomMapper.selectByItemCode(itemCode);
        
        // 查询 BOM 行
        var lines = bomMapper.selectLinesByItemCode(itemCode);
        
        return BomDetail.builder()
                .itemCode(itemCode)
                .bom(bom)
                .lines(lines)
                .build();
    }
    
    /**
     * BOM 详情
     */
    @lombok.Data
    @lombok.Builder
    public static class BomDetail {
        private String itemCode;
        private Object bom;
        private List<?> lines;
    }
}
