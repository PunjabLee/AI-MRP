package com.aimrp.inventory.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存 Mapper
 */
@Mapper
public interface InventoryMapper {
    
    Map<String, Object> selectByItemAndWarehouse(@Param("itemCode") String itemCode, 
                                               @Param("warehouseCode") String warehouseCode);
    
    List<Map<String, Object>> selectList(@Param("itemCode") String itemCode,
                                        @Param("warehouseCode") String warehouseCode);
    
    void increaseQty(@Param("id") Long id, @Param("qty") BigDecimal qty);
    
    void decreaseQty(@Param("id") Long id, @Param("qty") BigDecimal qty);
    
    void insert(@Param("itemCode") String itemCode,
                @Param("warehouseCode") String warehouseCode,
                @Param("qty") BigDecimal qty);
}
