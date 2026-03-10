package com.aimrp.inventory.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.inventory.domain.entity.InventoryTransaction;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存交易 Mapper
 */
@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {
    
    /**
     * 插入交易记录
     */
    @Insert("INSERT INTO t_inventory_transaction (trans_no, trans_type, item_code, item_name, " +
            "warehouse_code, location_code, trans_qty, after_qty, batch_no, ref_no, " +
            "trans_date, operator, remark, created_by, created_at) " +
            "VALUES (#{transNo}, #{transType}, #{itemCode}, #{itemName}, " +
            "#{warehouseCode}, #{locationCode}, #{transQty}, #{afterQty}, #{batchNo}, #{refNo}, " +
            "#{transDate}, #{operator}, #{remark}, #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTransaction(InventoryTransaction transaction);
    
    /**
     * 根据物料查询交易记录
     */
    @Select("SELECT * FROM t_inventory_transaction WHERE item_code = #{itemCode} ORDER BY trans_date DESC LIMIT #{limit}")
    List<InventoryTransaction> selectByItemCode(@Param("itemCode") String itemCode, @Param("limit") int limit);
    
    /**
     * 根据时间范围查询
     */
    @Select("SELECT * FROM t_inventory_transaction WHERE trans_date BETWEEN #{startDate} AND #{endDate} ORDER BY trans_date DESC")
    List<InventoryTransaction> selectByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计出入库
     */
    @Select("SELECT trans_type, SUM(trans_qty) as total FROM t_inventory_transaction " +
            "WHERE item_code = #{itemCode} AND trans_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY trans_type")
    List<Map<String, Object>> selectSummary(@Param("itemCode") String itemCode,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}
