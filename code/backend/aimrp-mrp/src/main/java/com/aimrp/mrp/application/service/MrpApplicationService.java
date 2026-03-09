package com.aimrp.mrp.application.service;

import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.entity.MrpSuggestion;
import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import com.aimrp.mrp.domain.service.MrpCalculator;
import com.aimrp.mrp.domain.service.BomExpander;
import com.aimrp.mrp.domain.service.DemandMerger;
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
    private final BomExpander bomExpander;
    private final DemandMerger demandMerger;
    
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
     * 加载物料主数据
     */
    private Map<String, MrpContext.ItemVO> loadItems() {
        // TODO: 从数据库查询
        Map<String, MrpContext.ItemVO> items = new HashMap<>();
        
        // 示例数据
        MrpContext.ItemVO itemA = MrpContext.ItemVO.builder()
                .itemCode("A001")
                .itemName("产品A")
                .itemType("FINISHED")
                .source("MAKE")
                .leadTime(7)
                .lotSizeRule("LOT_FOR_LOT")
                .minLotSize(BigDecimal.ONE)
                .maxLotSize(new BigDecimal("10000"))
                .safetyStock(new BigDecimal("100"))
                .yieldRate(new BigDecimal("0.98"))
                .build();
        items.put("A001", itemA);
        
        MrpContext.ItemVO itemB = MrpContext.ItemVO.builder()
                .itemCode("B001")
                .itemName("部件B")
                .itemType("SEMI")
                .source("MAKE")
                .leadTime(3)
                .lotSizeRule("LOT_FOR_LOT")
                .safetyStock(new BigDecimal("50"))
                .build();
        items.put("B001", itemB);
        
        MrpContext.ItemVO itemC = MrpContext.ItemVO.builder()
                .itemCode("C001")
                .itemName("物料C")
                .itemType("RAW")
                .source("BUY")
                .leadTime(5)
                .lotSizeRule("FIXED")
                .minLotSize(new BigDecimal("100"))
                .safetyStock(new BigDecimal("200"))
                .build();
        items.put("C001", itemC);
        
        return items;
    }
    
    /**
     * 加载 BOM 数据
     */
    private Map<String, List<MrpContext.BomLineVO>> loadBomMap() {
        Map<String, List<MrpContext.BomLineVO>> bomMap = new HashMap<>();
        
        // A001 的 BOM
        List<MrpContext.BomLineVO> a001Lines = new ArrayList<>();
        a001Lines.add(MrpContext.BomLineVO.builder()
                .bomId(1L)
                .parentItemCode("A001")
                .childItemCode("B001")
                .childItemName("部件B")
                .usageQty(new BigDecimal("2"))
                .lossRate(BigDecimal.ZERO)
                .level(1)
                .build());
        a001Lines.add(MrpContext.BomLineVO.builder()
                .bomId(1L)
                .parentItemCode("A001")
                .childItemCode("C001")
                .childItemName("物料C")
                .usageQty(new BigDecimal("5"))
                .lossRate(new BigDecimal("0.05"))
                .level(1)
                .build());
        bomMap.put("A001", a001Lines);
        
        // B001 的 BOM
        List<MrpContext.BomLineVO> b001Lines = new ArrayList<>();
        b001Lines.add(MrpContext.BomLineVO.builder()
                .bomId(2L)
                .parentItemCode("B001")
                .childItemCode("C001")
                .childItemName("物料C")
                .usageQty(new BigDecimal("3"))
                .lossRate(BigDecimal.ZERO)
                .level(1)
                .build());
        bomMap.put("B001", b001Lines);
        
        return bomMap;
    }
    
    /**
     * 加载库存数据
     */
    private Map<String, MrpContext.InventoryVO> loadInventory() {
        Map<String, MrpContext.InventoryVO> inventory = new HashMap<>();
        
        inventory.put("A001", MrpContext.InventoryVO.builder()
                .itemCode("A001")
                .warehouseCode("WH01")
                .onHandQty(new BigDecimal("500"))
                .allocatedQty(new BigDecimal("100"))
                .availableQty(new BigDecimal("400"))
                .build());
        
        inventory.put("B001", MrpContext.InventoryVO.builder()
                .itemCode("B001")
                .warehouseCode("WH01")
                .onHandQty(new BigDecimal("200"))
                .allocatedQty(BigDecimal.ZERO)
                .availableQty(new BigDecimal("200"))
                .build());
        
        inventory.put("C001", MrpContext.InventoryVO.builder()
                .itemCode("C001")
                .warehouseCode("WH01")
                .onHandQty(new BigDecimal("1000"))
                .allocatedQty(new BigDecimal("50"))
                .availableQty(new BigDecimal("950"))
                .build());
        
        return inventory;
    }
    
    /**
     * 加载需求数据
     */
    private Map<String, List<MrpContext.DemandVO>> loadDemands(MrpRun runRecord) {
        Map<String, List<MrpContext.DemandVO>> demands = new HashList<>();
        
        // 销售订单需求
        MrpContext.DemandVO demand1 = MrpContext.DemandVO.builder()
                .demandId(1L)
                .demandType("ORDER")
                .itemCode("A001")
                .qty(new BigDecimal("1000"))
                .dueDate(LocalDate.now().plusDays(30))
                .priority(5)
                .build();
        
        demands.computeIfAbsent("A001", k -> new ArrayList<>()).add(demand1);
        
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
     * 加载在制生产
     */
    private Map<String, List<MrpContext.ProductionOnWayVO>> loadProductionOnWay() {
        // TODO: 从数据库查询
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
