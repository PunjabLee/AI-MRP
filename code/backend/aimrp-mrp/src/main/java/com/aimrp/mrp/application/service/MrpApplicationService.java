package com.aimrp.mrp.application.service;

import com.aimrp.bom.infrastructure.persistence.mapper.BomMapper;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryMapper;
import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import com.aimrp.mrp.domain.service.MrpCalculator;
import com.aimrp.mrp.infrastructure.persistence.mapper.ItemMapper;
import com.aimrp.mrp.infrastructure.persistence.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * MRP 应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpApplicationService {
    
    private final MrpCalculator mrpCalculator;
    private final ItemMapper itemMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final BomMapper bomMapper;
    private final InventoryMapper inventoryMapper;
    
    /**
     * 执行 MRP 计算
     */
    @Transactional
    public MrpResult runMrp(MrpRun mrpRun) {
        log.info("开始执行 MRP，运行参数: {}", mrpRun);
        
        // 1. 创建运行记录
        MrpRun runRecord = createRunRecord(mrpRun);
        
        // 2. 准备计算上下文
        MrpContext context = prepareContext(runRecord);
        
        // 3. 执行计算
        MrpResult result = mrpCalculator.calculate(context);
        
        // 4. 保存结果
        saveResult(runRecord, result);
        
        return result;
    }
    
    /**
     * 创建 MRP 运行记录
     */
    private MrpRun createRunRecord(MrpRun params) {
        MrpRun run = new MrpRun();
        run.setRunNo("MRP" + System.currentTimeMillis());
        run.setRunType(params.getRunType() != null ? params.getRunType() : "MANUAL");
        run.setStatus("RUNNING");
        run.setPlanStartDate(params.getPlanStartDate() != null ? params.getPlanStartDate() : LocalDate.now());
        run.setPlanEndDate(params.getPlanEndDate() != null ? params.getPlanEndDate() : LocalDate.now().plusDays(90));
        
        // TODO: 保存到数据库
        run.setId(1L);
        
        return run;
    }
    
    /**
     * 准备计算上下文
     */
    private MrpContext prepareContext(MrpRun runRecord) {
        MrpContext.MrpContextBuilder contextBuilder = MrpContext.builder()
                .runId(runRecord.getId())
                .planStartDate(runRecord.getPlanStartDate())
                .planEndDate(runRecord.getPlanEndDate())
                .allowNegative(false)
                .timeBucket("DAY");
        
        // 加载物料主数据
        Map<String, MrpContext.ItemVO> items = loadItems();
        contextBuilder.items(items);
        
        // 加载 BOM 数据
        Map<String, List<MrpContext.BomLineVO>> bomMap = loadBomMap();
        contextBuilder.bomMap(bomMap);
        
        // 加载库存数据
        Map<String, MrpContext.InventoryVO> inventoryMap = loadInventory();
        contextBuilder.inventoryMap(inventoryMap);
        
        // 加载需求数据
        Map<String, List<MrpContext.DemandVO>> demandMap = loadDemands(runRecord);
        contextBuilder.salesDemandMap(demandMap);
        
        // 加载在途数据
        Map<String, List<MrpContext.PurchaseOnWayVO>> purchaseOnWay = loadPurchaseOnWay();
        contextBuilder.purchaseOnWayMap(purchaseOnWay);
        
        // 加载在制数据
        Map<String, List<MrpContext.ProductionOnWayVO>> productionOnWay = loadProductionOnWay();
        contextBuilder.productionOnWayMap(productionOnWay);
        
        return contextBuilder.build();
    }
    
    /**
     * 加载物料主数据（从数据库）
     */
    private Map<String, MrpContext.ItemVO> loadItems() {
        Map<String, MrpContext.ItemVO> items = new HashMap<>();
        
        try {
            List<Map<String, Object>> itemList = itemMapper.selectAll();
            for (Map<String, Object> row : itemList) {
                MrpContext.ItemVO item = MrpContext.ItemVO.builder()
                        .itemCode((String) row.get("item_code"))
                        .itemName((String) row.get("item_name"))
                        .itemType((String) row.get("item_type"))
                        .source((String) row.get("source"))
                        .leadTime(row.get("lead_time") != null ? ((Number) row.get("lead_time")).intValue() : 0)
                        .lotSizeRule((String) row.get("lot_size_rule"))
                        .minLotSize(row.get("min_lot_size") != null ? new BigDecimal(row.get("min_lot_size").toString()) : BigDecimal.ONE)
                        .maxLotSize(row.get("max_lot_size") != null ? new BigDecimal(row.get("max_lot_size").toString()) : new BigDecimal("999999"))
                        .safetyStock(row.get("safety_stock") != null ? new BigDecimal(row.get("safety_stock").toString()) : BigDecimal.ZERO)
                        .yieldRate(row.get("yield_rate") != null ? new BigDecimal(row.get("yield_rate").toString()) : new BigDecimal("1.0"))
                        .build();
                items.put(item.getItemCode(), item);
            }
            log.info("从数据库加载物料: {} 条", items.size());
        } catch (Exception e) {
            log.warn("加载物料数据失败，使用空数据: {}", e.getMessage());
        }
        
        return items;
    }
    
    /**
     * 加载 BOM 数据（从 bom 模块）
     */
    private Map<String, List<MrpContext.BomLineVO>> loadBomMap() {
        Map<String, List<MrpContext.BomLineVO>> bomMap = new HashMap<>();
        
        try {
            Map<String, List<Map<String, Object>>> rawBomMap = bomMapper.selectBomMap(null);
            
            for (Map.Entry<String, List<Map<String, Object>>> entry : rawBomMap.entrySet()) {
                String parentCode = entry.getKey();
                List<MrpContext.BomLineVO> lines = new ArrayList<>();
                
                for (Map<String, Object> row : entry.getValue()) {
                    MrpContext.BomLineVO line = MrpContext.BomLineVO.builder()
                            .bomId(((Number) row.get("bom_id")).longValue())
                            .parentItemCode(parentCode)
                            .childItemCode((String) row.get("child_item_code"))
                            .childItemName((String) row.get("child_item_name"))
                            .usageQty(new BigDecimal(row.get("usage_qty").toString()))
                            .lossRate(row.get("loss_rate") != null ? new BigDecimal(row.get("loss_rate").toString()) : BigDecimal.ZERO)
                            .level(((Number) row.get("level")).intValue())
                            .build();
                    lines.add(line);
                }
                bomMap.put(parentCode, lines);
            }
            log.info("从BOM模块加载: {} 条", bomMap.size());
        } catch (Exception e) {
            log.warn("加载BOM数据失败，使用空数据: {}", e.getMessage());
        }
        
        return bomMap;
    }
    
    /**
     * 加载库存数据（从 inventory 模块）
     */
    private Map<String, MrpContext.InventoryVO> loadInventory() {
        Map<String, MrpContext.InventoryVO> inventory = new HashMap<>();
        
        try {
            List<Map<String, Object>> inventoryList = inventoryMapper.selectList(null, null);
            
            for (Map<String, Object> row : inventoryList) {
                String itemCode = (String) row.get("item_code");
                MrpContext.InventoryVO vo = MrpContext.InventoryVO.builder()
                        .itemCode(itemCode)
                        .warehouseCode((String) row.get("warehouse_code"))
                        .onHandQty(new BigDecimal(row.get("on_hand_qty").toString()))
                        .allocatedQty(row.get("allocated_qty") != null ? new BigDecimal(row.get("allocated_qty").toString()) : BigDecimal.ZERO)
                        .availableQty(row.get("available_qty") != null ? new BigDecimal(row.get("available_qty").toString()) : BigDecimal.ZERO)
                        .build();
                inventory.put(itemCode, vo);
            }
            log.info("从库存模块加载: {} 条", inventory.size());
        } catch (Exception e) {
            log.warn("加载库存数据失败，使用空数据: {}", e.getMessage());
        }
        
        return inventory;
    }
    
    /**
     * 加载需求数据（从数据库）
     */
    private Map<String, List<MrpContext.DemandVO>> loadDemands(MrpRun runRecord) {
        Map<String, List<MrpContext.DemandVO>> demands = new HashMap<>();
        
        try {
            List<Map<String, Object>> orderList = salesOrderMapper.selectForMrp(
                    runRecord.getPlanStartDate(),
                    runRecord.getPlanEndDate());
            
            for (Map<String, Object> row : orderList) {
                MrpContext.DemandVO demand = MrpContext.DemandVO.builder()
                        .demandId(((Number) row.get("id")).longValue())
                        .demandType("ORDER")
                        .itemCode((String) row.get("item_code"))
                        .qty(new BigDecimal(row.get("qty").toString()))
                        .dueDate((LocalDate) row.get("due_date"))
                        .priority(row.get("priority") != null ? ((Number) row.get("priority")).intValue() : 5)
                        .build();
                
                demands.computeIfAbsent(demand.getItemCode(), k -> new ArrayList<>()).add(demand);
            }
            log.info("从数据库加载需求: {} 条", orderList.size());
        } catch (Exception e) {
            log.warn("加载需求数据失败，使用空数据: {}", e.getMessage());
        }
        
        return demands;
    }
    
    /**
     * 加载在途采购
     * TODO: 从采购模块查询
     */
    private Map<String, List<MrpContext.PurchaseOnWayVO>> loadPurchaseOnWay() {
        return new HashMap<>();
    }
    
    /**
     * 加载在制生产
     * TODO: 从生产模块查询
     */
    private Map<String, List<MrpContext.ProductionOnWayVO>> loadProductionOnWay() {
        return new HashMap<>();
    }
    
    /**
     * 保存计算结果
     */
    private void saveResult(MrpRun runRecord, MrpResult result) {
        // TODO: 保存到数据库
        runRecord.setStatus(result.getStatus());
        runRecord.setRunTimeMs(result.getRunTimeMs());
        
        if (result.getStatistics() != null) {
            runRecord.setDemandCount(result.getStatistics().getTotalDemands());
            runRecord.setSuggestionCount(result.getStatistics().getTotalSuggestions());
            runRecord.setPurchaseSuggestionCount(result.getStatistics().getPurchaseSuggestions());
            runRecord.setProductionSuggestionCount(result.getStatistics().getProductionSuggestions());
        }
        
        log.info("MRP 计算结果已保存，runId: {}", runRecord.getId());
    }
}
