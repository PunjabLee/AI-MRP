package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.inventory.api.dto.InventoryCreateRequest;
import com.aimrp.inventory.api.dto.InventoryTransRequest;
import com.aimrp.inventory.domain.entity.Inventory;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理 Controller
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryMapper inventoryMapper;
    
    /**
     * 查询库存列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        List<Map<String, Object>> list = inventoryMapper.selectList(itemCode, warehouseCode);
        
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<Map<String, Object>> pageList = fromIndex < total ?
                list.subList(fromIndex, toIndex) : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 库存详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable Long id) {
        Inventory inventory = inventoryMapper.selectById(id);
        Map<String, Object> result = new HashMap<>();
        if (inventory != null) {
            result.put("id", inventory.getId());
            result.put("itemCode", inventory.getItemCode());
            result.put("warehouseCode", inventory.getWarehouseCode());
            result.put("onHandQty", inventory.getOnHandQty());
        }
        return ApiResponse.ok(result);
    }
    
    /**
     * 按物料查询
     */
    @GetMapping("/item/{itemCode}")
    public ApiResponse<List<Map<String, Object>>> getByItemCode(@PathVariable String itemCode) {
        List<Map<String, Object>> list = inventoryMapper.selectList(itemCode, null);
        return ApiResponse.ok(list);
    }
    
    /**
     * 入库
     */
    @PostMapping("/in")
    public ApiResponse<Map<String, Object>> inStock(@Validated @RequestBody InventoryTransRequest request) {
        // 查询现有库存
        Map<String, Object> existing = inventoryMapper.selectByItemAndWarehouse(
            request.getItemCode(), request.getWarehouseCode());
        
        if (existing != null) {
            Long id = ((Number) existing.get("id")).longValue();
            inventoryMapper.increaseQty(id, request.getQty());
        } else {
            inventoryMapper.insert(request.getItemCode(), request.getWarehouseCode(), 
                request.getQty() != null ? request.getQty() : BigDecimal.ZERO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "入库成功");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 出库
     */
    @PostMapping("/out")
    public ApiResponse<Map<String, Object>> outStock(@Validated @RequestBody InventoryTransRequest request) {
        Map<String, Object> existing = inventoryMapper.selectByItemAndWarehouse(
            request.getItemCode(), request.getWarehouseCode());
        
        if (existing == null) {
            return ApiResponse.fail("库存记录不存在");
        }
        
        BigDecimal availableQty = new BigDecimal(existing.get("available_qty").toString());
        if (availableQty.compareTo(request.getQty()) < 0) {
            return ApiResponse.fail("库存不足");
        }
        
        Long id = ((Number) existing.get("id")).longValue();
        inventoryMapper.decreaseQty(id, request.getQty());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "出库成功");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 创建库存
     */
    @PostMapping
    public ApiResponse<Inventory> create(@Validated @RequestBody InventoryCreateRequest request) {
        Inventory inventory = new Inventory();
        inventory.setItemCode(request.getItemCode());
        inventory.setWarehouseCode(request.getWarehouseCode());
        inventory.setLocationCode(request.getLocationCode());
        inventory.setOnHandQty(request.getOnHandQty() != null ? request.getOnHandQty() : BigDecimal.ZERO);
        inventory.setAvailableQty(request.getOnHandQty() != null ? request.getOnHandQty() : BigDecimal.ZERO);
        
        inventoryMapper.insert(inventory.getItemCode(), inventory.getWarehouseCode(), inventory.getOnHandQty());
        
        return ApiResponse.ok(inventory);
    }
}
