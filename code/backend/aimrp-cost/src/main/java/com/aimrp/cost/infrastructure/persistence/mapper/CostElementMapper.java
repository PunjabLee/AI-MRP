package com.aimrp.cost.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.cost.domain.entity.CostElement;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 成本要素 Mapper
 */
@Mapper
public interface CostElementMapper extends BaseMapper<CostElement> {
    
    @Select("<script>" +
            "SELECT * FROM t_cost_element " +
            "<where>" +
            "  <if test='costType != null'> AND cost_type = #{costType} </if>" +
            "  <if test='keyword != null'> AND (cost_code LIKE CONCAT('%', #{keyword}, '%') OR cost_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</where>" +
            " ORDER BY cost_type, id" +
            "</script>")
    List<CostElement> selectList(@Param("costType") String costType, @Param("keyword") String keyword);
}
