package com.aimrp.org.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 组织 Mapper
 */
@Mapper
public interface OrganizationMapper {
    
    List<Map<String, Object>> selectList(@Param("orgType") String orgType, @Param("status") String status);
    
    Map<String, Object> selectById(@Param("id") Long id);
    
    Map<String, Object> selectByCode(@Param("orgCode") String orgCode);
    
    Long insert(Map<String, Object> data);
    
    void update(@Param("id") Long id, Map<String, Object> data);
    
    void delete(@Param("id") Long id);
    
    int countByParentId(@Param("parentId") Long parentId);
}
