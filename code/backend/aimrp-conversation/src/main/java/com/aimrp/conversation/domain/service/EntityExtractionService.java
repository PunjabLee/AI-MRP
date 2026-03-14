package com.aimrp.conversation.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实体提取服务
 * 
 * 负责从用户输入中提取业务实体（物料、订单、仓库等）
 */
@Slf4j
@Service
public class EntityExtractionService {
    
    /**
     * 实体类型
     */
    public enum EntityType {
        ITEM_CODE("物料编码", "物料的唯一标识"),
        ITEM_NAME("物料名称", "物料的名称"),
        ORDER_NO("订单编号", "订单的唯一标识"),
        CUSTOMER_NAME("客户名称", "客户名称"),
        WAREHOUSE_CODE("仓库编码", "仓库的唯一标识"),
        QUANTITY("数量", "数值"),
        DATE("日期", "日期"),
        UNKNOWN("未知", "无法识别的实体");
        
        private final String label;
        private final String description;
        
        EntityType(String label, String description) {
            this.label = label;
            this.description = description;
        }
        
        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }
    
    /**
     * 实体提取结果
     */
    @Data
    @Builder
    public static class Entity {
        private EntityType type;
        private String value;
        private String originalText;
        private int startIndex;
        private int endIndex;
    }
    
    // 物料编码正则 (例如: A001, ITEM-123)
    private static final Pattern ITEM_CODE_PATTERN = Pattern.compile(
        "\\b([A-Z]{1,3}[-_]?\\d{3,6})\\b", 
        Pattern.CASE_INSENSITIVE
    );
    
    // 订单编号正则 (例如: SO20240301001)
    private static final Pattern ORDER_NO_PATTERN = Pattern.compile(
        "\\b(SO|PO|MO)\\d{8,12}\\b", 
        Pattern.CASE_INSENSITIVE
    );
    
    // 仓库编码正则 (例如: WH01, WAREHOUSE-001)
    private static final Pattern WAREHOUSE_PATTERN = Pattern.compile(
        "\\b(WH|WAREHOUSE)[-_]?\\d{2,4}\\b", 
        Pattern.CASE_INSENSITIVE
    );
    
    // 数量正则 (例如: 100个, 50件)
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*(个|件|台|套|kg|千克|吨|米|PCS|PC)?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 日期正则 (例如: 2024-03-01, 3月1日, 明天, 后天)
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{4}[-/年]\\d{1,2}[-/月]\\d{1,2}日?)|(\\d{1,2}[-/月]\\d{1,2}[日]?)|(今天|明天|后天|下周|下月)",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 提取所有实体
     * 
     * @param userInput 用户输入
     * @return 实体列表
     */
    public List<Entity> extractAll(String userInput) {
        List<Entity> entities = new ArrayList<>();
        
        if (userInput == null || userInput.trim().isEmpty()) {
            return entities;
        }
        
        // 提取物料编码
        entities.addAll(extractEntities(userInput, ITEM_CODE_PATTERN, EntityType.ITEM_CODE));
        
        // 提取订单编号
        entities.addAll(extractEntities(userInput, ORDER_NO_PATTERN, EntityType.ORDER_NO));
        
        // 提取仓库编码
        entities.addAll(extractEntities(userInput, WAREHOUSE_PATTERN, EntityType.WAREHOUSE_CODE));
        
        // 提取数量
        entities.addAll(extractEntities(userInput, QUANTITY_PATTERN, EntityType.QUANTITY));
        
        // 提取日期
        entities.addAll(extractEntities(userInput, DATE_PATTERN, EntityType.DATE));
        
        // 尝试提取物料名称（基于常见词汇）
        entities.addAll(extractItemNames(userInput));
        
        log.info("从输入 '{}' 提取到 {} 个实体", userInput, entities.size());
        
        return entities;
    }
    
    /**
     * 提取特定类型实体
     */
    public List<Entity> extract(String userInput, EntityType type) {
        return extractAll(userInput).stream()
                .filter(e -> e.getType() == type)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 提取物料编码
     */
    public List<String> extractItemCodes(String userInput) {
        return extract(userInput, EntityType.ITEM_CODE).stream()
                .map(Entity::getValue)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 提取订单编号
     */
    public List<String> extractOrderNos(String userInput) {
        return extract(userInput, EntityType.ORDER_NO).stream()
                .map(Entity::getValue)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 提取数量
     */
    public List<Double> extractQuantities(String userInput) {
        return extract(userInput, EntityType.QUANTITY).stream()
                .map(e -> {
                    try {
                        return Double.parseDouble(e.getValue());
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 提取日期
     */
    public List<String> extractDates(String userInput) {
        return extract(userInput, EntityType.DATE).stream()
                .map(Entity::getValue)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 提取实体（通用方法）
     */
    private List<Entity> extractEntities(String input, Pattern pattern, EntityType type) {
        List<Entity> entities = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            String value = matcher.group(1);
            if (value != null && !value.isEmpty()) {
                Entity entity = Entity.builder()
                        .type(type)
                        .value(value.trim())
                        .originalText(matcher.group(0))
                        .startIndex(matcher.start())
                        .endIndex(matcher.end())
                        .build();
                entities.add(entity);
            }
        }
        
        return entities;
    }
    
    /**
     * 提取物料名称（简单关键词匹配）
     */
    private List<Entity> extractItemNames(String input) {
        List<Entity> entities = new ArrayList<>();
        
        // 常见物料名称关键词
        String[] itemKeywords = {"产品", "部件", "物料", "原材料", "组件"};
        
        for (String keyword : itemKeywords) {
            int index = input.indexOf(keyword);
            if (index >= 0) {
                // 尝试提取后面的名称
                String afterKeyword = input.substring(index + keyword.length()).trim();
                if (!afterKeyword.isEmpty()) {
                    // 取前20个字符作为物料名称
                    String name = afterKeyword.length() > 20 
                            ? afterKeyword.substring(0, 20) 
                            : afterKeyword;
                    name = name.replaceAll("[，。,.]", " ").trim().split("\\s+")[0];
                    
                    if (!name.isEmpty()) {
                        entities.add(Entity.builder()
                                .type(EntityType.ITEM_NAME)
                                .value(name)
                                .originalText(keyword + name)
                                .startIndex(index)
                                .endIndex(index + keyword.length() + name.length())
                                .build());
                    }
                }
            }
        }
        
        return entities;
    }
    
    /**
     * 获取实体描述
     */
    public String getEntityDescription(Entity entity) {
        if (entity == null || entity.getType() == null) {
            return "";
        }
        return String.format("[%s: %s]", entity.getType().getLabel(), entity.getValue());
    }
}
