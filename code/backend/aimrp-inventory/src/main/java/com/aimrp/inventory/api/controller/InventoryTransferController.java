package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.inventory.application.service.InventoryTransferService;
import com.aimrp.inventory.domain.entity.InventoryTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存调拨 Controller
 */
@RestController
@RequestMapping("/api/inventory/transfer")
@RequiredArgsConstructor
public class InventoryTransferController {
    
    private final InventoryTransferService transferService;
    
    /**
     * 创建调拨单
     */
    @PostMapping
    public ApiResponse<InventoryTransfer> create(@RequestBody InventoryTransfer transfer) {
        InventoryTransfer result = transferService.createTransfer(transfer);
        return ApiResponse.ok(result);
    }
    
    /**
     * 审核调拨单
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, 
                                      @RequestBody Map<String, String> params) {
        String approver = params.get("approver");
        String remark = params.get("remark");
        
        boolean success = transferService.approveTransfer(id, approver, remark);
        
        if (success) {
            return ApiResponse.ok();
        } else {
            return ApiResponse.fail("审核失败");
        }
    }
    
    /**
     * 执行调拨
     */
    @PostMapping("/{id}/execute")
    public ApiResponse<Void> execute(@PathVariable Long id) {
        try {
            boolean success = transferService.executeTransfer(id);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
    
    /**
     * 取消调拨单
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        
        try {
            boolean success = transferService.cancelTransfer(id, reason);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
    
    /**
     * 查询调拨单列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromWarehouse,
            @RequestParam(required = false) String toWarehouse,
            @RequestParam(required = false) String itemCode,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        List<InventoryTransfer> list = transferService.listTransfers(
                status, fromWarehouse, toWarehouse, itemCode);
        
        // 分页
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<InventoryTransfer> pageList = fromIndex < total 
                ? list.subList(fromIndex, toIndex) 
                : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 查询调拨单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<InventoryTransfer> getById(@PathVariable Long id) {
        InventoryTransfer transfer = transferService.getTransfer(id);
        
        if (transfer == null) {
            return ApiResponse.fail("调拨单不存在");
        }
        
        return ApiResponse.ok(transfer);
    }
}
