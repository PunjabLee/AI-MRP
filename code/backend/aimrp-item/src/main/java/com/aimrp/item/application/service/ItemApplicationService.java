package com.aimrp.item.application.service;

import com.aimrp.item.domain.service.ItemDomainService;
import com.aimrp.item.infrastructure.persistence.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 物料应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemApplicationService {
    
    private final ItemDomainService domainService;
    private final ItemMapper itemMapper;
    
    /**
     * 创建物料
     */
    @Transactional
    public Long createItem(CreateItemRequest request) {
        log.info("创建物料 - itemCode: {}", request.getItemCode());
        
        // 校验编码
        if (!domainService.validateItemCode(request.getItemCode())) {
            throw new IllegalArgumentException("物料编码格式不正确");
        }
        
        // 保存
        return itemMapper.insert(request.getItemCode(), request.getItemName(), 
                request.getSource(), request.getLeadTime());
    }
    
    /**
     * 查询物料列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String itemType) {
        return itemMapper.selectList(itemType);
    }
    
    @lombok.Data
    public static class CreateItemRequest {
        private String itemCode;
        private String itemName;
        private String source;
        private Integer leadTime;
    }
}
