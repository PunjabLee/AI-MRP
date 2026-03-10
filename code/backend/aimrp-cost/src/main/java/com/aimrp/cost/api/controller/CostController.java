package com.aimrp.cost.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.cost.domain.entity.CostElement;
import com.aimrp.cost.infrastructure.persistence.mapper.CostElementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成本管理 Controller
 */
@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
public class CostController {
    
    private final CostElementMapper costElementMapper;
    
    /**
     * 查询成本要素列表
     */
    @GetMapping("/elements")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String costType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        List<CostElement> list = costElementMapper.selectList(costType, keyword);
        
        // 分页
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<CostElement> pageList = fromIndex < total ?
                list.subList(fromIndex, toIndex) : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 查询成本要素详情
     */
    @GetMapping("/elements/{id}")
    public ApiResponse<CostElement> getById(@PathVariable Long id) {
        CostElement element = costElementMapper.selectById(id);
        return ApiResponse.ok(element);
    }
    
    /**
     * 创建成本要素
     */
    @PostMapping("/elements")
    public ApiResponse<CostElement> create(@RequestBody CostElement element) {
        costElementMapper.insert(element);
        return ApiResponse.ok(element);
    }
    
    /**
     * 更新成本要素
     */
    @PutMapping("/elements/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CostElement element) {
        element.setId(id);
        costElementMapper.updateById(element);
        return ApiResponse.ok();
    }
    
    /**
     * 删除成本要素
     */
    @DeleteMapping("/elements/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        costElementMapper.deleteById(id);
        return ApiResponse.ok();
    }
    
    /**
     * 计算产品成本
     */
    @PostMapping("/calculate")
    public ApiResponse<Map<String, Object>> calculate(@RequestBody Map<String, Object> params) {
        String itemCode = (String) params.get("itemCode");
        
        // TODO: 根据BOM和成本要素计算产品成本
        Map<String, Object> result = new HashMap<>();
        result.put("itemCode", itemCode);
        result.put("materialCost", 0);
        result.put("laborCost", 0);
        result.put("overheadCost", 0);
        result.put("totalCost", 0);
        
        return ApiResponse.ok(result);
    }
}
