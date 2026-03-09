package com.aimrp.item.infrastructure.persistence.mapper;

import com.aimrp.item.domain.entity.Item;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料 Mapper
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}
