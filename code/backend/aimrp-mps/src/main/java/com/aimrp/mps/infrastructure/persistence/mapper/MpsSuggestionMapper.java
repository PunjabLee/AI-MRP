package com.aimrp.mps.infrastructure.persistence.mapper;

import com.aimrp.mps.domain.entity.MpsSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MPS 建议 Mapper
 */
@Mapper
public interface MpsSuggestionMapper {

    /**
     * 插入建议
     */
    int insert(MpsSuggestion suggestion);

    /**
     * 更新建议
     */
    int updateById(MpsSuggestion suggestion);

    /**
     * 根据ID查询
     */
    MpsSuggestion selectById(@Param("id") Long id);

    /**
     * 根据计划ID查询建议列表
     */
    List<MpsSuggestion> selectByPlanId(@Param("planId") Long planId);

    /**
     * 根据状态查询建议列表
     */
    List<MpsSuggestion> selectByStatus(@Param("status") String status);

    /**
     * 批量插入建议
     */
    int batchInsert(@Param("suggestions") List<MpsSuggestion> suggestions);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("remark") String remark);
}
