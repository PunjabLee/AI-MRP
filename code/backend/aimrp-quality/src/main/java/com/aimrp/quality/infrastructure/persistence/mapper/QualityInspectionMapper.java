package com.aimrp.quality.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.quality.domain.entity.QualityInspection;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QualityInspectionMapper extends BaseMapper<QualityInspection> {
    
    @Select("<script>" +
            "SELECT * FROM t_quality_inspection " +
            "<where>" +
            "  <if test='inspectionType != null'> AND inspection_type = #{inspectionType} </if>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "</where>" +
            " ORDER BY created_at DESC" +
            "</script>")
    List<QualityInspection> selectList(@Param("inspectionType") String type, @Param("status") String status);
}
