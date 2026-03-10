package com.aimrp.whatif.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.whatif.domain.model.WhatIfScenario;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * What-If 场景 Mapper
 */
@Mapper
public interface WhatIfScenarioMapper extends BaseMapper<WhatIfScenario> {
    
    /**
     * 查询所有场景
     */
    @Select("SELECT * FROM t_whatif_scenario ORDER BY created_at DESC")
    List<WhatIfScenario> selectAll();
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM t_whatif_scenario WHERE scenario_id = #{scenarioId}")
    WhatIfScenario selectByScenarioId(@Param("scenarioId") Long scenarioId);
    
    /**
     * 查询最近的场景
     */
    @Select("SELECT * FROM t_whatif_scenario ORDER BY created_at DESC LIMIT #{limit}")
    List<WhatIfScenario> selectRecent(@Param("limit") int limit);
    
    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM t_whatif_scenario WHERE status = #{status} ORDER BY created_at DESC")
    List<WhatIfScenario> selectByStatus(@Param("status") String status);
    
    /**
     * 更新状态
     */
    @Update("UPDATE t_whatif_scenario SET status = #{status}, updated_at = NOW() WHERE scenario_id = #{scenarioId}")
    int updateStatus(@Param("scenarioId") Long scenarioId, @Param("status") String status);
    
    /**
     * 删除场景
     */
    @Delete("DELETE FROM t_whatif_scenario WHERE scenario_id = #{scenarioId}")
    int deleteByScenarioId(@Param("scenarioId") Long scenarioId);
    
    /**
     * 插入场景
     */
    @Insert("INSERT INTO t_whatif_scenario (scenario_id, scenario_name, description, scenario_type, " +
            "baseline_id, changes_json, status, created_at) " +
            "VALUES (#{scenarioId}, #{scenarioName}, #{description}, #{scenarioType}, " +
            "#{baselineId}, #{changesJson}, #{status}, NOW())")
    int insertScenario(WhatIfScenario scenario);
    
    /**
     * 更新场景
     */
    @Update("UPDATE t_whatif_scenario SET scenario_name = #{scenarioName}, description = #{description}, " +
            "changes_json = #{changesJson}, status = #{status}, updated_at = NOW() " +
            "WHERE scenario_id = #{scenarioId}")
    int updateScenario(WhatIfScenario scenario);
}
