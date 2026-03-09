package com.aimrp.mps.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MPS 主生产计划接口
 */
@Slf4j
@RestController
@RequestMapping("/api/mps")
@RequiredArgsConstructor
public class MpsController {
    
    /**
     * 获取MPS计划列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<Map<String, Object>> list = new ArrayList<>();
        
        // 模拟数据
        Map<String, Object> mps1 = new HashMap<>();
        mps1.put("id", 1L);
        mps1.put("planNo", "MPS20240301");
        mps1.put("planType", "MONTHLY");
        mps1.put("status", "APPROVED");
        mps1.put("startDate", "2024-03-01");
        mps1.put("endDate", "2024-03-31");
        list.add(mps1);
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 获取MPS详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Map<String, Object> mps = new HashMap<>();
        mps.put("id", id);
        mps.put("planNo", "MPS20240301");
        mps.put("planType", "MONTHLY");
        mps.put("status", "APPROVED");
        
        return Map.of("code", 200, "data", mps);
    }
    
    /**
     * 创建MPS计划
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        log.info("创建MPS计划: {}", data);
        data.put("id", System.currentTimeMillis());
        
        return Map.of("code", 200, "data", data, "message", "创建成功");
    }
    
    /**
     * 运行MPS计算
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody Map<String, Object> params) {
        log.info("运行MPS计算: {}", params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("plannedOrders", 10);
        result.put("capacityCheck", "PASSED");
        result.put("suggestions", new ArrayList<>());
        
        return Map.of("code", 200, "data", result, "message", "计算完成");
    }
    
    /**
     * 确认MPS计划
     */
    @PostMapping("/{id}/confirm")
    public Map<String, Object> confirm(@PathVariable Long id) {
        log.info("确认MPS计划: {}", id);
        
        return Map.of("code", 200, "message", "确认成功");
    }
    
    /**
     * 粗产能检查
     */
    @PostMapping("/{id}/capacity-check")
    public Map<String, Object> capacityCheck(@PathVariable Long id) {
        log.info("粗产能检查: {}", id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("workCenter", "WC01");
        result.put("requiredCapacity", 1000);
        result.put("availableCapacity", 1200);
        result.put("utilization", "83%");
        result.put("status", "OK");
        
        return Map.of("code", 200, "data", result);
    }
}
