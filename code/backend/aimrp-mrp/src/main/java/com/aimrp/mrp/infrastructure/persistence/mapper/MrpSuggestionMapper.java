package com.aimrp.mrp.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.mrp.domain.entity.MrpSuggestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * MRP 建议 Mapper
 */
@Mapper
public interface MrpSuggestionMapper extends BaseMapper<MrpSuggestion> {
    
    /**
     * 根据RunId查询建议列表
     */
    @Select("SELECT * FROM t_mrp_suggestion WHERE run_id = #{runId} ORDER BY priority, item_code")
    List<MrpSuggestion> selectByRunId(@Param("runId") Long runId);
    
    /**
     * 根据类型查询建议
     */
    @Select("SELECT * FROM t_mrp_suggestion WHERE run_id = #{runId} AND suggestion_type = #{type} ORDER BY priority")
    List<MrpSuggestion> selectByType(@Param("runId") Long runId, @Param("type") String type);
    
    /**
     * 查询待确认的建议
     */
    @Select("SELECT * FROM t_mrp_suggestion WHERE status = 'PENDING' ORDER BY priority, item_code")
    List<MrpSuggestion> selectPending();
    
    /**
     * 根据状态查询
     */
    @Select("SELECT * FROM t_mrp_suggestion WHERE status = #{status} ORDER BY priority")
    List<MrpSuggestion> selectByStatus(@Param("status") String status);
    
    /**
     * 更新状态
     */
    @Update("UPDATE t_mrp_suggestion SET status = #{status}, reject_reason = #{reason}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("reason") String reason);
    
    /**
     * 批量插入建议
     */
    @Insert("<script>" +
            "INSERT INTO t_mrp_suggestion (run_id, suggestion_type, item_code, item_name, " +
            "warehouse_code, suggestion_qty, due_date, priority, status, source, memo, created_at) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.runId}, #{item.suggestionType}, #{item.itemCode}, #{item.itemName}, " +
            "#{item.warehouseCode}, #{item.suggestionQty}, #{item.dueDate}, #{item.priority}, " +
            "#{item.status}, #{item.source}, #{item.memo}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<MrpSuggestion> suggestions);
    
    /**
     * 根据RunId删除建议
     */
    @Delete("DELETE FROM t_mrp_suggestion WHERE run_id = #{runId}")
    int deleteByRunId(@Param("runId") Long runId);
}
