package com.aimrp.mrp.application.service;

import com.aimrp.bom.infrastructure.feign.BomFeignClient;
import com.aimrp.inventory.infrastructure.feign.InventoryFeignClient;
import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.entity.MrpSuggestion;
import com.aimrp.mrp.domain.valueobject.MrpContext;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import com.aimrp.mrp.domain.service.MrpCalculator;
import com.aimrp.mrp.infrastructure.persistence.mapper.ItemMapper;
import com.aimrp.mrp.infrastructure.persistence.mapper.MrpRunMapper;
import com.aimrp.mrp.infrastructure.persistence.mapper.MrpSuggestionMapper;
import com.aimrp.mrp.infrastructure.persistence.mapper.MrpPurchaseOnWayMapper;
import com.aimrp.mrp.infrastructure.persistence.mapper.MrpProductionOnWayMapper;
import com.aimrp.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * MRP 应用服务
 * 使用 Feign 客户端进行跨模块调用，遵循 DDD 和微服务架构规范
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpApplicationService {

    private final MrpCalculator mrpCalculator;
    private final ItemMapper itemMapper;
    // 使用 Feign 客户端替代直接 Mapper 引用
    private final BomFeignClient bomFeignClient;
    private final InventoryFeignClient inventoryFeignClient;
    // 同模块内的 Mapper 保持不变
    private final MrpPurchaseOnWayMapper purchaseOnWayMapper;
    private final MrpProductionOnWayMapper productionOnWayMapper;
    private final MrpRunMapper mrpRunMapper;
    private final MrpSuggestionMapper mrpSuggestionMapper;

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

        // 保存到数据库
        mrpRunMapper.insert(run);

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

        // 通过 Feign 加载 BOM 数据
        Map<String, List<MrpContext.BomLineVO>> bomMap = loadBomMap();
        contextBuilder.bomMap(bomMap);

        // 加载库存数据
        Map<String, MrpContext.InventoryVO> inventoryMap = loadInventoryMap();
        contextBuilder.inventoryMap(inventoryMap);

        // 加载需求数据
        Map<String, List<MrpContext.DemandVO>> demandMap = loadDemandMap();
        contextBuilder.demandMap(demandMap);

        // 加载在途数据
        Map<String, BigDecimal> purchaseOnWay = loadPurchaseOnWay();
        contextBuilder.purchaseOnWay(purchaseOnWay);

        Map<String, BigDecimal> productionOnWay = loadProductionOnWay();
        contextBuilder.productionOnWay(productionOnWay);

        return contextBuilder.build();
    }

    /**
     * 加载物料主数据
     */
    private Map<String, MrpContext.ItemVO> loadItems() {
        Map<String, MrpContext.ItemVO> items = new HashMap<>();
        // 使用同模块的 Mapper
        List<Map<String, Object>> itemList = itemMapper.selectItemList(null, null);
        for (Map<String, Object> item : itemList) {
            MrpContext.ItemVO vo = MrpContext.ItemVO.builder()
                    .itemCode((String) item.get("item_code"))
                    .itemName((String) item.get("item_name"))
                    .itemType((String) item.get("item_type"))
                    .unit((String) item.get("unit"))
                    .planningLeadTime(item.get("planning_lead_time") != null ?
                            ((Number) item.get("planning_lead_time")).intValue() : 0)
                    .minLotSize(item.get("min_lot_size") != null ?
                            new BigDecimal(item.get("min_lot_size").toString()) : BigDecimal.ONE)
                    .maxLotSize(item.get("max_lot_size") != null ?
                            new BigDecimal(item.get("max_lot_size").toString()) : BigDecimal.valueOf(999999))
                    .build();
            items.put(vo.getItemCode(), vo);
        }
        return items;
    }

    /**
     * 通过 Feign 加载 BOM 数据
     */
    private Map<String, List<MrpContext.BomLineVO>> loadBomMap() {
        Map<String, List<MrpContext.BomLineVO>> bomMap = new HashMap<>();
        try {
            ApiResponse<List<Map<String, Object>>> response = bomFeignClient.selectBomMap();
            if (response != null && response.getData() != null) {
                for (Map<String, Object> bom : response.getData()) {
                    String parentCode = (String) bom.get("parent_item_code");
                    MrpContext.BomLineVO line = MrpContext.BomLineVO.builder()
                            .bomId(bom.get("bom_id") != null ? ((Number) bom.get("bom_id")).longValue() : 0L)
                            .parentItemCode(parentCode)
                            .childItemCode((String) bom.get("child_item_code"))
                            .usageQty(new BigDecimal(bom.get("usage_qty").toString()))
                            .lossRate(bom.get("loss_rate") != null ?
                                    new BigDecimal(bom.get("loss_rate").toString()) : BigDecimal.ZERO)
                            .level(bom.get("level") != null ?
                                    ((Number) bom.get("level")).intValue() : 1)
                            .build();
                    bomMap.computeIfAbsent(parentCode, k -> new ArrayList<>()).add(line);
                }
            }
        } catch (Exception e) {
            log.warn("通过 Feign 获取 BOM 数据失败: {}", e.getMessage());
        }
        return bomMap;
    }

    /**
     * 通过 Feign 加载库存数据
     */
    private Map<String, MrpContext.InventoryVO> loadInventoryMap() {
        Map<String, MrpContext.InventoryVO> inventoryMap = new HashMap<>();
        try {
            ApiResponse<List<Map<String, Object>>> response = inventoryFeignClient.listInventory();
            if (response != null && response.getData() != null) {
                for (Map<String, Object> inv : response.getData()) {
                    String itemCode = (String) inv.get("item_code");
                    MrpContext.InventoryVO vo = MrpContext.InventoryVO.builder()
                            .itemCode(itemCode)
                            .onHandQty(inv.get("on_hand_qty") != null ?
                                    new BigDecimal(inv.get("on_hand_qty").toString()) : BigDecimal.ZERO)
                            .allocatedQty(inv.get("allocated_qty") != null ?
                                    new BigDecimal(inv.get("allocated_qty").toString()) : BigDecimal.ZERO)
                            .availableQty(inv.get("available_qty") != null ?
                                    new BigDecimal(inv.get("available_qty").toString()) : BigDecimal.ZERO)
                            .safetyStock(inv.get("safety_stock") != null ?
                                    new BigDecimal(inv.get("safety_stock").toString()) : BigDecimal.ZERO)
                            .build();
                    inventoryMap.put(itemCode, vo);
                }
            }
        } catch (Exception e) {
            log.warn("通过 Feign 获取库存数据失败: {}", e.getMessage());
        }
        return inventoryMap;
    }

    /**
     * 加载需求数据
     */
    private Map<String, List<MrpContext.DemandVO>> loadDemandMap() {
        // 简化实现，实际应调用需求模块
        return new HashMap<>();
    }

    /**
     * 加载采购在途数据
     */
    private Map<String, BigDecimal> loadPurchaseOnWay() {
        Map<String, BigDecimal> onWay = new HashMap<>();
        List<Map<String, Object>> list = purchaseOnWayMapper.selectOnWayList(null);
        for (Map<String, Object> item : list) {
            String itemCode = (String) item.get("item_code");
            BigDecimal qty = new BigDecimal(item.get("on_way_qty").toString());
            onWay.merge(itemCode, qty, BigDecimal::add);
        }
        return onWay;
    }

    /**
     * 加载生产在途数据
     */
    private Map<String, BigDecimal> loadProductionOnWay() {
        Map<String, BigDecimal> onWay = new HashMap<>();
        List<Map<String, Object>> list = productionOnWayMapper.selectOnWayList(null);
        for (Map<String, Object> item : list) {
            String itemCode = (String) item.get("item_code");
            BigDecimal qty = new BigDecimal(item.get("on_way_qty").toString());
            onWay.merge(itemCode, qty, BigDecimal::add);
        }
        return onWay;
    }

    /**
     * 保存 MRP 结果
     */
    private void saveResult(MrpRun runRecord, MrpResult result) {
        runRecord.setStatus("COMPLETED");
        runRecord.setCompletedAt(java.time.LocalDateTime.now());
        mrpRunMapper.updateById(runRecord);

        // 保存建议
        if (result.getSuggestions() != null) {
            for (MrpResult.Suggestion sug : result.getSuggestions()) {
                MrpSuggestion suggestion = new MrpSuggestion();
                suggestion.setRunId(runRecord.getId());
                suggestion.setItemCode(sug.getItemCode());
                suggestion.setSuggestionType(sug.getType());
                suggestion.setSuggestionQty(sug.getQty());
                suggestion.setDueDate(sug.getDueDate());
                suggestion.setPriority(sug.getPriority() != null ? sug.getPriority() : 5);
                suggestion.setStatus("PENDING");
                mrpSuggestionMapper.insert(suggestion);
            }
        }
    }
}
