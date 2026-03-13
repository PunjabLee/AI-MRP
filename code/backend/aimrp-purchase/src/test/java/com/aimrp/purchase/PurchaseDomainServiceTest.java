package com.aimrp.purchase;

import com.aimrp.purchase.domain.service.PurchaseDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PurchaseDomainService TDD Tests
 */
class PurchaseDomainServiceTest {

    private PurchaseDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new PurchaseDomainService();
    }

    // ===== Calculate Amount Tests =====

    @Test
    void calculateAmount_validInputs_shouldReturnCorrectAmount() {
        // Given
        BigDecimal price = new BigDecimal("10.00");
        BigDecimal qty = new BigDecimal("100");

        // When
        BigDecimal amount = domainService.calculateAmount(price, qty);

        // Then
        assertEquals(0, new BigDecimal("1000").compareTo(amount));
    }

    @Test
    void calculateAmount_nullPrice_shouldReturnZero() {
        // Given
        BigDecimal price = null;
        BigDecimal qty = new BigDecimal("100");

        // When
        BigDecimal amount = domainService.calculateAmount(price, qty);

        // Then
        assertEquals(BigDecimal.ZERO, amount);
    }

    @Test
    void calculateAmount_nullQty_shouldReturnZero() {
        // Given
        BigDecimal price = new BigDecimal("10");
        BigDecimal qty = null;

        // When
        BigDecimal amount = domainService.calculateAmount(price, qty);

        // Then
        assertEquals(BigDecimal.ZERO, amount);
    }

    // ===== Calculate Tax Tests =====

    @Test
    void calculateTax_validInputs_shouldReturnCorrectTax() {
        // Given
        BigDecimal amount = new BigDecimal("1000");
        BigDecimal taxRate = new BigDecimal("0.13");

        // When
        BigDecimal tax = domainService.calculateTax(amount, taxRate);

        // Then
        assertEquals(0, new BigDecimal("130").compareTo(tax));
    }

    @Test
    void calculateTax_nullAmount_shouldReturnZero() {
        // Given
        BigDecimal amount = null;
        BigDecimal taxRate = new BigDecimal("0.13");

        // When
        BigDecimal tax = domainService.calculateTax(amount, taxRate);

        // Then
        assertEquals(BigDecimal.ZERO, tax);
    }

    @Test
    void calculateTax_nullTaxRate_shouldReturnZero() {
        // Given
        BigDecimal amount = new BigDecimal("1000");
        BigDecimal taxRate = null;

        // When
        BigDecimal tax = domainService.calculateTax(amount, taxRate);

        // Then
        assertEquals(BigDecimal.ZERO, tax);
    }

    // ===== Calculate Total With Tax Tests =====

    @Test
    void calculateTotalWithTax_shouldReturnAmountPlusTax() {
        // Given
        BigDecimal amount = new BigDecimal("1000");
        BigDecimal taxRate = new BigDecimal("0.13");

        // When
        BigDecimal total = domainService.calculateTotalWithTax(amount, taxRate);

        // Then - 1000 + 130 = 1130
        assertEquals(0, new BigDecimal("1130").compareTo(total));
    }

    @Test
    void calculateTotalWithTax_zeroTaxRate_shouldReturnOriginalAmount() {
        // Given
        BigDecimal amount = new BigDecimal("1000");
        BigDecimal taxRate = BigDecimal.ZERO;

        // When
        BigDecimal total = domainService.calculateTotalWithTax(amount, taxRate);

        // Then
        assertEquals(0, amount.compareTo(total));
    }

    @Test
    void calculateTotalWithTax_nullValues_shouldReturnZero() {
        // Given
        BigDecimal amount = null;
        BigDecimal taxRate = null;

        // When
        BigDecimal total = domainService.calculateTotalWithTax(amount, taxRate);

        // Then
        assertEquals(BigDecimal.ZERO, total);
    }
}
