package com.aimrp.mrp.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.mrp.domain.entity.MrpRun;
import org.apache.ibatis.annotations.*;

/**
 * MRP 运行记录 Mapper
 */
@Mapper
public interface MrpRunMapper extends BaseMapper<MrpRun> {
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM t_mrp_run WHERE id = #{id}")
    MrpRun selectById(@Param("id") Long id);
    
    /**
     * 根据运行编号查询
     */
    @Select("SELECT * FROM t_mrp_run WHERE run_no = #{runNo}")
    MrpRun selectByRunNo(@Param("runNo") String runNo);
    
    /**
     * 查询最近一次运行记录
     */
    @Select("SELECT * FROM t_mrp_run ORDER BY created_at DESC LIMIT 1")
    MrpRun selectLastRun();
    
    /**
     * 分页查询
     */
    @Select("SELECT * FROM t_mrp_run ORDER BY created_at DESC LIMIT #{pageSize} OFFSET #{offset}")
    java.util.List<MrpRun> selectPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);
    
    /**
     * 统计总数
     */
    @Select("SELECT COUNT(*) FROM t_mrp_run")
    Long countAll();
}
