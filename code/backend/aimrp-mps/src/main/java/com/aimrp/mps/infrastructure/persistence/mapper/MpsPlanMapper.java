package com.aimrp.mps.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.mps.domain.entity.MpsPlan;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * MPS计划 Mapper
 */
@Mapper
public interface MpsPlanMapper extends BaseMapper<MpsPlan> {
    
    @Select("<script>" +
            "SELECT * FROM t_mps_plan " +
            "<where>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "  <if test='itemCode != null'> AND item_code LIKE CONCAT('%', #{itemCode}, '%') </if>" +
            "</where>" +
            " ORDER BY start_date" +
            "</script>")
    List<MpsPlan> selectList(@Param("status") String status, @Param("itemCode") String itemCode);
    
    @Select("SELECT * FROM t_mps_plan WHERE id = #{id}")
    MpsPlan selectById(@Param("id") Long id);
}
