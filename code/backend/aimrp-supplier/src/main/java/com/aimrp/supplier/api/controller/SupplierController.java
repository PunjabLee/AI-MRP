package com.aimrp.supplier.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.supplier.api.dto.SupplierCreateRequest;
import com.aimrp.supplier.api.dto.SupplierResponse;
import com.aimrp.supplier.domain.entity.Supplier;
import com.aimrp.supplier.infrastructure.persistence.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商 Controller
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    
    private final SupplierMapper supplierMapper;
    
    /**
     * 分页查询供应商
     */
    @GetMapping
    public ApiResponse<Page<SupplierResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName) {
        
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierCode != null) wrapper.like(Supplier::getSupplierCode, supplierCode);
        if (supplierName != null) wrapper.like(Supplier::getSupplierName, supplierName);
        wrapper.orderByDesc(Supplier::getId);
        
        Page<Supplier> result = supplierMapper.selectPage(page, wrapper);
        
        Page<SupplierResponse> response = new Page<>();
        response.setRecords(result.getRecords().stream().map(this::convertToResponse).toList());
        response.setTotal(result.getTotal());
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getById(@PathVariable Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        return ApiResponse.ok(convertToResponse(supplier));
    }
    
    /**
     * 创建供应商
     */
    @PostMapping
    public ApiResponse<SupplierResponse> create(@Validated @RequestBody SupplierCreateRequest request) {
        // 检查编码是否存在
        Supplier exist = supplierMapper.selectBySupplierCode(request.getSupplierCode());
        if (exist != null) {
            return ApiResponse.fail("供应商编码已存在");
        }
        
        Supplier supplier = convertToEntity(request);
        supplier.setStatus("ACTIVE");
        supplierMapper.insert(supplier);
        
        return ApiResponse.ok(convertToResponse(supplier));
    }
    
    /**
     * 更新供应商
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody SupplierCreateRequest request) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            return ApiResponse.fail("供应商不存在");
        }
        
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContact(request.getContact());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setLeadTimeDays(request.getLeadTimeDays());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setRemark(request.getRemark());
        
        supplierMapper.updateById(supplier);
        
        return ApiResponse.ok();
    }
    
    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 根据编码查询
     */
    @GetMapping("/code/{supplierCode}")
    public ApiResponse<SupplierResponse> getByCode(@PathVariable String supplierCode) {
        Supplier supplier = supplierMapper.selectBySupplierCode(supplierCode);
        return ApiResponse.ok(convertToResponse(supplier));
    }
    
    /**
     * Entity转Response
     */
    private SupplierResponse convertToResponse(Supplier supplier) {
        if (supplier == null) return null;
        
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setSupplierCode(supplier.getSupplierCode());
        response.setSupplierName(supplier.getSupplierName());
        response.setContact(supplier.getContact());
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setAddress(supplier.getAddress());
        response.setLeadTimeDays(supplier.getLeadTimeDays());
        response.setPaymentTerms(supplier.getPaymentTerms());
        response.setStatus(supplier.getStatus());
        response.setRemark(supplier.getRemark());
        response.setCreatedAt(supplier.getCreatedAt());
        
        return response;
    }
    
    /**
     * Request转Entity
     */
    private Supplier convertToEntity(SupplierCreateRequest request) {
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContact(request.getContact());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setLeadTimeDays(request.getLeadTimeDays());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setRemark(request.getRemark());
        return supplier;
    }
}
