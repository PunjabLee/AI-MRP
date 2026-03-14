package com.aimrp.inventory.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 库存领域服务
 */
@Slf4j
@Service
public class InventoryDomainService {
    
    /**
     * 计算可用库存
     */
    public BigDecimal calculateAvailable(BigDecimal onHandQty, BigDecimal allocatedQty) {
        if (onHandQty == null) return BigDecimal.ZERO;
        if (allocatedQty == null) allocatedQty = BigDecimal.ZERO;
        return onHandQty.subtract(allocatedQty);
    }
    
    /**
     * 检查库存是否充足
     */
    public boolean isSufficient(BigDecimal availableQty, BigDecimal requiredQty) {
        return availableQty.compareTo(requiredQty) >= 0;
    }
    
    /**
     * 计算缺料数量
     */
    public BigDecimal calculateShortage(BigDecimal availableQty, BigDecimal requiredQty) {
        if (availableQty.compareTo(requiredQty) >= 0) {
            return BigDecimal.ZERO;
        }
        return requiredQty.subtract(availableQty);
    }
}
