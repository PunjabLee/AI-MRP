package com.aimrp.equipment.infrastructure.persistence.mapper;

import com.aimrp.equipment.domain.entity.EquipmentMaintenance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EquipmentMaintenanceMapper {
    
    void insert(EquipmentMaintenance maintenance);
    
    void updateById(EquipmentMaintenance maintenance);
    
    EquipmentMaintenance selectById(Long id);
    
    List<EquipmentMaintenance> selectList(@Param("equipmentId") Long equipmentId, 
                                           @Param("status") String status);
    
    List<EquipmentMaintenance> selectPendingList();
}
