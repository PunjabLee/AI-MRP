package com.aimrp.supplier.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 供应商领域服务
 */
@Slf4j
@Service
public class SupplierDomainService {
    
    /**
     * 校验供应商编码
     */
    public boolean validateSupplierCode(String supplierCode) {
        if (supplierCode == null || supplierCode.isEmpty()) {
            return false;
        }
        return supplierCode.matches("^S[0-9]{3,10}$");
    }
    
    /**
     * 计算供应商等级
     */
    public String calculateGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 70) return "B";
        if (score >= 50) return "C";
        return "D";
    }
}
