package com.aimrp.supplier.application.service;

import com.aimrp.supplier.domain.service.SupplierDomainService;
import com.aimrp.supplier.infrastructure.persistence.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 供应商应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierApplicationService {
    
    private final SupplierDomainService domainService;
    private final SupplierMapper supplierMapper;
    
    /**
     * 创建供应商
     */
    @Transactional
    public Long createSupplier(CreateSupplierRequest request) {
        log.info("创建供应商 - supplierCode: {}", request.getSupplierCode());
        
        if (!domainService.validateSupplierCode(request.getSupplierCode())) {
            throw new IllegalArgumentException("供应商编码格式不正确");
        }
        
        return supplierMapper.insert(request.getSupplierCode(), request.getSupplierName(), 
                request.getContact(), request.getPhone());
    }
    
    /**
     * 查询供应商列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String grade) {
        return supplierMapper.selectList(grade);
    }
    
    @Data
    public static class CreateSupplierRequest {
        private String supplierCode;
        private String supplierName;
        private String contact;
        private String phone;
    }
}
