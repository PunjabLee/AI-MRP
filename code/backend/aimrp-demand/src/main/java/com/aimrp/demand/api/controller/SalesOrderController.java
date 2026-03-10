package com.aimrp.demand.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.demand.api.dto.SalesOrderCreateRequest;
import com.aimrp.demand.api.dto.SalesOrderResponse;
import com.aimrp.demand.application.service.OrderImportService;
import com.aimrp.demand.domain.entity.SalesOrder;
import com.aimrp.demand.infrastructure.persistence.mapper.SalesOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
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
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String status) {
        
        Page<SalesOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (orderNo != null) wrapper.like(SalesOrder::getOrderNo, orderNo);
        if (customerCode != null) wrapper.eq(SalesOrder::getCustomerCode, customerCode);
        if (status != null) wrapper.eq(SalesOrder::getStatus, status);
        
        wrapper.orderByDesc(SalesOrder::getId);
        
        Page<SalesOrder> result = salesOrderMapper.selectPage(page, wrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 根据 ID 查询
     */
    @GetMapping("/{id}")
    public ApiResponse<SalesOrderResponse> getById(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        return ApiResponse.ok(convertToResponse(order));
    }
    
    /**
     * 创建订单
     */
    @PostMapping
    public ApiResponse<SalesOrderResponse> create(@Validated @RequestBody SalesOrderCreateRequest request) {
        // 转换Request为Entity
        SalesOrder order = new SalesOrder();
        order.setOrderNo("SO" + System.currentTimeMillis());
        order.setCustomerCode(request.getCustomerCode());
        order.setCustomerName(request.getCustomerName());
        order.setItemCode(request.getItemCode());
        order.setItemName(request.getItemName());
        order.setQty(request.getQty());
        order.setUnitPrice(request.getUnitPrice());
        order.setTotalAmount(request.getTotalAmount());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setPriority(request.getPriority() != null ? request.getPriority() : 5);
        order.setRemark(request.getRemark());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDate.now());
        
        salesOrderMapper.insert(order);
        
        return ApiResponse.ok(convertToResponse(order));
    }
    
    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody SalesOrderCreateRequest request) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order == null) {
            return ApiResponse.fail("订单不存在");
        }
        
        order.setCustomerCode(request.getCustomerCode());
        order.setCustomerName(request.getCustomerName());
        order.setItemCode(request.getItemCode());
        order.setItemName(request.getItemName());
        order.setQty(request.getQty());
        order.setUnitPrice(request.getUnitPrice());
        order.setTotalAmount(request.getTotalAmount());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setPriority(request.getPriority());
        order.setRemark(request.getRemark());
        
        salesOrderMapper.updateById(order);
        
        return ApiResponse.ok();
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
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        order.setStatus("CONFIRMED");
        salesOrderMapper.updateById(order);
        return ApiResponse.ok();
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        order.setStatus("CANCELLED");
        salesOrderMapper.updateById(order);
        return ApiResponse.ok();
    }
    
    /**
     * 批量导入订单
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrorCount());
            
            return ApiResponse.ok(response);
            
        } catch (Exception e) {
            return ApiResponse.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取导入模板
     */
    @GetMapping("/import-template")
    public ApiResponse<Map<String, Object>> getImportTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("filename", "订单导入模板.xlsx");
        template.put("fields", new String[]{
            "customerCode:客户编码(必填)",
            "customerName:客户名称",
            "itemCode:物料编码(必填)",
            "itemName:物料名称",
            "qty:数量(必填)",
            "unitPrice:单价",
            "deliveryDate:交货日期",
            "priority:优先级",
            "remark:备注"
        });
        
        return ApiResponse.ok(template);
    }
    
    /**
     * Entity转Response
     */
    private SalesOrderResponse convertToResponse(SalesOrder order) {
        if (order == null) return null;
        
        SalesOrderResponse response = new SalesOrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setCustomerCode(order.getCustomerCode());
        response.setCustomerName(order.getCustomerName());
        response.setItemCode(order.getItemCode());
        response.setItemName(order.getItemName());
        response.setQty(order.getQty());
        response.setUnitPrice(order.getUnitPrice());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());
        response.setDeliveryDate(order.getDeliveryDate());
        response.setStatus(order.getStatus());
        response.setPriority(order.getPriority());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());
        
        return response;
    }
}
