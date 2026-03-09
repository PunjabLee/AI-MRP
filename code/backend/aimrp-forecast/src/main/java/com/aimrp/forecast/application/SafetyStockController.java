package com.aimrp.forecast.application;

import com.aimrp.forecast.domain.service.SafetyStockService;
import com.aimrp.forecast.domain.service.SafetyStockService.SafetyStockRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 安全库存 API
 */
@Slf4j
@RestController
@RequestMapping("/api/safety-stock")
@RequiredArgsConstructor
public class SafetyStockController {
    
    private final SafetyStockService safetyStockService;
    
    /**
     * 计算安全库存推荐
     * 
     * POST /api/safety-stock/recommend
     * {
     *   "itemCode": "A001",
     *   "avgDemand": 100,
     *   "demandHistory": [90, 110, 95, 105, 100],
     *   "leadTime": 7,
     *   "method": "STATISTICAL",
     *   "targetServiceLevel": 0.95
     * }
     */
    @PostMapping("/recommend")
    public SafetyStockRecommendation recommend(@RequestBody RecommendRequest request) {
        log.info("计算安全库存 - itemCode: {}", request.getItemCode());
        
        SafetyStockService.CalculationMethod method = request.getMethod() != null
                ? SafetyStockService.CalculationMethod.valueOf(request.getMethod())
                : SafetyStockService.CalculationMethod.STATISTICAL;
        
        return safetyStockService.recommend(
                request.getItemCode(),
                request.getAvgDemand(),
                request.getDemandHistory(),
                request.getLeadTime(),
                method,
                request.getTargetServiceLevel()
        );
    }
    
    /**
     * 批量计算安全库存
     */
    @PostMapping("/batch")
    public List<SafetyStockRecommendation> recommendBatch(@RequestBody BatchRecommendRequest request) {
        log.info("批量计算安全库存 - 物料数: {}", request.getItems().size());
        
        return request.getItems().stream()
                .map(item -> safetyStockService.recommend(
                        item.getItemCode(),
                        item.getAvgDemand(),
                        item.getDemandHistory(),
                        item.getLeadTime(),
                        SafetyStockService.CalculationMethod.STATISTICAL,
                        0.95
                ))
                .toList();
    }
    
    @lombok.Data
    public static class RecommendRequest {
        private String itemCode;
        private BigDecimal avgDemand;
        private List<BigDecimal> demandHistory;
        private Integer leadTime;
        private String method;
        private Double targetServiceLevel;
    }
    
    @lombok.Data
    public static class BatchRecommendRequest {
        private List<ItemRecommendRequest> items;
        
        @lombok.Data
        public static class ItemRecommendRequest {
            private String itemCode;
            private BigDecimal avgDemand;
            private List<BigDecimal> demandHistory;
            private Integer leadTime;
        }
    }
}
