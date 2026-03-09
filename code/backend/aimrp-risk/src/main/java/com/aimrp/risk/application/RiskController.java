package com.aimrp.risk.application;

import com.aimrp.risk.domain.model.RiskItem;
import com.aimrp.risk.domain.service.RiskMonitorService;
import com.aimrp.risk.domain.service.RiskMonitorService.RiskStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 风险监控 API
 */
@Slf4j
@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {
    
    private final RiskMonitorService riskMonitorService;
    
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
        // TODO: 从数据库查询
        return null;
    }
    
    /**
     * 更新风险状态
     * 
     * PUT /api/risk/{id}/status
     */
    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新风险状态: id={}, status={}", id, status);
        // TODO: 更新数据库
        return "状态已更新";
    }
    
    /**
     * 忽略风险
     * 
     * POST /api/risk/{id}/ignore
     */
    @PostMapping("/{id}/ignore")
    public String ignoreRisk(@PathVariable Long id) {
        log.info("忽略风险: {}", id);
        return "风险已忽略";
    }
    
    /**
     * 解决风险
     * 
     * POST /api/risk/{id}/resolve
     */
    @PostMapping("/{id}/resolve")
    public String resolveRisk(@PathVariable Long id) {
        log.info("解决风险: {}", id);
        return "风险已解决";
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
