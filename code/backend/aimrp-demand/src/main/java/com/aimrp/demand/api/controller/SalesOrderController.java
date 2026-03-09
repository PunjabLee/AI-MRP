package com.aimrp.demand.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.demand.domain.entity.SalesOrder;
import com.aimrp.demand.infrastructure.persistence.mapper.SalesOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 销售订单 Controller
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class SalesOrderController {
    
    private final SalesOrderMapper salesOrderMapper;
    
    /**
     * 分页查询订单
     */
    @GetMapping
    public ApiResponse<Page<SalesOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        Page<SalesOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (orderNo != null) wrapper.like(SalesOrder::getOrderNo, orderNo);
        if (customerCode != null) wrapper.eq(SalesOrder::getCustomerCode, customerCode);
        if (status != null) wrapper.eq(SalesOrder::getStatus, status);
        if (startDate != null) wrapper.ge(SalesOrder::getOrderDate, startDate);
        if (endDate != null) wrapper.le(SalesOrder::getOrderDate, endDate);
        
        wrapper.orderByDesc(SalesOrder::getId);
        
        return ApiResponse.ok(salesOrderMapper.selectPage(page, wrapper));
    }
    
    /**
     * 根据 ID 查询
     */
    @GetMapping("/{id}")
    public ApiResponse<SalesOrder> getById(@PathVariable Long id) {
        return ApiResponse.ok(salesOrderMapper.selectById(id));
    }
    
    /**
     * 创建订单
     */
    @PostMapping
    public ApiResponse<SalesOrder> create(@RequestBody SalesOrder order) {
        // 生成订单编号
        String orderNo = "SO" + System.currentTimeMillis();
        order.setOrderNo(orderNo);
        order.setStatus("PENDING");
        salesOrderMapper.insert(order);
        return ApiResponse.ok(order);
    }
    
    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public ApiResponse<SalesOrder> update(@PathVariable Long id, @RequestBody SalesOrder order) {
        order.setId(id);
        salesOrderMapper.updateById(order);
        return ApiResponse.ok(order);
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        salesOrderMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 确认订单
     */
    @PostMapping("/{id}/confirm")
    public ApiResponse<SalesOrder> confirm(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        order.setStatus("CONFIRMED");
        salesOrderMapper.updateById(order);
        return ApiResponse.ok(order);
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesOrder> cancel(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        order.setStatus("CANCELLED");
        salesOrderMapper.updateById(order);
        return ApiResponse.ok(order);
    }
}
