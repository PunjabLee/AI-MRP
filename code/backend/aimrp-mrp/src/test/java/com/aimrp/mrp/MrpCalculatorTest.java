package com.aimrp.mrp;

import com.aimrp.mrp.domain.service.MrpCalculator;
import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MrpCalculator TDD Tests
 */
class MrpCalculatorTest {

    private MrpCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MrpCalculator();
    }

    @Test
    void calculate_withDemand_shouldGeneratePurchaseSuggestion() {
        // Given - Create context with demand for a BUY item with no inventory
        MrpContext context = MrpContext.builder()
                .runId(1L)
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(30))
                .items(Map.of(
                        "ITEM-A", MrpContext.ItemVO.builder()
                                .itemCode("ITEM-A")
                                .itemName("Item A")
                                .source("BUY")
                                .leadTime(7)
                                .lotSizeRule("LOT_FOR_LOT")
                                .safetyStock(BigDecimal.ZERO)
                                .build()
                ))
                .salesDemandMap(Map.of(
                        "ITEM-A", List.of(
                                MrpContext.DemandVO.builder()
                                        .demandNo("SO-001")
                                        .itemCode("ITEM-A")
                                        .qty(new BigDecimal("100"))
                                        .dueDate(LocalDate.now().plusDays(14))
                                        .build()
                        )
                ))
                .inventoryMap(Map.of(
                        "ITEM-A", MrpContext.InventoryVO.builder()
                                .itemCode("ITEM-A")
                                .availableQty(BigDecimal.ZERO)
                                .onHandQty(BigDecimal.ZERO)
                                .build()
                ))
                .build();

        // When
        MrpResult result = calculator.calculate(context);

        // Then - Should generate purchase suggestion
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getPurchaseSuggestions());
        assertFalse(result.getPurchaseSuggestions().isEmpty());
        assertEquals("PURCHASE", result.getPurchaseSuggestions().get(0).getSuggestionType());
        assertEquals("ITEM-A", result.getPurchaseSuggestions().get(0).getItemCode());
    }

    @Test
    void calculate_withEnoughInventory_shouldNotGenerateSuggestion() {
        // Given - Create context with demand but enough inventory
        MrpContext context = MrpContext.builder()
                .runId(2L)
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(30))
                .items(Map.of(
                        "ITEM-B", MrpContext.ItemVO.builder()
                                .itemCode("ITEM-B")
                                .itemName("Item B")
                                .source("BUY")
                                .leadTime(7)
                                .lotSizeRule("LOT_FOR_LOT")
                                .safetyStock(BigDecimal.ZERO)
                                .build()
                ))
                .salesDemandMap(Map.of(
                        "ITEM-B", List.of(
                                MrpContext.DemandVO.builder()
                                        .demandNo("SO-002")
                                        .itemCode("ITEM-B")
                                        .qty(new BigDecimal("50"))
                                        .dueDate(LocalDate.now().plusDays(14))
                                        .build()
                        )
                ))
                .inventoryMap(Map.of(
                        "ITEM-B", MrpContext.InventoryVO.builder()
                                .itemCode("ITEM-B")
                                .availableQty(new BigDecimal("100")) // Enough inventory
                                .onHandQty(new BigDecimal("100"))
                                .build()
                ))
                .build();

        // When
        MrpResult result = calculator.calculate(context);

        // Then - Should NOT generate suggestion since inventory is enough
        assertEquals("COMPLETED", result.getStatus());
        assertTrue(result.getPurchaseSuggestions().isEmpty());
    }

    @Test
    void calculate_withFixedLotSize_shouldRoundUp() {
        // Given - FIXED lot size rule with demand less than lot size
        MrpContext context = MrpContext.builder()
                .runId(3L)
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(30))
                .items(Map.of(
                        "ITEM-C", MrpContext.ItemVO.builder()
                                .itemCode("ITEM-C")
                                .itemName("Item C")
                                .source("BUY")
                                .leadTime(7)
                                .lotSizeRule("FIXED")
                                .minLotSize(new BigDecimal("100"))
                                .maxLotSize(new BigDecimal("500"))
                                .safetyStock(BigDecimal.ZERO)
                                .build()
                ))
                .salesDemandMap(Map.of(
                        "ITEM-C", List.of(
                                MrpContext.DemandVO.builder()
                                        .demandNo("SO-003")
                                        .itemCode("ITEM-C")
                                        .qty(new BigDecimal("50")) // Less than min lot size
                                        .dueDate(LocalDate.now().plusDays(14))
                                        .build()
                        )
                ))
                .inventoryMap(Map.of(
                        "ITEM-C", MrpContext.InventoryVO.builder()
                                .itemCode("ITEM-C")
                                .availableQty(BigDecimal.ZERO)
                                .onHandQty(BigDecimal.ZERO)
                                .build()
                ))
                .build();

        // When
        MrpResult result = calculator.calculate(context);

        // Then - Should round up to min lot size
        assertEquals("COMPLETED", result.getStatus());
        assertFalse(result.getPurchaseSuggestions().isEmpty());
        BigDecimal suggestedQty = result.getPurchaseSuggestions().get(0).getSuggestQty();
        assertTrue(suggestedQty.compareTo(new BigDecimal("100")) >= 0);
    }

    @Test
    void calculate_withSafetyStockRisk_shouldGenerateAlert() {
        // Given - Inventory below safety stock
        MrpContext context = MrpContext.builder()
                .runId(4L)
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now().plusDays(30))
                .items(Map.of(
                        "ITEM-D", MrpContext.ItemVO.builder()
                                .itemCode("ITEM-D")
                                .itemName("Item D")
                                .source("BUY")
                                .leadTime(7)
                                .lotSizeRule("LOT_FOR_LOT")
                                .safetyStock(new BigDecimal("50")) // Safety stock 50
                                .build()
                ))
                .salesDemandMap(Map.of(
                        "ITEM-D", List.of(
                                MrpContext.DemandVO.builder()
                                        .demandNo("SO-004")
                                        .itemCode("ITEM-D")
                                        .qty(new BigDecimal("30"))
                                        .dueDate(LocalDate.now().plusDays(14))
                                        .build()
                        )
                ))
                .inventoryMap(Map.of(
                        "ITEM-D", MrpContext.InventoryVO.builder()
                                .itemCode("ITEM-D")
                                .availableQty(new BigDecimal("20")) // Below safety stock!
                                .onHandQty(new BigDecimal("20"))
                                .build()
                ))
                .build();

        // When
        MrpResult result = calculator.calculate(context);

        // Then - Should generate risk alert
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getRiskAlerts());
        assertFalse(result.getRiskAlerts().isEmpty());
        assertEquals("STOCKOUT", result.getRiskAlerts().get(0).getRiskType());
    }
}
