package com.aimrp.whatif.domain.service;

import com.aimrp.whatif.domain.model.WhatIfScenario;
import com.aimrp.demand.infrastructure.feign.DemandFeignClient;
import com.aimrp.inventory.infrastructure.feign.InventoryFeignClient;
import com.aimrp.purchase.infrastructure.feign.PurchaseFeignClient;
import com.aimrp.production.infrastructure.feign.ProductionFeignClient;
import com.aimrp.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What-If 应用服务
 *
 * 负责将模拟场景应用到生产系统
 * 使用 Feign 客户端进行跨模块调用，遵循 DDD 和微服务架构规范
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatIfApplicationService {

    // 使用 Feign 客户端进行跨模块调用
    private final DemandFeignClient demandFeignClient;
    private final InventoryFeignClient inventoryFeignClient;
    private final PurchaseFeignClient purchaseFeignClient;
    private final ProductionFeignClient productionFeignClient;

    /**
     * 应用场景到生产系统
     *
     * @param scenarioId 场景ID
     * @param changes 变更列表
     * @return 应用结果
     */
    public ApplyResult applyToProduction(Long scenarioId, List<WhatIfScenario.Change> changes) {
        log.info("开始应用 What-if 场景到生产系统 - scenarioId: {}, 变更数: {}",
                scenarioId, changes != null ? changes.size() : 0);

        List<ApplyDetail> details = new ArrayList<>();
        boolean allSuccess = true;

        if (changes == null || changes.isEmpty()) {
            return ApplyResult.builder()
                    .success(false)
                    .message("没有变更需要应用")
                    .details(details)
                    .build();
        }

        // 按变更类型分别处理
        for (WhatIfScenario.Change change : changes) {
            ApplyDetail detail = applyChange(change);
            details.add(detail);

            if (!detail.isSuccess()) {
                allSuccess = false;
            }
        }

        // 汇总结果
        long successCount = details.stream().filter(ApplyDetail::isSuccess).count();

        ApplyResult result = ApplyResult.builder()
                .success(allSuccess)
                .message(String.format("应用完成 - 成功: %d, 失败: %d",
                        successCount, details.size() - successCount))
                .appliedCount((int) successCount)
                .failedCount(details.size() - (int) successCount)
                .details(details)
                .build();

        log.info("What-if 场景应用完成 - scenarioId: {}, 结果: {}", scenarioId, result.getMessage());

        return result;
    }

    /**
     * 应用单个变更
     */
    private ApplyDetail applyChange(WhatIfScenario.Change change) {
        String changeType = change.getChangeType();
        String targetType = change.getTargetType();
        String targetCode = change.getTargetCode();

        log.info("应用变更 - 类型: {}, 目标: {}, 字段: {}, 新值: {}",
                changeType, targetType, targetCode, change.getNewValue());

        try {
            switch (targetType) {
                case "ORDER":
                    return applyOrderChange(change);
                case "INVENTORY":
                    return applyInventoryChange(change);
                case "PURCHASE":
                    return applyPurchaseChange(change);
                case "PRODUCTION":
                    return applyProductionChange(change);
                case "MRP_PARAMETER":
                    return applyMrpParameterChange(change);
                default:
                    return ApplyDetail.builder()
                            .success(false)
                            .targetType(targetType)
                            .targetCode(targetCode)
                            .message("未知目标类型: " + targetType)
                            .build();
            }
        } catch (Exception e) {
            log.error("应用变更失败 - {}", e.getMessage(), e);
            return ApplyDetail.builder()
                    .success(false)
                    .targetType(targetType)
                    .targetCode(targetCode)
                    .message("应用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 应用订单变更 - 通过 Feign 调用需求模块
     */
    @Transactional
    private ApplyDetail applyOrderChange(WhatIfScenario.Change change) {
        String orderNo = change.getTargetCode();
        String fieldName = change.getFieldName();
        String newValue = change.getNewValue();

        log.info("应用订单变更 - order: {}, field: {}, newValue: {}", orderNo, fieldName, newValue);

        try {
            ApiResponse<Integer> result;
            switch (fieldName) {
                case "qty":
                case "quantity":
                    result = demandFeignClient.updateQuantityByOrderNo(orderNo, new BigDecimal(newValue));
                    break;
                case "dueDate":
                case "delivery_date":
                    result = demandFeignClient.updateDueDateByOrderNo(orderNo, newValue);
                    break;
                case "priority":
                    result = demandFeignClient.updatePriorityByOrderNo(orderNo, Integer.parseInt(newValue));
                    break;
                case "status":
                    result = demandFeignClient.updateStatusByOrderNo(orderNo, newValue);
                    break;
                default:
                    log.warn("未知的订单变更字段: {}", fieldName);
                    return ApplyDetail.builder()
                            .success(false)
                            .targetType("ORDER")
                            .targetCode(orderNo)
                            .message("未知的变更字段: " + fieldName)
                            .build();
            }

            if (result != null && result.getData() != null && result.getData() > 0) {
                return ApplyDetail.builder()
                        .success(true)
                        .targetType("ORDER")
                        .targetCode(orderNo)
                        .message("订单变更已应用 - 字段: " + fieldName)
                        .build();
            } else {
                return ApplyDetail.builder()
                        .success(false)
                        .targetType("ORDER")
                        .targetCode(orderNo)
                        .message("订单不存在或更新失败")
                        .build();
            }
        } catch (Exception e) {
            log.error("应用订单变更失败 - {}", e.getMessage(), e);
            return ApplyDetail.builder()
                    .success(false)
                    .targetType("ORDER")
                    .targetCode(orderNo)
                    .message("应用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 应用库存变更 - 通过 Feign 调用库存模块
     */
    @Transactional
    private ApplyDetail applyInventoryChange(WhatIfScenario.Change change) {
        String itemCode = change.getTargetCode();
        String fieldName = change.getFieldName();
        String newValue = change.getNewValue();

        log.info("应用库存变更 - item: {}, field: {}, newValue: {}", itemCode, fieldName, newValue);

        try {
            ApiResponse<Integer> result;
            switch (fieldName) {
                case "safetyStock":
                case "safety_stock":
                    result = inventoryFeignClient.updateSafetyStockByItemCode(itemCode, new BigDecimal(newValue));
                    break;
                case "maxStock":
                case "max_stock":
                    result = inventoryFeignClient.updateMaxStockByItemCode(itemCode, new BigDecimal(newValue));
                    break;
                case "onHandQty":
                case "quantity":
                    result = inventoryFeignClient.updateOnHandQtyByItemCode(itemCode, new BigDecimal(newValue));
                    break;
                default:
                    log.warn("未知的库存变更字段: {}", fieldName);
                    return ApplyDetail.builder()
                            .success(false)
                            .targetType("INVENTORY")
                            .targetCode(itemCode)
                            .message("未知的变更字段: " + fieldName)
                            .build();
            }

            if (result != null && result.getData() != null && result.getData() > 0) {
                return ApplyDetail.builder()
                        .success(true)
                        .targetType("INVENTORY")
                        .targetCode(itemCode)
                        .message("库存变更已应用 - 字段: " + fieldName)
                        .build();
            } else {
                return ApplyDetail.builder()
                        .success(false)
                        .targetType("INVENTORY")
                        .targetCode(itemCode)
                        .message("物料不存在或更新失败")
                        .build();
            }
        } catch (Exception e) {
            log.error("应用库存变更失败 - {}", e.getMessage(), e);
            return ApplyDetail.builder()
                    .success(false)
                    .targetType("INVENTORY")
                    .targetCode(itemCode)
                    .message("应用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 应用采购变更 - 通过 Feign 调用采购模块
     */
    @Transactional
    private ApplyDetail applyPurchaseChange(WhatIfScenario.Change change) {
        String purchaseNo = change.getTargetCode();
        String fieldName = change.getFieldName();
        String newValue = change.getNewValue();

        log.info("应用采购变更 - purchase: {}, field: {}, newValue: {}", purchaseNo, fieldName, newValue);

        try {
            ApiResponse<Integer> result;
            switch (fieldName) {
                case "qty":
                case "quantity":
                    result = purchaseFeignClient.updateQuantityByPurchaseNo(purchaseNo, new BigDecimal(newValue));
                    break;
                case "dueDate":
                case "delivery_date":
                    result = purchaseFeignClient.updateDueDateByPurchaseNo(purchaseNo, newValue);
                    break;
                case "status":
                    result = purchaseFeignClient.updateStatusByPurchaseNo(purchaseNo, newValue);
                    break;
                default:
                    log.warn("未知的采购变更字段: {}", fieldName);
                    return ApplyDetail.builder()
                            .success(false)
                            .targetType("PURCHASE")
                            .targetCode(purchaseNo)
                            .message("未知的变更字段: " + fieldName)
                            .build();
            }

            if (result != null && result.getData() != null && result.getData() > 0) {
                return ApplyDetail.builder()
                        .success(true)
                        .targetType("PURCHASE")
                        .targetCode(purchaseNo)
                        .message("采购变更已应用 - 字段: " + fieldName)
                        .build();
            } else {
                return ApplyDetail.builder()
                        .success(false)
                        .targetType("PURCHASE")
                        .targetCode(purchaseNo)
                        .message("采购单不存在或更新失败")
                        .build();
            }
        } catch (Exception e) {
            log.error("应用采购变更失败 - {}", e.getMessage(), e);
            return ApplyDetail.builder()
                    .success(false)
                    .targetType("PURCHASE")
                    .targetCode(purchaseNo)
                    .message("应用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 应用生产变更 - 通过 Feign 调用生产模块
     */
    @Transactional
    private ApplyDetail applyProductionChange(WhatIfScenario.Change change) {
        String productionNo = change.getTargetCode();
        String fieldName = change.getFieldName();
        String newValue = change.getNewValue();

        log.info("应用生产变更 - production: {}, field: {}, newValue: {}", productionNo, fieldName, newValue);

        try {
            ApiResponse<Integer> result;
            switch (fieldName) {
                case "qty":
                case "quantity":
                    result = productionFeignClient.updateQuantityByProductionNo(productionNo, new BigDecimal(newValue));
                    break;
                case "startDate":
                    result = productionFeignClient.updateStartDateByProductionNo(productionNo, newValue);
                    break;
                case "endDate":
                case "dueDate":
                    result = productionFeignClient.updateEndDateByProductionNo(productionNo, newValue);
                    break;
                case "priority":
                    result = productionFeignClient.updatePriorityByProductionNo(productionNo, Integer.parseInt(newValue));
                    break;
                case "status":
                    result = productionFeignClient.updateStatusByProductionNo(productionNo, newValue);
                    break;
                default:
                    log.warn("未知的生产变更字段: {}", fieldName);
                    return ApplyDetail.builder()
                            .success(false)
                            .targetType("PRODUCTION")
                            .targetCode(productionNo)
                            .message("未知的变更字段: " + fieldName)
                            .build();
            }

            if (result != null && result.getData() != null && result.getData() > 0) {
                return ApplyDetail.builder()
                        .success(true)
                        .targetType("PRODUCTION")
                        .targetCode(productionNo)
                        .message("生产变更已应用 - 字段: " + fieldName)
                        .build();
            } else {
                return ApplyDetail.builder()
                        .success(false)
                        .targetType("PRODUCTION")
                        .targetCode(productionNo)
                        .message("生产工单不存在或更新失败")
                        .build();
            }
        } catch (Exception e) {
            log.error("应用生产变更失败 - {}", e.getMessage(), e);
            return ApplyDetail.builder()
                    .success(false)
                    .targetType("PRODUCTION")
                    .targetCode(productionNo)
                    .message("应用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 应用 MRP 参数变更
     */
    @Transactional
    private ApplyDetail applyMrpParameterChange(WhatIfScenario.Change change) {
        String paramCode = change.getTargetCode();
        String newValue = change.getNewValue();

        log.info("应用MRP参数变更 - param: {}, newValue: {}", paramCode, newValue);

        try {
            // MRP参数通过库存模块更新物料的MRP相关参数
            ApiResponse<Integer> result = inventoryFeignClient.updateMrpParameterByItemCode(
                    paramCode, // 使用参数码作为物料编码
                    paramCode,
                    newValue
            );

            return ApplyDetail.builder()
                    .success(true)
                    .targetType("MRP_PARAMETER")
                    .targetCode(paramCode)
                    .message("MRP参数变更已应用: " + paramCode + " = " + newValue)
                    .build();

        } catch (Exception e) {
            log.error("应用MRP参数变更失败 - {}", e.getMessage(), e);
            return ApplyDetail.builder()
                    .success(false)
                    .targetType("MRP_PARAMETER")
                    .targetCode(paramCode)
                    .message("应用失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 预览应用效果（不实际应用）
     */
    public PreviewResult preview(Long scenarioId, List<WhatIfScenario.Change> changes) {
        log.info("预览 What-if 场景 - scenarioId: {}", scenarioId);

        // 分析变更影响
        List<ImpactAnalysis> impacts = analyzeImpacts(changes);

        return PreviewResult.builder()
                .scenarioId(scenarioId)
                .changeCount(changes != null ? changes.size() : 0)
                .impacts(impacts)
                .estimatedTime(impacts.size() * 5) // 预估分钟
                .build();
    }

    /**
     * 分析变更影响
     */
    private List<ImpactAnalysis> analyzeImpacts(List<WhatIfScenario.Change> changes) {
        List<ImpactAnalysis> impacts = new ArrayList<>();

        for (WhatIfScenario.Change change : changes) {
            ImpactAnalysis impact = ImpactAnalysis.builder()
                    .changeType(change.getChangeType())
                    .targetType(change.getTargetType())
                    .targetCode(change.getTargetCode())
                    .description(change.getDescription())
                    .affectedModules(List.of(change.getTargetType()))
                    .riskLevel("MEDIUM")
                    .build();

            impacts.add(impact);
        }

        return impacts;
    }

    /**
     * 应用结果
     */
    @lombok.Data
    @lombok.Builder
    public static class ApplyResult {
        private boolean success;
        private String message;
        private int appliedCount;
        private int failedCount;
        private List<ApplyDetail> details;
    }

    /**
     * 应用明细
     */
    @lombok.Data
    @lombok.Builder
    public static class ApplyDetail {
        private boolean success;
        private String targetType;
        private String targetCode;
        private String message;
    }

    /**
     * 预览结果
     */
    @lombok.Data
    @lombok.Builder
    public static class PreviewResult {
        private Long scenarioId;
        private int changeCount;
        private List<ImpactAnalysis> impacts;
        private int estimatedTime; // 预估耗时（分钟）
    }

    /**
     * 影响分析
     */
    @lombok.Data
    @lombok.Builder
    public static class ImpactAnalysis {
        private String changeType;
        private String targetType;
        private String targetCode;
        private String description;
        private List<String> affectedModules;
        private String riskLevel;
    }
}
