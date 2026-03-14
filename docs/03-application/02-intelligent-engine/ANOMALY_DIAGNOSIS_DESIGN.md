# AI MRP 异常诊断模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

异常诊断模块自动分析MRP运行结果，诊断缺料、延期等异常原因。

### 1.2 诊断类型

| 类型 | 说明 |
|------|------|
| 缺料诊断 | 分析为什么会缺料 |
| 延期诊断 | 分析订单为什么会延期 |
| 库存异常 | 预警异常库存 |
| 需求异常 | 预警需求突变 |

---

## 二、诊断算法

### 2.1 缺料诊断

```python
# 缺料根因分析
class ShortageDiagnoser:
    
    def diagnose(self, item_id, shortage_qty):
        causes = []
        
        # 1. 检查需求是否异常
        demand_spike = self.check_demand(item_id)
        if demand_spike:
            causes.append({
                "type": "DEMAND_SPIKE",
                "description": f"需求突增 {demand_spike}%",
                "impact": shortage_qty * demand_spike / 100
            })
        
        # 2. 检查供应商交期
        supplier_delay = self.check_supplier(item_id)
        if supplier_delay:
            causes.append({
                "type": "SUPPLIER_DELAY",
                "description": f"供应商延迟 {supplier_delay} 天",
                "impact": supplier_delay * 10
            })
        
        # 3. 检查安全库存
        safety_stock = self.check_safety_stock(item_id)
        if safety_stock < shortage_qty:
            causes.append({
                "type": "LOW_SAFETY_STOCK",
                "description": f"安全库存偏低 (当前{safety_stock})",
                "impact": shortage_qty - safety_stock
            })
        
        # 4. 检查在途采购
        on_order = self.check_on_order(item_id)
        causes.append({
            "type": "IN_TRANSIT",
            "description": f"在途采购 {on_order}",
            "impact": -on_order
        })
        
        return causes
```

### 2.2 延期诊断

```python
# 延期原因分析
class DelayDiagnoser:
    
    def diagnose(self, order_id):
        causes = []
        
        # 1. 产能不足
        capacity = self.check_capacity(order_id)
        if capacity < 100:
            causes.append({
                "type": "CAPACITY_SHORTAGE",
                "description": f"产能利用率 {capacity}%",
                "recommendation": "考虑加班或外协"
            })
        
        # 2. 物料短缺
        material = self.check_material(order_id)
        if material:
            causes.append({
                "type": "MATERIAL_SHORTAGE",
                "description": f"缺料: {material}"
            })
        
        # 3. 优先级冲突
        priority = self.check_priority(order_id)
        causes.append({
            "type": "PRIORITY_CONFLICT",
            "description": f"被 {priority} 个订单挤占"
        })
        
        return causes
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/diagnose/shortage` | GET | 缺料诊断 |
| `/api/diagnose/delay` | GET | 延期诊断 |
| `/api/diagnose/inventory` | GET | 库存异常诊断 |
| `/api/diagnose/demand` | GET | 需求异常诊断 |

---

## 四、诊断结果

```python
# 诊断结果
class DiagnosisResult:
    issue_type: str           # 问题类型
    severity: str             # 严重程度: HIGH/MEDIUM/LOW
    causes: List[Cause]       # 根因列表
    recommendations: List[str]  # 建议
    
class Cause:
    type: str                 # 原因类型
    description: str          # 描述
    impact: float             # 影响程度
    probability: float        # 发生概率
```

---

## 五、诊断报告

```python
# 生成诊断报告
def generate_report(item_id):
    diagnosis = diagnose_shortage(item_id)
    
    return f"""
📊 诊断报告 - {item_id}
    
⚠️ 问题: 缺料 {diagnosis.shortage_qty} 件

📋 原因分析:
{chr(10).join(f"{i+1}. {c.description} (影响: {c.impact})" 
               for i, c in enumerate(diagnosis.causes))}

💡 建议:
{chr(10).join(f"- {r}" for r in diagnosis.recommendations)}
    """
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
