package com.aimrp.bom.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.bom.domain.entity.Bom;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {
    
    @GetMapping
    public ApiResponse<Page<Bom>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode) {
        
        Page<Bom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<>();
        if (itemCode != null) wrapper.eq(Bom::getItemCode, itemCode);
        wrapper.orderByDesc(Bom::getId);
        
        // Mock data for demo
        List<Bom> list = new ArrayList<>();
        Bom b = new Bom();
        b.setId(1L);
        b.setBomNo("BOM001");
        b.setItemCode("A001");
        b.setItemName("产品A");
        b.setVersion("V1");
        b.setStatus("ACTIVE");
        list.add(b);
        
        page.setRecords(list);
        page.setTotal(1);
        return ApiResponse.ok(page);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Bom> getById(@PathVariable Long id) {
        Bom bom = new Bom();
        bom.setId(id);
        bom.setBomNo("BOM001");
        bom.setItemCode("A001");
        return ApiResponse.ok(bom);
    }
    
    @PostMapping
    public ApiResponse<Bom> create(@RequestBody Bom bom) {
        bom.setId(1L);
        bom.setBomNo("BOM" + System.currentTimeMillis());
        bom.setStatus("DRAFT");
        return ApiResponse.ok(bom);
    }
    
    @GetMapping("/{itemCode}/expand")
    public ApiResponse<List<Map<String, Object>>> expand(@PathVariable String itemCode) {
        // 模拟 BOM 展开
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(Map.of("level", 1, "itemCode", "A001", "itemName", "产品A", "childCode", "B001", "childName", "部件B", "qty", 2));
        result.add(Map.of("level", 2, "itemCode", "B001", "itemName", "部件B", "childCode", "C001", "childName", "物料C", "qty", 5));
        return ApiResponse.ok(result);
    }
}
