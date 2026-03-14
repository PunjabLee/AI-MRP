package com.aimrp.mrp.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 成本分析历史 Mapper
 */
@Mapper
public interface CostAnalysisHistoryMapper {
    
    /**
     * 保存成本分析记录
     */
    @Insert("INSERT INTO t_cost_analysis_history (scenario_name, item_cost_change, labor_cost_change, " +
            "emergency_cost_change, total_cost_change, cost_change_rate, analysis_details, " +
            "created_by, created_at) " +
            "VALUES (#{scenarioName}, #{itemCostChange}, #{laborCostChange}, #{emergencyCostChange}, " +
            "#{totalCostChange}, #{costChangeRate}, #{analysisDetails}, #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Map<String, Object> record);
    
    /**
     * 查询历史记录
     */
    @Select("SELECT * FROM t_cost_analysis_history ORDER BY created_at DESC LIMIT #{limit}")
    List<Map<String, Object>> selectRecent(@Param("limit") int limit);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM t_cost_analysis_history WHERE id = #{id}")
    Map<String, Object> selectById(@Param("id") Long id);
    
    /**
     * 删除历史记录
     */
    @Delete("DELETE FROM t_cost_analysis_history WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
