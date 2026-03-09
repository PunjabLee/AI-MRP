package com.aimrp.whatif.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * What-if 场景 Mapper
 */
@Mapper
public interface WhatIfScenarioMapper {
    
    /**
     * 查询场景列表
     */
    List<Map<String, Object>> selectScenarios(
            @Param("userId") String userId,
            @Param("status") String status);
    
    /**
     * 查询场景详情
     */
    Map<String, Object> selectScenarioById(@Param("id") Long id);
    
    /**
     * 保存场景
     */
    Long insertScenario(
            @Param("scenarioName") String scenarioName,
            @Param("userId") String userId,
            @Param("description") String description,
            @Param("parameters") String parameters);
    
    /**
     * 更新场景
     */
    void updateScenario(
            @Param("id") Long id,
            @Param("parameters") String parameters,
            @Param("results") String results);
    
    /**
     * 删除场景
     */
    void deleteScenario(@Param("id") Long id);
    
    /**
     * 保存场景对比结果
     */
    void insertComparison(
            @Param("scenarioId1") Long scenarioId1,
            @Param("scenarioId2") Long scenarioId2,
            @Param("comparisonResult") String comparisonResult);
    
    /**
     * 查询对比历史
     */
    List<Map<String, Object>> selectComparisonHistory(
            @Param("userId") String userId);
}
