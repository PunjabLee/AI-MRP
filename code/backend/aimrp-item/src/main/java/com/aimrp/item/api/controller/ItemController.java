package com.aimrp.item.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.item.domain.entity.Item;
import com.aimrp.item.infrastructure.persistence.mapper.ItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 物料 Controller
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemMapper itemMapper;
    
    @GetMapping
    public ApiResponse<Page<Item>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName) {
        
        Page<Item> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        if (itemCode != null) wrapper.like(Item::getItemCode, itemCode);
        if (itemName != null) wrapper.like(Item::getItemName, itemName);
        wrapper.orderByDesc(Item::getId);
        
        return ApiResponse.ok(itemMapper.selectPage(page, wrapper));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Item> getById(@PathVariable Long id) {
        return ApiResponse.ok(itemMapper.selectById(id));
    }
    
    @PostMapping
    public ApiResponse<Item> create(@RequestBody Item item) {
        itemMapper.insert(item);
        return ApiResponse.ok(item);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Item> update(@PathVariable Long id, @RequestBody Item item) {
        item.setId(id);
        itemMapper.updateById(item);
        return ApiResponse.ok(item);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
