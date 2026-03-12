package com.aimrp.mps.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.mps.application.service.MpsApplicationService;
import com.aimrp.mps.application.service.MpsApplicationService.DemandSummary;
import com.aimrp.mps.application.service.MpsApplicationService.MpsResult;
import com.aimrp.mps.application.service.MpsApplicationService.MpsRunParam;
import com.aimrp.mps.domain.entity.MpsPlan;
import com.aimrp.mps.domain.entity.MpsSuggestion;
import com.aimrp.mps.infrastructure.persistence.mapper.MpsPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MPS主生产计划 Controller
 */
@RestController
@RequestMapping("/api/mps")
@RequiredArgsConstructor
public class MpsController {

    private final MpsPlanMapper mpsPlanMapper;
    private final MpsApplicationService mpsApplicationService;

    /**
     * 执行MPS运算
     */
    @PostMapping("/run")
    public ApiResponse<MpsResult> runMps(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MANUAL") String runType) {

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(90);
        }

        MpsRunParam param = MpsRunParam.builder()
                .startDate(startDate)
                .endDate(endDate)
                .runType(runType)
                .build();

        MpsResult result = mpsApplicationService.runMps(param);
        return ApiResponse.ok(result);
    }

    /**
     * 查询MPS计划列表
     */
    @GetMapping("/plans")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String itemCode,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        List<MpsPlan> list = mpsPlanMapper.selectList(status, itemCode);

        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<MpsPlan> pageList = fromIndex < total ?
                list.subList(fromIndex, toIndex) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);

        return ApiResponse.ok(result);
    }

    /**
     * 查询MPS计划详情
     */
    @GetMapping("/plans/{id}")
    public ApiResponse<MpsPlan> getById(@PathVariable Long id) {
        MpsPlan plan = mpsPlanMapper.selectById(id);
        return ApiResponse.ok(plan);
    }

    /**
     * 创建MPS计划
     */
    @PostMapping("/plans")
    public ApiResponse<MpsPlan> create(@RequestBody MpsPlan plan) {
        plan.setPlanNo("MPS" + System.currentTimeMillis());
        plan.setStatus("DRAFT");
        mpsPlanMapper.insert(plan);

        return ApiResponse.ok(plan);
    }

    /**
     * 更新MPS计划
     */
    @PutMapping("/plans/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody MpsPlan plan) {
        plan.setId(id);
        mpsPlanMapper.updateById(plan);

        return ApiResponse.ok();
    }

    /**
     * 下达MPS计划
     */
    @PostMapping("/plans/{id}/release")
    public ApiResponse<Void> release(@PathVariable Long id) {
        MpsPlan plan = new MpsPlan();
        plan.setId(id);
        plan.setStatus("RELEASED");
        mpsPlanMapper.updateById(plan);

        return ApiResponse.ok();
    }

    /**
     * 获取MPS建议列表
     */
    @GetMapping("/suggestions")
    public ApiResponse<Map<String, Object>> getSuggestions(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String status) {

        List<MpsSuggestion> suggestions = mpsApplicationService.getSuggestions(planId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", suggestions);
        result.put("total", suggestions.size());

        return ApiResponse.ok(result);
    }

    /**
     * 审核MPS建议
     */
    @PostMapping("/suggestions/{id}/approve")
    public ApiResponse<Void> approveSuggestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean approved,
            @RequestParam(required = false) String remark) {

        mpsApplicationService.approveSuggestion(id, approved ? "APPROVED" : "REJECTED", remark);
        return ApiResponse.ok();
    }
}
