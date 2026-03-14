package com.aimrp.equipment.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.equipment.api.dto.EquipmentCreateRequest;
import com.aimrp.equipment.domain.entity.Equipment;
import com.aimrp.equipment.infrastructure.persistence.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备管理 Controller
 */
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    
    private final EquipmentMapper equipmentMapper;
    
    /**
     * 获取设备列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) String workCenterCode) {
        
        var list = equipmentMapper.selectList(status, equipmentType, workCenterCode, null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取设备详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Equipment> getById(@PathVariable Long id) {
        return ApiResponse.ok(equipmentMapper.selectById(id));
    }
    
    /**
     * 创建设备
     */
    @PostMapping
    public ApiResponse<Equipment> create(@Validated @RequestBody EquipmentCreateRequest request) {
        // 检查编码是否存在
        Equipment exist = equipmentMapper.selectByCode(request.getEquipmentCode());
        if (exist != null) {
            return ApiResponse.fail("设备编码已存在");
        }
        
        Equipment equipment = new Equipment();
        equipment.setEquipmentCode(request.getEquipmentCode());
        equipment.setEquipmentName(request.getEquipmentName());
        equipment.setEquipmentType(request.getEquipmentType());
        equipment.setSpec(request.getSpec());
        equipment.setWorkshopCode(request.getWorkshopCode());
        equipment.setWorkCenterCode(request.getWorkCenterCode());
        equipment.setCapacity(request.getCapacity());
        equipment.setResponsible(request.getResponsible());
        equipment.setRemark(request.getRemark());
        equipment.setStatus("IDLE");
        
        equipmentMapper.insert(equipment);
        
        return ApiResponse.ok(equipment);
    }
    
    /**
     * 更新设备
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody EquipmentCreateRequest request) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            return ApiResponse.fail("设备不存在");
        }
        
        equipment.setEquipmentCode(request.getEquipmentCode());
        equipment.setEquipmentName(request.getEquipmentName());
        equipment.setEquipmentType(request.getEquipmentType());
        equipment.setSpec(request.getSpec());
        equipment.setWorkshopCode(request.getWorkshopCode());
        equipment.setWorkCenterCode(request.getWorkCenterCode());
        equipment.setCapacity(request.getCapacity());
        equipment.setResponsible(request.getResponsible());
        equipment.setRemark(request.getRemark());
        
        equipmentMapper.updateById(equipment);
        
        return ApiResponse.ok();
    }
    
    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        equipmentMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 设备报修
     */
    @PostMapping("/{id}/report-fault")
    public ApiResponse<Void> reportFault(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        Equipment equipment = equipmentMapper.selectById(id);
        equipment.setStatus("BROKEN");
        if (params != null && params.containsKey("remark")) {
            equipment.setRemark(params.get("remark"));
        }
        equipmentMapper.updateById(equipment);
        
        return ApiResponse.ok();
    }
    
    /**
     * 设备维修完成
     */
    @PostMapping("/{id}/repair-complete")
    public ApiResponse<Void> repairComplete(@PathVariable Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        equipment.setStatus("IDLE");
        equipmentMapper.updateById(equipment);
        
        return ApiResponse.ok();
    }
}
