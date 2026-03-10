package com.aimrp.production.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aimrp.production.domain.entity.ProductionReport;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 生产报工 Mapper
 */
@Mapper
public interface ProductionReportMapper extends BaseMapper<ProductionReport> {
    
    /**
     * 根据工单查询报工记录
     */
    @Select("SELECT * FROM t_production_report WHERE mo_id = #{moId} ORDER BY report_date DESC")
    List<ProductionReport> selectByMoId(@Param("moId") Long moId);
    
    /**
     * 查询待审核记录
     */
    @Select("SELECT * FROM t_production_report WHERE status = 'PENDING' ORDER BY report_date DESC")
    List<ProductionReport> selectPending();
    
    /**
     * 统计工单完成数量
     */
    @Select("SELECT COALESCE(SUM(report_qty), 0) FROM t_production_report WHERE mo_id = #{moId} AND status = 'APPROVED'")
    java.math.BigDecimal sumReportedQty(@Param("moId") Long moId);
    
    /**
     * 更新状态
     */
    @Update("UPDATE t_production_report SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
