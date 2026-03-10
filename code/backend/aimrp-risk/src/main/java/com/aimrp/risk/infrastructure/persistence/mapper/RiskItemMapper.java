package com.aimrp.risk.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.risk.domain.model.RiskItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 风险数据 Mapper
 */
@Mapper
public interface RiskItemMapper extends BaseMapper<RiskItem> {
    
    /**
     * 查询所有风险
     */
    @Select("SELECT * FROM t_risk_item WHERE status != 'RESOLVED' ORDER BY risk_level, created_at DESC")
    List<RiskItem> selectAllActive();
    
    /**
     * 按级别统计
     */
    @Select("SELECT risk_level, COUNT(*) as count FROM t_risk_item WHERE status != 'RESOLVED' GROUP BY risk_level")
    List<Map<String, Object>> countByLevel();
    
    /**
     * 按类型统计
     */
    @Select("SELECT risk_type, COUNT(*) as count FROM t_risk_item WHERE status != 'RESOLVED' GROUP BY risk_type")
    List<Map<String, Object>> countByType();
    
    /**
     * 更新状态
     */
    @Update("UPDATE t_risk_item SET status = #{status}, resolve_comment = #{comment}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("comment") String comment);
    
    /**
     * 插入风险记录
     */
    @Insert("INSERT INTO t_risk_item (risk_type, risk_level, title, description, item_code, " +
            "supplier_code, current_value, threshold, risk_impact, suggestion, status, created_at) " +
            "VALUES (#{riskType}, #{riskLevel}, #{title}, #{description}, #{itemCode}, " +
            "#{supplierCode}, #{currentValue}, #{threshold}, #{riskImpact}, #{suggestion}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRisk(RiskItem item);
    
    /**
     * 查询最近的
     */
    @Select("SELECT * FROM t_risk_item ORDER BY created_at DESC LIMIT #{limit}")
    List<RiskItem> selectRecent(@Param("limit") int limit);
}
