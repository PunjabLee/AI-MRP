package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.inventory.domain.entity.Inventory;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
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
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 从数据库查询
        List<Map<String, Object>> list = inventoryMapper.selectList(itemCode, warehouseCode);
        
        // 分页
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
        // TODO: 根据ID查询
        return ApiResponse.ok(new HashMap<>());
    }
    
    /**
     * 根据物料查询库存
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
    public ApiResponse<Map<String, Object>> inStock(@RequestBody Map<String, Object> params) {
        String itemCode = (String) params.get("itemCode");
        String warehouseCode = (String) params.get("warehouseCode");
        BigDecimal qty = new BigDecimal(params.get("qty").toString());
        String type = (String) params.get("type");
        
        // 查询现有库存
        Map<String, Object> existing = inventoryMapper.selectByItemAndWarehouse(itemCode, warehouseCode);
        
        if (existing != null) {
            // 更新数量
            Long id = ((Number) existing.get("id")).longValue();
            inventoryMapper.increaseQty(id, qty);
        } else {
            // 创建新库存记录
            inventoryMapper.insert(itemCode, warehouseCode, qty);
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
    public ApiResponse<Map<String, Object>> outStock(@RequestBody Map<String, Object> params) {
        String itemCode = (String) params.get("itemCode");
        String warehouseCode = (String) params.get("warehouseCode");
        BigDecimal qty = new BigDecimal(params.get("qty").toString());
        
        // 查询现有库存
        Map<String, Object> existing = inventoryMapper.selectByItemAndWarehouse(itemCode, warehouseCode);
        
        if (existing == null) {
            return ApiResponse.fail("库存记录不存在");
        }
        
        // 检查库存是否充足
        BigDecimal availableQty = new BigDecimal(existing.get("available_qty").toString());
        if (availableQty.compareTo(qty) < 0) {
            return ApiResponse.fail("库存不足");
        }
        
        // 扣减库存
        Long id = ((Number) existing.get("id")).longValue();
        inventoryMapper.decreaseQty(id, qty);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "出库成功");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 创建库存
     */
    @PostMapping
    public ApiResponse<Inventory> create(@RequestBody Inventory inventory) {
        inventoryMapper.insert(
            inventory.getItemCode(),
            inventory.getWarehouseCode(),
            inventory.getOnHandQty() != null ? inventory.getOnHandQty() : BigDecimal.ZERO
        );
        return ApiResponse.ok(inventory);
    }
    
    /**
     * 更新库存
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Inventory inventory) {
        // TODO: 完整更新逻辑
        return ApiResponse.ok();
    }
    
    /**
     * 删除库存
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // TODO: 删除逻辑
        return ApiResponse.ok();
    }
}
