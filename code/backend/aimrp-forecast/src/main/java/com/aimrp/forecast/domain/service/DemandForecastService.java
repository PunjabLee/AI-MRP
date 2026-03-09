package com.aimrp.forecast.domain.service;

import com.aimrp.forecast.domain.model.ForecastResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 需求预测服务
 * 
 * 使用时间序列算法进行需求预测
 * 当前实现：简单移动平均 + 指数平滑（可替换为 Prophet）
 */
@Slf4j
@Service
public class DemandForecastService {
    
    /**
     * 预测方法类型
     */
    public enum ForecastMethod {
        SIMPLE_MA,      // 简单移动平均
        WEIGHTED_MA,   // 加权移动平均
        EXPONENTIAL    // 指数平滑
    }
    
    /**
     * 执行需求预测
     * 
     * @param itemCode 物料编码
     * @param historicalData 历史数据（日期 -> 数量）
     * @param forecastDays 预测天数
     * @param method 预测方法
     * @return 预测结果列表
     */
    public List<ForecastResult> forecast(String itemCode, 
                                          List<HistoricalData> historicalData, 
                                          int forecastDays,
                                          ForecastMethod method) {
        
        log.info("开始预测 - itemCode: {}, 方法: {}, 预测天数: {}", itemCode, method, forecastDays);
        
        if (historicalData == null || historicalData.isEmpty()) {
            log.warn("历史数据为空，无法预测");
            return new ArrayList<>();
        }
        
        // 按日期排序
        historicalData.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        
        List<ForecastResult> results = new ArrayList<>();
        
        switch (method) {
            case WEIGHTED_MA:
                results = forecastWeightedMA(itemCode, historicalData, forecastDays);
                break;
            case EXPONENTIAL:
                results = forecastExponential(itemCode, historicalData, forecastDays);
                break;
            case SIMPLE_MA:
            default:
                results = forecastSimpleMA(itemCode, historicalData, forecastDays);
                break;
        }
        
        log.info("预测完成 - itemCode: {}, 结果数: {}", itemCode, results.size());
        
        return results;
    }
    
    /**
     * 简单移动平均预测
     */
    private List<ForecastResult> forecastSimpleMA(String itemCode, 
                                                     List<HistoricalData> data, 
                                                     int days) {
        List<ForecastResult> results = new ArrayList<>();
        
        // 取最近7天的平均值
        int windowSize = Math.min(7, data.size());
        BigDecimal avg = calculateAverage(data, data.size() - windowSize, data.size());
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        
        for (int i = 0; i < days; i++) {
            ForecastResult result = new ForecastResult();
            result.setItemCode(itemCode);
            result.setForecastDate(startDate.plusDays(i));
            result.setForecastQty(avg);
            result.setLowerBound(avg.multiply(new BigDecimal("0.8")).setScale(2, RoundingMode.HALF_UP));
            result.setUpperBound(avg.multiply(new BigDecimal("1.2")).setScale(2, RoundingMode.HALF_UP));
            result.setForecastType("DAILY");
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 加权移动平均预测（近期数据权重更高）
     */
    private List<ForecastResult> forecastWeightedMA(String itemCode, 
                                                      List<HistoricalData> data, 
                                                      int days) {
        List<ForecastResult> results = new ArrayList<>();
        
        int windowSize = Math.min(7, data.size());
        BigDecimal weightedSum = BigDecimal.ZERO;
        int weightSum = 0;
        
        for (int i = 0; i < windowSize; i++) {
            int idx = data.size() - windowSize + i;
            int weight = windowSize - i; // 越近权重越高
            weightedSum = weightedSum.add(data.get(idx).getQty().multiply(new BigDecimal(weight)));
            weightSum += weight;
        }
        
        BigDecimal avg = weightedSum.divide(new BigDecimal(weightSum), 2, RoundingMode.HALF_UP);
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        
        // 加权平均可能更准确，置信区间更窄
        for (int i = 0; i < days; i++) {
            ForecastResult result = new ForecastResult();
            result.setItemCode(itemCode);
            result.setForecastDate(startDate.plusDays(i));
            result.setForecastQty(avg);
            result.setLowerBound(avg.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP));
            result.setUpperBound(avg.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP));
            result.setForecastType("DAILY");
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 指数平滑预测
     */
    private List<ForecastResult> forecastExponential(String itemCode, 
                                                      List<HistoricalData> data, 
                                                      int days) {
        List<ForecastResult> results = new ArrayList<>();
        
        double alpha = 0.3; // 平滑系数
        double level = data.get(0).getQty().doubleValue();
        
        // 计算初始水平
        for (HistoricalData d : data) {
            double qty = d.getQty().doubleValue();
            level = alpha * qty + (1 - alpha) * level;
        }
        
        // 预测
        LocalDate startDate = LocalDate.now().plusDays(1);
        
        for (int i = 0; i < days; i++) {
            ForecastResult result = new ForecastResult();
            result.setItemCode(itemCode);
            result.setForecastDate(startDate.plusDays(i));
            result.setForecastQty(BigDecimal.valueOf(level).setScale(2, RoundingMode.HALF_UP));
            
            // 指数平滑置信区间随预测天数增加而增大
            double margin = 0.1 + (i * 0.02);
            result.setLowerBound(BigDecimal.valueOf(level * (1 - margin)).setScale(2, RoundingMode.HALF_UP));
            result.setUpperBound(BigDecimal.valueOf(level * (1 + margin)).setScale(2, RoundingMode.HALF_UP));
            result.setForecastType("DAILY");
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 计算平均值
     */
    private BigDecimal calculateAverage(List<HistoricalData> data, int start, int end) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = start; i < end; i++) {
            sum = sum.add(data.get(i).getQty());
        }
        return sum.divide(new BigDecimal(end - start), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 历史数据
     */
    @lombok.Data
    public static class HistoricalData {
        private LocalDate date;
        private BigDecimal qty;
    }
}
