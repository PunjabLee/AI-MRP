package com.aimrp.cost.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.cost.domain.entity.CostElement;
import com.aimrp.cost.infrastructure.persistence.mapper.CostElementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        BigDecimal quantity = params.get("quantity") != null
            ? new BigDecimal(params.get("quantity").toString()) : BigDecimal.ONE;

        // 根据BOM和成本要素计算产品成本
        Map<String, Object> result = calculateProductCost(itemCode, quantity);

        return ApiResponse.ok(result);
    }

    /**
     * 计算产品成本
     * 根据BOM结构和成本要素计算
     */
    private Map<String, Object> calculateProductCost(String itemCode, BigDecimal quantity) {
        Map<String, Object> result = new HashMap<>();
        result.put("itemCode", itemCode);
        result.put("quantity", quantity);

        // 1. 获取物料成本 - 通过BOM展开计算
        BigDecimal materialCost = calculateMaterialCost(itemCode, quantity);
        result.put("materialCost", materialCost);

        // 2. 获取人工成本
        BigDecimal laborCost = calculateLaborCost(itemCode, quantity);
        result.put("laborCost", laborCost);

        // 3. 获取制造费用
        BigDecimal overheadCost = calculateOverheadCost(itemCode, quantity);
        result.put("overheadCost", overheadCost);

        // 4. 计算总成本
        BigDecimal totalCost = materialCost.add(laborCost).add(overheadCost);
        result.put("totalCost", totalCost);
        result.put("unitCost", totalCost.divide(quantity, 2, java.math.RoundingMode.HALF_UP));

        return result;
    }

    private BigDecimal calculateMaterialCost(String itemCode, BigDecimal quantity) {
        // TODO: 通过BOM展开计算物料成本
        // 1. 展开BOM获取所有子物料
        // 2. 查询每个物料的单价
        // 3. 汇总计算
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateLaborCost(String itemCode, BigDecimal quantity) {
        // TODO: 根据工艺路线计算人工成本
        // 1. 获取物料的工艺路线
        // 2. 获取每个工序的标准工时
        // 3. 获取人工费率
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateOverheadCost(String itemCode, BigDecimal quantity) {
        // TODO: 根据制造费用分摊规则计算
        // 1. 获取制造费用分摊率
        // 2. 根据分摊基数（人工工时/机器工时）计算
        return BigDecimal.ZERO;
    }
}
