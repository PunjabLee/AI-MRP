package com.aimrp.mrp.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.mrp.api.dto.MrpRunRequest;
import com.aimrp.mrp.application.service.MrpApplicationService;
import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.entity.MrpSuggestion;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import com.aimrp.mrp.infrastructure.persistence.mapper.MrpRunMapper;
import com.aimrp.mrp.infrastructure.persistence.mapper.MrpSuggestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MRP 计算 Controller
 */
@RestController
@RequestMapping("/api/mrp")
@RequiredArgsConstructor
public class MrpController {
    
    private final MrpApplicationService mrpApplicationService;
    private final MrpRunMapper mrpRunMapper;
    private final MrpSuggestionMapper mrpSuggestionMapper;
    
    MRP 计算
     */
    @ /**
     * 执行PostMapping("/run")
    public ApiResponse<MrpResult> runMrp(@Validated @RequestBody(required = false) MrpRunRequest request) {
        MrpRun mrpRun = new MrpRun();
        
        if (request != null) {
            if (request.getRunType() != null) {
                mrpRun.setRunType(request.getRunType());
            }
            if (request.getPlanStartDate() != null) {
                mrpRun.setPlanStartDate(request.getPlanStartDate());
            }
            if (request.getPlanEndDate() != null) {
                mrpRun.setPlanEndDate(request.getPlanEndDate());
            }
        }
        
        MrpResult result = mrpApplicationService.runMrp(mrpRun);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取 MRP 运行结果
     */
    @GetMapping("/result/{runId}")
    public ApiResponse<Map<String, Object>> getResult(@PathVariable Long runId) {
        MrpRun runRecord = mrpRunMapper.selectById(runId);
        
        if (runRecord == null) {
            return ApiResponse.fail("运行记录不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("runId", runRecord.getId());
        result.put("runNo", runRecord.getRunNo());
        result.put("status", runRecord.getStatus());
        result.put("runType", runRecord.getRunType());
        result.put("planStartDate", runRecord.getPlanStartDate());
        result.put("planEndDate", runRecord.getPlanEndDate());
        result.put("itemCount", runRecord.getItemCount());
        result.put("demandCount", runRecord.getDemandCount());
        result.put("suggestionCount", runRecord.getSuggestionCount());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取最近一次 MRP 运行结果
     */
    @GetMapping("/result/latest")
    public ApiResponse<Map<String, Object>> getLatestResult() {
        MrpRun runRecord = mrpRunMapper.selectLastRun();
        
        if (runRecord == null) {
            return ApiResponse.fail("暂无运行记录");
        }
        
        return getResult(runRecord.getId());
    }
    
    /**
     * 获取 MRP 参数
     */
    @GetMapping("/parameters")
    public ApiResponse<Map<String, Object>> getParameters() {
        MrpRun lastRun = mrpRunMapper.selectLastRun();
        
        Map<String, Object> params = new HashMap<>();
        params.put("planningHorizon", 90);
        
        if (lastRun != null) {
            params.put("planStartDate", lastRun.getPlanStartDate());
            params.put("planEndDate", lastRun.getPlanEndDate());
            params.put("runType", lastRun.getRunType());
        } else {
            params.put("planStartDate", LocalDate.now());
            params.put("planEndDate", LocalDate.now().plusDays(90));
            params.put("runType", "MANUAL");
        }
        
        return ApiResponse.ok(params);
    }
    
    /**
     * 获取 MRP 建议列表
     */
    @GetMapping("/suggestions")
    public ApiResponse<Map<String, Object>> getSuggestions(
            @RequestParam(required = false) Long runId,
            @RequestParam(required = false) String suggestionType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        if (runId == null) {
            MrpRun lastRun = mrpRunMapper.selectLastRun();
            if (lastRun == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("suggestions", List.of());
                result.put("total", 0);
                return ApiResponse.ok(result);
            }
            runId = lastRun.getId();
        }
        
        List<MrpSuggestion> suggestions;
        
        if (suggestionType != null) {
            suggestions = mrpSuggestionMapper.selectByType(runId, suggestionType);
        } else if (status != null) {
            suggestions = mrpSuggestionMapper.selectByStatus(status);
        } else {
            suggestions = mrpSuggestionMapper.selectByRunId(runId);
        }
        
        int total = suggestions.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<MrpSuggestion> pageList = fromIndex < total 
            ? suggestions.subList(fromIndex, toIndex) 
            : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", pageList);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 确认采购建议
     */
    @PostMapping("/suggestions/{id}/accept")
    public ApiResponse<Void> acceptSuggestion(@PathVariable Long id) {
        int rows = mrpSuggestionMapper.updateStatus(id, "ACCEPTED", null);
        
        if (rows > 0) {
            return ApiResponse.ok();
        } else {
            return ApiResponse.fail("建议不存在或已处理");
        }
    }
    
    /**
     * 拒绝采购建议
     */
    @PostMapping("/suggestions/{id}/reject")
    public ApiResponse<Void> rejectSuggestion(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        
        int rows = mrpSuggestionMapper.updateStatus(id, "REJECTED", reason);
        
        if (rows > 0) {
            return ApiResponse.ok();
        } else {
            return ApiResponse.fail("建议不存在或已处理");
        }
    }
    
    /**
     * 获取 MRP 运行历史
     */
    @GetMapping("/history")
    public ApiResponse<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long total = mrpRunMapper.countAll();
        List<MrpRun> runs = mrpRunMapper.selectPage((pageNum - 1) * pageSize, pageSize);
        
        List<Map<String, Object>> records = runs.stream().map(run -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", run.getId());
            map.put("runNo", run.getRunNo());
            map.put("runType", run.getRunType());
            map.put("status", run.getStatus());
            map.put("suggestionCount", run.getSuggestionCount());
            map.put("createdAt", run.getCreatedAt());
            return map;
        }).toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
}
