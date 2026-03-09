package com.aimrp.conversation.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 意图识别服务
 * 
 * 负责识别用户对话的意图
 */
@Slf4j
@Service
public class IntentRecognitionService {
    
    /**
     * 意图类型枚举
     */
    public enum Intent {
        // 订单相关
        ORDER_QUERY("订单查询", "查询订单信息"),
        ORDER_CREATE("创建订单", "创建新订单"),
        ORDER_UPDATE("更新订单", "更新订单信息"),
        ORDER_CONFIRM("确认订单", "确认订单"),
        ORDER_CANCEL("取消订单", "取消订单"),
        
        // 库存相关
        INVENTORY_QUERY("库存查询", "查询库存信息"),
        INVENTORY_IN("入库", "入库操作"),
        INVENTORY_OUT("出库", "出库操作"),
        
        // BOM相关
        BOM_QUERY("BOM查询", "查询BOM信息"),
        BOM_EXPAND("BOM展开", "展开BOM"),
        
        // MRP相关
        MRP_RUN("MRP执行", "执行MRP计算"),
        MRP_QUERY("MRP结果查询", "查询MRP结果"),
        
        // 通用
        HELP("帮助", "获取帮助信息"),
        UNKNOWN("未知", "无法识别的意图");
        
        private final String label;
        private final String description;
        
        Intent(String label, String description) {
            this.label = label;
            this.description = description;
        }
        
        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }
    
    /**
     * 意图关键词映射
     */
    private static final Map<Intent, List<String>> INTENT_KEYWORDS = new HashMap<>();
    
    static {
        // 订单
        INTENT_KEYWORDS.put(Intent.ORDER_QUERY, Arrays.asList("查询", "看看", "有哪些", "订单", "list", "query"));
        INTENT_KEYWORDS.put(Intent.ORDER_CREATE, Arrays.asList("创建", "新增", "下单", "添加", "create", "add"));
        INTENT_KEYWORDS.put(Intent.ORDER_UPDATE, Arrays.asList("修改", "更新", "编辑", "update"));
        INTENT_KEYWORDS.put(Intent.ORDER_CONFIRM, Arrays.asList("确认", "审核", "approve"));
        INTENT_KEYWORDS.put(Intent.ORDER_CANCEL, Arrays.asList("取消", "作废", "cancel"));
        
        // 库存
        INTENT_KEYWORDS.put(Intent.INVENTORY_QUERY, Arrays.asList("库存", "还有多少", "stock", "inventory"));
        INTENT_KEYWORDS.put(Intent.INVENTORY_IN, Arrays.asList("入库", "in", "收入"));
        INTENT_KEYWORDS.put(Intent.INVENTORY_OUT, Arrays.asList("出库", "out", "领用"));
        
        // BOM
        INTENT_KEYWORDS.put(Intent.BOM_QUERY, Arrays.asList("BOM", "配方", "结构"));
        INTENT_KEYWORDS.put(Intent.BOM_EXPAND, Arrays.asList("展开", "分解", "explode"));
        
        // MRP
        INTENT_KEYWORDS.put(Intent.MRP_RUN, Arrays.asList("运行", "执行", "计算", "run", "execute", "计算MRP"));
        INTENT_KEYWORDS.put(Intent.MRP_QUERY, Arrays.asList("MRP结果", "建议", "proposal"));
        
        // 帮助
        INTENT_KEYWORDS.put(Intent.HELP, Arrays.asList("帮助", "help", "帮帮我", "怎么"));
    }
    
    /**
     * 识别意图
     * 
     * @param userInput 用户输入
     * @return 识别到的意图
     */
    public Intent recognize(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return Intent.UNKNOWN;
        }
        
        String input = userInput.toLowerCase().trim();
        
        // 优先级匹配
        // 1. MRP执行 - 最高优先级
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.MRP_RUN))) {
            return Intent.MRP_RUN;
        }
        
        // 2. BOM展开
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.BOM_EXPAND))) {
            return Intent.BOM_EXPAND;
        }
        
        // 3. 库存查询
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.INVENTORY_QUERY))) {
            return Intent.INVENTORY_QUERY;
        }
        
        // 4. 订单查询
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.ORDER_QUERY))) {
            return Intent.ORDER_QUERY;
        }
        
        // 5. 入库/出库
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.INVENTORY_IN))) {
            return Intent.INVENTORY_IN;
        }
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.INVENTORY_OUT))) {
            return Intent.INVENTORY_OUT;
        }
        
        // 6. BOM查询
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.BOM_QUERY))) {
            return Intent.BOM_QUERY;
        }
        
        // 7. 订单创建
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.ORDER_CREATE))) {
            return Intent.ORDER_CREATE;
        }
        
        // 8. 帮助
        if (containsAny(input, INTENT_KEYWORDS.get(Intent.HELP))) {
            return Intent.HELP;
        }
        
        log.info("无法识别意图: {}", userInput);
        return Intent.UNKNOWN;
    }
    
    /**
     * 检查输入是否包含任意关键词
     */
    private boolean containsAny(String input, List<String> keywords) {
        if (keywords == null) return false;
        for (String keyword : keywords) {
            if (input.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取意图描述
     */
    public String getIntentDescription(Intent intent) {
        return intent != null ? intent.getDescription() : "未知意图";
    }
}
