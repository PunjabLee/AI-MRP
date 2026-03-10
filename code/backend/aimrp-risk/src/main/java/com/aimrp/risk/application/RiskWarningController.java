package com.aimrp.risk.application;

import com.aimrp.risk.domain.model.RiskItem;
import com.aimrp.risk.domain.service.RiskMonitorService;
import com.aimrp.risk.infrastructure.persistence.mapper.RiskWarningHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 风险预警 API
 */
@Slf4j
@RestController
@RequestMapping("/api/risk-warning")
@RequiredArgsConstructor
public class RiskWarningController {
    
    private final RiskMonitorService riskMonitorService;
    private final RiskWarningHistoryMapper warningHistoryMapper;
    
    /**
     * 发送风险预警
     * 
     * POST /api/risk-warning/send
     */
    @PostMapping("/send")
    public String sendWarning(@RequestBody RiskWarningRequest request) {
        log.info("发送风险预警 - 风险ID: {}", request.getRiskId());
        
        // 1. 获取风险详情
        List<RiskItem> risks = riskMonitorService.scanAllRisks();
        
        RiskItem targetRisk = risks.stream()
                .filter(r -> r.getRiskId().equals(request.getRiskId()))
                .findFirst()
                .orElse(null);
        
        if (targetRisk == null) {
            return "风险不存在";
        }
        
        // 2. 生成预警消息
        String warningMessage = generateWarningMessage(targetRisk);
        
        // 3. 发送通知（实际应接入消息服务）
        log.info("风险预警: {}", warningMessage);
        
        // 4. 更新状态
        targetRisk.setStatus(RiskItem.RiskStatus.MONITORING);
        
        return "预警已发送";
    }
    
    /**
     * 批量发送预警
     * 
     * POST /api/risk-warning/batch
     */
    @PostMapping("/batch")
    public String sendBatchWarnings(@RequestBody BatchWarningRequest request) {
        log.info("批量发送风险预警 - 风险等级: {}", request.getRiskLevel());
        
        List<RiskItem> risks = riskMonitorService.scanAllRisks();
        
        int sentCount = 0;
        for (RiskItem risk : risks) {
            if (request.getRiskLevel().equals(risk.getRiskLevel().name())) {
                String message = generateWarningMessage(risk);
                log.info("预警: {}", message);
                sentCount++;
            }
        }
        
        return String.format("已发送 %d 条预警", sentCount);
    }
    
    /**
     * 自动预警（定时任务）
     * 
     * POST /api/risk-warning/auto
     */
    @PostMapping("/auto")
    public String autoWarning() {
        log.info("执行自动风险预警检查");
        
        List<RiskItem> risks = riskMonitorService.scanAllRisks();
        
        int warningCount = 0;
        for (RiskItem risk : risks) {
            // 自动发送高风险和严重风险预警
            if (risk.getRiskLevel() == RiskItem.RiskLevel.HIGH ||
                risk.getRiskLevel() == RiskItem.RiskLevel.CRITICAL) {
                
                String message = generateWarningMessage(risk);
                log.warn("⚠️ 自动预警: {}", message);
                warningCount++;
            }
        }
        
        return String.format("自动检查完成，发现 %d 个高风险", warningCount);
    }
    
    /**
     * 获取预警历史
     * 
     * GET /api/risk-warning/history
     */
    @GetMapping("/history")
    public Map<String, Object> getWarningHistory(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long riskId,
            @RequestParam(required = false) String receiver) {
        
        List<Map<String, Object>> records;
        
        if (riskId != null) {
            records = warningHistoryMapper.selectByRiskId(riskId);
        } else if (receiver != null) {
            records = warningHistoryMapper.selectByReceiver(receiver, limit);
        } else {
            records = warningHistoryMapper.selectRecent(limit);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", records.size());
        
        return result;
    }
    
    /**
     * 生成预警消息
     */
    private String generateWarningMessage(RiskItem risk) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(risk.getRiskLevel().name()).append("】");
        sb.append(risk.getTitle()).append("\n");
        sb.append("描述: ").append(risk.getDescription()).append("\n");
        sb.append("风险值: ").append(risk.getRiskValue()).append("\n");
        sb.append("建议: ").append(risk.getSuggestedAction());
        
        return sb.toString();
    }
    
    // ==================== 请求模型 ====================
    
    @lombok.Data
    public static class RiskWarningRequest {
        private Long riskId;
        private String recipient;
        private String channel; // EMAIL, SMS, WECHAT, DINGTALK
    }
    
    @lombok.Data
    public static class BatchWarningRequest {
        private String riskLevel; // HIGH, CRITICAL
        private String channel;
    }
    
    @lombok.Data
    public static class WarningRecord {
        private Long warningId;
        private Long riskId;
        private String message;
        private String sentAt;
        private String channel;
        private boolean acknowledged;
    }
}
