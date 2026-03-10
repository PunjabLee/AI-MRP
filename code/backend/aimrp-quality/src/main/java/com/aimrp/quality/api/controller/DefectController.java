package com.aimrp.quality.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.quality.api.dto.DefectCreateRequest;
import com.aimrp.quality.domain.entity.QualityDefect;
import com.aimrp.quality.infrastructure.persistence.mapper.QualityDefectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 不良品管理 Controller
 */
@RestController
@RequestMapping("/api/quality/defect")
@RequiredArgsConstructor
public class DefectController {
    
    private final QualityDefectMapper defectMapper;
    
    /**
     * 登记不良品
     */
    @PostMapping
    public ApiResponse<QualityDefect> create(@Validated @RequestBody DefectCreateRequest request) {
        QualityDefect defect = new QualityDefect();
        defect.setDefectNo("DF" + System.currentTimeMillis());
        defect.setInspectionId(request.getInspectionId());
        defect.setInspectionNo(request.getInspectionNo());
        defect.setItemCode(request.getItemCode());
        defect.setItemName(request.getItemName());
        defect.setDefectQty(request.getDefectQty());
        defect.setDefectType(request.getDefectType());
        defect.setReason(request.getReason());
        defect.setStatus("PENDING");
        
        defectMapper.insert(defect);
        
        return ApiResponse.ok(defect);
    }
    
    /**
     * 处理不良品
     */
    @PostMapping("/{id}/handle")
    public ApiResponse<Void> handle(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        QualityDefect defect = defectMapper.selectById(id);
        if (defect == null) {
            return ApiResponse.fail("不良品记录不存在");
        }
        
        if (params.containsKey("handlingMethod")) {
            defect.setHandlingMethod(params.get("handlingMethod").toString());
        }
        if (params.containsKey("handlingQty")) {
            defect.setHandlingQty(new BigDecimal(params.get("handlingQty").toString()));
        }
        if (params.containsKey("handler")) {
            defect.setHandler(params.get("handler").toString());
        }
        if (params.containsKey("remark")) {
            defect.setRemark(params.get("remark").toString());
        }
        
        defect.setHandlingDate(LocalDate.now());
        defect.setStatus("COMPLETED");
        
        defectMapper.updateById(defect);
        
        return ApiResponse.ok();
    }
    
    /**
     * 获取不良品列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String handlingMethod) {
        
        List<QualityDefect> list = defectMapper.selectList(status, handlingMethod);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取不良品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<QualityDefect> getById(@PathVariable Long id) {
        return ApiResponse.ok(defectMapper.selectById(id));
    }
    
    /**
     * 统计不良品
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        List<QualityDefect> all = defectMapper.selectList(null, null);
        
        BigDecimal totalDefect = all.stream()
            .map(QualityDefect::getDefectQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalHandled = all.stream()
            .filter(d -> "COMPLETED".equals(d.getStatus()))
            .map(QualityDefect::getHandlingQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDefect", totalDefect);
        stats.put("totalHandled", totalHandled);
        stats.put("pendingCount", all.stream().filter(d -> "PENDING".equals(d.getStatus())).count());
        stats.put("completedCount", all.stream().filter(d -> "COMPLETED".equals(d.getStatus())).count());
        
        return ApiResponse.ok(stats);
    }
}
