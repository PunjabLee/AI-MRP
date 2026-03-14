package com.aimrp.mrp.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 销售订单 Mapper（MRP 需求数据）
 */
@Mapper
public interface SalesOrderMapper {
    
    /**
     * 查询指定日期范围内的销售订单
     */
    List<Map<String, Object>> selectForMrp(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 查询指定物料的销售订单
     */
    List<Map<String, Object>> selectByItemCode(
            @Param("itemCode") String itemCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
