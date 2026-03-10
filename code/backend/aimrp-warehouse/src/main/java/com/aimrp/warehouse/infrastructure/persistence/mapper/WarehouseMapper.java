package com.aimrp.warehouse.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.warehouse.domain.entity.Warehouse;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 仓库 Mapper
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
    
    @Select("<script>" +
            "SELECT * FROM t_warehouse " +
            "<where>" +
            "  <if test='warehouseType != null'> AND warehouse_type = #{warehouseType} </if>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "</where>" +
            " ORDER BY id" +
            "</script>")
    List<Warehouse> selectList(@Param("warehouseType") String warehouseType, @Param("status") String status);
    
    @Select("SELECT * FROM t_warehouse WHERE warehouse_code = #{code}")
    Warehouse selectByCode(@Param("code") String code);
}
