package com.aimrp.quality.infrastructure.persistence.mapper;

import com.aimrp.quality.domain.entity.QualityDefect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QualityDefectMapper {
    
    void insert(QualityDefect defect);
    
    void updateById(QualityDefect defect);
    
    QualityDefect selectById(Long id);
    
    List<QualityDefect> selectList(@Param("status") String status, @Param("handlingMethod") String handlingMethod);
}
