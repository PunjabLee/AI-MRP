package com.aimrp.inventory.application.service;

import com.aimrp.inventory.domain.entity.InventoryTransfer;
import com.aimrp.inventory.infrastructure.persistence.mapper.InventoryTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存调拨服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransferService {
    
    private final InventoryTransferMapper transferMapper;
    
    /**
     * 创建调拨单
     */
    @Transactional
    public InventoryTransfer createTransfer(InventoryTransfer transfer) {
        log.info("创建库存调拨单 - {}", transfer);
        
        // 生成调拨单号
        String transferNo = "TR" + System.currentTimeMillis();
        transfer.setTransferNo(transferNo);
        transfer.setTransferType("TRANSFER");
        transfer.setStatus("DRAFT");
        
        if (transfer.getTransferDate() == null) {
            transfer.setTransferDate(LocalDate.now());
        }
        
        transferMapper.insert(transfer);
        
        return transfer;
    }
    
    /**
     * 审核调拨单
     */
    @Transactional
    public boolean approveTransfer(Long id, String approver, String remark) {
        log.info("审核调拨单 - id: {}, approver: {}", id, approver);
        
        InventoryTransfer transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw new RuntimeException("调拨单不存在");
        }
        
        if (!"DRAFT".equals(transfer.getStatus()) && !"PENDING".equals(transfer.getStatus())) {
            throw new RuntimeException("只有草稿或待审核状态可以审核");
        }
        
        transfer.setStatus("APPROVED");
        transfer.setApprover(approver);
        transfer.setApproveTime(LocalDateTime.now());
        transfer.setApproveRemark(remark);
        
        transferMapper.updateById(transfer);
        
        return true;
    }
    
    /**
     * 执行调拨
     */
    @Transactional
    public boolean executeTransfer(Long id) {
        log.info("执行库存调拨 - id: {}", id);
        
        InventoryTransfer transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw new RuntimeException("调拨单不存在");
        }
        
        if (!"APPROVED".equals(transfer.getStatus())) {
            throw new RuntimeException("只有已审核状态可以执行");
        }
        
        // 检查调出仓库库存是否充足
        // TODO: 实际检查库存
        
        // 1. 减少调出仓库库存
        int deducted = transferMapper.deductInventory(
            transfer.getItemCode(), 
            transfer.getFromWarehouseCode(),
            transfer.getTransferQty()
        );
        
        if (deducted == 0) {
            throw new RuntimeException("调出仓库库存不足或物料不存在");
        }
        
        // 2. 增加调入仓库库存
        int added = transferMapper.addInventory(
            transfer.getItemCode(),
            transfer.getToWarehouseCode(),
            transfer.getTransferQty()
        );
        
        if (added == 0) {
            // 如果调入仓库没有该物料，需要创建库存记录
            // TODO: 创建库存记录
        }
        
        // 更新调拨单状态
        transfer.setStatus("COMPLETED");
        transfer.setActualDate(LocalDate.now());
        transferMapper.updateById(transfer);
        
        log.info("调拨完成 - transferNo: {}", transfer.getTransferNo());
        
        return true;
    }
    
    /**
     * 取消调拨单
     */
    @Transactional
    public boolean cancelTransfer(Long id, String reason) {
        log.info("取消调拨单 - id: {}", id);
        
        InventoryTransfer transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw new RuntimeException("调拨单不存在");
        }
        
        if ("COMPLETED".equals(transfer.getStatus()) || "CANCELLED".equals(transfer.getStatus())) {
            throw new RuntimeException("已完成或已取消的调拨单不能取消");
        }
        
        transfer.setStatus("CANCELLED");
        transfer.setRemark(transfer.getRemark() + " | 取消原因: " + reason);
        
        transferMapper.updateById(transfer);
        
        return true;
    }
    
    /**
     * 查询调拨单列表
     */
    public List<InventoryTransfer> listTransfers(String status, String fromWarehouse, 
                                                   String toWarehouse, String itemCode) {
        return transferMapper.selectList(status, fromWarehouse, toWarehouse, itemCode);
    }
    
    /**
     * 查询调拨单详情
     */
    public InventoryTransfer getTransfer(Long id) {
        return transferMapper.selectById(id);
    }
}
