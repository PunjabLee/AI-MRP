package com.aimrp.production.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 生产工单 Mapper
 */
@Mapper
public interface ProductionOrderMapper {
    
    List<Map<String, Object>> selectList(@Param("status") String status,
                                        @Param("itemCode") String itemCode);
    
    Map<String, Object> selectById(@Param("id") Long id);
    
    Long insert(Map<String, Object> order);
    
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    void updateDates(@Param("id") Long id, 
                    @Param("startDate") String startDate,
                    @Param("endDate") String endDate);
}
