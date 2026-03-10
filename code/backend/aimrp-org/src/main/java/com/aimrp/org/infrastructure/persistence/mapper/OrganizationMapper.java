package com.aimrp.org.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.org.domain.entity.Organization;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 组织 Mapper
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {
    
    @Select("<script>" +
            "SELECT * FROM t_organization " +
            "<where>" +
            "  <if test='parentId != null'> AND parent_id = #{parentId} </if>" +
            "  <if test='orgType != null'> AND org_type = #{orgType} </if>" +
            "  <if test='status != null'> AND status = #{status} </if>" +
            "</where>" +
            " ORDER BY level, sort_order" +
            "</script>")
    List<Organization> selectList(@Param("parentId") Long parentId, 
                                @Param("orgType") String orgType, 
                                @Param("status") String status);
    
    @Select("SELECT * FROM t_organization WHERE parent_id IS NULL ORDER BY sort_order")
    List<Organization> selectRoot();
    
    @Select("SELECT * FROM t_organization WHERE org_code = #{code}")
    Organization selectByCode(@Param("code") String code);
}
