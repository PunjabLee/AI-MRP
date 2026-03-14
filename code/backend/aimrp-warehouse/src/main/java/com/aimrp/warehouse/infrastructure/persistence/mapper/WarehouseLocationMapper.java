package com.aimrp.warehouse.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.warehouse.domain.entity.WarehouseLocation;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 库位 Mapper
 */
@Mapper
public interface WarehouseLocationMapper extends BaseMapper<WarehouseLocation> {
    
    @Select("<script>" +
            "SELECT * FROM t_warehouse_location " +
            "<where>" +
            "  <if test='warehouseCode != null'> AND warehouse_code = #{warehouseCode} </if>" +
            "  <if test='areaCode != null'> AND area_code = #{areaCode} </if>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "  <if test='keyword != null'> AND (location_code LIKE CONCAT('%', #{keyword}, '%') OR location_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</where>" +
            " ORDER BY sort_order" +
            "</script>")
    List<WarehouseLocation> selectList(@Param("warehouseCode") String warehouseCode,
                                       @Param("areaCode") String areaCode,
                                       @Param("status") String status,
                                       @Param("keyword") String keyword);
    
    @Select("SELECT * FROM t_warehouse_location WHERE location_code = #{code}")
    WarehouseLocation selectByCode(@Param("code") String code);
}
