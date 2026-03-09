package com.aimrp.mrp.application.service;

import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.entity.MrpSuggestion;
import com.aimrp.mrp.domain.entity.MrpParameter;
import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import com.aimrp.mrp.domain.service.MrpCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MRP 应用服务
 * 
 * 负责 MRP 计算的编排和事务管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpApplicationService {
    
    private final MrpCalculator mrpCalculator;
    
    /**
     * 执行 MRP 计算
     * 
     * 流程：
     * 1. 创建 MRP 运行记录
     * 2. 准备计算上下文
     * 3. 执行计算
     * 4. 保存计算结果
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
        return run;
    }
    
    /**
     * 准备计算上下文
     * 
     * 从数据库加载 MRP 计算所需的所有数据
     */
    private MrpContext prepareContext(MrpRun runRecord) {
        MrpContext.MrpContextBuilder contextBuilder = MrpContext.builder()
                .runId(runRecord.getId())
                .planStartDate(runRecord.getPlanStartDate())
                .planEndDate(runRecord.getPlanEndDate())
                .allowNegative(false);
        
        // TODO: 从数据库加载实际数据
        // 1. 加载物料主数据
        Map<String, MrpContext.ItemVO> items = loadItems();
        contextBuilder.items(items);
        
        // 2. 加载 BOM 数据
        Map<String, List<MrpContext.BomLineVO>> bomMap = loadBomMap();
        contextBuilder.bomMap(bomMap);
        
        // 3. 加载库存数据
        Map<String, MrpContext.InventoryVO> inventoryMap = loadInventory();
        contextBuilder.inventoryMap(inventoryMap);
        
        // 4. 加载需求数据
        Map<String, List<MrpContext.DemandVO>> demandMap = loadDemands(runRecord);
        contextBuilder.salesDemandMap(demandMap);
        
        // 5. 加载在途数据
        Map<String, List<MrpContext.PurchaseOnWayVO>> purchaseOnWay = loadPurchaseOnWay();
        contextBuilder.purchaseOnWayMap(purchaseOnWay);
        
        return contextBuilder.build();
    }
    
    /**
     * 加载物料主数据
     */
    private Map<String, MrpContext.ItemVO> loadItems() {
        // TODO: 从数据库查询
        // 示例数据
        Map<String, MrpContext.ItemVO> items = new HashMap<>();
        
        MrpContext.ItemVO item = MrpContext.ItemVO.builder()
                .itemCode("A001")
                .itemName("产品A")
                .itemType("FINISHED")
                .source("MAKE")
                .leadTime(7)
                .lotSizeRule("LOT_FOR_LOT")
                .minLotSize(new java.math.BigDecimal("1"))
                .maxLotSize(new java.math.BigDecimal("10000"))
                .safetyStock(new java.math.BigDecimal("100"))
                .yieldRate(new java.math.BigDecimal("0.98"))
                .build();
        items.put("A001", item);
        
        return items;
    }
    
    /**
     * 加载 BOM 数据
     */
    private Map<String, List<MrpContext.BomLineVO>> loadBomMap() {
        // TODO: 从数据库查询
        return new HashMap<>();
    }
    
    /**
     * 加载库存数据
     */
    private Map<String, MrpContext.InventoryVO> loadInventory() {
        // TODO: 从数据库查询
        Map<String, MrpContext.InventoryVO> inventory = new HashMap<>();
        
        MrpContext.InventoryVO inv = MrpContext.InventoryVO.builder()
                .itemCode("A001")
                .warehouseCode("WH01")
                .onHandQty(new java.math.BigDecimal("500"))
                .allocatedQty(new java.math.BigDecimal("100"))
                .availableQty(new java.math.BigDecimal("400"))
                .build();
        inventory.put("A001", inv);
        
        return inventory;
    }
    
    /**
     * 加载需求数据
     */
    private Map<String, List<MrpContext.DemandVO>> loadDemands(MrpRun runRecord) {
        // TODO: 从数据库查询已确认的销售订单
        Map<String, List<MrpContext.DemandVO>> demands = new HashMap<>();
        
        // 示例需求
        MrpContext.DemandVO demand = MrpContext.DemandVO.builder()
                .demandId(1L)
                .demandType("ORDER")
                .itemCode("A001")
                .qty(new java.math.BigDecimal("1000"))
                .dueDate(LocalDate.now().plusDays(30))
                .priority(5)
                .build();
        
        demands.computeIfAbsent("A001", k -> new java.util.ArrayList<>()).add(demand);
        
        return demands;
    }
    
    /**
     * 加载在途采购
     */
    private Map<String, List<MrpContext.PurchaseOnWayVO>> loadPurchaseOnWay() {
        // TODO: 从数据库查询
        return new HashMap<>();
    }
    
    /**
     * 保存计算结果
     */
    private void saveResult(MrpRun runRecord, MrpResult result) {
        // TODO: 保存到数据库
        // 1. 更新运行记录
        runRecord.setStatus(result.getStatus());
        runRecord.setRunTimeMs(result.getRunTimeMs());
        
        // 2. 保存建议
        if (result.getPurchaseSuggestions() != null) {
            for (MrpResult.Suggestion suggestion : result.getPurchaseSuggestions()) {
                saveSuggestion(runRecord, suggestion);
            }
        }
        
        if (result.getProductionSuggestions() != null) {
            for (MrpResult.Suggestion suggestion : result.getProductionSuggestions()) {
                saveSuggestion(runRecord, suggestion);
            }
        }
        
        log.info("MRP 计算结果已保存，runId: {}, 建议数: {}", 
                runRecord.getId(), 
                (result.getPurchaseSuggestions() != null ? result.getPurchaseSuggestions().size() : 0) + 
                (result.getProductionSuggestions() != null ? result.getProductionSuggestions().size() : 0));
    }
    
    /**
     * 保存单条建议
     */
    private void saveSuggestion(MrpRun runRecord, MrpResult.Suggestion suggestion) {
        MrpSuggestion mrpSuggestion = new MrpSuggestion();
        mrpSuggestion.setRunId(runRecord.getId());
        mrpSuggestion.setSuggestionType(suggestion.getSuggestionType());
        mrpSuggestion.setItemCode(suggestion.getItemCode());
        mrpSuggestion.setItemName(suggestion.getItemName());
        mrpSuggestion.setSuggestQty(suggestion.getSuggestQty());
        mrpSuggestion.setNeedDate(suggestion.getNeedDate());
        mrpSuggestion.setSuggestOrderDate(suggestion.getSuggestOrderDate());
        mrpSuggestion.setDemandSource(suggestion.getDemandSource());
        mrpSuggestion.setPriority(suggestion.getPriority());
        mrpSuggestion.setStatus("PENDING");
        
        // TODO: 保存到数据库
    }
}
