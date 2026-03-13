package com.aimrp.bom;

import com.aimrp.bom.domain.service.BomDomainService;
import com.aimrp.bom.domain.service.BomDomainService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BomDomainService TDD Tests
 */
class BomDomainServiceTest {

    private BomDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new BomDomainService();
    }

    // ===== BOM Expansion Tests =====

    @Test
    void expandBom_singleLevel_shouldReturnChildren() {
        // Given
        String parentItemCode = "PRODUCT-A";
        Map<String, List<BomLine>> bomMap = new HashMap<>();

        List<BomLine> children = new ArrayList<>();
        children.add(BomLine.builder()
                .childItemCode("COMPONENT-A")
                .childItemName("Component A")
                .usageQty(new BigDecimal("2"))
                .lossRate(BigDecimal.ZERO)
                .build());
        children.add(BomLine.builder()
                .childItemCode("COMPONENT-B")
                .childItemName("Component B")
                .usageQty(new BigDecimal("1"))
                .lossRate(BigDecimal.ZERO)
                .build());

        bomMap.put(parentItemCode, children);

        // When
        List<BomExpandResult> results = domainService.expandBom(
                parentItemCode,
                new BigDecimal("10"),
                bomMap,
                1
        );

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getChildItemCode().equals("COMPONENT-A")));
        assertTrue(results.stream().anyMatch(r -> r.getChildItemCode().equals("COMPONENT-B")));
    }

    @Test
    void expandBom_withLossRate_shouldCalculateCorrectly() {
        // Given
        String parentItemCode = "PRODUCT-A";
        Map<String, List<BomLine>> bomMap = new HashMap<>();

        List<BomLine> children = new ArrayList<>();
        children.add(BomLine.builder()
                .childItemCode("COMPONENT-A")
                .usageQty(new BigDecimal("2"))
                .lossRate(new BigDecimal("0.1"))  // 10% loss
                .build());
        bomMap.put(parentItemCode, children);

        // When
        List<BomExpandResult> results = domainService.expandBom(
                parentItemCode,
                new BigDecimal("10"),
                bomMap,
                1
        );

        // Then - requiredQty = 10 * 2 * (1 + 0.1) = 22
        assertEquals(1, results.size());
        assertEquals(0, new BigDecimal("22").compareTo(results.get(0).getRequiredQty()));
    }

    @Test
    void expandBom_multiLevel_shouldExpandAllLevels() {
        // Given - 3 level BOM
        String level1 = "PRODUCT-A";
        String level2 = "SUB-ASSY-1";
        String level3 = "COMPONENT-A";

        Map<String, List<BomLine>> bomMap = new HashMap<>();

        // Level 1 -> Level 2
        bomMap.put(level1, List.of(
                BomLine.builder()
                        .childItemCode(level2)
                        .usageQty(new BigDecimal("1"))
                        .lossRate(BigDecimal.ZERO)
                        .build()
        ));

        // Level 2 -> Level 3
        bomMap.put(level2, List.of(
                BomLine.builder()
                        .childItemCode(level3)
                        .usageQty(new BigDecimal("2"))
                        .lossRate(BigDecimal.ZERO)
                        .build()
        ));

        // When - expand 2 levels
        List<BomExpandResult> results = domainService.expandBom(
                level1,
                new BigDecimal("10"),
                bomMap,
                2
        );

        // Then - should have 2 results (level 1->2 and level 2->3)
        assertEquals(2, results.size());
    }

    @Test
    void expandBom_noChildren_shouldReturnEmpty() {
        // Given
        Map<String, List<BomLine>> bomMap = new HashMap<>();

        // When
        List<BomExpandResult> results = domainService.expandBom(
                "PRODUCT-X",
                new BigDecimal("10"),
                bomMap,
                1
        );

        // Then
        assertTrue(results.isEmpty());
    }

    // ===== Aggregate Tests =====

    @Test
    void aggregate_shouldSumByItemCode() {
        // Given
        List<BomExpandResult> results = List.of(
                BomExpandResult.builder()
                        .childItemCode("COMP-A")
                        .requiredQty(new BigDecimal("100"))
                        .build(),
                BomExpandResult.builder()
                        .childItemCode("COMP-A")
                        .requiredQty(new BigDecimal("50"))
                        .build(),
                BomExpandResult.builder()
                        .childItemCode("COMP-B")
                        .requiredQty(new BigDecimal("30"))
                        .build()
        );

        // When
        Map<String, BigDecimal> aggregated = domainService.aggregate(results);

        // Then
        assertEquals(new BigDecimal("150"), aggregated.get("COMP-A"));
        assertEquals(new BigDecimal("30"), aggregated.get("COMP-B"));
    }

    @Test
    void aggregate_emptyList_shouldReturnEmptyMap() {
        // Given
        List<BomExpandResult> results = List.of();

        // When
        Map<String, BigDecimal> aggregated = domainService.aggregate(results);

        // Then
        assertTrue(aggregated.isEmpty());
    }
}
