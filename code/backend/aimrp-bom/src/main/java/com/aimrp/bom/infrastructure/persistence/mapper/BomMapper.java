package com.aimrp.bom.infrastructure.persistence.mapper;

import com.aimrp.bom.domain.entity.Bom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * BOM Mapper
 */
@Mapper
public interface BomMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<Bom> {
    
    /**
     * 根据物料编码查询 BOM
     */
    Bom selectByItemCode(@Param("itemCode") String itemCode);
    
    /**
     * 根据物料编码查询 BOM 行
     */
    List<Map<String, Object>> selectLinesByItemCode(@Param("itemCode") String itemCode);
    
    /**
     * 查询 BOM 映射（用于 MRP）
     */
    Map<String, List<Map<String, Object>>> selectBomMap(@Param("itemCode") String itemCode);
    
    /**
     * BOM展开查询
     */
    List<Map<String, Object>> selectExpand(@Param("itemCode") String itemCode, @Param("level") int level);
}
