# AI MRP LLM理解模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

LLM理解模块是AI MRP的"大脑"，负责理解用户自然语言输入，提取意图和实体。

### 1.2 能力等级

| 等级 | 说明 |
|------|------|
| L0 | 简单问答 |
| L1 | 操作执行 |
| L2 | 流程编排 |
| L3 | 智能推荐 |
| L4 | 自主决策 |

---

## 二、功能设计

### 2.1 意图识别

```python
# 意图分类
INTENTS = {
    "QUERY": ["查一下", "看看", "有多少"],
    "CREATE": ["创建", "新增", "添加"],
    "UPDATE": ["修改", "更新", "调整"],
    "DELETE": ["删除", "取消"],
    "EXECUTE": ["执行", "跑一下", "计算"],
    "ANALYZE": ["分析", "为什么", "影响"],
    "SUGGEST": ["建议", "推荐", "优化"]
}
```

### 2.2 实体提取

```python
# 实体类型
ENTITIES = {
    "item": ["物料", "产品", "零件"],
    "quantity": ["数量", "多少", "件"],
    "date": ["日期", "什么时候", "交货"],
    "warehouse": ["仓库", "库位"],
    "order": ["订单", "工单"],
    "supplier": ["供应商"]
}
```

### 2.3 参数补全

```python
# 缺失参数时主动询问
def fill_missing_params(intent, entities, context):
    missing = []
    for param in REQUIRED_PARAMS[intent]:
        if param not in entities:
            missing.append(param)
    
    if missing:
        return AskForParams(missing)
    return ExecuteAction(intent, entities)
```

---

## 三、API 设计

### 3.1 理解接口

```python
# 理解用户输入
POST /api/ai/understand
{
    "message": "帮我查一下A物料的库存",
    "context": {}  # 可选，上下文
}

# 返回
{
    "intent": "QUERY",
    "entities": {
        "item": "A物料"
    },
    "confidence": 0.95,
    "action": {
        "type": "QUERY_INVENTORY",
        "params": {"itemId": "xxx"}
    }
}
```

---

## 四、对话管理

```python
# 多轮对话上下文
class ConversationContext:
    user_id: str
    session_id: str
    history: List[Message]
    slots: Dict[str, Any]  # 已填充的参数
    
    def update(self, intent, entities):
        self.slots.update(entities)
        
    def can_execute(self):
        return all(
            self.slots.get(p) 
            for p in REQUIRED_PARAMS.get(self.history[-1].intent, [])
        )
```

---

## 五、技术实现

### 5.1 提示词模板

```python
INTENT_PROMPT = """
你是一个MRP系统助手。根据用户输入，识别意图和实体。

意图类型: {intents}
实体类型: {entities}

用户输入: {user_input}

返回JSON格式:
{{
    "intent": "意图",
    "entities": {{"实体": "值"}},
    "confidence": 0.0-1.0
}}
"""
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
