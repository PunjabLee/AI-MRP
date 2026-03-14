package com.aimrp.risk.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 风险预警历史 Mapper
 */
@Mapper
public interface RiskWarningHistoryMapper {
    
    /**
     * 保存预警记录
     */
    @Insert("INSERT INTO t_risk_warning_history (risk_id, risk_title, risk_level, warning_type, " +
            "receiver, channel, send_status, send_time, message) " +
            "VALUES (#{riskId}, #{riskTitle}, #{riskLevel}, #{warningType}, " +
            "#{receiver}, #{channel}, #{sendStatus}, NOW(), #{message})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Map<String, Object> record);
    
    /**
     * 查询预警历史
     */
    @Select("SELECT * FROM t_risk_warning_history ORDER BY send_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectRecent(@Param("limit") int limit);
    
    /**
     * 按风险ID查询
     */
    @Select("SELECT * FROM t_risk_warning_history WHERE risk_id = #{riskId} ORDER BY send_time DESC")
    List<Map<String, Object>> selectByRiskId(@Param("riskId") Long riskId);
    
    /**
     * 按接收人查询
     */
    @Select("SELECT * FROM t_risk_warning_history WHERE receiver = #{receiver} ORDER BY send_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectByReceiver(@Param("receiver") String receiver, @Param("limit") int limit);
    
    /**
     * 统计发送次数
     */
    @Select("SELECT COUNT(*) FROM t_risk_warning_history WHERE risk_id = #{riskId}")
    int countByRiskId(@Param("riskId") Long riskId);
}
