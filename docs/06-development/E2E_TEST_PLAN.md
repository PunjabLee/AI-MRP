# AI MRP End-to-End Test Plan

> **Version**: 2.0
> **Date**: 2026-03-14
> **Based on**: PRD.md, API_REFERENCE.md, BUSINESS_LOGIC_MEMO.md
> **Coverage**: Complete feature coverage from PRD.md

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Test Strategy](#2-test-strategy)
3. [Dialogue Layer Tests](#3-dialogue-layer-tests-p0)
4. [Core Business Layer Tests](#4-core-business-layer-tests-p0)
5. [AI Engine Layer Tests](#5-ai-engine-layer-tests-p1)
6. [Data Layer Tests](#6-data-layer-tests)
7. [Sandbox Tests](#7-sandbox-tests-p1)
8. [Non-Functional Tests](#8-non-functional-tests)
9. [Enterprise Feature Tests](#9-enterprise-feature-tests-p2)
10. [Test Coverage Matrix](#10-test-coverage-matrix)

---

## 1. System Overview

### 1.1 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     1. Dialogue Layer                          │
│   AI Dialog Window | Intent Understanding | Execution Engine   │
├─────────────────────────────────────────────────────────────────┤
│                   2. Core Business Layer                       │
│   Demand | BOM | Inventory | MRP | Purchase | Production     │
├─────────────────────────────────────────────────────────────────┤
│                    3. AI Engine Layer                          │
│   LLM | OR Solver | Prediction | Anomaly | Risk | Impact     │
├─────────────────────────────────────────────────────────────────┤
│                     4. Data Layer                              │
│   Master Data | Transaction Data | Analysis Data             │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Capability Matrix (L0-L4)

| Level | Capability | Example | Priority |
|-------|------------|---------|----------|
| L0 | Simple Q&A | "什么是 MRP?" | P1 |
| L1 | Operation Execution | "帮我查一下 A 物料的库存" | P0 |
| L2 | Process Orchestration | "帮我跑一下 MRP" | P0 |
| L3 | AI Recommendation | "根据你的历史，建议安全库存调高 20%" | P1 |
| L4 | Autonomous Decision | "检测到缺料风险，已自动调整采购计划" | P2 |

---

## 2. Test Strategy

### 2.1 Priority Definitions

| Priority | Description | Test Coverage |
|----------|-------------|----------------|
| P0 | Critical path - must pass | 100% |
| P1 | Important features | 80% |
| P2 | Nice to have | 50% |

### 2.2 Test Environments

| Environment | URL | Purpose |
|-------------|-----|---------|
| Development | localhost:8080 / localhost:8000 | Local dev |
| Test | test.aimrp.local | Integration |
| Staging | staging.aimrp.local | Pre-production |
| Production | prod.aimrp.local | Live |

---

## 3. Dialogue Layer Tests (P0)

### 3.1 AI Dialog Window

#### TC-DL-001: Dialog Window Launch

**Objective**: Verify dialog window accessible from any page

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Navigate to any page (Order/BOM/Inventory) | Dialog icon visible |
| 2 | Click dialog icon | Dialog window opens |
| 3 | Verify dialog position | Side or bottom floating |
| 4 | Send test message | Message appears in chat |

**Verification Points**:
- [ ] Dialog icon visible on all pages
- [ ] Opens without page reload
- [ ] Supports text input
- [ ] Message history maintained

---

#### TC-DL-002: Multi-Message Display

**Objective**: Verify dialog displays text, tables, charts, cards

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Query inventory | Table displayed |
| 2 | Query forecast | Chart displayed |
| 3 | Query order status | Card with status |
| 4 | Query multiple items | Mixed format |

**Verification Points**:
- [ ] Text messages display correctly
- [ ] Tables render with data
- [ ] Charts visualize correctly
- [ ] Cards show key info

---

#### TC-DL-003: Context Memory

**Objective**: Verify multi-turn conversation maintains context

**Test Steps**:
| Step | User Input | Expected |
|------|------------|----------|
| 1 | "查一下 ITEM001" | Returns ITEM001 data |
| 2 | "够不够?" | Uses ITEM001 context |
| 3 | "显示订单" | Lists orders |
| 4 | "刚才那个 ITEM001 呢?" | Returns to ITEM001 |

**Verification Points**:
- [ ] Previous context remembered
- [ ] Entity references resolved
- [ ] Context clears after timeout

---

#### TC-DL-004: Dialog History

**Objective**: Verify conversation history persists

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Start conversation | Messages added |
| 2 | Refresh page | History loads |
| 3 | Click history | Previous conversation shown |
| 4 | Search history | Can find past queries |

**Verification Points**:
- [ ] History saved to database
- [ ] Loads on page refresh
- [ ] Searchable

---

### 3.2 Intent Understanding

#### TC-DL-005: Intent Recognition - Query Operations

**Objective**: Verify system recognizes query intents

| Input | Expected Intent | Entities |
|-------|-----------------|----------|
| "查一下物料 A 的库存" | query_stock | {item: A} |
| "本周有哪些订单" | query_orders | {time: week} |
| "看看 BOM" | query_bom | {} |
| "跑一下 MRP" | run_mrp | {} |

---

#### TC-DL-006: Intent Recognition - Execution Operations

**Objective**: Verify system recognizes execution intents

| Input | Expected Intent | Entities |
|-------|-----------------|----------|
| "创建一张订单" | create_order | {} |
| "入库 100 个物料 A" | create_receipt | {item: A, qty: 100} |
| "取消订单 001" | cancel_order | {order: 001} |
| "帮我采购" | create_purchase | {} |

---

#### TC-DL-007: Entity Extraction

**Objective**: Verify entities extracted from natural language

**Test Scenarios**:
| Input | Extracted Entities |
|-------|---------------------|
| "创建订单：客户华为，产品 X100，数量 500，3月15日交货" | {customer: 华为, product: X100, qty: 500, date: 2026-03-15} |
| "入库 100 个 ITEM001" | {item: ITEM001, qty: 100} |
| "设置安全库存 100" | {qty: 100} |

**Verification Points**:
- [ ] Item codes extracted
- [ ] Quantities parsed
- [ ] Dates recognized
- [ ] Customer names extracted

---

#### TC-DL-008: Parameter Clarification

**Objective**: System asks for missing required parameters

**Test Scenario**:
| Step | User Input | System Response |
|------|------------|-----------------|
| 1 | "创建订单" | "请提供产品信息" |
| 2 | "产品 X100" | "请提供数量" |
| 3 | "500 件" | "请提供交货日期" |
| 4 | "3月15日" | "请确认：订单 X100, 500件, 3月15日" |

---

#### TC-DL-009: Intent Error Handling

**Objective**: System handles misunderstanding gracefully

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Send ambiguous input | System asks for clarification |
| 2 | Provide correction | System adjusts |
| 3 | Intent completely wrong | System suggests alternatives |

---

### 3.3 Execution Engine

#### TC-DL-010: Execution Routing

**Objective**: Verify intents route to correct services

**Test Matrix**:
| Intent | Expected Service |
|--------|------------------|
| query_stock | InventoryService |
| create_order | OrderService |
| run_mrp | MrpService |
| query_forecast | ForecastService |

---

#### TC-DL-011: Permission Validation

**Objective**: Verify unauthorized operations blocked

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login as user without permission | Limited access |
| 2 | Try to create order | Operation allowed |
| 3 | Try to delete data | Blocked with error |
| 4 | Try to view reports | Access denied |

---

#### TC-DL-012: Transaction Atomicity

**Objective**: Verify operations are atomic - all or nothing

**Test Scenario**: Create order + reserve inventory
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Create order that requires inventory | Order created, inventory reserved |
| 2 | Fail at second step (e.g., BOM not found) | Order rolled back, inventory not reserved |

---

#### TC-DL-013: Execution Logging

**Objective**: Verify all operations logged

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute operation via dialog | Log entry created |
| 2 | Query logs | Operation recorded |
| 3 | Verify log format | Contains: user, action, timestamp, result |

---

### 3.4 Result Presentation

#### TC-DL-014: Natural Language Response

**Objective**: Verify results presented in natural language

**Test Scenarios**:
| Query | Expected Response Format |
|-------|-------------------------|
| "查一下库存" | "当前共有 X 个物料，总价值 ¥Y" |
| "订单进度" | "订单 001 已完成生产，等待发货" |
| "MRP 结果" | "共生成 X 条采购建议，Y 条生产建议" |

---

#### TC-DL-015: Data Table Display

**Objective**: Verify data tables render correctly

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Query list data | Table displays |
| 2 | Sort column | Sorted correctly |
| 3 | Filter data | Filtered correctly |
| 4 | Paginate | Pages correctly |

---

#### TC-DL-016: Chart Visualization

**Objective**: Verify charts render for data analysis

**Test Scenarios**:
| Query | Expected Chart |
|-------|----------------|
| "库存分布" | Pie chart |
| "需求趋势" | Line chart |
| "订单对比" | Bar chart |

---

#### TC-DL-017: Next Step Suggestions

**Objective**: Verify system suggests next actions

**Test Scenario**:
| Step | User Action | System Suggests |
|------|-------------|-----------------|
| 1 | Run MRP | "查看采购建议" / "查看生产建议" |
| 2 | Check inventory | "创建采购订单" / "调整安全库存" |
| 3 | Create order | "运行 MRP" / "查看 BOM" |

---

## 4. Core Business Layer Tests (P0)

### 4.1 Demand Management - Sales Orders

#### TC-DM-001: Create Sales Order via Dialog

**Objective**: Create order using natural language

**Test Dialog**:
```
User: "创建一张订单：客户华为，产品 X100，数量 500，3月15日交货"

Expected Response:
"好的，为您创建订单：
- 客户：华为
- 产品：X100
- 数量：500
- 交货日期：2026-03-15

订单已创建，编号：SO20260314001，是否确认？"
```

---

#### TC-DM-002: Query Orders

**Objective**: Query orders using various filters

**Test Scenarios**:
| Query | Expected Result |
|-------|-----------------|
| "查一下本周有哪些订单" | Orders due this week |
| "订单 20260308-001 进展如何" | Specific order status |
| "显示所有 Pending 订单" | Orders with PENDING status |
| "华为的订单" | Orders for customer 华为 |

---

#### TC-DM-003: Modify Order

**Objective**: Modify existing order via dialog

**Test Dialog**:
```
User: "把订单 001 的数量改到 600"

System: "确认修改订单 001：
- 原数量：500
- 新数量：600

修改后将重新计算 MRP，是否确认？"
```

---

#### TC-DM-004: Cancel Order

**Objective**: Cancel order with impact analysis

**Test Dialog**:
```
User: "取消订单 002"

System: "确认取消订单 002？
影响：
- 采购建议 3 条将被取消
- 库存预留将释放
- 生产工单 1 条将被取消

是否确认取消？"
```

---

#### TC-DM-005: Order Import (P1)

**Objective**: Import orders from Excel

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Upload Excel file | File validated |
| 2 | Preview data | Data shown in table |
| 3 | Confirm import | Orders created |
| 4 | Verify | Orders in system |

---

#### TC-DM-006: Order API Sync (P1)

**Objective**: Sync orders from external ERP

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Trigger ERP sync | Sync initiated |
| 2 | Check progress | Progress shown |
| 3 | Complete sync | New orders imported |
| 4 | Verify | Orders in system |

---

### 4.2 Demand Management - Sales Forecast

#### TC-DM-007: AI Demand Forecast

**Objective**: Generate AI demand forecast

**Test Dialog**:
```
User: "预测一下下个月的销量"

System: "请选择要预测的产品："
[Shows product list]

User: "预测 X100"

System: "基于历史数据分析：
- 预测方法：Prophet
- 预测期间：2026-03-15 至 2026-04-14
- 预测销量：1250 件
- 置信区间：1100-1400 件

[Shows forecast chart]
是否应用到 MRP？"
```

---

#### TC-DM-008: Manual Forecast Entry

**Objective**: Manually enter forecast values

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | "录入预测：产品 A 下月 1000 件" | Forecast created |
| 2 | View forecast | Shows in list |
| 3 | Modify forecast | Updated |

---

#### TC-DM-009: Forecast vs Actual Comparison

**Objective**: Compare forecast with actual demand (P2)

**Test Dialog**:
```
User: "预测和实际差异多大？"

System: "预测 vs 实际对比：
| 产品 | 预测 | 实际 | 差异 |
|------|------|------|------|
| X100 | 1250 | 1180 | -5.6% |
| Y200 | 800 | 850 | +6.2% |

整体 MAPE：8.5%"
```

---

### 4.3 Demand Management - Demand Pool

#### TC-DM-010: View Demand Pool

**Objective**: View consolidated demand pool

**Test Dialog**:
```
User: "看看最近的需求"

System: "当前需求池（共 15 条）：

【销售订单】5 条
- SO001: X100, 500件, 3/20
- SO002: Y200, 300件, 3/22
...

【销售预测】10 条
- X100: 1250件, 4月
- ...
```

---

#### TC-DM-011: Priority Adjustment

**Objective**: Adjust demand priorities

**Test Dialog**:
```
User: "把 A 订单优先级调到最高"

System: "已将订单 A 优先级调整为 P1（最高）

当前优先级分布：
- P1: 3 个订单
- P2: 8 个订单
- P3: 4 个订单"
```

---

### 4.4 BOM Management

#### TC-DM-012: Create BOM

**Objective**: Create bill of materials

**Test Dialog**:
```
User: "为产品 X 创建 BOM"

System: "请输入 BOM 信息：
[Shows BOM creation form]

User: "添加子件：物料 A，用量 2"

System: "已添加：
- 子件：A
- 用量：2
- 损耗率：0%

继续添加或确认？"
```

---

#### TC-DM-013: View BOM Structure

**Objective**: View multi-level BOM

**Test Dialog**:
```
User: "看看 X 产品的 BOM"

System: "X 产品 BOM 结构：

┌─ X (1)
│  ├─ A (2)
│  │  └─ A1 (1)
│  │     └─ A1-1 (3)
│  └─ B (3)
│     └─ B1 (2)

总层级：3 层
总子件：6 种"
```

---

#### TC-DM-014: Modify BOM

**Objective**: Modify BOM quantities

**Test Dialog**:
```
User: "把 A 物料的用量改成 3"

System: "确认修改：
- 产品：X
- 子件：A
- 原用量：2
- 新用量：3

是否确认？"
```

---

#### TC-DM-015: Multi-Level Explosion

**Objective**: Verify 3+ level BOM explosion

**Test Data**:
```
Level 1: PRODUCT_A = 10
Level 2: SUB_ASSEMBLY = 2 × PRODUCT_A = 20
Level 3: COMPONENT = 3 × SUB_ASSEMBLY = 60
```

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Run MRP for PRODUCT_A, qty=10 | BOM exploded to 3 levels |
| 2 | Check COMPONENT requirement | 60 units (10×2×3) |
| 3 | Verify suggestions | Purchase suggestions for COMPONENT |

---

#### TC-DM-016: BOM Cost Calculation (P1)

**Objective**: Calculate product cost from BOM

**Test Dialog**:
```
User: "这个产品成本多少？"

System: "X 产品成本明细：
- A: 20 × ¥5 = ¥100
- B: 30 × ¥8 = ¥240
- 加工费：¥50
--------------
总成本：¥390"
```

---

### 4.5 Inventory Management

#### TC-DM-017: Query Inventory

**Objective**: Query inventory via dialog

**Test Dialog**:
```
User: "A 物料还有多少？"

System: "物料 A 当前库存：
- 现有量：150 件
- 预留量：30 件
- 可用量：120 件
- 安全库存：50 件
- 库位：仓库1-A-01"
```

---

#### TC-DM-018: Multi-Item Query

**Objective**: Query multiple items at once

**Test Dialog**:
```
User: "看看这几个物料的库存：A、B、C"

System: "物料库存汇总：
| 物料 | 现有量 | 可用量 | 安全库存 | 状态 |
|------|--------|--------|----------|------|
| A | 150 | 120 | 50 | 正常 |
| B | 30 | 10 | 20 | 不足 |
| C | 500 | 480 | 100 | 正常 |"
```

---

#### TC-DM-019: Inventory Receipt

**Objective**: Record inventory receipt

**Test Dialog**:
```
User: "入库 100 个物料 A"

System: "确认入库：
- 物料：A
- 数量：100
- 类型：采购入库
- 仓库：默认仓库

是否确认？"
```

---

#### TC-DM-020: Inventory Issue

**Objective**: Record inventory issue

**Test Dialog**:
```
User: "出库 20 个物料 B"

System: "确认出库：
- 物料：B
- 数量：20
- 类型：生产领料
- 工单：WO001

是否确认？"
```

---

#### TC-DM-021: Inventory Transfer (P1)

**Objective**: Transfer inventory between warehouses

**Test Dialog**:
```
User: "调拨 50 个 A 从仓库 1 到仓库 2"

System: "确认调拨：
- 物料：A
- 数量：50
- 从：仓库 1
- 到：仓库 2

是否确认？"
```

---

#### TC-DM-022: Inventory Alert

**Objective**: System alerts on low stock

**Test Scenario**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Inventory drops below safety | Alert triggered |
| 2 | Check alerts | Shows affected items |
| 3 | Take action | Can create purchase from alert |

---

#### TC-DM-023: Inventory Age Analysis (P1)

**Objective**: Analyze inventory age

**Test Dialog**:
```
User: "哪些物料库存超过 3 个月了？"

System: "库龄分析（>90天）：
| 物料 | 数量 | 库龄 | 金额 |
|------|------|------|------|
| D | 100 | 95天 | ¥500 |
| E | 50 | 120天 | ¥200 |

呆滞风险：高"
```

---

### 4.6 MRP Calculation

#### TC-DM-024: Run MRP

**Objective**: Execute MRP calculation

**Test Dialog**:
```
User: "跑一下 MRP"

System: "MRP 计算中...
✓ 已处理 15 个需求
✓ 已展开 BOM 25 个
✓ 已匹配库存 50 个

结果：
- 采购建议：8 条
- 生产建议：3 条
- 缺料预警：2 条

是否查看详情？"
```

---

#### TC-DM-025: Time-Phased MRP

**Objective**: Run MRP by time bucket

**Test Dialog**:
```
User: "按周跑 MRP"

System: "MRP 结果（按周）：

【第12周】2026-03-16 ~ 2023-03-22
- 采购建议：3 条
- 生产建议：1 条

【第13周】2026-03-23 ~ 2026-03-29
- 采购建议：5 条
..."
```

---

#### TC-DM-026: MRP by Product

**Objective**: Run MRP for specific product (P1)

**Test Dialog**:
```
User: "只算产品 X 的 MRP"

System: "MRP 结果 - 产品 X：

需求：500 件（订单 SO001）
BOM 展开：
- A: 1000 件 → 库存 200 → 建议采购 800
- B: 1500 件 → 库存 500 → 建议采购 1000

采购建议：
| 物料 | 数量 | 交期 |
|------|------|------|
| A | 800 | 3/20 |
| B | 1000 | 3/22 |"
```

---

#### TC-DM-027: Demand Traceability

**Objective**: Trace requirement source

**Test Dialog**:
```
User: "看看这个采购建议是哪里来的"

System: "采购建议追溯：

【建议】物料 A, 800件, 3/20

来源追溯：
└─ 订单 SO001: X100, 500件, 3/25
   └─ BOM 展开
      └─ 子件 A: 2 × 500 = 1000 件
         └─ 库存：200 件
            └─ 缺口：800 件"
```

---

#### TC-DM-028: Purchase Suggestion Review

**Objective**: Review and approve purchase suggestions

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Run MRP | Suggestions generated |
| 2 | Review suggestions | List with details |
| 3 | Approve suggestion | Status changes to APPROVED |
| 4 | Generate PO | PO created |

---

#### TC-DM-029: Production Suggestion Review

**Objective**: Review and create production orders

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Run MRP | Production suggestions shown |
| 2 | Review suggestions | List with details |
| 3 | Generate work order | WO created |
| 4 | Schedule production | WO scheduled |

---

#### TC-DM-030: Shortage Analysis

**Objective**: Analyze material shortages

**Test Dialog**:
```
User: "看看有没有缺料"

System: "缺料分析：

【严重缺料】2 项
| 物料 | 需要 | 现有 | 缺口 | 影响订单 |
|------|------|------|------|----------|
| A | 800 | 200 | 600 | SO001 |
| B | 500 | 0 | 500 | SO001 |

【轻微缺料】3 项
..."
```

---

### 4.7 Purchase Management

#### TC-DM-031: Create Supplier

**Objective**: Add new supplier

**Test Dialog**:
```
User: "添加一个供应商：某某公司"

System: "请输入供应商信息：
[Shows supplier form]

User: "名称：某某公司，联系人：张三，电话：13800138000"

System: "供应商已创建：
- 编码：SUP001
- 名称：某某公司
- 联系人：张三
- 电话：13800138000"
```

---

#### TC-DM-032: Supplier Query

**Objective**: Query supplier information

**Test Dialog**:
```
User: "我们有哪些供应商？"

System: "供应商列表（共 10 家）：

| 编码 | 名称 | 联系人 | 交期 | 状态 |
|------|------|--------|------|------|
| SUP001 | 某某公司 | 张三 | 7天 | 正常 |
| SUP002 | 某某厂 | 李四 | 5天 | 正常 |
..."
```

---

#### TC-DM-033: Set Supplier Lead Time

**Objective**: Configure supplier delivery time

**Test Dialog**:
```
User: "设置供应商 A 交期 10 天"

System: "确认修改：
- 供应商：A
- 原交期：7 天
- 新交期：10 天

是否确认？"
```

---

#### TC-DM-034: Generate Purchase Order

**Objective**: Create PO from MRP suggestions

**Test Dialog**:
```
User: "根据采购建议生成采购单"

System: "待生成采购单（5 条）：

| 物料 | 数量 | 供应商 | 建议交期 |
|------|------|--------|----------|
| A | 800 | 供应商A | 3/20 |
| B | 1000 | 供应商B | 3/22 |

[Select all] [Select none] [Customize]

确认生成？"
```

---

#### TC-DM-035: View Purchase In-Transit

**Objective**: Query purchase orders in transit

**Test Dialog**:
```
User: "有哪些采购在途中？"

System: "在途采购订单（8 条）：

| PO号 | 物料 | 数量 | 预计到货 | 状态 |
|------|------|------|----------|------|
| PO001 | A | 500 | 3/15 | 运输中 |
| PO002 | B | 300 | 3/18 | 已发货 |
..."
```

---

#### TC-DM-036: Goods Receipt

**Objective**: Record incoming goods

**Test Dialog**:
```
User: "物料 A 到了，入库"

System: "找到在途采购：
- PO001: 物料 A, 500件, 预计 3/15

确认入库数量：[500]
(可部分入库)"
```

---

### 4.8 Production Management

#### TC-DM-037: Generate Work Order

**Objective**: Create work order from suggestions

**Test Dialog**:
```
User: "根据建议生成工单"

System: "待生成工单（3 条）：

| 产品 | 数量 | 建议开始 | 建议完成 |
|------|------|----------|----------|
| X100 | 500 | 3/15 | 3/20 |
| Y200 | 300 | 3/18 | 3/22 |

确认生成？"
```

---

#### TC-DM-038: Create Work Order

**Objective**: Manually create work order

**Test Dialog**:
```
User: "创建工单：产品 X，500 件"

System: "确认创建工单：
- 产品：X
- 数量：500 件
- 计划开始：2026-03-15
- 计划完成：2026-03-20

是否确认？"
```

---

#### TC-DM-039: Production Reporting (P1)

**Objective**: Report production progress

**Test Dialog**:
```
User: "工单 001 报工，完成 100 件"

System: "确认报工：
- 工单：WO001
- 完成数量：100 件
- 不良品：0 件
- 报工人：张三

是否确认？"
```

---

#### TC-DM-040: Work Order Completion

**Objective**: Complete work order and receive goods

**Test Dialog**:
```
User: "工单 001 完工入库"

System: "工单 WO001 当前状态：
- 计划数量：500
- 已完成：500
- 已入库：0

确认完工入库？"
```

---

## 5. AI Engine Layer Tests (P1)

### 5.1 LLM Understanding

#### TC-AI-001: Natural Language Parsing

**Objective**: Verify LLM understands complex queries

**Test Scenarios**:
| Input | Parsed Intent | Entities |
|-------|---------------|----------|
| "帮我看看明天要出的货" | query_orders | {time: tomorrow, status: pending} |
| "哪个物料快用完了" | query_low_stock | {} |
| "把 X 产品的安全库存改成 100" | update_safety_stock | {item: X, qty: 100} |

---

#### TC-AI-002: Context Management

**Objective**: Verify multi-turn context

**Test Flow**:
```
Turn 1:
User: "查一下 ITEM001"
System: Returns ITEM001 info, saves to context

Turn 2:
User: "够不够?"
System: Uses context, analyzes if 库存 >= 需求

Turn 3:
User: "那显示订单"
System: Shows orders (new query, not using context)
```

---

#### TC-AI-003: Knowledge Q&A (P1)

**Objective**: Answer MRP-related questions

**Test Scenarios**:
| Question | Expected Answer |
|----------|-----------------|
| "什么是 MRP?" | "MRP（物料需求计划）是一种..." |
| "安全库存怎么算?" | "安全库存的计算方法有..." |
| "BOM 是什么?" | "BOM（物料清单）是..." |

---

#### TC-AI-004: Intent Recommendation

**Objective**: System recommends next actions

**Test Scenario**:
```
After user runs MRP:
System: "建议：
1. 查看采购建议
2. 调整安全库存
3. 运行排程优化"
```

---

### 5.2 OR Solver

#### TC-AI-005: Scheduling with Multiple Constraints

**Objective**: Optimize schedule with constraints

**Test Constraints**:
- Resource capacity
- Order priorities
- Delivery dates
- Setup times

**Expected Result**: Valid schedule meeting all constraints

---

#### TC-AI-006: Batch Size Optimization (P1)

**Objective**: Find optimal batch sizes

**Test Dialog**:
```
User: "帮我优化一下批量"

System: "批量优化分析：

当前设置：
- 产品 X: 批量 500
- 产品 Y: 批量 300

优化建议：
- 产品 X: 批量 450（节省 10% 成本）
- 产品 Y: 批量 350（减少换线次数）

是否应用？"
```

---

#### TC-AI-007: Safety Stock Optimization (P1)

**Objective**: AI recommends optimal safety stock

**Test Dialog**:
```
User: "帮我优化一下 MRP 参数"

System: "AI 参数优化建议：

【安全库存】
- ITEM001: 当前 50 → 建议 80（基于需求波动分析）
- ITEM002: 当前 30 → 建议 40（基于交期波动分析）

【批量规则】
- ITEM001: 建议改用 LEAST_LOT（当前 FIXED）

应用建议？"
```

---

### 5.3 Prediction Analysis

#### TC-AI-008: Demand Forecasting - All Methods

**Objective**: Test all forecasting algorithms

**Methods to Test**:
| Method | Test Data | Verification |
|--------|------------|--------------|
| Prophet | 30+ days historical | Trend + seasonality |
| LSTM | Time series | Deep learning prediction |
| ARIMA | Stationary data | Statistical accuracy |
| XGBoost | With features | Feature importance |
| Moving Average | Simple data | Baseline comparison |
| Exponential Smoothing | With trend | Weighted historical |

---

#### TC-AI-009: Supplier Delivery Prediction (P1)

**Objective**: Predict supplier delivery times

**Test Dialog**:
```
User: "供应商 A 最近交期怎么样？"

System: "供应商 A 交期分析：

历史表现：
- 平均交期：7.2 天
- 准时率：85%
- 延迟波动：±2 天

预测：
- 下单后预计 8 天到达
- 风险等级：中等

建议：提前 2 天下单"
```

---

#### TC-AI-010: Inventory Level Prediction (P1)

**Objective**: Predict future inventory levels

**Test Steps**:
| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Query inventory prediction | Shows future levels |
| 2 | View confidence interval | Range shown |
| 3 | Check alerts | Low stock warnings |

---

#### TC-AI-011: Safety Stock Recommendation

**Objective**: AI recommends safety stock

**Test Data**:
```json
{
  "item_code": "ITEM001",
  "service_level": 0.95,
  "lead_time_days": 7,
  "historical_demand": [...]
}
```

**Verification**:
- [ ] Statistical method calculated
- [ ] Service level method calculated
- [ ] ML method calculated
- [ ] Best method recommended

---

#### TC-AI-012: Risk Prediction

**Objective**: Predict supply chain risks

**Test Dialog**:
```
User: "有什么风险需要关注？"

System: "风险预测：

【高风险】
1. 供应商 A：延迟概率 70%
   - 影响：3 个采购订单
   - 建议：寻找替代供应商

2. 物料 B：缺货概率 60%
   - 当前库存：20
   - 需求：150
   - 建议：紧急采购

【中风险】
..."
```

---

### 5.4 Anomaly Diagnosis

#### TC-AI-013: Shortage Diagnosis

**Objective**: Diagnose root cause of shortages

**Test Dialog**:
```
User: "为什么又缺料了？"

System: "缺料原因分析：

1️⃣ 供应商交期延迟（主因）
   - 供应商 A：延误 3 天
   - 影响：采购订单 PO001

2️⃣ 安全库存偏低
   - 当前：50
   - 建议：100

3️⃣ 需求预测偏差
   - 预测：800
   - 实际：920 (+15%)

建议：调整安全库存至 100，可避免 80% 类似缺料"
```

---

#### TC-AI-014: Order Delay Diagnosis

**Objective**: Analyze why orders are delayed

**Test Dialog**:
```
User: "为什么 A 订单会延期？"

System: "订单延期原因分析：

【订单 A】原定 3/15 → 预计 3/18

延期原因：
1. 产能不足（权重 60%）
   - 车间利用率 95%
   - 插单影响

2. 物料延迟（权重 30%）
   - 物料 B 短缺 2 天

3. 设备故障（权重 10%）
   - 3/12 停机 4 小时

建议：调整产能或协商交期"
```

---

#### TC-AI-015: Inventory Anomaly Detection (P1)

**Objective**: Detect abnormal inventory patterns

**Test Scenarios**:
| Anomaly Type | Detection Criteria |
|--------------|-------------------|
| Sudden spike | Daily change > 200% |
| Slow-moving | No movement > 60 days |
| Excessive | Stock > 180 days usage |
| Circuitous | Multiple location moves |

---

### 5.5 Risk Warning System

#### TC-AI-016: Supplier Risk Monitoring

**Objective**: Monitor supplier delivery risks

**Warning Trigger**:
| Condition | Warning Level |
|-----------|---------------|
| Delay > 2 days | Yellow |
| Delay > 5 days | Orange |
| Delay > 7 days | Red |

---

#### TC-AI-017: Inventory Risk Alert

**Objective**: Alert when inventory below safety stock

**Test Scenario**:
| Step | Action | Expected |
|------|--------|----------|
| 1 | Inventory drops below safety | Alert triggered |
| 2 | View alerts | Shows affected items |
| 3 | Take action | Quick purchase creation |

---

#### TC-AI-018: Demand Spike Alert

**Objective**: Alert on unexpected demand changes

**Test Dialog**:
```
User: "需求突然变化了有没有影响？"

System: "需求突变检测：

【产品 X】
- 预测：1000 件/月
- 实际：1350 件（本月）
- 突变：+35%

影响分析：
- 产能利用率：80% → 108%
- 需要加班或外协

建议：立即调整生产计划"
```

---

#### TC-AI-019: Auto Notification

**Objective**: System auto-notifies on risks

**Test Steps**:
| Step | Action | Expected |
|------|--------|----------|
| 1 | Risk detected | Notification sent |
| 2 | Check notification | Shows risk details |
| 3 | Configure channels | Email/SMS/Webhook |

---

#### TC-AI-020: One-Click Risk Mitigation

**Objective**: Generate risk mitigation plan

**Test Dialog**:
```
User: "帮我生成一个规避方案"

System: "风险规避方案：

【方案 A：紧急采购】
- 成本：¥5,000
- 交期：3 天
- 风险降低：90%

【方案 B：寻找替代物料】
- 成本：¥3,500
- 交期：5 天
- 风险降低：80%

【方案 C：调整计划】
- 成本：¥1,000
- 交期：0 天
- 风险降低：50%

推荐方案 A，是否执行？"
```

---

### 5.6 Impact Analysis

#### TC-AI-021: Rush Order Impact Analysis

**Objective**: Analyze impact of inserting urgent order

**Test Dialog** (from PRD):
```
User: "有个急单要插进来，500 件，优先级最高。"

System: "📊 初步分析结果：

1️⃣ 产能影响
   - 当前利用率：85%
   - 插入后：98%
   - ⚠️ 需要加班 2 小时

2️⃣ 订单影响
   - 订单 A（3/15）→ 延后 2 天
   - 订单 B（3/18）→ 延后 1 天

3️⃣ 物料影响
   - 物料 X 缺 200 件 → 需紧急采购
   - 成本：¥1,500

4️⃣ 成本影响
   - 加班：¥800
   - 急采：¥1,500
   - 换线：¥200
   - 总计：¥2,500

💡 建议方案：
   方案 A：接受插单（¥2,500）
   方案 B：拒绝插单（¥0）
   方案 C：部分满足（¥1,000）

请选择？"
```

---

#### TC-AI-022: Order Change Impact

**Objective**: Analyze impact of changing order quantity

**Test Dialog**:
```
User: "客户要把数量从 500 改成 800，有什么影响？"

System: "变更影响分析：

【数量变更】500 → 800 (+60%)

📊 全面影响：

1️⃣ 产能
   - 原需求：500件 → 8小时
   - 新需求：800件 → 13小时
   - 超出产能：5小时

2️⃣ 物料
   - 物料 A：+600 件
   - 物料 B：+900 件

3️⃣ 交期
   - 原定：3/20
   - 新定：3/25 (+5天)

4️⃣ 成本
   - 额外材料：¥2,000
   - 额外人工：¥500
   - 总计：¥2,500"
```

---

#### TC-AI-023: BOM Change Impact (P1)

**Objective**: Analyze impact of changing BOM

**Test Dialog**:
```
User: "换了一个原材料，对成本有什么影响？"

System: "BOM 变更影响：

【变更】物料 C → 物料 D

成本影响：
- 原成本：¥100/件
- 新成本：¥85/件
- 单价变化：-15%

批量影响：
- 年度用量：10,000 件
- 年度节省：¥150,000

其他影响：
- 供应商：新
- 交期：相同
- 质量：待确认"
```

---

#### TC-AI-024: One-Click Execution

**Objective**: Execute recommended solution

**Test Dialog**:
```
User: "按方案 A 执行"

System: "正在执行方案 A...

✓ 订单优先级已调整
✓ 加班安排已生成
✓ 紧急采购已创建
✓ 成本记录已更新

执行完成！
- 新订单交期：3/22
- 额外成本：¥2,500"
```

---

### 5.7 What-If Simulation

#### TC-AI-025: Demand Change Scenario

**Objective**: Simulate demand variations

**Test Dialog**:
```
User: "如果需求增加 20% 会怎样？"

System: "What-if 分析：

【基准】需求 1000 件/月
【场景】需求 +20% = 1200 件/月

📊 影响分析：

1️⃣ 产能
   - 利用率：80% → 96%
   - 需要：加班或外协

2️⃣ 成本
   - 加班费用：+¥2,000/月
   - 急采费用：+¥1,500/月

3️⃣ 库存
   - 安全库存建议：+20%

4️⃣ 交期
   - 按时交付率：95% → 85%

💡 建议：
   - 短期：加班处理
   - 中期：增加产能
   - 长期：优化需求预测"
```

---

#### TC-AI-026: Scenario Comparison

**Objective**: Compare multiple scenarios

**Test Dialog**:
```
User: "方案 A vs 方案 B，哪个更好？"

System: "方案对比分析：

| 指标 | 方案A | 方案B | 最优 |
|------|-------|-------|------|
| 总成本 | ¥50,000 | ¥48,000 | B |
| 交期 | 3/20 | 3/25 | A |
| 产能利用率 | 95% | 85% | B |
| 风险等级 | 中 | 低 | B |

🏆 综合推荐：方案 B

理由：成本低 ¥2,000，风险更低"
```

---

#### TC-AI-027: Multi-Scenario Recommendation (P1)

**Objective**: Get multiple solution options

**Test Dialog**:
```
User: "给我几个可选方案"

System: "可选方案：

【方案 1：最短交期】
- 交期：3/18
- 成本：¥55,000
- 产能：需加班

【方案 2：最低成本】
- 交期：3/25
- 成本：¥45,000
- 产能：正常

【方案 3：平衡】
- 交期：3/22
- 成本：¥48,000
- 产能：略紧

请选择？"
```

---

#### TC-AI-028: Sensitivity Analysis (P2)

**Objective**: Identify most impactful factors

**Test Dialog**:
```
User: "哪个因素影响最大？"

System: "敏感度分析：

影响因子排序：
1. 需求波动：影响度 45%
2. 供应商交期：影响度 30%
3. 产能利用率：影响度 15%
4. 价格变动：影响度 10%

建议重点关注：
- 需求预测准确性
- 供应商可靠性"
```

---

## 6. Data Layer Tests

### 6.1 Master Data

#### TC-DATA-001: Item Master Management

**Test Scenarios**:
| Operation | Dialog | Verification |
|-----------|--------|--------------|
| Create | "添加物料：编码 A01，名称 螺丝" | Created |
| Query | "查一下物料 A01" | Details shown |
| Update | "修改物料 A01，单价改成 5" | Updated |
| Delete | "删除物料 A01" | Soft deleted |

---

#### TC-DATA-002: Supplier Master

**Test Scenarios**:
| Operation | Verification |
|-----------|--------------|
| Create supplier | Fields: name, contact, phone, lead time |
| Query suppliers | List with filters |
| Update lead time | Affects MRP calculations |
| Link items | Item-supplier relationship |

---

#### TC-DATA-003: Warehouse Management

**Test Scenarios**:
| Operation | Verification |
|-----------|--------------|
| Create warehouse | Warehouse created |
| Manage locations | Location hierarchy |
| Transfer | Between warehouses |

---

#### TC-DATA-004: Routing Management

**Test Scenarios**:
| Operation | Verification |
|-----------|--------------|
| Create routing |工序+设备+工时 |
| Link to item | Item-routing relationship |
| Use in scheduling | Affects production planning |

---

## 7. Sandbox Tests (P1)

### 7.1 Sandbox Entry

#### TC-SB-001: Enter Sandbox

**Test Dialog**:
```
User: "进入沙箱"

System: "已进入沙箱环境 🏖️

沙箱说明：
- 所有操作不会影响生产数据
- 计算结果仅供参考
- 确认后可应用到生产

当前沙箱数据：基于 2026-03-14 生产数据
[沙箱操作记录]"
```

---

#### TC-SB-002: MRP Preview

**Test Dialog**:
```
User: "预览一下 MRP 结果"

System: "MRP 预览（沙箱）：

⚠️ 预览结果，不会执行

采购建议：8 条
生产建议：3 条
缺料预警：2 条

[查看详情] [应用到生产] [返回]"
```

---

#### TC-SB-003: Parameter Simulation

**Test Dialog**:
```
User: "如果安全库存改成 150 会怎样？"

System: "参数模拟（沙箱）：

【变更】安全库存：50 → 150

📊 影响分析：

1️⃣ 库存
   - 当前库存：¥500,000
   - 变更后：¥800,000
   - 增加：¥300,000

2️⃣ 成本
   - 持有成本：+¥60,000/年

3️⃣ 缺货率
   - 当前：5%
   - 变更后：1%

是否应用到生产？"
```

---

#### TC-SB-004: Compare Solutions

**Test Dialog**:
```
User: "比较一下三种批量策略"

System: "方案对比（沙箱）：

| 策略 | 批量数 | 总成本 | 交期 | 缺货率 |
|------|--------|--------|------|--------|
| A: Fixed 500 | 20 | ¥50,000 | 3/20 | 3% |
| B: LEAST | 18 | ¥48,000 | 3/22 | 5% |
| C: EOQ | 15 | ¥45,000 | 3/25 | 8% |

推荐：方案 A（平衡）"
```

---

#### TC-SB-005: Apply to Production

**Test Dialog**:
```
User: "确认，执行这个方案"

System: "⚠️ 确认将沙箱方案应用到生产？

变更内容：
- 安全库存：50 → 150
- 批量规则：FIXED → LEAST

应用后无法撤销，确认？"
```

---

#### TC-SB-006: Sandbox History

**Test Dialog**:
```
User: "看看之前的模拟记录"

System: "沙箱历史记录：

| 时间 | 操作 | 状态 |
|------|------|------|
| 3/14 10:00 | 安全库存模拟 | 已应用 |
| 3/14 09:30 | 批量策略对比 | 未应用 |
| 3/13 15:00 | MRP预览 | 已应用 |

[查看详情] [重新执行]"
```

---

## 8. Non-Functional Tests

### 8.1 Performance Tests

| Metric | Target | Test Method |
|--------|--------|--------------|
| MRP (1000 demands) | < 10s | Load test |
| MRP recalculation | < 3s | Load test |
| Chat response (simple) | < 3s | Performance test |
| Chat response (complex) | < 10s | Performance test |
| Page load | < 2s | Frontend test |
| Concurrent users | 100+ | Load test |

---

### 8.2 Security Tests

| Test | Description |
|------|-------------|
| JWT validation | All APIs reject invalid tokens |
| Role-based access | Users can only access permitted resources |
| Input validation | SQL injection, XSS prevention |
| Data encryption | Sensitive data encrypted |

---

### 8.3 Availability Tests

| Test | Target |
|------|--------|
| System availability | 99.9% |
| Recovery time | < 30 min |
| Backup | Daily auto |

---

### 8.4 Compatibility Tests

| Environment | Target |
|-------------|--------|
| Chrome | Latest - 1 version |
| Edge | Latest - 1 version |
| Firefox | Latest - 1 version |
| Resolution | 1280×720+ |

---

## 9. Enterprise Feature Tests (P2)

### TC-ENT-001: Multi-Organization (P2)

**Test Scope**:
| Feature | Test |
|---------|------|
| Organization hierarchy | Multiple org units |
| Data isolation | Org-level access |
| Cross-org orders | Multi-org transactions |

---

### TC-ENT-002: Multi-Warehouse (P2)

**Test Scope**:
| Feature | Test |
|---------|------|
| Warehouse transfer | Between warehouses |
| Inventory consolidation | View all warehouses |
| Location management | Bin/location tracking |

---

### TC-ENT-003: MPS+MRP+DRP Linkage (P2)

**Test Scope**:
| Feature | Test |
|---------|------|
| MPS generation | Master production schedule |
| MRP calculation | Material requirements |
| DRP distribution | Distribution requirements |

---

### TC-ENT-004: Supplier Portal (P2)

**Test Scope**:
| Feature | Test |
|---------|------|
| Supplier login | External access |
| Order confirmation | Supplier accepts |
| Delivery scheduling | Supplier proposes |

---

### TC-ENT-005: ERP Integration (P2)

**Test Scope**:
| Feature | Test |
|---------|------|
| API endpoints | RESTful APIs |
| Data sync | Bidirectional sync |
| Error handling | Failed sync recovery |

---

### TC-ENT-006: Reports & Export (P2)

**Test Scope**:
| Feature | Test |
|---------|------|
| Report generation | Various report types |
| Export formats | Excel, PDF |
| Scheduling | Auto-generate reports |

---

## 10. Test Coverage Matrix

### Summary by Layer

| Layer | P0 Tests | P1 Tests | P2 Tests | Total |
|-------|----------|----------|----------|-------|
| Dialogue Layer | 17 | 5 | 0 | 22 |
| Core Business | 28 | 10 | 0 | 38 |
| AI Engine | 15 | 18 | 2 | 35 |
| Data Layer | 4 | 0 | 0 | 4 |
| Sandbox | 3 | 3 | 0 | 6 |
| Non-Functional | 4 | 4 | 2 | 10 |
| Enterprise | 0 | 0 | 6 | 6 |
| **Total** | **71** | **40** | **10** | **121** |

### PRD Feature Coverage

| PRD Section | Test Coverage |
|-------------|----------------|
| 4.2 Dialogue Layer | ✅ Complete |
| 4.3 Core Business | ✅ Complete |
| 4.4 AI Engine | ✅ Complete |
| 4.5 Data Layer | ✅ Complete |
| 5.1 Performance | ✅ Complete |
| 5.2 Availability | ✅ Complete |
| 5.3 Security | ✅ Complete |
| 5.4 Compatibility | ✅ Complete |
| 5.6 Sandbox | ✅ Complete |
| 6.2 Enterprise | ⚠️ P2 Coverage |

---

## Appendix: Test Execution Checklist

### Pre-Execution
- [ ] Test environment deployed
- [ ] Test data loaded
- [ ] Services running
- [ ] Test users configured

### Execution Order
1. Unit tests (per module)
2. API integration tests
3. E2E scenario tests
4. Performance tests
5. Security tests
6. User acceptance tests

### Post-Execution
- [ ] Test results documented
- [ ] Defects logged
- [ ] Coverage report generated
- [ ] Test summary created

---

*Document Version: 2.0*
*Updated: 2026-03-14*
*Based on: PRD.md v1.1*
