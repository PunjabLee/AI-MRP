# AI MRP 需求管理模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

需求管理是MRP系统的输入源头，负责收集和管理所有物料需求来源，包括销售订单、销售预测和独立需求。

### 1.2 需求类型

| 类型 | 说明 | 来源 |
|------|------|------|
| 销售订单 | 客户正式订单 | 销售模块/手动创建 |
| 销售预测 | 未来需求预测 | AI预测/手动录入 |
| 独立需求 | 备品备件需求 | 维护计划 |

---

## 二、功能设计

### 2.1 销售订单管理

```java
// 核心功能
- 创建销售订单
- 修改销售订单
- 取消销售订单
- 订单拆分/合并
- 订单状态流转
```

### 2.2 销售预测

```java
// 核心功能
- AI需求预测 (时间序列)
- 手动预测录入
- 预测调整
- 预测版本管理
```

### 2.3 需求汇总

```java
// 需求来源汇总
List<Demand> demands = demandService.aggregate(
    DemandSource.ORDER,      // 销售订单
    DemandSource.FORECAST,   // 销售预测
    DemandSource.INDEPENDENT // 独立需求
);
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/demand/order/create` | POST | 创建销售订单 |
| `/api/demand/order/update` | PUT | 更新销售订单 |
| `/api/demand/order/cancel` | POST | 取消订单 |
| `/api/demand/forecast/create` | POST | 创建预测 |
| `/api/demand/aggregate` | POST | 需求汇总 |
| `/api/demand/list` | GET | 需求列表查询 |

---

## 四、数据模型

```sql
-- 销售订单表
CREATE TABLE t_sales_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    customer_id BIGINT,
    order_date DATE,
    delivery_date DATE,
    status VARCHAR(20),  -- DRAFT/CONFIRMED/PRODUCING/SHIPPED/CANCELLED
    priority INT DEFAULT 5,
    total_amount DECIMAL(18,2),
    create_time DATETIME
);

-- 订单明细
CREATE TABLE t_sales_order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT,
    item_id BIGINT,
    quantity DECIMAL(18,3),
    delivered_qty DECIMAL(18,3),
    unit_price DECIMAL(18,4)
);

-- 销售预测
CREATE TABLE t_sales_forecast (
    id BIGINT PRIMARY KEY,
    item_id BIGINT,
    forecast_date DATE,
    quantity DECIMAL(18,3),
    confidence DECIMAL(5,2),  -- 置信度
    source VARCHAR(20),       -- AI/MANUAL
    version INT
);
```

---

## 五、与其他模块集成

| 集成模块 | 集成内容 |
|----------|----------|
| MRP计算 | 提供需求来源 |
| 库存管理 | 订单占用库存 |
| 生产管理 | 订单生成工单 |
| 采购管理 | 订单触发采购 |

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
