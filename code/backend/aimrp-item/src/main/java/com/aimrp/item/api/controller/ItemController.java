package com.aimrp.item.api.controller;

import com.aimrp.common.result.R;
import com.aimrp.item.application.dto.ItemDTO;
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
    
    /**
     * 分页查询
     */
    @GetMapping
    public R<Page<Item>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String status) {
        
        Page<Item> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        if (itemCode != null) {
            wrapper.like(Item::getItemCode, itemCode);
        }
        if (itemName != null) {
            wrapper.like(Item::getItemName, itemName);
        }
        if (itemType != null) {
            wrapper.eq(Item::getItemType, itemType);
        }
        if (status != null) {
            wrapper.eq(Item::getStatus, status);
        }
        
        wrapper.orderByDesc(Item::getId);
        
        return R.ok(itemMapper.selectPage(page, wrapper));
    }
    
    /**
     * 根据 ID 查询
     */
    @GetMapping("/{id}")
    public R<Item> getById(@PathVariable Long id) {
        Item item = itemMapper.selectById(id);
        return R.ok(item);
    }
    
    /**
     * 根据编码查询
     */
    @GetMapping("/code/{itemCode}")
    public R<Item> getByCode(@PathVariable String itemCode) {
        Item item = itemMapper.selectOne(
            new LambdaQueryWrapper<Item>().eq(Item::getItemCode, itemCode)
        );
        return R.ok(item);
    }
    
    /**
     * 创建物料
     */
    @PostMapping
    public R<Item> create(@RequestBody ItemDTO dto) {
        Item item = new Item();
        copyDTOToEntity(dto, item);
        itemMapper.insert(item);
        return R.ok(item);
    }
    
    /**
     * 更新物料
     */
    @PutMapping("/{id}")
    public R<Item> update(@PathVariable Long id, @RequestBody ItemDTO dto) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            return R.fail("物料不存在");
        }
        copyDTOToEntity(dto, item);
        itemMapper.updateById(item);
        return R.ok(item);
    }
    
    /**
     * 删除物料
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return R.ok();
    }
    
    private void copyDTOToEntity(ItemDTO dto, Item item) {
        if (dto.getItemCode() != null) item.setItemCode(dto.getItemCode());
        if (dto.getItemName() != null) item.setItemName(dto.getItemName());
        if (dto.getItemType() != null) item.setItemType(dto.getItemType());
        if (dto.getSpec() != null) item.setSpec(dto.getSpec());
        if (dto.getUnit() != null) item.setUnit(dto.getUnit());
        if (dto.getCategoryId() != null) item.setCategoryId(dto.getCategoryId());
        if (dto.getSource() != null) item.setSource(dto.getSource());
        if (dto.getSafetyStock() != null) item.setSafetyStock(dto.getSafetyStock());
        if (dto.getMinStock() != null) item.setMinStock(dto.getMinStock());
        if (dto.getMaxStock() != null) item.setMaxStock(dto.getMaxStock());
        if (dto.getLeadTime() != null) item.setLeadTime(dto.getLeadTime());
        if (dto.getMoq() != null) item.setMoq(dto.getMoq());
        if (dto.getStandardCost() != null) item.setStandardCost(dto.getStandardCost());
        if (dto.getStatus() != null) item.setStatus(dto.getStatus());
        if (dto.getMemo() != null) item.setMemo(dto.getMemo());
    }
}
