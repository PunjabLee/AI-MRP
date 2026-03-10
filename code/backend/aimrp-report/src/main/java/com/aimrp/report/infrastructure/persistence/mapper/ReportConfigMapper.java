package com.aimrp.report.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.report.domain.entity.ReportConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 报表配置 Mapper
 */
@Mapper
public interface ReportConfigMapper extends BaseMapper<ReportConfig> {
    
    @Select("<script>" +
            "SELECT * FROM t_report_config " +
            "<where>" +
            "  <if test='reportType != null'> AND report_type = #{reportType} </if>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "  <if test='keyword != null'> AND (report_code LIKE CONCAT('%', #{keyword}, '%') OR report_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</where>" +
            " ORDER BY report_type, id" +
            "</script>")
    List<ReportConfig> selectList(@Param("reportType") String reportType, @Param("status") String status, @Param("keyword") String keyword);
}
