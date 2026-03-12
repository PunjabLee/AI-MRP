# M3 阶段开发清单与交付标准

> **阶段**：M3 MRP 核心  
> **周期**：第 5-6 周  
> **目标**：完成 MRP 计算闭环

---

## 一、M3 开发清单

### 任务 1：MRP 计算引擎

| 项目 | 内容 |
|------|------|
| **分支** | `feature/mrp-calculation` |
| **优先级** | P0 |
| **预估工期** | 4 天 |

#### 1.1 功能需求

| 功能 | 描述 | 交付标准 |
|------|------|----------|
| MRP 参数配置 | MRP 计算参数设置 | 可配置提前期、批量规则等 |
| 需求来源合并 | 订单 + 预测 → 合并需求 | 正确合并多个需求来源 |
| BOM 展开 | 多层 BOM 递归展开 | 支持 N 层 BOM 展开 |
| 净需求计算 | 毛需求 - 在途 - 库存 | 计算结果正确 |
| 计划订单生成 | 生成采购/生产建议 | 按批量规则生成建议 |

#### 1.2 交付标准

```
✅ 代码：
   - MRP 计算核心类（MrpCalculator）
   - BOM 展开服务（BomExpander）
   - 需求合并服务（DemandMerger）
   - 计划订单生成器（PlanOrderGenerator）

✅ API：
   - POST /api/mrp/run           执行 MRP
   - GET  /api/mrp/result/{id}  查询结果
   - GET  /api/mrp/parameters    参数查询
   - PUT  /api/mrp/parameters   参数配置

✅ 数据表：
   - t_mrp_run (MRP 执行记录)
   - t_mrp_suggestion (建议记录)

✅ 单元测试：
   - BOM 展开测试
   - 净需求计算测试
```

---

### 任务 2：采购建议生成

| 项目 | 内容 |
|------|------|
| **分支** | `feature/purchase-suggestion` |
| **优先级** | P0 |
| **预估工期** | 2 天 |

#### 2.1 功能需求

| 功能 | 描述 | 交付标准 |
|------|------|----------|
| 采购建议查询 | 查询 MRP 生成的采购建议 | 按物料/供应商筛选 |
| 建议确认 | 确认采购建议生成订单 | 一键转采购订单 |
| 建议拒绝 | 拒绝不合理建议 | 记录拒绝原因 |
| 批量处理 | 批量确认/拒绝建议 | 提升操作效率 |

#### 2.2 交付标准

```
✅ 代码：
   - PurchaseSuggestionService

✅ API：
   - GET  /api/mrp/suggestions/purchase    采购建议列表
   - POST /api/mrp/suggestions/{id}/accept  确认建议
   - POST /api/mrp/suggestions/{id}/reject  拒绝建议
   - POST /api/mrp/suggestions/batch/accept 批量确认

✅ 测试：
   - 建议生成测试
   - 建议转订单测试
```

---

### 任务 3：采购管理

| 项目 | 内容 |
|------|------|
| **分支** | `feature/purchase-management` |
| **优先级** | P0 |
| **预估工期** | 2 天 |

#### 3.1 功能需求

| 功能 | 描述 | 交付标准 |
|------|------|----------|
| 采购订单创建 | 从建议生成或手动创建 | 支持两种方式 |
| 采购订单查询 | 分页列表 + 详情 | 多条件筛选 |
| 采购订单更新 | 修改数量/交期 | 状态校验 |
| 采购订单确认 | 确认生效 | 状态流转正确 |
| 到货接收 | 采购入库 | 更新库存 |

#### 3.2 交付标准

```
✅ 代码：
   - PurchaseOrder 实体
   - PurchaseOrderMapper
   - PurchaseOrderController

✅ API：
   - GET    /api/purchase-orders           采购订单列表
   - GET    /api/purchase-orders/{id}       采购订单详情
   - POST   /api/purchase-orders            创建采购订单
   - PUT    /api/purchase-orders/{id}       更新采购订单
   - POST   /api/purchase-orders/{id}/confirm  确认
   - POST   /api/purchase-orders/{id}/receive   到货接收
   - POST   /api/purchase-orders/{id}/cancel   取消

✅ 数据表：
   - t_purchase_order
   - t_purchase_order_line

✅ 测试：
   - CRUD 测试
   - 状态流转测试
```

---

### 任务 4：生产管理

| 项目 | 内容 |
|------|------|
| **分支** | `feature/production-management` |
| **优先级** | P0 |
| **预估工期** | 2 天 |

#### 4.1 功能需求

