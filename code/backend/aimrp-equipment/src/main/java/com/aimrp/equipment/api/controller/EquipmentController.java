package com.aimrp.equipment.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.equipment.domain.entity.Equipment;
import com.aimrp.equipment.infrastructure.persistence.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
            @RequestParam(required = false) String workCenterCode,
            @RequestParam(required = false) String keyword) {
        
        List<Equipment> list = equipmentMapper.selectList(status, equipmentType, workCenterCode, keyword);
        
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
    public ApiResponse<Equipment> create(@RequestBody Equipment equipment) {
        Equipment exist = equipmentMapper.selectByCode(equipment.getEquipmentCode());
        if (exist != null) {
            return ApiResponse.fail("设备编码已存在");
        }
        
        equipment.setStatus("IDLE");
        equipmentMapper.insert(equipment);
        
        return ApiResponse.ok(equipment);
    }
    
    /**
     * 更新设备
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Equipment equipment) {
        equipment.setId(id);
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
    public ApiResponse<Void> reportFault(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setStatus("BROKEN");
        equipment.setRemark(params.get("remark"));
        equipmentMapper.updateById(equipment);
        
        return ApiResponse.ok();
    }
    
    /**
     * 设备维修完成
     */
    @PostMapping("/{id}/repair-complete")
    public ApiResponse<Void> repairComplete(@PathVariable Long id) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setStatus("IDLE");
        equipmentMapper.updateById(equipment);
        
        return ApiResponse.ok();
    }
    
    /**
     * 设备维保计划
     */
    @GetMapping("/{id}/maintenance-plan")
    public ApiResponse<Map<String, Object>> getMaintenancePlan(@PathVariable Long id) {
        // TODO: 查询设备维保计划
        Map<String, Object> plan = new HashMap<>();
        plan.put("lastMaintenanceDate", "2026-01-01");
        plan.put("nextMaintenanceDate", "2026-04-01");
        plan.put("maintenanceCycle", 90); // 天
        
        return ApiResponse.ok(plan);
    }
}
