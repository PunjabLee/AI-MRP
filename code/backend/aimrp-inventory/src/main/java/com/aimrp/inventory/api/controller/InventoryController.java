package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.R;
import com.aimrp.inventory.domain.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    /**
     * 查询库存列表
     */
    @GetMapping
    public R<List<Inventory>> list(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String warehouseCode) {
        
        // Mock data
        List<Inventory> list = new ArrayList<>();
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setItemCode("A001");
        inv.setItemName("产品A");
        inv.setWarehouseCode("WH01");
        inv.setWarehouseName("主仓库");
        inv.setOnHandQty(new BigDecimal("1000"));
        inv.setAllocatedQty(new BigDecimal("200"));
        inv.setAvailableQty(new BigDecimal("800"));
        list.add(inv);
        return R.ok(list);
    }
    
    /**
     * 根据物料查询库存
     */
    @GetMapping("/item/{itemCode}")
    public R<List<Inventory>> getByItemCode(@PathVariable String itemCode) {
        List<Inventory> list = new ArrayList<>();
        Inventory inv = new Inventory();
        inv.setItemCode(itemCode);
        inv.setOnHandQty(new BigDecimal("1000"));
        inv.setAvailableQty(new BigDecimal("800"));
        list.add(inv);
        return R.ok(list);
    }
    
    /**
     * 入库
     */
    @PostMapping("/in")
    public R<Inventory> inStock(@RequestBody Map<String, Object> params) {
        Inventory inv = new Inventory();
        inv.setItemCode((String) params.get("itemCode"));
        inv.setOnHandQty(new BigDecimal(params.get("qty").toString()));
        return R.ok(inv);
    }
    
    /**
     * 出库
     */
    @PostMapping("/out")
    public R<Inventory> outStock(@RequestBody Map<String, Object> params) {
        Inventory inv = new Inventory();
        inv.setItemCode((String) params.get("itemCode"));
        inv.setOnHandQty(new BigDecimal(params.get("qty").toString()));
        return R.ok(inv);
    }
}
