package com.aimrp.risk.application.service;

import com.aimrp.risk.domain.service.RiskMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 风险应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskApplicationService {
    
    private final RiskMonitorService riskMonitorService;
    
    /**
     * 执行风险检测
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> detectRisks(RiskDetectRequest request) {
        log.info("执行风险检测 - itemCode: {}", request.getItemCode());
        
        var risks = riskMonitorService.detectRisk(request.getItemCode(), 
                request.getCurrentStock(), 
                request.getSafetyStock(), 
                request.getDemandQty());
        
        return risks.stream()
                .map(r -> Map.of(
                        "riskType", r.getRiskType(),
                        "level", r.getLevel(),
                        "description", r.getDescription()))
                .toList();
    }
    
    /**
     * 发送风险预警
     */
    @Transactional
    public void sendWarning(Long riskId) {
        log.info("发送风险预警 - riskId: {}", riskId);
        riskMonitorService.sendWarning(riskId);
    }
    
    @Data
    public static class RiskDetectRequest {
        private String itemCode;
        private java.math.BigDecimal currentStock;
        private java.math.BigDecimal safetyStock;
        private java.math.BigDecimal demandQty;
    }
}
