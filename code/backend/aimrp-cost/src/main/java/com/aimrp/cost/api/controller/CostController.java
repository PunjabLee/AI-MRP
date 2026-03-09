package com.aimrp.cost.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 成本管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
public class CostController {
    
    /**
     * 获取成本要素列表
     */
    @GetMapping("/elements")
    public Map<String, Object> listElements(
            @RequestParam(required = false) String costType) {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> e1 = new HashMap<>();
        e1.put("id", 1L);
        e1.put("elementCode", "MATERIAL");
        e1.put("elementName", "材料成本");
        e1.put("amount", 500000.00);
        e1.put("ratio", 0.65);
        list.add(e1);
        
        Map<String, Object> e2 = new HashMap<>();
        e2.put("id", 2L);
        e2.put("elementCode", "LABOR");
        e2.put("elementName", "人工成本");
        e2.put("amount", 150000.00);
        e2.put("ratio", 0.20);
        list.add(e2);
        
        Map<String, Object> e3 = new HashMap<>();
        e3.put("id", 3L);
        e3.put("elementCode", "OVERHEAD");
        e3.put("elementName", "制造费用");
        e3.put("amount", 120000.00);
        e3.put("ratio", 0.15);
        list.add(e3);
        
        return Map.of("code", 200, "data", list);
    }
    
    /**
     * 产品成本明细
     */
    @GetMapping("/product-cost")
    public Map<String, Object> getProductCost(@RequestParam String itemCode) {
        Map<String, Object> cost = new HashMap<>();
        cost.put("itemCode", itemCode);
        cost.put("materialCost", 80.00);
        cost.put("laborCost", 25.00);
        cost.put("overheadCost", 15.00);
        cost.put("totalCost", 120.00);
        
        return Map.of("code", 200, "data", cost);
    }
    
    /**
     * 成本分析
     */
    @GetMapping("/analysis")
    public Map<String, Object> analyze(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalCost", 770000.00);
        analysis.put("materialCost", 500000.00);
        analysis.put("laborCost", 150000.00);
        analysis.put("overheadCost", 120000.00);
        analysis.put("variance", -5000.00);
        analysis.put("varianceRate", -0.65);
        
        return Map.of("code", 200, "data", analysis);
    }
    
    /**
     * 订单成本
     */
    @GetMapping("/order-cost")
    public Map<String, Object> getOrderCost(@RequestParam Long orderId) {
        Map<String, Object> cost = new HashMap<>();
        cost.put("orderId", orderId);
        cost.put("orderNo", "SO20240309001");
        cost.put("productQty", 1000);
        cost.put("totalCost", 120000.00);
        cost.put("unitCost", 120.00);
        
        return Map.of("code", 200, "data", cost);
    }
}
