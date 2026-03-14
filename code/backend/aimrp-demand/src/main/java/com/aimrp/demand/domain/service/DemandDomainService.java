package com.aimrp.demand.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 需求领域服务
 */
@Slf4j
@Service
public class DemandDomainService {
    
    /**
     * 计算需求优先级
     */
    public int calculatePriority(String customerLevel, BigDecimal qty) {
        int basePriority = 5;
        
        // VIP 客户优先级提高
        if ("VIP".equals(customerLevel)) {
            basePriority -= 2;
        }
        
        // 大客户优先级提高
        if (qty.compareTo(new BigDecimal("1000")) > 0) {
            basePriority -= 1;
        }
        
        return Math.max(1, Math.min(10, basePriority));
    }
    
    /**
     * 校验需求有效性
     */
    public boolean validateDemand(String itemCode, BigDecimal qty) {
        if (itemCode == null || itemCode.isEmpty()) {
            return false;
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }
}
