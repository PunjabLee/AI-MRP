package com.aimrp.mrp.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 采购在途 Mapper（MRP 专用）
 */
@Mapper
public interface MrpPurchaseOnWayMapper {
    
    /**
     * 查询在途采购
     */
    List<Map<String, Object>> selectPurchaseOnWay(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 查询指定物料的在途采购
     */
    List<Map<String, Object>> selectByItemCode(
            @Param("itemCode") String itemCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
