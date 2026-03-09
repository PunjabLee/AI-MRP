package com.aimrp.forecast.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预测数据 Mapper
 */
@Mapper
public interface ForecastDataMapper {
    
    /**
     * 查询历史销售数据
     */
    List<Map<String, Object>> selectSalesHistory(
            @Param("itemCode") String itemCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 批量查询历史数据
     */
    List<Map<String, Object>> selectBatchSalesHistory(
            @Param("itemCodes") List<String> itemCodes,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 查询预测结果
     */
    List<Map<String, Object>> selectForecastResults(
            @Param("itemCode") String itemCode,
            @Param("forecastDate") LocalDate forecastDate);
    
    /**
     * 保存预测结果
     */
    void insertForecastResult(
            @Param("itemCode") String itemCode,
            @Param("forecastDate") LocalDate forecastDate,
            @Param("forecastQty") Double forecastQty,
            @Param("lowerBound") Double lowerBound,
            @Param("upperBound") Double upperBound,
            @Param("method") String method);
}
