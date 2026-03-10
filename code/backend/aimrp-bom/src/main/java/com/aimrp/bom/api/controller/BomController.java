package com.aimrp.bom.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.bom.api.dto.BomCreateRequest;
import com.aimrp.bom.domain.entity.Bom;
import com.aimrp.bom.infrastructure.persistence.mapper.BomMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * BOM Controller
 */
@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {
    
    private final BomMapper bomMapper;
    
    /**
     * 查询BOM列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String parentItemCode,
            @RequestParam(required = false) String keyword) {
        
        Page<Bom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<>();
        
        if (parentItemCode != null) {
            wrapper.eq(Bom::getItemCode, parentItemCode);
        }
        if (keyword != null) {
            wrapper.like(Bom::getItemName, keyword);
        }
        
        wrapper.orderByDesc(Bom::getId);
        
        Page<Bom> result = bomMapper.selectPage(page, wrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 查询BOM详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Bom> getById(@PathVariable Long id) {
        Bom bom = bomMapper.selectById(id);
        return ApiResponse.ok(bom);
    }
    
    /**
     * 创建BOM
     */
    @PostMapping
    public ApiResponse<Bom> create(@Validated @RequestBody BomCreateRequest request) {
        Bom bom = new Bom();
        bom.setItemCode(request.getItemCode());
        bom.setChildItemCode(request.getChildItemCode());
        bom.setUsageQty(request.getUsageQty());
        bom.setLossRate(request.getLossRate());
        bom.setLevel(request.getLevel());
        bom.setStatus("DRAFT");
        
        bomMapper.insert(bom);
        return ApiResponse.ok(bom);
    }
    
    /**
     * 更新BOM
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody BomCreateRequest request) {
        Bom bom = bomMapper.selectById(id);
        bom.setChildItemCode(request.getChildItemCode());
        bom.setUsageQty(request.getUsageQty());
        bom.setLossRate(request.getLossRate());
        bom.setLevel(request.getLevel());
        
        bomMapper.updateById(bom);
        return ApiResponse.ok();
    }
    
    /**
     * 删除BOM
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bomMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * BOM展开
     */
    @PostMapping("/expand")
    public ApiResponse<Object> expand(@RequestBody Map<String, Object> params) {
        String itemCode = (String) params.get("itemCode");
        Integer level = (Integer) params.get("level");
        
        var result = bomMapper.selectExpand(itemCode, level != null ? level : 3);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取物料的BOM
     */
    @GetMapping("/parent/{parentItemCode}")
    public ApiResponse<Object> getByParent(@PathVariable String parentItemCode) {
        var list = bomMapper.selectBomMap(parentItemCode);
        return ApiResponse.ok(list);
    }
}
