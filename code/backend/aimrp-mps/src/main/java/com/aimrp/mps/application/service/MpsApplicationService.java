package com.aimrp.mps.application.service;

import com.aimrp.mps.domain.entity.MpsPlan;
import com.aimrp.mps.domain.entity.MpsSuggestion;
import com.aimrp.mps.infrastructure.persistence.mapper.MpsPlanMapper;
import com.aimrp.mps.infrastructure.persistence.mapper.MpsSuggestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MPS 应用服务
 * 负责MPS计算逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MpsApplicationService {

    private final MpsPlanMapper mpsPlanMapper;
    private final MpsSuggestionMapper mpsSuggestionMapper;

    /**
     * 执行MPS运算
     * 1. 汇总需求（销售订单 + 预测 + 独立需求）
     * 2. 计算毛需求
     * 3. 生成MPS建议
     */
    @Transactional
    public MpsResult runMps(MpsRunParam param) {
        log.info("开始执行MPS运算，计划周期: {} - {}", param.getStartDate(), param.getEndDate());

        // 1. 创建MPS计划
        MpsPlan plan = createMpsPlan(param);

        // 2. 汇总需求
        List<DemandSummary> demands = summarizeDemands(param);

        // 3. 生成MPS建议
        List<MpsSuggestion> suggestions = generateSuggestions(plan, demands);

        // 4. 保存建议
        if (!suggestions.isEmpty()) {
            mpsSuggestionMapper.batchInsert(suggestions);
        }

        log.info("MPS运算完成，生成 {} 条建议", suggestions.size());

        return MpsResult.builder()
                .planId(plan.getId())
                .planNo(plan.getPlanNo())
                .itemCount(demands.size())
                .totalQty(suggestions.stream()
                        .map(MpsSuggestion::getSuggestedQty)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .suggestionCount(suggestions.size())
                .build();
    }

    /**
     * 创建MPS计划
     */
    private MpsPlan createMpsPlan(MpsRunParam param) {
        MpsPlan plan = new MpsPlan();
        plan.setPlanNo("MPS" + System.currentTimeMillis());
        plan.setPlanStartDate(param.getStartDate());
        plan.setPlanEndDate(param.getEndDate());
        plan.setStatus("DRAFT");
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        mpsPlanMapper.insert(plan);
        return plan;
    }

    /**
     * 汇总需求
     * 从销售订单、预测、独立需求汇总毛需求
     */
    private List<DemandSummary> summarizeDemands(MpsRunParam param) {
        // TODO: 从Demand模块获取实际需求数据
        // 1. 查询销售订单需求
        // 2. 查询预测需求
        // 3. 查询独立需求

        // 模拟数据
        List<DemandSummary> demands = new ArrayList<>();

        // 模拟：销售订单需求
        demands.add(DemandSummary.builder()
                .itemId(1L)
                .itemCode("FG001")
                .itemName("成品001")
                .qty(new BigDecimal("1000"))
                .dueDate(param.getStartDate().plusDays(30))
                .sourceType("ORDER")
                .sourceNo("SO20260312001")
                .priority(1)
                .build());

        // 模拟：预测需求
        demands.add(DemandSummary.builder()
                .itemId(1L)
                .itemCode("FG001")
                .itemName("成品001")
                .qty(new BigDecimal("500"))
                .dueDate(param.getStartDate().plusDays(45))
                .sourceType("FORECAST")
                .sourceNo("FC20260312001")
                .priority(2)
                .build());

        // 模拟：独立需求
        demands.add(DemandSummary.builder()
                .itemId(2L)
                .itemCode("FG002")
                .itemName("成品002")
                .qty(new BigDecimal("800"))
                .dueDate(param.getStartDate().plusDays(60))
                .sourceType("MANUAL")
                .sourceNo("IR20260312001")
                .priority(3)
                .build());

        return demands;
    }

    /**
     * 生成MPS建议
     */
    private List<MpsSuggestion> generateSuggestions(MpsPlan plan, List<DemandSummary> demands) {
        // 按物料汇总需求
        Map<Long, List<DemandSummary>> groupedByItem = demands.stream()
                .collect(Collectors.groupingBy(DemandSummary::getItemId));

        List<MpsSuggestion> suggestions = new ArrayList<>();

        for (Map.Entry<Long, List<DemandSummary>> entry : groupedByItem.entrySet()) {
            List<DemandSummary> itemDemands = entry.getValue();

            // 计算总需求量
            BigDecimal totalQty = itemDemands.stream()
                    .map(DemandSummary::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 获取最早需求日期
            LocalDate earliestDueDate = itemDemands.stream()
                    .map(DemandSummary::getDueDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());

            // 获取最高优先级
            Integer highestPriority = itemDemands.stream()
                    .map(DemandSummary::getPriority)
                    .min(Integer::compareTo)
                    .orElse(5);

            // 生成建议
            MpsSuggestion suggestion = new MpsSuggestion();
            suggestion.setPlanId(plan.getId());
            suggestion.setItemId(entry.getKey());
            suggestion.setItemCode(itemDemands.get(0).getItemCode());
            suggestion.setItemName(itemDemands.get(0).getItemName());
            suggestion.setSuggestedQty(totalQty);
            suggestion.setDueDate(earliestDueDate);
            suggestion.setPriority(highestPriority);
            suggestion.setSourceType(itemDemands.get(0).getSourceType());
            suggestion.setSourceNo(itemDemands.get(0).getSourceNo());
            suggestion.setStatus("PENDING");
            suggestion.setCreatedAt(LocalDateTime.now());
            suggestion.setUpdatedAt(LocalDateTime.now());

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * 审核MPS建议
     */
    @Transactional
    public void approveSuggestion(Long suggestionId, String approved, String remark) {
        String status = "APPROVED".equals(approved) ? "APPROVED" : "REJECTED";
        mpsSuggestionMapper.updateStatus(suggestionId, status, remark);
    }

    /**
     * 获取MPS建议列表
     */
    public List<MpsSuggestion> getSuggestions(Long planId, String status) {
        if (status != null) {
            return mpsSuggestionMapper.selectByStatus(status);
        } else if (planId != null) {
            return mpsSuggestionMapper.selectByPlanId(planId);
        }
        return new ArrayList<>();
    }

    // ===== 内部类 =====

    /**
     * MPS运行参数
     */
    @lombok.Data
    @lombok.Builder
    public static class MpsRunParam {
        private LocalDate startDate;
        private LocalDate endDate;
        private String runType;  // MANUAL/AUTO
    }

    /**
     * 需求汇总
     */
    @lombok.Data
    @lombok.Builder
    public static class DemandSummary {
        private Long itemId;
        private String itemCode;
        private String itemName;
        private BigDecimal qty;
        private LocalDate dueDate;
        private String sourceType;
        private String sourceNo;
        private Integer priority;
    }

    /**
     * MPS运算结果
     */
    @lombok.Data
    @lombok.Builder
    public static class MpsResult {
        private Long planId;
        private String planNo;
        private Integer itemCount;
        private BigDecimal totalQty;
        private Integer suggestionCount;
    }
}
