# AI MRP 风险预警模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

风险预警模块主动监控供应链风险，提前预警并提供规避方案。

### 1.2 预警类型

| 类型 | 说明 |
|------|------|
| 供应商风险 | 交期延迟、质量问题 |
| 库存风险 | 缺料、呆滞 |
| 需求风险 | 需求突变 |
| 物流风险 | 运输异常 |

---

## 二、预警规则

### 2.1 供应商风险

```python
# 供应商风险规则
SUPPLIER_RULES = {
    "delivery_delay": {
        "condition": "actual_delivery > promised_delivery + 2 days",
        "severity": "MEDIUM",
        "action": "alert_supplier"
    },
    "quality_issue": {
        "condition": "reject_rate > 5%",
        "severity": "HIGH",
        "action": "require_quality_review"
    },
    "capacity_risk": {
        "condition": "supplier_utilization > 90%",
        "severity": "MEDIUM",
        "action": "find_alternative"
    }
}
```

### 2.2 库存风险

```python
# 库存风险规则
INVENTORY_RULES = {
    "stockout": {
        "condition": "on_hand < safety_stock",
        "severity": "HIGH",
        "action": "trigger_purchase"
    },
    "overstock": {
        "condition": "on_hand > max_stock * 2",
        "severity": "MEDIUM",
        "action": "recommend_discount"
    },
    "sluggish": {
        "condition": "no_transaction > 90 days",
        "severity": "LOW",
        "action": "mark_sluggish"
    }
}
```

---

## 三、预警引擎

### 3.1 监控逻辑

```python
# 风险监控
class RiskMonitor:
    
    def check_all(self):
        risks = []
        
        # 供应商风险
        risks.extend(self.check_suppliers())
        
        # 库存风险
        risks.extend(self.check_inventory())
        
        # 需求风险
        risks.extend(self.check_demand())
        
        # 物流风险
        risks.extend(self.check_logistics())
        
        return risks
    
    def check_suppliers(self):
        risks = []
        
        # 检查延迟
        for po in purchase_orders:
            if po.is_delayed():
                risks.append(Risk(
                    type="SUPPLIER_DELAY",
                    severity="MEDIUM",
                    description=f"供应商 {po.supplier} 延迟 {po.delay_days} 天",
                    reference_id=po.id
                ))
        
        return risks
```

### 3.2 预警通知

```python
# 预警通知
class RiskNotifier:
    
    def notify(self, risks):
        for risk in risks:
            # 1. 站内通知
            notificationService.send(risk)
            
            # 2. 邮件通知
            if risk.severity == "HIGH":
                emailService.send(risk)
            
            # 3. 短信通知
            if risk.severity == "CRITICAL":
                smsService.send(risk)
```

---

## 四、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/risk/check` | GET | 风险检查 |
| `/api/risk/alerts` | GET | 预警列表 |
| `/api/risk/alert/{id}` | GET | 预警详情 |
| `/api/risk/acknowledge` | POST | 确认预警 |
| `/api/risk/mitigate` | POST | 执行规避方案 |

---

## 五、预警数据模型

```sql
-- 风险预警表
CREATE TABLE t_risk_alert (
    id BIGINT PRIMARY KEY,
    alert_no VARCHAR(50),
    risk_type VARCHAR(50),      -- SUPPLIER/INVENTORY/DEMAND/LOGISTICS
    severity VARCHAR(20),       -- CRITICAL/HIGH/MEDIUM/LOW
    title VARCHAR(100),
    description VARCHAR(500),
    reference_type VARCHAR(50),
    reference_id BIGINT,
    status VARCHAR(20),         -- NEW/ACKNOWLEDGED/RESOLVED
    acknowledged_by VARCHAR(50),
    acknowledged_at DATETIME,
    resolved_at DATETIME,
    create_time DATETIME
);
```

---

## 六、规避方案

```python
# 风险规避
class RiskMitigation:
    
    def suggest(self, risk):
        suggestions = {
            "SUPPLIER_DELAY": [
                "联系供应商确认交期",
                "寻找替代供应商",
                "调整生产计划"
            ],
            "STOCKOUT": [
                "触发紧急采购",
                "调整安全库存",
                "与客户协商延期"
            ]
        }
        
        return suggestions.get(risk.type, ["人工处理"])
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
