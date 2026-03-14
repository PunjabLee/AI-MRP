---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: 7d0fb3640b358e8bff10f4b2a6a3d106
    PropagateID: 7d0fb3640b358e8bff10f4b2a6a3d106
    ReservedCode1: 3046022100964e35f5b86aed9f366bc7c66747edb99b4522cca1e82f77805a031dffc05c9c022100a6c7ef29395e6419fc62bfd742c31ff200fc5ab39125a6df2b55c414d5612311
    ReservedCode2: 3045022004e1da2cc05b5f602675ce5e951374a4842c6257c1d472750b76f1e1ee3c5a70022100f17b9c144df6d9d74783fae9bc3e0cb1a6665ee4a9be0a72ca4707b52c24187e
---

# AI MRP 领域模型设计文档

> **版本**：1.0  
> **日期**：2026-03-08  
> **理念**：DDD 领域驱动设计

---

## 一、领域划分

基于业务边界和职责，将 AI MRP 划分为以下核心领域：

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          AI MRP 领域模型                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                      核心领域 (Core Domain)                      │  │
│  ├─────────────────────────────────────────────────────────────────┤  │
│  │                                                                  │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │  │
│  │  │   Demand    │  │     BOM    │  │  Inventory  │            │  │
│  │  │  (需求)    │  │   (BOM)    │  │  (库存)    │            │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘            │  │
│  │                                                                  │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │  │
│  │  │     MRP     │  │  Purchase   │  │ Production  │            │  │
│  │  │  (MRP)    │  │  (采购)    │  │  (生产)    │            │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘            │  │
│  │                                                                  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                      支持领域 (Support Domain)                   │  │
│  ├─────────────────────────────────────────────────────────────────┤  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │  │
│  │  │  Supplier   │  │ Organization│  │   Sandbox   │            │  │
│  │  │ (供应商)    │  │  (组织)    │  │  (沙箱)    │            │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘            │  │
│  │                                                                  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心领域详解

### 2.1 需求领域 (Demand)

**职责**：管理销售订单、需求预测、需求池

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| SalesOrder | 销售订单 | orderNo, customer, lines, status, priority |
| DemandForecast | 需求预测 | item, forecastDate, qty, confidence |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| OrderLine | 订单明细 | SalesOrder |
| DemandPoolItem | 需求池项 | SalesOrder |

#### 值对象

| 值对象 | 说明 | 属性 |
|--------|------|------|
| OrderNo | 订单号 | String |
| Priority | 优先级 | Int (1-10) |
| DueDate | 交货期 | LocalDate |
| CustomerCode | 客户编码 | String |

#### 领域服务

| 服务 | 说明 |
|------|------|
| DemandMergeService | 需求合并服务 |
| DemandForecastService | 需求预测服务 |

#### 领域事件

| 事件 | 说明 |
|------|------|
| OrderCreatedEvent | 订单创建事件 |
| OrderConfirmedEvent | 订单确认事件 |
| OrderCancelledEvent | 订单取消事件 |

---

### 2.2 BOM 领域 (BOM)

**职责**：管理物料清单结构、BOM 版本

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| BOM | 物料清单 | itemCode, version, status, lines |
| BOMVersion | BOM 版本管理 | bomId, version, effectiveDate |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| BOMLine | BOM 明细 | BOM |

#### 值对象

| 值对象 | 说明 |
|--------|------|
| ItemCode | 物料编码 |
| Quantity | 数量 |
| LossRate | 损耗率 |
| Unit | 单位 |

#### 领域服务

| 服务 | 说明 |
|------|------|
| BOMExpandService | BOM 展开服务（单层/多层） |
| BOMCompareService | BOM 对比服务 |

---

### 2.3 库存领域 (Inventory)

**职责**：管理库存数量、库存交易、库位

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| InventoryItem | 库存项 | itemCode, warehouse, qty, status |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| InventoryTransaction | 库存事务 | InventoryItem |
| InventoryReservation | 库存预留 | InventoryItem |

#### 值对象

| 值对象 | 说明 |
|--------|------|
| WarehouseCode | 仓库编码 |
| LocationCode | 库位编码 |
| Quantity | 数量 |

#### 领域服务

| 服务 | 说明 |
|------|------|
| InventoryAllocateService | 库存分配服务 |
| InventoryCheckService | 库存盘点服务 |

---

### 2.4 MRP 领域 (MRP)

**职责**：MRP 计算、净需求计算、计划订单生成

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| MRPRun | MRP 运行记录 | runNo, status, startTime, endTime |
| MRPResult | MRP 计算结果 | runId, suggestions |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| NetRequirement | 净需求 | MRPRun |
| PlanOrder | 计划订单 | MRPRun |
| ActionMessage | 行动消息 | MRPRun |

#### 值对象

| 值对象 | 说明 |
|--------|------|
| MRPPolicy | MRP 策略 |
| LeadTime | 提前期 |
| LotSize | 批量大小 |

#### 领域服务

