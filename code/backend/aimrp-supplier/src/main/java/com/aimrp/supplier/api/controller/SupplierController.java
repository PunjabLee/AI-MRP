package com.aimrp.supplier.api.controller;

import com.aimrp.common.result.R;
import com.aimrp.supplier.domain.entity.Supplier;
import com.aimrp.supplier.infrastructure.persistence.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    
    private final SupplierMapper supplierMapper;
    
    @GetMapping
    public R<Page<Supplier>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName) {
        
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierCode != null) wrapper.like(Supplier::getSupplierCode, supplierCode);
        if (supplierName != null) wrapper.like(Supplier::getSupplierName, supplierName);
        wrapper.orderByDesc(Supplier::getId);
        
        return R.ok(supplierMapper.selectPage(page, wrapper));
    }
    
    @GetMapping("/{id}")
    public R<Supplier> getById(@PathVariable Long id) {
        return R.ok(supplierMapper.selectById(id));
    }
    
    @PostMapping
    public R<Supplier> create(@RequestBody Supplier supplier) {
        supplierMapper.insert(supplier);
        return R.ok(supplier);
    }
    
    @PutMapping("/{id}")
    public R<Supplier> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierMapper.updateById(supplier);
        return R.ok(supplier);
    }
    
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        supplierMapper.deleteById(id);
        return R.ok();
    }
}
