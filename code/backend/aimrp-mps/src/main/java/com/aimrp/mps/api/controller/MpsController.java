package com.aimrp.mps.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.mps.domain.entity.MpsPlan;
import com.aimrp.mps.infrastructure.persistence.mapper.MpsPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
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
        
        // 分页
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
     * 生成MPS建议
     * 根据销售订单和预测生成主生产计划建议
     */
    @GetMapping("/suggestions")
    public ApiResponse<Map<String, Object>> getSuggestions() {
        // 根据销售订单和预测生成MPS建议
        List<Map<String, Object>> suggestions = generateMpsSuggestions();

        Map<String, Object> result = new HashMap<>();
        result.put("list", suggestions);
        result.put("total", suggestions.size());

        return ApiResponse.ok(result);
    }

    /**
     * 生成MPS建议
     * 1. 获取所有销售订单需求
     * 2. 获取需求预测数据
     * 3. 合并计算总需求
     * 4. 根据MPS规则生成建议
     */
    private List<Map<String, Object>> generateMpsSuggestions() {
        // TODO: 实现完整的MPS建议生成逻辑
        // 1. 查询销售订单
        // 2. 查询需求预测
        // 3. 需求合并
        // 4. 批量计算
        // 5. 生成建议

        List<Map<String, Object>> suggestions = new ArrayList<>();

        // 模拟数据
        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("id", 1L);
        suggestion.put("itemCode", "FG001");
        suggestion.put("itemName", "成品001");
        suggestion.put("suggestedQty", 1000);
        suggestion.put("dueDate", LocalDate.now().plusDays(30));
        suggestion.put("priority", 1);
        suggestion.put("sourceType", "ORDER");
        suggestion.put("sourceNo", "SO20260310001");
        suggestions.add(suggestion);

        return suggestions;
    }
}
