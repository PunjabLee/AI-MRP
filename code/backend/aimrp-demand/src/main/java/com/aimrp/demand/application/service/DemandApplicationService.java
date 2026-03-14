package com.aimrp.demand.application.service;

import com.aimrp.demand.domain.entity.SalesOrder;
import com.aimrp.demand.domain.service.DemandDomainService;
import com.aimrp.demand.infrastructure.persistence.mapper.SalesOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
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

        // 创建实体并保存
        SalesOrder order = new SalesOrder();
        order.setCustomerCode(request.getCustomerCode());
        order.setItemCode(request.getItemCode());
        order.setQty(request.getQty());
        order.setPriority(priority);
        order.setDueDate(request.getDueDate());
        order.setStatus("DRAFT");

        salesOrderMapper.insert(order);

        return order.getId();
    }

    /**
     * 查询订单列表
     */
    @Transactional(readOnly = true)
    public List<SalesOrder> listOrders(String customerCode, String status) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (customerCode != null && !customerCode.isEmpty()) {
            wrapper.eq(SalesOrder::getCustomerCode, customerCode);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SalesOrder::getStatus, status);
        }
        return salesOrderMapper.selectList(wrapper);
    }

    /**
     * 请求对象
     */
    @Data
    public static class CreateOrderRequest {
        private String customerCode;
        private String customerLevel;
        private String itemCode;
        private java.math.BigDecimal qty;
        private java.time.LocalDate dueDate;
    }
}
