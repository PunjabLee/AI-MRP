package com.aimrp.risk.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 风险数据 Mapper
 */
@Mapper
public interface RiskDataMapper {
    
    /**
     * 查询库存风险数据
     */
    List<Map<String, Object>> selectInventoryRisks(
            @Param("warehouseCode") String warehouseCode);
    
    /**
     * 查询供应商风险数据
     */
    List<Map<String, Object>> selectSupplierRisks(
            @Param("supplierCode") String supplierCode);
    
    /**
     * 查询需求风险数据
     */
    List<Map<String, Object>> selectDemandRisks(
            @Param("itemCode") String itemCode);
    
    /**
     * 保存风险记录
     */
    void insertRiskRecord(
            @Param("riskType") String riskType,
            @Param("itemCode") String itemCode,
            @Param("riskLevel") Integer riskLevel,
            @Param("description") String description,
            @Param("occurredAt") LocalDateTime occurredAt);
    
    /**
     * 查询历史风险记录
     */
    List<Map<String, Object>> selectRiskHistory(
            @Param("itemCode") String itemCode,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 更新风险处理状态
     */
    void updateRiskStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("handledBy") String handledBy,
            @Param("handledAt") LocalDateTime handledAt);
}
