package com.aimrp.equipment.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.equipment.api.dto.MaintenanceCreateRequest;
import com.aimrp.equipment.domain.entity.EquipmentMaintenance;
import com.aimrp.equipment.infrastructure.persistence.mapper.EquipmentMaintenanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备维护计划 Controller
 */
@RestController
@RequestMapping("/api/equipment/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
    
    private final EquipmentMaintenanceMapper maintenanceMapper;
    
    /**
     * 创建维护计划
     */
    @PostMapping
    public ApiResponse<EquipmentMaintenance> create(@Validated @RequestBody MaintenanceCreateRequest request) {
        EquipmentMaintenance maintenance = new EquipmentMaintenance();
        maintenance.setPlanNo("MP" + System.currentTimeMillis());
        maintenance.setEquipmentId(request.getEquipmentId());
        maintenance.setEquipmentCode(request.getEquipmentCode());
        maintenance.setEquipmentName(request.getEquipmentName());
        maintenance.setMaintenanceType(request.getMaintenanceType());
        maintenance.setPlanDate(request.getPlanDate());
        maintenance.setContent(request.getContent());
        maintenance.setMaintainer(request.getMaintainer());
        maintenance.setDuration(request.getDuration());
        maintenance.setCost(request.getCost());
        maintenance.setStatus("PENDING");
        
        maintenanceMapper.insert(maintenance);
        
        return ApiResponse.ok(maintenance);
    }
    
    /**
     * 开始维护
     */
    @PostMapping("/{id}/start")
    public ApiResponse<Void> start(@PathVariable Long id) {
        EquipmentMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            return ApiResponse.fail("维护计划不存在");
        }
        
        maintenance.setStatus("IN_PROGRESS");
        maintenance.setActualDate(LocalDate.now());
        maintenanceMapper.updateById(maintenance);
        
        return ApiResponse.ok();
    }
    
    /**
     * 完成维护
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        EquipmentMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            return ApiResponse.fail("维护计划不存在");
        }
        
        maintenance.setStatus("COMPLETED");
        maintenance.setActualDate(LocalDate.now());
        
        if (params.containsKey("duration")) {
            maintenance.setDuration(new BigDecimal(params.get("duration").toString()));
        }
        if (params.containsKey("cost")) {
            maintenance.setCost(new BigDecimal(params.get("cost").toString()));
        }
        if (params.containsKey("remark")) {
            maintenance.setRemark(params.get("remark").toString());
        }
        
        maintenanceMapper.updateById(maintenance);
        
        return ApiResponse.ok();
    }
    
    /**
     * 获取设备维护列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String status) {
        
        List<EquipmentMaintenance> list = maintenanceMapper.selectList(equipmentId, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取待执行的维护计划
     */
    @GetMapping("/pending")
    public ApiResponse<List<EquipmentMaintenance>> getPending() {
        return ApiResponse.ok(maintenanceMapper.selectPendingList());
    }
    
    /**
     * 获取维护详情
     */
    @GetMapping("/{id}")
    public ApiResponse<EquipmentMaintenance> getById(@PathVariable Long id) {
        return ApiResponse.ok(maintenanceMapper.selectById(id));
    }
    
    /**
     * 取消维护计划
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        EquipmentMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            return ApiResponse.fail("维护计划不存在");
        }
        
        if (!"PENDING".equals(maintenance.getStatus())) {
            return ApiResponse.fail("只有待执行状态可以取消");
        }
        
        maintenance.setStatus("CANCELLED");
        maintenanceMapper.updateById(maintenance);
        
        return ApiResponse.ok();
    }
}
