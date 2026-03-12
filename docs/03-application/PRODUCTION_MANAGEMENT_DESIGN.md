# AI MRP 生产管理模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

生产管理负责工单创建、生产排程、车间执行和报工。

### 1.2 核心流程

```
MRP建议 → 生产工单 → 排程 → 车间报工 → 完工入库
```

---

## 二、功能设计

### 2.1 生产工单

```java
// 从MRP建议生成工单
WorkOrder order = WorkOrder.builder()
    .orderNo(generateWorkOrderNo())
    .itemId(itemId)
    .quantity(quantity)
    .priority(5)
    .status("RELEASED")
    .build();

// 工艺路线
order.setRouting(routing);  // 工序列表
```

### 2.2 生产排程

```java
// 排程参数
ScheduleParam param = ScheduleParam.builder()
    .strategy(ScheduleStrategy.FIFO)  // 先进先出
    //.strategy(ScheduleStrategy.EARLIEST_DUE_DATE)
    //.strategy(ScheduleStrategy.SHORTEST_OPERATION)
    .build();

// 执行排程
ScheduleResult result = schedulingService.schedule(orders, resources, param);
```

### 2.3 报工

```java
// 报工
Report report = Report.builder()
    .workOrderId(workOrderId)
    .operationId(operationId)
    .quantity(100)          // 报工数量
    .goodQuantity(98)       // 良品数量
    .rejectQuantity(2)      // 不良品
    .reportTime(new Date())
    .build();
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/production/workorder/create` | POST | 创建工单 |
| `/api/production/workorder/release` | POST | 释放工单 |
| `/api/production/schedule` | POST | 生产排程 |
| `/api/production/report` | POST | 生产报工 |
| `/api/production/complete` | POST | 完工入库 |
| `/api/production/track` | GET | 工单追踪 |

---

## 四、数据模型

```sql
-- 生产工单
CREATE TABLE t_work_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    item_id BIGINT NOT NULL,
    quantity DECIMAL(18,3),
    produced_qty DECIMAL(18,3),
    status VARCHAR(20),    -- DRAFT/RELEASED/IN_PROGRESS/COMPLETED/CANCELLED
    priority INT DEFAULT 5,
    planned_start DATE,
    planned_end DATE,
    actual_start DATE,
    actual_end DATE,
    create_time DATETIME
);

-- 工艺路线
CREATE TABLE t_routing (
    id BIGINT PRIMARY KEY,
    item_id BIGINT,
    version VARCHAR(20),
    status VARCHAR(20)
);

-- 工序
CREATE TABLE t_operation (
    id BIGINT PRIMARY KEY,
    routing_id BIGINT,
    sequence INT,           -- 工序序号
    operation_name VARCHAR(50),
    work_center_id BIGINT,
    standard_time DECIMAL(10,2)
);

-- 报工记录
CREATE TABLE t_production_report (
    id BIGINT PRIMARY KEY,
    work_order_id BIGINT,
    operation_id BIGINT,
    quantity DECIMAL(18,3),
    good_qty DECIMAL(18,3),
    reject_qty DECIMAL(18,3),
    report_time DATETIME,
    operator_id VARCHAR(50)
);
```

---

## 五、与MRP/排程集成

```java
// MRP获取在产数量
public BigDecimal getInProductionQuantity(Long itemId) {
    return workOrderRepository.sumInProduction(itemId);
}
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
