package com.aimrp.risk.application;

import com.aimrp.risk.domain.model.RiskItem;
import com.aimrp.risk.domain.service.RiskMonitorService;
import com.aimrp.risk.domain.service.RiskMonitorService.RiskStatistics;
import com.aimrp.risk.infrastructure.persistence.mapper.RiskItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 风险监控 API
 */
@Slf4j
@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {
    
    private final RiskMonitorService riskMonitorService;
    private final RiskItemMapper riskItemMapper;
    
    /**
     * 扫描所有风险
     * 
     * GET /api/risk/scan
     */
    @GetMapping("/scan")
    public List<RiskItem> scanRisks() {
        log.info("接收风险扫描请求");
        return riskMonitorService.scanAllRisks();
    }
    
    /**
     * 获取风险统计
     * 
     * GET /api/risk/statistics
     */
    @GetMapping("/statistics")
    public RiskStatistics getStatistics() {
        List<RiskItem> risks = riskMonitorService.scanAllRisks();
        return riskMonitorService.getStatistics(risks);
    }
    
    /**
     * 获取风险详情
     * 
     * GET /api/risk/{id}
     */
    @GetMapping("/{id}")
    public RiskItem getRiskDetail(@PathVariable Long id) {
        log.info("获取风险详情: {}", id);
        return riskItemMapper.selectById(id);
    }
    
    /**
     * 获取所有风险列表
     * 
     * GET /api/risk/list
     */
    @GetMapping("/list")
    public List<RiskItem> getRiskList(
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String riskType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        // 简化实现：返回所有活跃风险
        return riskItemMapper.selectAllActive();
    }
    
    /**
     * 更新风险状态
     * 
     * PUT /api/risk/{id}/status
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam(required = false) String comment) {
        
        log.info("更新风险状态: id={}, status={}", id, status);
        
        int rows = riskItemMapper.updateStatus(id, status, comment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "状态已更新" : "更新失败");
        
        return result;
    }
    
    /**
     * 忽略风险
     * 
     * POST /api/risk/{id}/ignore
     */
    @PostMapping("/{id}/ignore")
    public Map<String, Object> ignoreRisk(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> params) {
        
        log.info("忽略风险: {}", id);
        String comment = params != null ? params.get("comment") : "用户忽略";
        
        int rows = riskItemMapper.updateStatus(id, "IGNORED", comment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        
        return result;
    }
    
    /**
     * 解决风险
     * 
     * POST /api/risk/{id}/resolve
     */
    @PostMapping("/{id}/resolve")
    public Map<String, Object> resolveRisk(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> params) {
        
        log.info("解决风险: {}", id);
        String comment = params != null ? params.get("comment") : "已解决";
        
        int rows = riskItemMapper.updateStatus(id, "RESOLVED", comment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        
        return result;
    }
    
    /**
     * 获取风险看板数据
     * 
     * GET /api/risk/dashboard
     */
    @GetMapping("/dashboard")
    public RiskDashboard getDashboard() {
        List<RiskItem> risks = riskMonitorService.scanAllRisks();
        RiskStatistics stats = riskMonitorService.getStatistics(risks);
        
        RiskDashboard dashboard = new RiskDashboard();
        dashboard.setTotalRisks(stats.getTotalCount());
        dashboard.setCriticalCount(stats.getByLevel().getOrDefault(RiskItem.RiskLevel.CRITICAL, 0L).intValue());
        dashboard.setHighCount(stats.getByLevel().getOrDefault(RiskItem.RiskLevel.HIGH, 0L).intValue());
        dashboard.setMediumCount(stats.getByLevel().getOrDefault(RiskItem.RiskLevel.MEDIUM, 0L).intValue());
        dashboard.setLowCount(stats.getByLevel().getOrDefault(RiskItem.RiskLevel.LOW, 0L).intValue());
        dashboard.setOverallRiskValue(stats.getOverallRiskValue());
        dashboard.setRecentRisks(risks.stream().limit(10).toList());
        
        return dashboard;
    }
    
    /**
     * 风险看板
     */
    @lombok.Data
    public static class RiskDashboard {
        private int totalRisks;
        private int criticalCount;
        private int highCount;
        private int mediumCount;
        private int lowCount;
        private java.math.BigDecimal overallRiskValue;
        private List<RiskItem> recentRisks;
    }
}
