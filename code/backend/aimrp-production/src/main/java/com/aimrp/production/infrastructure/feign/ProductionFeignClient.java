package com.aimrp.production.infrastructure.feign;

import com.aimrp.common.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 生产服务 Feign 客户端
 * 用于其他模块调用生产模块
 */
@FeignClient(name = "aimrp-production", path = "/api/production-orders")
public interface ProductionFeignClient {

    /**
     * 更新生产数量
     */
    @PutMapping("/update-quantity/{productionNo}")
    ApiResponse<Integer> updateQuantityByProductionNo(
            @PathVariable("productionNo") String productionNo,
            @RequestParam("quantity") BigDecimal quantity);

    /**
     * 更新开始日期
     */
    @PutMapping("/update-start-date/{productionNo}")
    ApiResponse<Integer> updateStartDateByProductionNo(
            @PathVariable("productionNo") String productionNo,
            @RequestParam("startDate") String startDate);

    /**
     * 更新结束日期
     */
    @PutMapping("/update-end-date/{productionNo}")
    ApiResponse<Integer> updateEndDateByProductionNo(
            @PathVariable("productionNo") String productionNo,
            @RequestParam("endDate") String endDate);

    /**
     * 更新优先级
     */
    @PutMapping("/update-priority/{productionNo}")
    ApiResponse<Integer> updatePriorityByProductionNo(
            @PathVariable("productionNo") String productionNo,
            @RequestParam("priority") Integer priority);

    /**
     * 更新状态
     */
    @PutMapping("/update-status/{productionNo}")
    ApiResponse<Integer> updateStatusByProductionNo(
            @PathVariable("productionNo") String productionNo,
            @RequestParam("status") String status);

    /**
     * 根据状态统计数量
     */
    @GetMapping("/count-by-status/{status}")
    ApiResponse<Long> countByStatus(@PathVariable("status") String status);

    /**
     * 统计延迟工单数量
     */
    @GetMapping("/count-delayed")
    ApiResponse<Long> countDelayedOrders();

    /**
     * 获取工作中心负载
     */
    @GetMapping("/work-center-load")
    ApiResponse<List<Map<String, Object>>> getWorkCenterLoad();

    /**
     * 根据工单号查询
     */
    @GetMapping("/{productionNo}")
    ApiResponse<Map<String, Object>> getProductionByNo(@PathVariable("productionNo") String productionNo);
}
