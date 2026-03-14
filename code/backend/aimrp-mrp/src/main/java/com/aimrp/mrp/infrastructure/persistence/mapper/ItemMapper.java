package com.aimrp.mrp.infrastructure.persistence.mapper;

import com.aimrp.mrp.domain.valueobject.MrpContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 物料主数据 Mapper
 */
@Mapper
public interface ItemMapper {
    
    /**
     * 查询所有物料
     */
    List<Map<String, Object>> selectAll();
    
    /**
     * 根据编码查询物料
     */
    Map<String, Object> selectByCode(@Param("itemCode") String itemCode);
    
    /**
     * 批量查询物料
     */
    List<Map<String, Object>> selectByCodes(@Param("itemCodes") List<String> itemCodes);
}
