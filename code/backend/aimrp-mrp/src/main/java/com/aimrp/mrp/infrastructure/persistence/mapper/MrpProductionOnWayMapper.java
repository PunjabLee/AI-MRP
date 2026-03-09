package com.aimrp.mrp.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 生产在制 Mapper（MRP 专用）
 */
@Mapper
public interface MrpProductionOnWayMapper {
    
    /**
     * 查询在制工单
     */
    List<Map<String, Object>> selectProductionOnWay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 查询指定物料的在制
     */
    List<Map<String, Object>> selectByItemCode(
            @Param("itemCode") String itemCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
