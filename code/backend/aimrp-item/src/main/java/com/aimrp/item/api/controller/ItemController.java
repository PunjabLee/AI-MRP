package com.aimrp.item.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.item.api.dto.ItemCreateRequest;
import com.aimrp.item.api.dto.ItemResponse;
import com.aimrp.item.domain.entity.Item;
import com.aimrp.item.infrastructure.persistence.mapper.ItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 物料 Controller
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemMapper itemMapper;
    
    /**
     * 分页查询物料
     */
    @GetMapping
    public ApiResponse<Page<ItemResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName) {
        
        Page<Item> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        if (itemCode != null) wrapper.like(Item::getItemCode, itemCode);
        if (itemName != null) wrapper.like(Item::getItemName, itemName);
        wrapper.orderByDesc(Item::getId);
        
        Page<Item> result = itemMapper.selectPage(page, wrapper);
        
        // 转换为Response
        Page<ItemResponse> response = new Page<>();
        response.setRecords(result.getRecords().stream().map(this::convertToResponse).toList());
        response.setTotal(result.getTotal());
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public ApiResponse<ItemResponse> getById(@PathVariable Long id) {
        Item item = itemMapper.selectById(id);
        return ApiResponse.ok(convertToResponse(item));
    }
    
    /**
     * 创建物料
     */
    @PostMapping
    public ApiResponse<ItemResponse> create(@Validated @RequestBody ItemCreateRequest request) {
        // 转换Request为Entity
        Item item = convertToEntity(request);
        item.setStatus("ACTIVE");
        
        itemMapper.insert(item);
        
        return ApiResponse.ok(convertToResponse(item));
    }
    
    /**
     * 更新物料
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody ItemCreateRequest request) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            return ApiResponse.fail("物料不存在");
        }
        
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setItemType(request.getItemType());
        item.setUnit(request.getUnit());
        item.setSource(request.getSource());
        item.setLeadTime(request.getLeadTime());
        item.setSafetyStock(request.getSafetyStock());
        item.setMinLotSize(request.getMinLotSize());
        item.setMaxLotSize(request.getMaxLotSize());
        item.setUnitCost(request.getUnitCost());
        item.setRemark(request.getRemark());
        
        itemMapper.updateById(item);
        
        return ApiResponse.ok();
    }
    
    /**
     * 删除物料
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 根据编码查询
     */
    @GetMapping("/code/{itemCode}")
    public ApiResponse<ItemResponse> getByCode(@PathVariable String itemCode) {
        Item item = itemMapper.selectByItemCode(itemCode);
        return ApiResponse.ok(convertToResponse(item));
    }
    
    /**
     * Entity转Response
     */
    private ItemResponse convertToResponse(Item item) {
        if (item == null) return null;
        
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setItemCode(item.getItemCode());
        response.setItemName(item.getItemName());
        response.setItemType(item.getItemType());
        response.setUnit(item.getUnit());
        response.setSource(item.getSource());
        response.setLeadTime(item.getLeadTime());
        response.setSafetyStock(item.getSafetyStock());
        response.setMinLotSize(item.getMinLotSize());
        response.setMaxLotSize(item.getMaxLotSize());
        response.setUnitCost(item.getUnitCost());
        response.setRemark(item.getRemark());
        response.setStatus(item.getStatus());
        response.setCreatedAt(item.getCreatedAt());
        
        return response;
    }
    
    /**
     * Request转Entity
     */
    private Item convertToEntity(ItemCreateRequest request) {
        Item item = new Item();
        item.setItemCode(request.getItemCode());
        item.setItemName(request.getItemName());
        item.setItemType(request.getItemType());
        item.setUnit(request.getUnit());
        item.setSource(request.getSource());
        item.setLeadTime(request.getLeadTime());
        item.setSafetyStock(request.getSafetyStock());
        item.setMinLotSize(request.getMinLotSize());
        item.setMaxLotSize(request.getMaxLotSize());
        item.setUnitCost(request.getUnitCost());
        item.setRemark(request.getRemark());
        return item;
    }
}
