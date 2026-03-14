package com.aimrp.quality.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.quality.api.dto.InspectionCreateRequest;
import com.aimrp.quality.domain.entity.QualityInspection;
import com.aimrp.quality.infrastructure.persistence.mapper.QualityInspectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 质量管理 Controller
 */
@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {
    
    private final QualityInspectionMapper inspectionMapper;
    
    /**
     * 获取检验单列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String inspectionType,
            @RequestParam(required = false) String status) {
        
        var list = inspectionMapper.selectList(inspectionType, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取检验单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<QualityInspection> getById(@PathVariable Long id) {
        return ApiResponse.ok(inspectionMapper.selectById(id));
    }
    
    /**
     * 创建检验单
     */
    @PostMapping
    public ApiResponse<QualityInspection> create(@Validated @RequestBody InspectionCreateRequest request) {
        QualityInspection inspection = new QualityInspection();
        inspection.setInspectionNo("QI" + System.currentTimeMillis());
        inspection.setInspectionType(request.getInspectionType());
        inspection.setSourceNo(request.getSourceNo());
        inspection.setItemCode(request.getItemCode());
        inspection.setItemName(request.getItemName());
        inspection.setInspectionQty(request.getInspectionQty());
        inspection.setInspector(request.getInspector());
        inspection.setRemark(request.getRemark());
        inspection.setStatus("PENDING");
        
        inspectionMapper.insert(inspection);
        
        return ApiResponse.ok(inspection);
    }
    
    /**
     * 提交检验结果
     */
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody QualityInspection inspection) {
        QualityInspection existing = inspectionMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("检验单不存在");
        }
        
        // 计算合格率
        if (inspection.getInspectionQty() != null && inspection.getInspectionQty().compareTo(BigDecimal.ZERO) > 0
            && inspection.getQualifiedQty() != null) {
            BigDecimal rate = inspection.getQualifiedQty()
                    .divide(inspection.getInspectionQty(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            existing.setQualifiedRate(rate);
            
            // 判断结果
            if (rate.compareTo(new BigDecimal("95")) >= 0) {
                existing.setResult("QUALIFIED");
            } else {
                existing.setResult("UNQUALIFIED");
            }
        }
        
        existing.setQualifiedQty(inspection.getQualifiedQty());
        existing.setDefectiveQty(inspection.getDefectiveQty());
        existing.setScrappedQty(inspection.getScrappedQty());
        existing.setRemark(inspection.getRemark());
        existing.setStatus("COMPLETED");
        
        inspectionMapper.updateById(existing);
        
        return ApiResponse.ok();
    }
    
    /**
     * 质量统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInspections", 100);
        stats.put("qualifiedCount", 95);
        stats.put("unqualifiedCount", 5);
        stats.put("qualifiedRate", 95.0);
        
        return ApiResponse.ok(stats);
    }
}
