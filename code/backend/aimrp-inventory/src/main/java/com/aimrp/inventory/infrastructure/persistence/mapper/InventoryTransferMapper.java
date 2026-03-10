package com.aimrp.inventory.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.inventory.domain.entity.InventoryTransfer;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 库存调拨 Mapper
 */
@Mapper
public interface InventoryTransferMapper extends BaseMapper<InventoryTransfer> {
    
    /**
     * 根据单号查询
     */
    @Select("SELECT * FROM t_inventory_transfer WHERE transfer_no = #{transferNo}")
    InventoryTransfer selectByTransferNo(@Param("transferNo") String transferNo);
    
    /**
     * 查询调拨单列表
     */
    @Select("<script>" +
            "SELECT * FROM t_inventory_transfer " +
            "<where>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "  <if test='fromWarehouseCode != null'> AND from_warehouse_code = #{fromWarehouseCode} </if>" +
            "  <if test='toWarehouseCode != null'> AND to_warehouse_code = #{toWarehouseCode} </if>" +
            "  <if test='itemCode != null'> AND item_code LIKE CONCAT('%', #{itemCode}, '%') </if>" +
            "</where>" +
            " ORDER BY created_at DESC" +
            "</script>")
    List<InventoryTransfer> selectList(@Param("status") String status,
                                       @Param("fromWarehouseCode") String fromWarehouseCode,
                                       @Param("toWarehouseCode") String toWarehouseCode,
                                       @Param("itemCode") String itemCode);
    
    /**
     * 更新状态
     */
    @Update("UPDATE t_inventory_transfer SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 执行调拨（减少调出库存，增加调入库存）
     */
    @Update("UPDATE t_inventory SET on_hand_qty = on_hand_qty - #{qty} " +
            "WHERE item_code = #{itemCode} AND warehouse_code = #{warehouseCode}")
    int deductInventory(@Param("itemCode") String itemCode, 
                        @Param("warehouseCode") String warehouseCode,
                        @Param("qty") java.math.BigDecimal qty);
    
    /**
     * 增加库存
     */
    @Update("UPDATE t_inventory SET on_hand_qty = on_hand_qty + #{qty} " +
            "WHERE item_code = #{itemCode} AND warehouse_code = #{warehouseCode}")
    int addInventory(@Param("itemCode") String itemCode, 
                      @Param("warehouseCode") String warehouseCode,
                      @Param("qty") java.math.BigDecimal qty);
}