| 功能 | 描述 | 交付标准 |
|------|------|----------|
| 生产工单创建 | 从 MRP 建议生成或手动创建 | 支持两种方式 |
| 生产工单查询 | 分页列表 + 详情 | 多条件筛选 |
| 生产工单下达 | 下达到车间 | 状态变更 |
| 生产报工 | 报告完成数量 | 记录报工信息 |
| 完工入库 | 生产完成入库 | 更新库存 |

#### 4.2 交付标准

```
✅ 代码：
   - ProductionOrder 实体
   - ProductionOrderMapper
   - ProductionOrderController
   - ProductionReportController

✅ API：
   - GET    /api/production-orders           工单列表
   - GET    /api/production-orders/{id}       工单详情
   - POST   /api/production-orders            创建工单
   - POST   /api/production-orders/{id}/release  下达
   - POST   /api/production-orders/{id}/complete 完工
   - POST   /api/production-orders/{id}/cancel   取消
   - POST   /api/production-reports            报工

✅ 数据表：
   - t_production_order
   - t_production_order_line

✅ 测试：
   - CRUD 测试
   - 报工测试
```

---

## 二、M3 阶段 API 总览

| 模块 | API | 方法 | 功能 |
|------|-----|------|------|
| **MRP** | /api/mrp/run | POST | 执行 MRP |
| | /api/mrp/result/{id} | GET | MRP 结果 |
| | /api/mrp/parameters | GET/PUT | 参数配置 |
| | /api/mrp/suggestions/purchase | GET | 采购建议 |
| | /api/mrp/suggestions/{id}/accept | POST | 确认建议 |
| | /api/mrp/suggestions/{id}/reject | POST | 拒绝建议 |
| **采购** | /api/purchase-orders | GET/POST | 订单列表/创建 |
| | /api/purchase-orders/{id} | GET/PUT/DELETE | 订单详情/更新/删除 |
| | /api/purchase-orders/{id}/confirm | POST | 确认 |
| | /api/purchase-orders/{id}/receive | POST | 到货接收 |
| **生产** | /api/production-orders | GET/POST | 工单列表/创建 |
| | /api/production-orders/{id}/release | POST | 下达 |
| | /api/production-orders/{id}/complete | POST | 完工 |
| | /api/production-reports | POST | 报工 |

---

## 三、M3 数据模型

### 3.1 MRP 运行记录

```sql
CREATE TABLE t_mrp_run (
    id BIGSERIAL PRIMARY KEY,
    run_no VARCHAR(50) NOT NULL,
    run_type VARCHAR(20),  -- MANUAL/AUTO
    status VARCHAR(20),      -- RUNNING/COMPLETED/FAILED
    total_demands INT,
    total_suggestions INT,
    run_time_ms BIGINT,
    created_by VARCHAR(50),
    created_at TIMESTAMP
);
```

### 3.2 MRP 建议

```sql
CREATE TABLE t_mrp_suggestion (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT,
    suggestion_type VARCHAR(20),  -- PURCHASE/PRODUCTION
    item_code VARCHAR(50),
    suggest_qty DECIMAL(18,6),
    due_date DATE,
    status VARCHAR(20),  -- PENDING/ACCEPTED/REJECTED
    created_at TIMESTAMP
);
```

### 3.3 采购订单

```sql
CREATE TABLE t_purchase_order (
    id BIGSERIAL PRIMARY KEY,
    po_no VARCHAR(50),
    supplier_code VARCHAR(50),
    status VARCHAR(20),  -- DRAFT/CONFIRMED/RECEIVED/CANCELLED
    total_amount DECIMAL(18,2),
    created_by VARCHAR(50),
    created_at TIMESTAMP
);
```

### 3.4 生产订单

```sql
CREATE TABLE t_production_order (
    id BIGSERIAL PRIMARY KEY,
    mo_no VARCHAR(50),
    item_code VARCHAR(50),
    plan_qty DECIMAL(18,6),
    status VARCHAR(20),  -- DRAFT/RELEASED/COMPLETED/CANCELLED
    start_date DATE,
    end_date DATE,
    created_by VARCHAR(50),
    created_at TIMESTAMP
);
```

---

## 四、里程碑

| 里程碑 | 时间 | 交付 |
|--------|------|------|
| M3.1 | 第5天 | MRP 计算引擎 |
| M3.2 | 第7天 | 采购建议 |
| M3.3 | 第9天 | 采购管理 |
| M3.4 | 第10天 | 生产管理 + 集成测试 |

---

## 五、依赖关系

```
MRP 计算引擎 (4d)
    ↓
采购建议生成 (2d)  ← 依赖 MRP 结果
    ↓
采购管理 (2d)      ← 依赖采购建议
    ↓
生产管理 (2d)
```

---

*M3 开发清单完成*
