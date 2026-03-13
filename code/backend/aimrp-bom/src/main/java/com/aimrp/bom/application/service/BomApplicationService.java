package com.aimrp.bom.application.service;

import com.aimrp.bom.domain.entity.Bom;
import com.aimrp.bom.domain.service.BomDomainService;
import com.aimrp.bom.domain.service.BomDomainService.*;
import com.aimrp.bom.infrastructure.persistence.mapper.BomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

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
        Map<String, List<Map<String, Object>>> rawBomMap = bomMapper.selectBomMap(itemCode);

        if (rawBomMap == null || rawBomMap.isEmpty()) {
            log.warn("物料 {} 无BOM数据", itemCode);
            return List.of();
        }

        // 2. 转换为领域对象
        Map<String, List<BomLine>> bomMap = convertToBomLineMap(rawBomMap);

        // 3. 调用领域服务展开
        List<BomExpandResult> results = bomDomainService.expandBom(
                itemCode,
                new BigDecimal(qty),
                bomMap,
                level != null ? level : 5);

        // 4. 汇总
        Map<String, BigDecimal> aggregated = bomDomainService.aggregate(results);

        log.info("BOM展开完成 - 物料: {}, 子件: {}", itemCode, aggregated.size());

        return results;
    }

    /**
     * 转换数据
     */
    private Map<String, List<BomLine>> convertToBomLineMap(Map<String, List<Map<String, Object>>> rawMap) {
        Map<String, List<BomLine>> result = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : rawMap.entrySet()) {
            List<BomLine> bomLines = new ArrayList<>();
            for (Map<String, Object> raw : entry.getValue()) {
                BomLine line = BomLine.builder()
                        .bomId(getLong(raw.get("id")))
                        .parentItemCode(entry.getKey())
                        .childItemCode((String) raw.get("child_item_code"))
                        .childItemName((String) raw.get("child_item_name"))
                        .usageQty(getBigDecimal(raw.get("usage_qty")))
                        .lossRate(getBigDecimal(raw.get("loss_rate")))
                        .level(getInt(raw.get("level")))
                        .build();
                bomLines.add(line);
            }
            result.put(entry.getKey(), bomLines);
        }
        return result;
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return new BigDecimal(value.toString());
        return BigDecimal.ZERO;
    }

    private Long getLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Integer getInt(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    /**
     * 获取 BOM 详情
     */
    @Transactional(readOnly = true)
    public BomDetail getDetail(String itemCode) {
        log.info("获取BOM详情 - itemCode: {}", itemCode);

        // 查询 BOM 主数据
        Bom bom = bomMapper.selectByItemCode(itemCode);

        // 查询 BOM 行
        List<Map<String, Object>> lines = bomMapper.selectLinesByItemCode(itemCode);

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
