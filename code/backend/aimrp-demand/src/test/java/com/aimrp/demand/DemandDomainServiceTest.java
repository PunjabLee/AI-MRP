package com.aimrp.demand;

import com.aimrp.demand.domain.service.DemandDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DemandDomainService TDD Tests
 *
 * Following Red -> Green -> Refactor:
 * 1. Write failing test first
 * 2. Run test to verify it fails
 * 3. Implement minimal code to pass
 * 4. Run test to verify it passes
 * 5. Refactor if needed
 */
class DemandDomainServiceTest {

    private DemandDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new DemandDomainService();
    }

    // ===== Priority Calculation Tests =====

    @Test
    void calculatePriority_vipCustomer_shouldReturnHigherPriority() {
        // Given - VIP customer with small order
        String customerLevel = "VIP";
        BigDecimal qty = new BigDecimal("100");

        // When
        int priority = domainService.calculatePriority(customerLevel, qty);

        // Then - VIP should have higher priority (lower number)
        assertTrue(priority < 5, "VIP customer should have higher priority");
    }

    @Test
    void calculatePriority_largeOrder_shouldReturnHigherPriority() {
        // Given - Regular customer with large order
        String customerLevel = "REGULAR";
        BigDecimal qty = new BigDecimal("1500");

        // When
        int priority = domainService.calculatePriority(customerLevel, qty);

        // Then - Large order should have higher priority
        assertTrue(priority <= 4, "Large order should have higher priority");
    }

    @Test
    void calculatePriority_vipWithLargeOrder_shouldReturnHighestPriority() {
        // Given
        String customerLevel = "VIP";
        BigDecimal qty = new BigDecimal("2000");

        // When
        int priority = domainService.calculatePriority(customerLevel, qty);

        // Then - VIP (base-2) + large order (base-1) = 2
        assertEquals(2, priority, "VIP with large order should have highest priority");
    }

    @Test
    void calculatePriority_regularCustomer_shouldReturnMediumPriority() {
        // Given
        String customerLevel = "REGULAR";
        BigDecimal qty = new BigDecimal("100");

        // When
        int priority = domainService.calculatePriority(customerLevel, qty);

        // Then
        assertEquals(5, priority, "Regular customer should have medium priority");
    }

    @Test
    void calculatePriority_priorityShouldBeWithinRange() {
        // Given - extreme case
        String customerLevel = "UNKNOWN";
        BigDecimal qty = new BigDecimal("999999");

        // When
        int priority = domainService.calculatePriority(customerLevel, qty);

        // Then - priority should be between 1 and 10
        assertTrue(priority >= 1 && priority <= 10,
            "Priority should be between 1 and 10, but was: " + priority);
    }

    // ===== Demand Validation Tests =====

    @Test
    void validateDemand_validInput_shouldReturnTrue() {
        // Given
        String itemCode = "ITEM-A";
        BigDecimal qty = new BigDecimal("100");

        // When
        boolean valid = domainService.validateDemand(itemCode, qty);

        // Then
        assertTrue(valid, "Valid demand should return true");
    }

    @Test
    void validateDemand_nullItemCode_shouldReturnFalse() {
        // Given
        String itemCode = null;
        BigDecimal qty = new BigDecimal("100");

        // When
        boolean valid = domainService.validateDemand(itemCode, qty);

        // Then
        assertFalse(valid, "Null item code should return false");
    }

    @Test
    void validateDemand_emptyItemCode_shouldReturnFalse() {
        // Given
        String itemCode = "";
        BigDecimal qty = new BigDecimal("100");

        // When
        boolean valid = domainService.validateDemand(itemCode, qty);

        // Then
        assertFalse(valid, "Empty item code should return false");
    }

    @Test
    void validateDemand_zeroQty_shouldReturnFalse() {
        // Given
        String itemCode = "ITEM-A";
        BigDecimal qty = BigDecimal.ZERO;

        // When
        boolean valid = domainService.validateDemand(itemCode, qty);

        // Then
        assertFalse(valid, "Zero quantity should return false");
    }

    @Test
    void validateDemand_negativeQty_shouldReturnFalse() {
        // Given
        String itemCode = "ITEM-A";
        BigDecimal qty = new BigDecimal("-10");

        // When
        boolean valid = domainService.validateDemand(itemCode, qty);

        // Then
        assertFalse(valid, "Negative quantity should return false");
    }

    @Test
    void validateDemand_nullQty_shouldReturnFalse() {
        // Given
        String itemCode = "ITEM-A";
        BigDecimal qty = null;

        // When
        boolean valid = domainService.validateDemand(itemCode, qty);

        // Then
        assertFalse(valid, "Null quantity should return false");
    }
}
