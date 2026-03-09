package com.aimrp.mrp.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.mrp.application.service.MrpApplicationService;
import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * MRP 计算 Controller
 */
@RestController
@RequestMapping("/api/mrp")
@RequiredArgsConstructor
public class MrpController {
    
    private final MrpApplicationService mrpApplicationService;
    
    /**
     * 执行 MRP 计算
     */
    @PostMapping("/run")
    public ApiResponse<MrpResult> runMrp(@RequestBody(required = false) Map<String, Object> params) {
        MrpRun mrpRun = new MrpRun();
        
        if (params != null) {
            if (params.containsKey("runType")) {
                mrpRun.setRunType((String) params.get("runType"));
            }
            if (params.containsKey("planStartDate")) {
                mrpRun.setPlanStartDate(LocalDate.parse((String) params.get("planStartDate")));
            }
            if (params.containsKey("planEndDate")) {
                mrpRun.setPlanEndDate(LocalDate.parse((String) params.get("planEndDate")));
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
        // TODO: 从数据库查询实际结果
        Map<String, Object> result = new HashMap<>();
        result.put("runId", runId);
        result.put("status", "COMPLETED");
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取 MRP 参数
     */
    @GetMapping("/parameters")
    public ApiResponse<Map<String, Object>> getParameters() {
        // TODO: 从数据库查询实际参数
        Map<String, Object> params = new HashMap<>();
        params.put("planningHorizon", 90);
        params.put("planStartDate", LocalDate.now());
        params.put("allowNegative", false);
        params.put("timeBucket", "DAY");
        
        return ApiResponse.ok(params);
    }
    
    /**
     * 更新 MRP 参数
     */
    @PutMapping("/parameters")
    public ApiResponse<Void> updateParameters(@RequestBody Map<String, Object> params) {
        // TODO: 保存到数据库
        return ApiResponse.ok();
    }
    
    /**
     * 获取 MRP 建议列表
     */
    @GetMapping("/suggestions")
    public ApiResponse<Map<String, Object>> getSuggestions(
            @RequestParam(required = false) String suggestionType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // TODO: 从数据库查询
        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", new Object[]{});
        result.put("total", 0);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 确认采购建议
     */
    @PostMapping("/suggestions/{id}/accept")
    public ApiResponse<Void> acceptSuggestion(@PathVariable Long id) {
        // TODO: 更新状态为 ACCEPTED
        return ApiResponse.ok();
    }
    
    /**
     * 拒绝采购建议
     */
    @PostMapping("/suggestions/{id}/reject")
    public ApiResponse<Void> rejectSuggestion(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        // TODO: 更新状态为 REJECTED，记录原因
        return ApiResponse.ok();
    }
}
