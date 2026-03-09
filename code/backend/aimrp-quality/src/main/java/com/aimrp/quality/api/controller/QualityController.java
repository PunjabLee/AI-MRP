package com.aimrp.quality.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 质量管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {
    
    /**
     * 获取检验单列表
     */
    @GetMapping("/inspections")
    public Map<String, Object> listInspections(
            @RequestParam(required = false) String inspectionType,
            @RequestParam(required = false) String status) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> i1 = new HashMap<>();
        i1.put("id", 1L);
        i1.put("inspectionNo", "IQC20240309001");
        i1.put("inspectionType", "IQC");
        i1.put("supplierCode", "SUP001");
        i1.put("itemCode", "MAT001");
        i1.put("qty", 1000);
        i1.put("qualifiedQty", 980);
        i1.put("status", "APPROVED");
        list.add(i1);
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 创建检验单
     */
    @PostMapping("/inspections")
    public Map<String, Object> createInspection(@RequestBody Map<String, Object> data) {
        log.info("创建检验单: {}", data);
        data.put("id", System.currentTimeMillis());
        return Map.of("code", 200, "data", data, "message", "创建成功");
    }
    
    /**
     * 检验确认
     */
    @PostMapping("/inspections/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        log.info("检验确认: {}", id);
        return Map.of("code", 200, "message", "确认成功");
    }
    
    /**
     * 质量统计
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInspections", 100);
        stats.put("qualifiedRate", 0.98);
        stats.put("iqcQualifiedRate", 0.99);
        stats.put("ipqcQualifiedRate", 0.97);
        stats.put("oqcQualifiedRate", 0.99);
        
        return Map.of("code", 200, "data", stats);
    }
    
    /**
     * 不良分析
     */
    @GetMapping("/defect-analysis")
    public Map<String, Object> getDefectAnalysis() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> d1 = new HashMap<>();
        d1.put("defectType", "尺寸超差");
        d1.put("count", 15);
        d1.put("rate", 0.15);
        list.add(d1);
        
        Map<String, Object> d2 = new HashMap<>();
        d2.put("defectType", "表面缺陷");
        d2.put("count", 8);
        d2.put("rate", 0.08);
        list.add(d2);
        
        return Map.of("code", 200, "data", list);
    }
}
