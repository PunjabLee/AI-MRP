package com.aimrp.inventory.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.inventory.api.dto.TransferApplyRequest;
import com.aimrp.inventory.api.dto.TransferExecuteRequest;
import com.aimrp.inventory.domain.entity.InventoryTransfer;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryTransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存调拨 Controller
 */
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {
    
    private final InventoryTransferMapper transferMapper;
    
    /**
     * 申请调拨
     */
    @PostMapping("/apply")
    public ApiResponse<InventoryTransfer> apply(@Validated @RequestBody TransferApplyRequest request) {
        InventoryTransfer transfer = new InventoryTransfer();
        transfer.setTransferNo("TR" + System.currentTimeMillis());
        transfer.setTransferType("TRANSFER");
        transfer.setItemCode(request.getItemCode());
        transfer.setItemName(request.getItemName());
        transfer.setFromWarehouseCode(request.getFromWarehouse());
        transfer.setToWarehouseCode(request.getToWarehouse());
        transfer.setTransferQty(request.getTransferQty());
        transfer.setApplyUser(request.getApplyUser());
        transfer.setRemark(request.getRemark());
        transfer.setStatus("PENDING");
        
        transferMapper.insert(transfer);
        
        return ApiResponse.ok(transfer);
    }
    
    /**
     * 审批调拨
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestParam String action) {
        InventoryTransfer transfer = transferMapper.selectById(id);
        if (transfer == null) {
            return ApiResponse.fail("调拨单不存在");
        }
        
        if ("approve".equals(action)) {
            transfer.setStatus("APPROVED");
        } else if ("reject".equals(action)) {
            transfer.setStatus("REJECTED");
        }
        
        transferMapper.updateById(transfer);
        
        return ApiResponse.ok();
    }
    
    /**
     * 执行调拨
     */
    @PostMapping("/{id}/execute")
    public ApiResponse<Void> execute(@PathVariable Long id, @Validated @RequestBody TransferExecuteRequest request) {
        InventoryTransfer transfer = transferMapper.selectById(id);
        if (transfer == null) {
            return ApiResponse.fail("调拨单不存在");
        }
        
        if (!"APPROVED".equals(transfer.getStatus())) {
            return ApiResponse.fail("调拨单未审批通过");
        }
        
        // TODO: 执行库存扣减和增加
        transfer.setStatus("COMPLETED");
        transfer.setActualTransferQty(request.getActualQty());
        transfer.setExecuteUser(request.getExecuteUser());
        
        transferMapper.updateById(transfer);
        
        return ApiResponse.ok();
    }
    
    /**
     * 获取调拨列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromWarehouse) {
        
        List<InventoryTransfer> list = transferMapper.selectList(status, fromWarehouse);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        
        return ApiResponse.ok(result);
    }
    
    /**
     * 获取调拨详情
     */
    @GetMapping("/{id}")
    public ApiResponse<InventoryTransfer> getById(@PathVariable Long id) {
        return ApiResponse.ok(transferMapper.selectById(id));
    }
    
    /**
     * 取消调拨
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        InventoryTransfer transfer = transferMapper.selectById(id);
        if (transfer == null) {
            return ApiResponse.fail("调拨单不存在");
        }
        
        if (!"PENDING".equals(transfer.getStatus())) {
            return ApiResponse.fail("只有待审批状态可以取消");
        }
        
        transfer.setStatus("CANCELLED");
        transferMapper.updateById(transfer);
        
        return ApiResponse.ok();
    }
}
