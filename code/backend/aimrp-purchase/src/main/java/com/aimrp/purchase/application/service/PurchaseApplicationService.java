package com.aimrp.purchase.application.service;

import com.aimrp.purchase.domain.service.PurchaseDomainService;
import com.aimrp.purchase.infrastructure.persistence.mapper.PurchaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 采购应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseApplicationService {
    
    private final PurchaseDomainService domainService;
    private final PurchaseMapper mapper;
    
    /**
     * 创建采购订单
     */
    @Transactional
    public Long createPurchaseOrder(CreatePurchaseOrderRequest request) {
        log.info("创建采购订单 - supplier: {}", request.getSupplierCode());
        
        // 计算金额
        var amount = domainService.calculateAmount(request.getUnitPrice(), request.getQty());
        var totalWithTax = domainService.calculateTotalWithTax(amount, request.getTaxRate());
        
        // 保存并返回实际ID
        Long orderId = mapper.insertPurchaseOrder(request.getSupplierCode(), request.getItemCode(), 
                request.getQty(), request.getUnitPrice(), totalWithTax);
        
        return orderId;
    }
    
    /**
     * 采购入库
     */
    @Transactional
    public void receive(Long orderId, java.math.BigDecimal qty) {
        log.info("采购入库 - orderId: {}, qty: {}", orderId, qty);
        mapper.updateReceivedQty(orderId, qty);
    }
    
    /**
     * 查询采购订单
     */
    @Transactional(readOnly = true)
    public List<?> list(String supplierCode, String status) {
        return mapper.selectList(supplierCode, status);
    }
    
    /**
     * 请求对象
     */
    @lombok.Data
    public static class CreatePurchaseOrderRequest {
        private String supplierCode;
        private String itemCode;
        private java.math.BigDecimal qty;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal taxRate;
    }
}
