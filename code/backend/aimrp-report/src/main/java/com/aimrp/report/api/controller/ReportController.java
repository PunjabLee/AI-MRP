package com.aimrp.report.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.report.domain.entity.ReportConfig;
import com.aimrp.report.infrastructure.persistence.mapper.ReportConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表 Controller
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportConfigMapper reportConfigMapper;
    
    /**
     * 查询报表列表
     */
    @GetMapping("/configs")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        List<ReportConfig> list = reportConfigMapper.selectList(reportType, status, keyword);
        
        // 分页
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<ReportConfig> pageList = fromIndex < total ?
                list.subList(fromIndex, toIndex) : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 查询报表详情
     */
    @GetMapping("/configs/{id}")
    public ApiResponse<ReportConfig> getById(@PathVariable Long id) {
        ReportConfig config = reportConfigMapper.selectById(id);
        return ApiResponse.ok(config);
    }
    
    /**
     * 创建报表配置
     */
    @PostMapping("/configs")
    public ApiResponse<ReportConfig> create(@RequestBody ReportConfig config) {
        config.setStatus("ENABLED");
        reportConfigMapper.insert(config);
        return ApiResponse.ok(config);
    }
    
    /**
     * 更新报表配置
     */
    @PutMapping("/configs/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ReportConfig config) {
        config.setId(id);
        reportConfigMapper.updateById(config);
        return ApiResponse.ok();
    }
    
    /**
     * 删除报表配置
     */
    @DeleteMapping("/configs/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reportConfigMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 执行报表
     */
    @PostMapping("/execute/{id}")
    public ApiResponse<Map<String, Object>> execute(@PathVariable Long id) {
        ReportConfig config = reportConfigMapper.selectById(id);
        
        // TODO: 根据数据源执行报表查询
        Map<String, Object> result = new HashMap<>();
        result.put("reportCode", config.getReportCode());
        result.put("reportName", config.getReportName());
        result.put("data", List.of());
        result.put("total", 0);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取报表数据（通用接口）
     */
    @GetMapping("/data")
    public ApiResponse<Map<String, Object>> getReportData(
            @RequestParam String reportType,
            @RequestParam(required = false) Map<String, Object> params) {
        
        // TODO: 根据报表类型查询相应数据
        Map<String, Object> result = new HashMap<>();
        result.put("reportType", reportType);
        result.put("data", List.of());
        result.put("total", 0);
        
        return ApiResponse.ok(result);
    }
}
