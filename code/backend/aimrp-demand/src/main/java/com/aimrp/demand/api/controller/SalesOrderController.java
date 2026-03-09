package com.aimrp.demand.api.controller;

import com.aimrp.common.result.R;
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
    public R<Page<SalesOrder>> list(
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
        
        return R.ok(salesOrderMapper.selectPage(page, wrapper));
    }
    
    /**
     * 根据 ID 查询
     */
    @GetMapping("/{id}")
    public R<SalesOrder> getById(@PathVariable Long id) {
        return R.ok(salesOrderMapper.selectById(id));
    }
    
    /**
     * 创建订单
     */
    @PostMapping
    public R<SalesOrder> create(@RequestBody SalesOrder order) {
        // 生成订单编号
        String orderNo = "SO" + System.currentTimeMillis();
        order.setOrderNo(orderNo);
        order.setStatus("PENDING");
        salesOrderMapper.insert(order);
        return R.ok(order);
    }
    
    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public R<SalesOrder> update(@PathVariable Long id, @RequestBody SalesOrder order) {
        order.setId(id);
        salesOrderMapper.updateById(order);
        return R.ok(order);
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        salesOrderMapper.deleteById(id);
        return R.ok();
    }
    
    /**
     * 确认订单
     */
    @PostMapping("/{id}/confirm")
    public R<SalesOrder> confirm(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        order.setStatus("CONFIRMED");
        salesOrderMapper.updateById(order);
        return R.ok(order);
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public R<SalesOrder> cancel(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        order.setStatus("CANCELLED");
        salesOrderMapper.updateById(order);
        return R.ok(order);
    }
}
