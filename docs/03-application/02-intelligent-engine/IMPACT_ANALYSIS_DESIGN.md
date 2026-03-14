# AI MRP 影响分析模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

影响分析是AI MRP的核心差异化功能，在插单、变更前全面评估影响。

### 1.2 分析类型

| 类型 | 说明 |
|------|------|
| 插单影响 | 插入新订单的影响分析 |
| 变更影响 | 需求/交期变更的影响 |
| 参数影响 | MRP参数调整的影响 |
| What-if | 场景模拟分析 |

---

## 二、影响分析

### 2.1 插单影响分析

```python
# 插单影响分析
class ImpactAnalyzer:
    
    def analyze_insert_order(self, new_order):
        # 1. 产能影响
        capacity_impact = self.check_capacity(new_order)
        
        # 2. 订单影响
        order_impact = self.check_order_impact(new_order)
        
        # 3. 物料影响
        material_impact = self.check_material_impact(new_order)
        
        # 4. 交期影响
        delivery_impact = self.check_delivery_impact(new_order)
        
        # 5. 成本影响
        cost_impact = self.check_cost_impact(new_order)
        
        return ImpactReport(
            capacity=capacity_impact,
            orders=order_impact,
            materials=material_impact,
            delivery=delivery_impact,
            cost=cost_impact
        )
```

### 2.2 产能影响

```python
def check_capacity(self, order):
    # 当前产能利用率
    current_utilization = get_current_utilization()
    
    # 插单后产能利用率
    new_utilization = calculate_utilization(order)
    
    # 是否需要加班
    need_overtime = new_utilization > 100
    
    return {
        "current": current_utilization,
        "after_insert": new_utilization,
        "need_overtime": need_overtime,
        "overtime_hours": max(0, new_utilization - 100) / 100 * 8
    }
```

### 2.3 订单影响

```python
def check_order_impact(self, new_order):
    # 找出会被影响的订单
    affected = []
    
    for order in existing_orders:
        if order.priority < new_order.priority:
            delay = calculate_delay(order, new_order)
            if delay > 0:
                affected.append({
                    "order": order.id,
                    "delay_days": delay,
                    "reason": "产能被挤占"
                })
    
    return {
        "affected_count": len(affected),
        "affected_orders": affected,
        "max_delay": max([a["delay_days"] for a in affected]) if affected else 0
    }
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/impact/analyze-insert` | POST | 插单影响分析 |
| `/api/impact/analyze-change` | POST | 变更影响分析 |
| `/api/impact/analyze-param` | POST | 参数影响分析 |
| `/api/impact/simulate` | POST | What-if模拟 |
| `/api/impact/compare` | POST | 方案对比 |

---

## 四、影响报告

```python
# 影响分析报告
class ImpactReport:
    # 产能影响
    capacity: CapacityImpact
    
    # 订单影响
    orders: List[OrderImpact]
    
    # 物料影响
    materials: List[MaterialImpact]
    
    # 交期影响
    delivery: DeliveryImpact
    
    # 成本影响
    cost: CostImpact
    
    # 建议方案
    suggestions: List[Suggestion]
    
    def to_message(self):
        return f"""
📊 插单影响分析

⚡ 产能影响
   - 当前利用率: {self.capacity.current}%
   - 插单后: {self.capacity.after_insert}%
   - 需加班: {self.capacity.need_overtime} 小时

📦 订单影响
   - 影响订单: {self.orders.affected_count} 个
   - 最大延迟: {self.orders.max_delay} 天

💰 成本影响
   - 加班成本: ¥{self.cost.overtime}
   - 急采成本: ¥{self.cost.expedite}
   - 总计: ¥{self.cost.total}
        """
```

---

## 五、方案推荐

```python
# 智能建议
class SuggestionGenerator:
    
    def generate(self, impact_report):
        suggestions = []
        
        # 方案A: 接受插单
        if impact_report.cost.total < 5000:
            suggestions.append({
                "name": "方案A",
                "description": "接受插单",
                "cost": impact_report.cost.total,
                "pros": ["满足客户需求"],
                "cons": ["增加成本", "影响其他订单"]
            })
        
        # 方案B: 拒绝插单
        suggestions.append({
            "name": "方案B",
            "description": "建议客户延期",
            "cost": 0,
            "pros": ["无额外成本"],
            "cons": ["可能丢失客户"]
        })
        
        # 方案C: 部分满足
        suggestions.append({
            "name": "方案C",
            "description": "部分满足",
            "cost": impact_report.cost.total / 2,
            "pros": ["平衡成本和客户"],
            "cons": ["需要客户同意"]
        })
        
        return suggestions
```

---

## 六、沙箱集成

```python
# 沙箱中执行影响分析
def analyze_in_sandbox(order):
    # 在沙箱环境模拟
    with sandbox():
        # 复制当前数据到沙箱
        sandbox.copy_data()
        
        # 添加新订单
        sandbox.add_order(order)
        
        # 运行MRP
        sandbox.run_mrp()
        
        # 获取影响结果
        result = sandbox.get_impact()
        
        # 不提交，保持预览
        sandbox.discard()
        
    return result
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
