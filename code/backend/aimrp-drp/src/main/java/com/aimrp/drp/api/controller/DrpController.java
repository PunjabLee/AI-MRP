package com.aimrp.drp.api.controller;

import com.aimrp.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DRP 配送需求计划 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/drp")
@RequiredArgsConstructor
public class DrpController {

    /**
     * 执行DRP运算
     */
    @PostMapping("/run")
    public ApiResponse<Map<String, Object>> runDrp(@RequestBody(required = false) DrpRequest request) {
        log.info("开始执行DRP运算");

        // TODO: 实现完整的DRP运算逻辑
        // 1. 汇总配送需求
        // 2. 获取各仓库库存
        // 3. 执行配送分配算法
        // 4. 生成配送计划

        // 模拟结果
        Map<String, Object> result = new HashMap<>();
        result.put("planCount", 5);
        result.put("totalShipQty", new BigDecimal("1500"));
        result.put("totalCost", new BigDecimal("5000.00"));
        result.put("status", "COMPLETED");

        return ApiResponse.ok(result);
    }

    /**
     * 获取配送计划列表
     */
    @GetMapping("/plans")
    public ApiResponse<Map<String, Object>> getPlans(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        List<Map<String, Object>> plans = new ArrayList<>();

        // 模拟数据
        Map<String, Object> plan = new HashMap<>();
        plan.put("id", 1L);
        plan.put("planNo", "DRP20260312001");
        plan.put("networkName", "门店A");
        plan.put("itemCode", "FG001");
        plan.put("planQty", new BigDecimal("500"));
        plan.put("warehouseName", "总仓");
        plan.put("planDate", LocalDate.now().plusDays(3));
        plan.put("status", "PENDING");
        plans.add(plan);

        Map<String, Object> result = new HashMap<>();
        result.put("list", plans);
        result.put("total", plans.size());

        return ApiResponse.ok(result);
    }

    /**
     * 确认配送
     */
    @PostMapping("/plans/{id}/ship")
    public ApiResponse<Void> ship(@PathVariable Long id) {
        log.info("确认配送，计划ID: {}", id);
        return ApiResponse.ok();
    }

    // ===== DTO =====

    @lombok.Data
    public static class DrpRequest {
        private List<DemandInput> demands;
        private String strategy;  // NEAREST/LOWEST_COST/BALANCED
    }

    @lombok.Data
    @lombok.Builder
    public static class DemandInput {
        private Long networkId;
        private Long itemId;
        private BigDecimal qty;
        private LocalDate demandDate;
    }
}
