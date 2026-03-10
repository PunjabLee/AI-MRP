package com.aimrp.equipment.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.equipment.domain.entity.Equipment;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 设备 Mapper
 */
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
    
    @Select("<script>" +
            "SELECT * FROM t_equipment " +
            "<where>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "  <if test='equipmentType != null'> AND equipment_type = #{equipmentType} </if>" +
            "  <if test='workCenterCode != null'> AND work_center_code = #{workCenterCode} </if>" +
            "  <if test='keyword != null'> AND (equipment_code LIKE CONCAT('%', #{keyword}, '%') OR equipment_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</where>" +
            " ORDER BY id" +
            "</script>")
    List<Equipment> selectList(@Param("status") String status,
                              @Param("equipmentType") String equipmentType,
                              @Param("workCenterCode") String workCenterCode,
                              @Param("keyword") String keyword);
    
    @Select("SELECT * FROM t_equipment WHERE equipment_code = #{code}")
    Equipment selectByCode(@Param("code") String code);
    
    @Select("SELECT * FROM t_equipment WHERE work_center_code = #{workCenterCode} AND status = 'RUNNING'")
    List<Equipment> selectRunningByWorkCenter(@Param("workCenterCode") String workCenterCode);
}
