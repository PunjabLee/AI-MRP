package com.aimrp.equipment.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 设备管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    
    /**
     * 获取设备列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) String status) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> e1 = new HashMap<>();
        e1.put("id", 1L);
        e1.put("equipmentCode", "EQ001");
        e1.put("equipmentName", "加工中心1");
        e1.put("equipmentType", "CNC");
        e1.put("status", "RUNNING");
        e1.put("oee", 0.85);
        list.add(e1);
        
        Map<String, Object> e2 = new HashMap<>();
        e2.put("id", 2L);
        e2.put("equipmentCode", "EQ002");
        e2.put("equipmentName", "车床1");
        e2.put("equipmentType", "LATHE");
        e2.put("status", "RUNNING");
        e2.put("oee", 0.92);
        list.add(e2);
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 获取设备详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Map<String, Object> equipment = new HashMap<>();
        equipment.put("id", id);
        equipment.put("equipmentCode", "EQ001");
        equipment.put("equipmentName", "加工中心1");
        equipment.put("equipmentType", "CNC");
        equipment.put("status", "RUNNING");
        equipment.put("oee", 0.85);
        
        return Map.of("code", 200, "data", equipment);
    }
    
    /**
     * 报修
     */
    @PostMapping("/{id}/repair")
    public Map<String, Object> repair(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        log.info("设备报修: {}", id);
        return Map.of("code", 200, "message", "报修成功");
    }
    
    /**
     * 保养计划
     */
    @GetMapping("/{id}/maintenance")
    public Map<String, Object> getMaintenance(@PathVariable Long id) {
        return Map.of("code", 200, "data", new ArrayList<>());
    }
    
    /**
     * OEE报表
     */
    @GetMapping("/oee-report")
    public Map<String, Object> oeeReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("averageOEE", 0.88);
        report.put("availability", 0.95);
        report.put("performance", 0.92);
        report.put("quality", 0.98);
        
        return Map.of("code", 200, "data", report);
    }
}
