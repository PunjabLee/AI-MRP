package com.aimrp.inventory.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.inventory.domain.entity.InventoryCheck;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 库存盘点 Mapper
 */
@Mapper
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck> {
    
    @Select("SELECT * FROM t_inventory_check ORDER BY created_at DESC")
    List<InventoryCheck> selectAll();
    
    @Select("SELECT * FROM t_inventory_check WHERE status = #{status} ORDER BY created_at DESC")
    List<InventoryCheck> selectByStatus(@Param("status") String status);
    
    @Update("UPDATE t_inventory_check SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}

/**
 * 库存盘点明细 Mapper
 */
@Mapper
public interface InventoryCheckLineMapper extends BaseMapper<Object> {
    
    @Insert("INSERT INTO t_inventory_check_line (check_id, item_code, item_name, location_code, " +
            "book_qty, check_qty, diff_qty, diff_type, status) " +
            "VALUES (#{checkId}, #{itemCode}, #{itemName}, #{locationCode}, " +
            "#{bookQty}, #{checkQty}, #{diffQty}, #{diffType}, 'PENDING')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLine(@Param("checkId") Long checkId,
                   @Param("itemCode") String itemCode,
                   @Param("itemName") String itemName,
                   @Param("locationCode") String locationCode,
                   @Param("bookQty") java.math.BigDecimal bookQty,
                   @Param("checkQty") java.math.BigDecimal checkQty,
                   @Param("diffQty") java.math.BigDecimal diffQty,
                   @Param("diffType") String diffType);
    
    @Select("SELECT * FROM t_inventory_check_line WHERE check_id = #{checkId}")
    List<Object> selectByCheckId(@Param("checkId") Long checkId);
}
