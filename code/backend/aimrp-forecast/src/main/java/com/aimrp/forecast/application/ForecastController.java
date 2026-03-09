package com.aimrp.forecast.application;

import com.aimrp.forecast.domain.model.ForecastResult;
import com.aimrp.forecast.domain.service.DemandForecastService;
import com.aimrp.forecast.domain.service.DemandForecastService.HistoricalData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 预测 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {
    
    private final DemandForecastService forecastService;
    
    /**
     * 需求预测
     * 
     * POST /api/forecast/demand
     * {
     *   "itemCode": "A001",
     *   "historicalData": [
     *     {"date": "2024-01-01", "qty": 100},
     *     {"date": "2024-01-02", "qty": 120}
     *   ],
     *   "forecastDays": 30,
     *   "method": "WEIGHTED_MA"
     * }
     */
    @PostMapping("/demand")
    public List<ForecastResult> forecastDemand(@RequestBody ForecastRequest request) {
        log.info("接收预测请求 - itemCode: {}", request.getItemCode());
        
        // 转换历史数据
        List<HistoricalData> historicalData = new ArrayList<>();
        if (request.getHistoricalData() != null) {
            for (ForecastRequest.HistoricalDataDTO dto : request.getHistoricalData()) {
                HistoricalData data = new HistoricalData();
                data.setDate(dto.getDate());
                data.setQty(dto.getQty());
                historicalData.add(data);
            }
        }
        
        // 执行预测
        DemandForecastService.ForecastMethod method = request.getMethod() != null 
                ? DemandForecastService.ForecastMethod.valueOf(request.getMethod())
                : DemandForecastService.ForecastMethod.WEIGHTED_MA;
        
        int forecastDays = request.getForecastDays() != null ? request.getForecastDays() : 30;
        
        return forecastService.forecast(request.getItemCode(), historicalData, forecastDays, method);
    }
    
    /**
     * 批量预测
     */
    @PostMapping("/batch")
    public List<ForecastResult> forecastBatch(@RequestBody BatchForecastRequest request) {
        log.info("接收批量预测请求 - 物料数: {}", request.getItems().size());
        
        List<ForecastResult> allResults = new ArrayList<>();
        
        for (String itemCode : request.getItems()) {
            // 模拟历史数据（实际应从数据库查询）
            List<HistoricalData> mockData = generateMockData(itemCode);
            
            DemandForecastService.ForecastMethod method = request.getMethod() != null 
                    ? DemandForecastService.ForecastMethod.valueOf(request.getMethod())
                    : DemandForecastService.ForecastMethod.WEIGHTED_MA;
            
            List<ForecastResult> results = forecastService.forecast(itemCode, mockData, 30, method);
            allResults.addAll(results);
        }
        
        return allResults;
    }
    
    /**
     * 生成模拟历史数据（实际应从销售订单查询）
     */
    private List<HistoricalData> generateMockData(String itemCode) {
        List<HistoricalData> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 生成30天历史数据
        for (int i = 30; i > 0; i--) {
            HistoricalData d = new HistoricalData();
            d.setDate(today.minusDays(i));
            // 随机数量 80-120
            int qty = 80 + (int)(Math.random() * 40);
            d.setQty(new BigDecimal(qty));
            data.add(d);
        }
        
        return data;
    }
    
    /**
     * 预测请求
     */
    @lombok.Data
    public static class ForecastRequest {
        private String itemCode;
        private List<HistoricalDataDTO> historicalData;
        private Integer forecastDays;
        private String method;
        
        @lombok.Data
        public static class HistoricalDataDTO {
            private LocalDate date;
            private BigDecimal qty;
        }
    }
    
    /**
     * 批量预测请求
     */
    @lombok.Data
    public static class BatchForecastRequest {
        private List<String> items;
        private Integer forecastDays;
        private String method;
    }
}
