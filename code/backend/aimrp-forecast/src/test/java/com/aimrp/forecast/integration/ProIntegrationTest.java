package com.aimrp.forecast.integration;

import com.aimrp.forecast.domain.service.DemandForecastService;
import com.aimrp.forecast.domain.service.DemandForecastService.HistoricalData;
import com.aimrp.forecast.domain.service.SafetyStockService;
import com.aimrp.forecast.domain.model.ForecastResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pro 阶段集成测试
 * 
 * 测试AI预测、安全库存、排程等Pro功能
 */
@SpringBootTest
public class ProIntegrationTest {
    
    @Autowired
    private DemandForecastService forecastService;
    
    @Autowired
    private SafetyStockService safetyStockService;
    
    /**
     * 测试场景1：需求预测流程
     */
    @Test
    public void testDemandForecast() {
        System.out.println("=== 测试：AI需求预测 ===");
        
        // 准备历史数据（30天）
        List<HistoricalData> history = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        Random random = new Random(42); // 固定种子保证测试稳定
        for (int i = 30; i > 0; i--) {
            HistoricalData data = new HistoricalData();
            data.setDate(today.minusDays(i));
            // 模拟趋势：100 ± 20
            data.setQty(new BigDecimal(100 + random.nextInt(40) - 20));
            history.add(data);
        }
        
        // 测试不同预测方法
        System.out.println("\n1. 简单移动平均:");
        List<ForecastResult> maResults = forecastService.forecast(
                "A001", history, 7, DemandForecastService.ForecastMethod.SIMPLE_MA);
        printForecastResults(maResults);
        
        System.out.println("\n2. 加权移动平均:");
        List<ForecastResult> wmaResults = forecastService.forecast(
                "A001", history, 7, DemandForecastService.ForecastMethod.WEIGHTED_MA);
        printForecastResults(wmaResults);
        
        System.out.println("\n3. 指数平滑:");
        List<ForecastResult> expResults = forecastService.forecast(
                "A001", history, 7, DemandForecastService.ForecastMethod.EXPONENTIAL);
        printForecastResults(expResults);
        
        // 验证
        assertTrue(maResults.size() == 7);
        assertTrue(wmaResults.size() == 7);
        assertTrue(expResults.size() == 7);
        
        // 验证预测值在合理范围
        for (ForecastResult r : maResults) {
            assertTrue(r.getForecastQty().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(r.getLowerBound());
            assertNotNull(r.getUpperBound());
        }
        
        System.out.println("\n=== 测试通过 ===");
    }
    
    /**
     * 测试场景2：安全库存计算
     */
    @Test
    public void testSafetyStock() {
        System.out.println("=== 测试：AI安全库存推荐 ===");
        
        // 准备需求历史
        List<BigDecimal> demandHistory = Arrays.asList(
                new BigDecimal("95"), new BigDecimal("105"),
                new BigDecimal("98"), new BigDecimal("102"),
                new BigDecimal("99"), new BigDecimal("101"),
                new BigDecimal("103"), new BigDecimal("97"),
                new BigDecimal("100"), new BigDecimal("104")
        );
        
        // 测试不同计算方法
        System.out.println("\n1. 固定天数法:");
        var fixedResult = safetyStockService.recommend(
                "A001",
                new BigDecimal("100"),
                demandHistory,
                7,
                SafetyStockService.CalculationMethod.FIXED_DAYS,
                null
        );
        System.out.printf("  推荐安全库存: %.2f%n", fixedResult.getRecommendedSafetyStock());
        System.out.printf("  说明: %s%n", fixedResult.getExplanation());
        
        System.out.println("\n2. 统计法:");
        var statResult = safetyStockService.recommend(
                "A001",
                new BigDecimal("100"),
                demandHistory,
                7,
                SafetyStockService.CalculationMethod.STATISTICAL,
                0.95
        );
        System.out.printf("  推荐安全库存: %.2f%n", statResult.getRecommendedSafetyStock());
        System.out.printf("  标准差: %.2f%n", statResult.getStandardDeviation());
        System.out.printf("  Z值: %.2f%n", statResult.getZScore());
        
        System.out.println("\n3. 服务水平法:");
        var serviceResult = safetyStockService.recommend(
                "A001",
                new BigDecimal("100"),
                demandHistory,
                7,
                SafetyStockService.CalculationMethod.SERVICE_LEVEL,
                0.95
        );
        System.out.printf("  推荐安全库存: %.2f%n", serviceResult.getRecommendedSafetyStock());
        System.out.printf("  目标服务水平: %.0f%%%n", 
                serviceResult.getTargetServiceLevel().multiply(new BigDecimal("100")));
        
        // 验证
        assertNotNull(fixedResult.getRecommendedSafetyStock());
        assertNotNull(statResult.getRecommendedSafetyStock());
        assertNotNull(serviceResult.getRecommendedSafetyStock());
        
        // 统计法应该更精确
        assertNotNull(statResult.getStandardDeviation());
        
        System.out.println("\n=== 测试通过 ===");
    }
    
    /**
     * 测试场景3：预测+安全库存联动
     */
    @Test
    public void testForecastAndSafetyStock() {
        System.out.println("=== 测试：预测+安全库存联动 ===");
        
        // 1. 获取预测
        List<HistoricalData> history = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 30; i > 0; i--) {
            HistoricalData data = new HistoricalData();
            data.setDate(today.minusDays(i));
            data.setQty(new BigDecimal(100 + (i % 20)));
            history.add(data);
        }
        
        List<ForecastResult> forecasts = forecastService.forecast(
                "A001", history, 30, DemandForecastService.ForecastMethod.WEIGHTED_MA);
        
        // 计算平均预测需求
        BigDecimal avgForecast = forecasts.stream()
                .map(ForecastResult::getForecastQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(forecasts.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        System.out.printf("  30天平均预测需求: %.2f%n", avgForecast);
        
        // 2. 基于预测计算安全库存
        List<BigDecimal> forecastQtys = new ArrayList<>();
        for (ForecastResult f : forecasts) {
            forecastQtys.add(f.getForecastQty());
        }
        
        var safetyRec = safetyStockService.recommend(
                "A001",
                avgForecast,
                forecastQtys,
                7,
                SafetyStockService.CalculationMethod.STATISTICAL,
                0.95
        );
        
        System.out.printf("  建议安全库存: %.2f%n", safetyRec.getRecommendedSafetyStock());
        System.out.printf("  说明: %s%n", safetyRec.getExplanation());
        
        // 3. 计算补货点
        // 补货点 = 安全库存 + (日均需求 × 采购周期)
        BigDecimal reorderPoint = safetyRec.getRecommendedSafetyStock()
                .add(avgForecast.multiply(new BigDecimal("7")));
        
        System.out.printf("  建议补货点: %.2f%n", reorderPoint);
        
        // 验证
        assertTrue(reorderPoint.compareTo(safetyRec.getRecommendedSafetyStock()) > 0);
        
        System.out.println("\n=== 测试通过 ===");
    }
    
    /**
     * 打印预测结果
     */
    private void printForecastResults(List<ForecastResult> results) {
        for (int i = 0; i < Math.min(3, results.size()); i++) {
            ForecastResult r = results.get(i);
            System.out.printf("  Day%d: %.2f [%.2f - %.2f]%n", 
                    i + 1,
                    r.getForecastQty(),
                    r.getLowerBound(),
                    r.getUpperBound());
        }
        if (results.size() > 3) {
            System.out.printf("  ... (%d more days)%n", results.size() - 3);
        }
    }
}
