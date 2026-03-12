package com.aimrp.demand.infrastructure.feign;

import com.aimrp.common.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 需求服务 Feign 客户端
 * 用于其他模块调用需求模块
 */
@FeignClient(name = "aimrp-demand", path = "/api/orders")
public interface DemandFeignClient {

    /**
     * 根据订单号更新订单数量
     */
    @PutMapping("/update-quantity/{orderNo}")
    ApiResponse<Integer> updateQuantityByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam("quantity") BigDecimal quantity);

    /**
     * 根据订单号更新交期
     */
    @PutMapping("/update-due-date/{orderNo}")
    ApiResponse<Integer> updateDueDateByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam("dueDate") String dueDate);

    /**
     * 根据订单号更新优先级
     */
    @PutMapping("/update-priority/{orderNo}")
    ApiResponse<Integer> updatePriorityByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam("priority") Integer priority);

    /**
     * 根据订单号更新状态
     */
    @PutMapping("/update-status/{orderNo}")
    ApiResponse<Integer> updateStatusByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam("status") String status);

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderNo}")
    ApiResponse<Map<String, Object>> getOrderByNo(@PathVariable("orderNo") String orderNo);
}
