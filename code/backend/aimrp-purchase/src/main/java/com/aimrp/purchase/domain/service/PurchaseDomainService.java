package com.aimrp.purchase.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 采购领域服务
 */
@Slf4j
@Service
public class PurchaseDomainService {
    
    /**
     * 计算采购金额
     */
    public BigDecimal calculateAmount(BigDecimal price, BigDecimal qty) {
        if (price == null || qty == null) return BigDecimal.ZERO;
        return price.multiply(qty);
    }
    
    /**
     * 计算税率
     */
    public BigDecimal calculateTax(BigDecimal amount, BigDecimal taxRate) {
        if (amount == null || taxRate == null) return BigDecimal.ZERO;
        return amount.multiply(taxRate);
    }
    
    /**
     * 计算含税金额
     */
    public BigDecimal calculateTotalWithTax(BigDecimal amount, BigDecimal taxRate) {
        return amount.add(calculateTax(amount, taxRate));
    }
}
