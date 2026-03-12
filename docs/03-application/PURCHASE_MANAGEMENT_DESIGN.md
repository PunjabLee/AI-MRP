# AI MRP 采购管理模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

采购管理负责供应商管理和采购订单执行，与MRP建议对接。

### 1.2 核心流程

```
MRP建议 → 采购建议 → 采购订单 → 入库
```

---

## 二、功能设计

### 2.1 供应商管理

```java
// 供应商主数据
Supplier supplier = Supplier.builder()
    .supplierCode("SUP001")
    .supplierName("某某公司")
    .contact("张三")
    .phone("13800138000")
    .address("某某市某某区")
    .paymentTerms("NET30")     // 付款条款
    .leadTime(7)               // 标准交期(天)
    .rating(5)                // 评级
    .status("ACTIVE")
    .build();
```

### 2.2 采购订单

```java
// 从MRP建议生成采购单
PurchaseOrder order = PurchaseOrder.builder()
    .orderNo(generateOrderNo())
    .supplierId(supplierId)
    .items(items)
    .totalAmount(calculateTotal())
    .status("DRAFT")
    .build();

// 审批后释放
order.release();
```

### 2.3 到货跟踪

```java
// 在途查询
List<PurchaseOrder> getOnOrder(Long itemId);

// 到货入库
receive(orderId, quantity, warehouseId);
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/purchase/supplier/create` | POST | 创建供应商 |
| `/api/purchase/supplier/list` | GET | 供应商列表 |
| `/api/purchase/order/create` | POST | 创建采购单 |
| `/api/purchase/order/approve` | POST | 审批采购单 |
| `/api/purchase/receive` | POST | 到货入库 |
| `/api/purchase/on-order` | GET | 在途查询 |

---

## 四、数据模型

```sql
-- 供应商主数据
CREATE TABLE t_supplier (
    id BIGINT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    address VARCHAR(200),
    payment_terms VARCHAR(20),
    lead_time INT,           -- 交期(天)
    rating INT,              -- 评级 1-5
    status VARCHAR(20),     -- ACTIVE/INACTIVE
    create_time DATETIME
);

-- 采购订单
CREATE TABLE t_purchase_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    supplier_id BIGINT,
    order_date DATE,
    expected_date DATE,
    total_amount DECIMAL(18,2),
    status VARCHAR(20),    -- DRAFT/APPROVED/RECEIVED/PARTIAL/CANCELLED
    create_time DATETIME
);

-- 采购订单明细
CREATE TABLE t_purchase_order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT,
    item_id BIGINT,
    quantity DECIMAL(18,3),
    received_qty DECIMAL(18,3),
    unit_price DECIMAL(18,4)
);
```

---

## 五、与MRP集成

```java
// 获取在途采购数量
public BigDecimal getOnOrderQuantity(Long itemId) {
    return purchaseOrderRepository.sumOnOrderQuantity(itemId);
}
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
