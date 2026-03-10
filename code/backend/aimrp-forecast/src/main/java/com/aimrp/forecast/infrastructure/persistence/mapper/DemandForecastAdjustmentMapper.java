package com.aimrp.forecast.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.forecast.domain.entity.DemandForecastAdjustment;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 预测调整 Mapper
 */
@Mapper
public interface DemandForecastAdjustmentMapper extends BaseMapper<DemandForecastAdjustment> {
    
    /**
     * 查询物料的预测调整
     */
    @Select("SELECT * FROM t_demand_forecast_adjustment WHERE item_code = #{itemCode} AND status = 'ACTIVE' ORDER BY forecast_date")
    List<DemandForecastAdjustment> selectByItemCode(@Param("itemCode") String itemCode);
    
    /**
     * 查询日期范围内的调整
     */
    @Select("SELECT * FROM t_demand_forecast_adjustment WHERE item_code = #{itemCode} " +
            "AND forecast_date BETWEEN #{startDate} AND #{endDate} AND status = 'ACTIVE'")
    List<DemandForecastAdjustment> selectByDateRange(@Param("itemCode") String itemCode,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    /**
     * 取消调整
     */
    @Update("UPDATE t_demand_forecast_adjustment SET status = 'CANCELLED', updated_at = NOW() WHERE id = #{id}")
    int cancel(@Param("id") Long id);
}
