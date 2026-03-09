package com.aimrp.inventory.application.service;

import com.aimrp.inventory.domain.service.InventoryDomainService;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {
    
    private final InventoryDomainService domainService;
    private final InventoryMapper mapper;
    
    /**
     * 入库
     */
    @Transactional
    public void inStock(String itemCode, String warehouseCode, java.math.BigDecimal qty) {
        log.info("入库 - item: {}, warehouse: {}, qty: {}", itemCode, warehouseCode, qty);
        // 检查库存是否存在
        var inventory = mapper.selectByItemAndWarehouse(itemCode, warehouseCode);
        if (inventory != null) {
            // 更新数量
            mapper.increaseQty(inventory.getId(), qty);
        } else {
            // 新增库存
            mapper.insert(itemCode, warehouseCode, qty);
        }
    }
    
    /**
     * 出库
     */
    @Transactional
    public void outStock(String itemCode, String warehouseCode, java.math.BigDecimal qty) {
        log.info("出库 - item: {}, warehouse: {}, qty: {}", itemCode, warehouseCode, qty);
        var inventory = mapper.selectByItemAndWarehouse(itemCode, warehouseCode);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        // 检查库存是否充足
        java.math.BigDecimal available = domainService.calculateAvailable(
                inventory.getOnHandQty(), inventory.getAllocatedQty());
        if (!domainService.isSufficient(available, qty)) {
            throw new RuntimeException("库存不足");
        }
        mapper.decreaseQty(inventory.getId(), qty);
    }
    
    /**
     * 查询库存
     */
    @Transactional(readOnly = true)
    public List<?> list(String itemCode, String warehouseCode) {
        return mapper.selectList(itemCode, warehouseCode);
    }
}
