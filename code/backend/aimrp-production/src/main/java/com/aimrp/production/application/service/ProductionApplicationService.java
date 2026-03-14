package com.aimrp.production.application.service;

import com.aimrp.production.domain.service.SchedulerService;
import com.aimrp.production.infrastructure.persistence.mapper.ProductionOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 生产应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionApplicationService {
    
    private final SchedulerService schedulerService;
    private final ProductionOrderMapper productionOrderMapper;
    
    /**
     * 排产
     */
    @Transactional
    public List<Map<String, Object>> schedule(ScheduleRequest request) {
        log.info("执行排产 - method: {}", request.getMethod());
        
        // 获取工单列表
        List<Map<String, Object>> orders = productionOrderMapper.selectList(null, null);
        
        // 调用排程服务
        return schedulerService.schedule(orders, request.getMethod());
    }
    
    /**
     * 创建生产工单
     */
    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        log.info("创建生产工单 - item: {}", request.getItemCode());
        
        Map<String, Object> order = new java.util.HashMap<>();
        order.put("item_code", request.getItemCode());
        order.put("qty", request.getQty());
        order.put("due_date", request.getDueDate());
        
        return productionOrderMapper.insert(order);
    }
    
    /**
     * 开始生产
     */
    @Transactional
    public void startProduction(Long orderId) {
        log.info("开始生产 - orderId: {}", orderId);
        productionOrderMapper.updateStatus(orderId, "IN_PRODUCTION");
    }
    
    /**
     * 结束生产
     */
    @Transactional
    public void completeProduction(Long orderId) {
        log.info("完成生产 - orderId: {}", orderId);
        productionOrderMapper.updateStatus(orderId, "COMPLETED");
    }
    
    @Data
    public static class ScheduleRequest {
        private String method; // FIFO, PRIORITY, EDD, SPT
    }
    
    @Data
    public static class CreateOrderRequest {
        private String itemCode;
        private java.math.BigDecimal qty;
        private java.time.LocalDate dueDate;
    }
}
