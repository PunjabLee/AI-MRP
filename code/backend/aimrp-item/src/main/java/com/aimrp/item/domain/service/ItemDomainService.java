package com.aimrp.item.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 物料领域服务
 */
@Slf4j
@Service
public class ItemDomainService {
    
    /**
     * 校验物料编码
     */
    public boolean validateItemCode(String itemCode) {
        if (itemCode == null || itemCode.isEmpty()) {
            return false;
        }
        // 物料编码格式：字母开头+数字
        return itemCode.matches("^[A-Z][A-Z0-9]{2,10}$");
    }
    
    /**
     * 判断物料类型
     */
    public String getItemType(String source) {
        if ("MAKE".equals(source)) {
            return "自制";
        } else if ("BUY".equals(source)) {
            return "采购";
        } else if ("CONTRACT".equals(source)) {
            return "委外";
        }
        return "未知";
    }
}
