package com.aimrp.forecast.application.service;

import com.aimrp.forecast.domain.service.DemandForecastService;
import com.aimrp.forecast.domain.service.DemandForecastService.ForecastMethod;
import com.aimrp.forecast.domain.service.DemandForecastService.HistoricalData;
import com.aimrp.forecast.domain.service.SafetyStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 预测应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastApplicationService {
    
    private final DemandForecastService forecastService;
    private final SafetyStockService safetyStockService;
    
    /**
     * 执行需求预测
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> forecast(ForecastRequest request) {
        log.info("执行需求预测 - itemCode: {}, method: {}", request.getItemCode(), request.getMethod());
        
        // 准备历史数据
        List<HistoricalData> history = request.getHistory();
        
        // 执行预测
        var results = forecastService.forecast(
                request.getItemCode(),
                history,
                request.getPeriods(),
                ForecastMethod.valueOf(request.getMethod()));
        
        // 转换为 Map 格式
        return results.stream()
                .map(r -> Map.of(
                        "date", r.getDate().toString(),
                        "forecastQty", r.getForecastQty(),
                        "lowerBound", r.getLowerBound(),
                        "upperBound", r.getUpperBound()))
                .toList();
    }
    
    /**
     * 推荐安全库存
     */
    @Transactional(readOnly = true)
    public Map<String, Object> recommendSafetyStock(SafetyStockRequest request) {
        log.info("推荐安全库存 - itemCode: {}", request.getItemCode());
        
        var result = safetyStockService.recommend(
                request.getItemCode(),
                request.getAvgDemand(),
                request.getDemandHistory(),
                request.getLeadTime(),
                SafetyStockService.CalculationMethod.valueOf(request.getMethod()),
                request.getServiceLevel());
        
        return Map.of(
                "recommendedSafetyStock", result.getRecommendedSafetyStock(),
                "explanation", result.getExplanation(),
                "standardDeviation", result.getStandardDeviation() != null ? result.getStandardDeviation() : 0,
                "zScore", result.getZScore() != null ? result.getZScore() : 0);
    }
    
    @lombok.Data
    public static class ForecastRequest {
        private String itemCode;
        private List<HistoricalData> history;
        private Integer periods = 7;
        private String method = "SIMPLE_MA";
    }
    
    @lombok.Data
    public static class SafetyStockRequest {
        private String itemCode;
        private BigDecimal avgDemand;
        private List<BigDecimal> demandHistory;
        private Integer leadTime = 7;
        private String method = "FIXED_DAYS";
        private Double serviceLevel = 0.95;
    }
}
