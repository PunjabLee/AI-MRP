# AI MRP 库存管理模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

库存管理负责物料的入库、出库、库存查询和库存分析，是MRP计算的重要输入。

### 1.2 库存类型

| 类型 | 说明 |
|------|------|
| 原材料 | 采购物料 |
| 半成品 | 在制品 |
| 成品 | 完工产品 |
| 辅料 | 辅助材料 |

---

## 二、功能设计

### 2.1 库存交易

```java
// 入库
InventoryTransaction in = InventoryTransaction.builder()
    .type(TransactionType.PURCHASE_IN)
    .itemId(itemId)
    .warehouseId(warehouseId)
    .quantity(100)
    .batchNo("BATCH001")
    .build();

// 出库
InventoryTransaction out = InventoryTransaction.builder()
    .type(TransactionType.PRODUCTION_OUT)
    .itemId(itemId)
    .workOrderId(workOrderId)
    .quantity(50)
    .build();
```

### 2.2 库存查询

```java
// 实时库存
InventorySnapshot getOnHand(Long itemId, Long warehouseId);

// 可用库存 (预留后)
InventoryAvailable getAvailable(Long itemId, Long warehouseId);

// 库存流水
List<InventoryTransaction> getTransactions(Long itemId, DateRange range);
```

### 2.3 库存分析

```java
// 库龄分析
InventoryAgeAnalysis analyzeAge(Long itemId);

// 呆滞分析
List<SluggishItem> analyzeSluggish(int daysThreshold);

// 周转分析
InventoryTurnover analyzeTurnover(Long itemId, DateRange range);
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/inventory/in` | POST | 入库 |
| `/api/inventory/out` | POST | 出库 |
| `/api/inventory/transfer` | POST | 调拨 |
| `/api/inventory/onhand` | GET | 库存查询 |
| `/api/inventory/available` | GET | 可用库存 |
| `/api/inventory/transactions` | GET | 库存流水 |
| `/api/inventory/age-analysis` | GET | 库龄分析 |
| `/api/inventory/sluggish` | GET | 呆滞分析 |

---

## 四、数据模型

```sql
-- 库存台账
CREATE TABLE t_inventory_onhand (
    id BIGINT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    warehouse_id BIGINT,
    location_id BIGINT,
    batch_no VARCHAR(50),
    quantity DECIMAL(18,3),
    update_time DATETIME
);

-- 库存交易流水
CREATE TABLE t_inventory_transaction (
    id BIGINT PRIMARY KEY,
    transaction_no VARCHAR(50),
    type VARCHAR(20),      -- PURCHASE_IN/PRODUCTION_IN/...
    item_id BIGINT,
    warehouse_id BIGINT,
    quantity DECIMAL(18,3),
    reference_type VARCHAR(20),  -- ORDER/WORKORDER/...
    reference_id BIGINT,
    create_time DATETIME
);
```

---

## 五、与MRP集成

```java
// MRP获取库存
public BigDecimal getInventoryForMRP(Long itemId, Long warehouseId) {
    return inventoryRepository.getOnHand(itemId, warehouseId);
}
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
