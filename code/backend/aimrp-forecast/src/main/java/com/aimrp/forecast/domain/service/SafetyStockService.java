package com.aimrp.forecast.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 安全库存推荐服务
 * 
 * 基于需求波动和供应风险计算推荐安全库存
 */
@Slf4j
@Service
public class SafetyStockService {
    
    /**
     * 安全库存计算方法
     */
    public enum CalculationMethod {
        /**
         * 固定天数法
         */
        FIXED_DAYS,
        /**
         * 统计法（基于标准差）
         */
        STATISTICAL,
        /**
         * 服务水平法
         */
        SERVICE_LEVEL
    }
    
    /**
     * 计算安全库存推荐
     * 
     * @param itemCode 物料编码
     * @param avgDemand 平均需求
     * @param demandHistory 需求历史（用于计算波动）
     * @param leadTime 采购周期（天）
     * @param method 计算方法
     * @param targetServiceLevel 目标服务水平（0-1）
     * @return 安全库存推荐
     */
    public SafetyStockRecommendation recommend(String itemCode,
                                                BigDecimal avgDemand,
                                                List<BigDecimal> demandHistory,
                                                int leadTime,
                                                CalculationMethod method,
                                                Double targetServiceLevel) {
        
        log.info("计算安全库存 - itemCode: {}, 方法: {}", itemCode, method);
        
        SafetyStockRecommendation recommendation = new SafetyStockRecommendation();
        recommendation.setItemCode(itemCode);
        recommendation.setLeadTime(leadTime);
        
        switch (method) {
            case STATISTICAL:
                recommendStatistical(recommendation, avgDemand, demandHistory, leadTime, targetServiceLevel);
                break;
            case SERVICE_LEVEL:
                recommendServiceLevel(recommendation, avgDemand, demandHistory, leadTime, targetServiceLevel);
                break;
            case FIXED_DAYS:
            default:
                recommendFixedDays(recommendation, avgDemand, leadTime);
                break;
        }
        
        log.info("安全库存计算完成 - itemCode: {}, 推荐值: {}", itemCode, recommendation.getRecommendedSafetyStock());
        
        return recommendation;
    }
    
    /**
     * 固定天数法
     * 安全库存 = 平均日需求 × 固定天数
     */
    private void recommendFixedDays(SafetyStockRecommendation rec, BigDecimal avgDemand, int leadTime) {
        // 默认安全库存 = 采购周期天数
        int safetyDays = leadTime;
        
        BigDecimal safetyStock = avgDemand.multiply(new BigDecimal(safetyDays))
                .setScale(2, RoundingMode.HALF_UP);
        
        rec.setMethod(CalculationMethod.FIXED_DAYS);
        rec.setRecommendedSafetyStock(safetyStock);
        rec.setExplanation(String.format("固定%d天安全库存 = 日均需求 × %d天", safetyDays, safetyDays));
    }
    
    /**
     * 统计法（基于标准差）
     * 安全库存 = Z × √(leadTime) × σ
     * 其中 Z 为安全系数，σ 为需求标准差
     */
    private void recommendStatistical(SafetyStockRecommendation rec, 
                                        BigDecimal avgDemand, 
                                        List<BigDecimal> history,
                                        int leadTime,
                                        Double serviceLevel) {
        
        if (history == null || history.size() < 2) {
            // 数据不足，使用固定天数法
            recommendFixedDays(rec, avgDemand, leadTime);
            return;
        }
        
        // 计算标准差
        double stdDev = calculateStdDev(history);
        
        // 安全系数（默认95%服务水平对应1.65）
        double zScore = serviceLevel != null ? getZScore(serviceLevel) : 1.65;
        
        // 安全库存 = Z × √LT × σ
        double safetyStock = zScore * Math.sqrt(leadTime) * stdDev;
        
        rec.setMethod(CalculationMethod.STATISTICAL);
        rec.setRecommendedSafetyStock(BigDecimal.valueOf(safetyStock).setScale(2, RoundingMode.HALF_UP));
        rec.setStandardDeviation(BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP));
        rec.setZScore(zScore);
        rec.setExplanation(String.format("统计法: Z=%.2f, σ=%.2f, √LT=%.2f", zScore, stdDev, Math.sqrt(leadTime)));
    }
    
    /**
     * 服务水平法
     * 安全库存 = (日均需求 × 采购周期) × (服务水平系数 - 1)
     */
    private void recommendServiceLevel(SafetyStockRecommendation rec,
                                        BigDecimal avgDemand,
                                        List<BigDecimal> history,
                                        int leadTime,
                                        Double serviceLevel) {
        
        double targetLevel = serviceLevel != null ? serviceLevel : 0.95;
        
        // 日均需求 × 采购周期
        double cycleStock = avgDemand.doubleValue() * leadTime;
        
        // 安全库存 = 周期库存 × (服务水平系数 - 1)
        double safetyFactor = getZScore(targetLevel);
        double safetyStock = cycleStock * (safetyFactor - 1);
        
        // 简化计算：安全库存 = 日均需求 × 采购周期 × 0.5（假设服务水平95%）
        safetyStock = avgDemand.doubleValue() * leadTime * 0.5;
        
        rec.setMethod(CalculationMethod.SERVICE_LEVEL);
        rec.setRecommendedSafetyStock(BigDecimal.valueOf(safetyStock).setScale(2, RoundingMode.HALF_UP));
        rec.setTargetServiceLevel(BigDecimal.valueOf(targetLevel).setScale(2, RoundingMode.HALF_UP));
        rec.setExplanation(String.format("服务水平法: 目标%.0f%%", targetLevel * 100));
    }
    
    /**
     * 计算标准差
     */
    private double calculateStdDev(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return 0;
        
        // 计算平均值
        double sum = 0;
        for (BigDecimal v : values) {
            sum += v.doubleValue();
        }
        double mean = sum / values.size();
        
        // 计算方差
        double variance = 0;
        for (BigDecimal v : values) {
            double diff = v.doubleValue() - mean;
            variance += diff * diff;
        }
        variance /= values.size();
        
        return Math.sqrt(variance);
    }
    
    /**
     * 根据服务水平获取Z值
     */
    private double getZScore(double serviceLevel) {
        // 常用服务水平对应的Z值
        if (serviceLevel >= 0.99) return 2.33;
        if (serviceLevel >= 0.98) return 2.05;
        if (serviceLevel >= 0.95) return 1.65;
        if (serviceLevel >= 0.90) return 1.28;
        if (serviceLevel >= 0.85) return 1.04;
        if (serviceLevel >= 0.80) return 0.84;
        return 1.0; // 默认
    }
    
    /**
     * 安全库存推荐结果
     */
    @Data
    public static class SafetyStockRecommendation {
        private String itemCode;
        private Integer leadTime;
        private CalculationMethod method;
        private BigDecimal recommendedSafetyStock;
        private BigDecimal standardDeviation;
        private Double zScore;
        private BigDecimal targetServiceLevel;
        private String explanation;
    }
}
