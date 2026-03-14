package com.aimrp.bom.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.bom.domain.entity.BomSubstitute;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * BOM替代料 Mapper
 */
@Mapper
public interface BomSubstituteMapper extends BaseMapper<BomSubstitute> {
    
    /**
     * 查询物料的替代料
     */
    @Select("SELECT * FROM t_bom_substitute WHERE item_code = #{itemCode} AND status = 'ACTIVE' ORDER BY priority")
    List<BomSubstitute> selectByItemCode(@Param("itemCode") String itemCode);
    
    /**
     * 启用/禁用替代料
     */
    @Update("UPDATE t_bom_substitute SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