| 服务 | 说明 |
|------|------|
| MRPCalculateService | MRP 计算服务 |
| NetRequirementService | 净需求计算服务 |
| PlanOrderService | 计划订单生成服务 |

---

### 2.5 采购领域 (Purchase)

**职责**：采购订单管理、供应商管理

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| PurchaseOrder | 采购订单 | poNo, supplier, lines, status |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| PurchaseOrderLine | 采购明细 | PurchaseOrder |

#### 领域服务

| 服务 | 说明 |
|------|------|
| PurchaseSuggestService | 采购建议服务 |
| SupplierService | 供应商服务 |

---

### 2.6 生产领域 (Production)

**职责**：生产工单管理、生产排程

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| ProductionOrder | 生产工单 | moNo, item, qty, status, schedule |
| ProductionSchedule | 生产排程 | date, lines, resources |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| ProductionOrderLine | 生产明细 | ProductionOrder |
| WorkOrder | 工序工单 | ProductionOrder |

#### 领域服务

| 服务 | 说明 |
|------|------|
| ProductionScheduleService | 生产排程服务 |
| ProductionExecutionService | 生产执行服务 |

---

## 三、支持领域详解

### 3.1 供应商领域 (Supplier)

#### 聚合根

| 聚合根 | 说明 |
|--------|------|
| Supplier | 供应商主数据 |
| SupplierItem | 供应商物料关联 |

---

### 3.2 组织领域 (Organization)

#### 聚合根

| 聚合根 | 说明 |
|--------|------|
| Organization | 组织（公司/工厂） |
| Warehouse | 仓库 |
| Project | 项目 |

---

### 3.3 沙箱领域 (Sandbox)

#### 聚合根

| 聚合根 | 说明 | 核心属性 |
|--------|------|----------|
| SandboxSession | 沙箱会话 | sessionNo, type, status, sourceData, resultData |

#### 实体

| 实体 | 说明 | 聚合根归属 |
|------|------|------------|
| SandboxMRPSuggestion | 沙箱 MRP 结果 | SandboxSession |
| SandboxAIPlan | 沙箱 AI 方案 | SandboxSession |
| SandboxAudit | 沙箱审计日志 | SandboxSession |

#### 领域服务

| 服务 | 说明 |
|------|------|
| SandboxService | 沙箱管理服务 |
| SimulationService | 模拟分析服务 |

---

## 四、领域服务依赖关系

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        领域服务依赖图                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│    Demand ─────────┐                                                   │
│         │          │                                                   │
│         ▼          │                                                   │
│    BOM ────────► MRP ◄────────── Inventory                          │
│         │          │                │                                 │
│         │          ▼                │                                 │
│         │     PlanOrder ◄─────────┘                                 │
│         │          │                                                 │
│         ▼          ▼                                                 │
│    Purchase ◄──────┬───────► Production                             │
│         │          │                                                   │
│         │          ▼                                                   │
│         │     Supplier                                               │
│         │                                                            │
│         └─────────────────────────────────────────────────────────► Sandbox
│         │                                                            │
│    Organization                                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 五、聚合根状态机

### 5.1 SalesOrder 状态

```
PENDING → CONFIRMED → IN_PRODUCTION → COMPLETED
    │           │              │              │
    └───────────┴──────────────┴──────────────┘ CANCELLED
```

### 5.2 PurchaseOrder 状态

```
PENDING → APPROVED → SENT → PARTIAL_RECEIVED → RECEIVED
    │           │         │              │            │
    └───────────┴─────────┴──────────────┴──────────── CANCELLED
```

### 5.3 ProductionOrder 状态

```
PENDING → SCHEDULED → IN_PRODUCTION → PARTIAL_COMPLETED → COMPLETED
    │            │            │               │               │
    └────────────┴────────────┴───────────────┴───────────── CANCELLED
```

### 5.4 SandboxSession 状态

```
RUNNING → CONFIRMED / CANCELLED / EXPIRED
   │
   └─ 前端预览
       │
       ├─► 确认 → 同步到生产
       ├─► 取消 → 清理数据
       └─► 超时 → 自动过期
```

---

## 六、代码包结构

```
ai-mrp-domain/
├── demand/                    # 需求领域
│   ├── entity/
│   │   ├── SalesOrder.java
│   │   ├── OrderLine.java
│   │   └── DemandForecast.java
│   ├── valueobject/
│   │   ├── OrderNo.java
│   │   ├── Priority.java
│   │   └── DueDate.java
│   ├── repository/
│   │   └── SalesOrderRepository.java
│   ├── service/
│   │   └── DemandMergeService.java
│   └── event/
│       └── OrderCreatedEvent.java
│
├── bom/                      # BOM 领域
├── inventory/                 # 库存领域
├── mrp/                      # MRP 领域
├── purchase/                 # 采购领域
├── production/               # 生产领域
├── supplier/                # 供应商领域
├── organization/            # 组织领域
└── sandbox/                 # 沙箱领域
```

---

*领域模型设计完成*
