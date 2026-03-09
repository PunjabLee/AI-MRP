package com.aimrp.demand.application.service;

import com.aimrp.demand.domain.service.DemandDomainService;
import com.aimrp.demand.infrastructure.persistence.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 需求应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandApplicationService {
    
    private final DemandDomainService domainService;
    private final SalesOrderMapper salesOrderMapper;
    
    /**
     * 创建销售订单
     */
    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        log.info("创建销售订单 - customer: {}", request.getCustomerCode());
        
        // 校验需求
        if (!domainService.validateDemand(request.getItemCode(), request.getQty())) {
            throw new IllegalArgumentException("需求无效");
        }
        
        // 计算优先级
        int priority = domainService.calculatePriority(request.getCustomerLevel(), request.getQty());
        
        // 保存订单
        Long orderId = salesOrderMapper.insert(
                request.getCustomerCode(),
                request.getItemCode(),
                request.getQty(),
                priority,
                request.getDueDate());
        
        return orderId;
    }
    
    /**
     * 查询订单列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listOrders(String customerCode, String status) {
        return salesOrderMapper.selectList(customerCode, status);
    }
    
    /**
     * 请求对象
     */
    @lombok.Data
    public static class CreateOrderRequest {
        private String customerCode;
        private String customerLevel;
        private String itemCode;
        private java.math.BigDecimal qty;
        private java.time.LocalDate dueDate;
    }
}
