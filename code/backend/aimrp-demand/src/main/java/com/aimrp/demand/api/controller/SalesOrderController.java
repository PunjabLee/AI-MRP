package com.aimrp.demand.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.demand.application.service.OrderImportService;
import com.aimrp.demand.domain.entity.SalesOrder;
import com.aimrp.demand.infrastructure.persistence.mapper.SalesOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

/**
 * 销售订单 Controller
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class SalesOrderController {
    
    private final SalesOrderMapper salesOrderMapper;
    private final OrderImportService orderImportService;
    
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
    
    /**
     * 批量导入订单
     * 
     * POST /api/orders/import
     * 支持Excel格式(.xlsx)，表头需包含：
     * - customerCode: 客户编码
     * - customerName: 客户名称
     * - itemCode: 物料编码
     * - itemName: 物料名称
     * - qty: 数量
     * - unitPrice: 单价
     * - deliveryDate: 交货日期
     */
    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importOrders(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail("请选择要导入的文件");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ApiResponse.fail("仅支持Excel文件(.xlsx, .xls)");
        }
        
        try {
            OrderImportService.ImportResult result = orderImportService.importOrders(file);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrorCount());
            response.put("successList", result.getSuccessList());
            response.put("errorList", result.getErrorList());
            
            return ApiResponse.ok(response);
            
        } catch (Exception e) {
            log.error("导入订单失败", e);
            return ApiResponse.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载导入模板
     */
    @GetMapping("/import-template")
    public ApiResponse<Map<String, Object>> getImportTemplate() {
        // 返回模板字段说明
        Map<String, Object> template = new java.util.HashMap<>();
        template.put("filename", "订单导入模板.xlsx");
        template.put("fields", new String[]{
            "customerCode:客户编码(必填)",
            "customerName:客户名称",
            "itemCode:物料编码(必填)",
            "itemName:物料名称",
            "qty:数量(必填)",
            "unitPrice:单价",
            "deliveryDate:交货日期(yyyy-MM-dd)",
            "priority:优先级(1-10)",
            "remark:备注"
        });
        
        return ApiResponse.ok(template);
    }
}
