package com.aimrp.purchase.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 采购 Mapper
 */
@Mapper
public interface PurchaseMapper {
    
    Long insertPurchaseOrder(@Param("supplierCode") String supplierCode,
                           @Param("itemCode") String itemCode,
                           @Param("qty") BigDecimal qty,
                           @Param("unitPrice") BigDecimal unitPrice,
                           @Param("totalAmount") BigDecimal totalAmount);
    
    void updateReceivedQty(@Param("orderId") Long orderId, @Param("qty") BigDecimal qty);
    
    List<Map<String, Object>> selectList(@Param("supplierCode") String supplierCode,
                                        @Param("status") String status);
    
    Map<String, Object> selectById(@Param("id") Long id);
}
