# 业务逻辑实现备忘录（深度版）

> **日期**：2026-03-09  
> **版本**：2.0  
> **分析深度**：基于代码级详细检查

---

## Pro 阶段进度总结

### 已完成功能（框架级）

| 功能 | 状态 | 业务逻辑差距 |
|------|------|--------------|
| AI 需求预测 | ✅ | 简化，需接入历史订单数据 |
| AI 安全库存 | ✅ | 简化，需接入库存数据 |
| 工艺路线/工作中心/资源 | ✅ | 需完善 CRUD + 接入数据 |
| OR 排程 | ✅ | 简化，需接入工单数据 |
| 插单影响分析 | ✅ | 简化逻辑 |
| 冲突检测 | ✅ | 简化，需接入排程数据 |
| What-if 模拟 | ✅ | 简化，场景未持久化 |
| 风险监控 | ✅ | 简化，需接入库存/供应商数据 |
| 风险预警 | ⚠️ | 仅日志输出，需接入消息服务 |

> **注**：以上功能框架已实现，业务逻辑待完善，见下方详细清单

---

## 一、模块架构总览

### 1.1 后端模块统计

| 模块 | Java文件 | Entity | Controller | Mapper | Service | 状态 |
|------|----------|--------|------------|--------|---------|------|
| demand | 4 | ✅ | ✅ | ✅ | ❌ | 简化 |
| item | 3 | ✅ | ✅ | ✅ | ❌ | 简化 |
| supplier | 3 | ✅ | ✅ | ✅ | ❌ | 简化 |
| bom | 2 | ✅ | ✅ | ❌ | ❌ | 差 |
| inventory | 2 | ✅ | ✅ | ❌ | ❌ | 差 |
| purchase | 4 | ✅ | ✅ | ❌ | ❌ | 差 |
| production | 10 | ✅ | ✅ | ❌ | ✅ | 中 |
| mrp | 12 | ✅ | ✅ | ❌ | ✅ | 中 |
| forecast | 5 | ✅ | ✅ | ❌ | ✅ | 中 |
| risk | 4 | ✅ | ✅ | ❌ | ✅ | 中 |
| whatif | 4 | ✅ | ✅ | ❌ | ✅ | 中 |

---

## 二、MVP 阶段详细分析

### 2.1 需求管理（aimrp-demand）

| 组件 | 文件 | 实现情况 | 详细说明 |
|------|------|----------|----------|
| Entity | SalesOrder.java | ✅ 完整 | 包含主从表结构 |
| Entity | SalesOrderLine.java | ✅ 完整 | 订单明细 |
| Controller | SalesOrderController.java | ⚠️ 直接调用Mapper | CRUD完整，但跳过Service层 |
| Mapper | SalesOrderMapper.java | ✅ 完整 | 继承BaseMapper |
| Service | 无 | ❌ 缺失 | 业务逻辑在Controller |

**差距**：
- 缺少 Service 层（不符合DDD分层）
- 业务逻辑分散在Controller
- 无事务管理

### 2.2 物料管理（aimrp-item）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Entity | Item.java | ✅ 完整 |
| Controller | ItemController.java | ⚠️ 直接调用Mapper |
| Mapper | ItemMapper.java | ✅ 完整 |

**差距**：
- 缺少 Service 层
- 业务逻辑在 Controller

### 2.3 供应商管理（aimrp-supplier）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Entity | Supplier.java | ✅ 完整 |
| Controller | SupplierController.java | ⚠️ 直接调用Mapper |
| Mapper | SupplierMapper.java | ✅ 完整 |

**差距**：
- 缺少 Service 层

### 2.4 BOM 管理（aimrp-bom）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Entity | Bom.java | ✅ 完整 |
| Controller | BomController.java | ⚠️ 简化 |

**差距**：
- 缺少 Mapper
- 缺少 Service
- BOM展开逻辑在 BomExpander（mrp模块）

### 2.5 库存管理（aimrp-inventory）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Entity | Inventory.java | ✅ 完整 |
| Controller | InventoryController.java | ⚠️ 简化 |

**差距**：
- 缺少 Mapper
- 缺少 Service
- 无出入库事务处理

### 2.6 采购管理（aimrp-purchase）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Entity | PurchaseOrder.java | ✅ 完整 |
| Entity | PurchaseOrderLine.java | ✅ 完整 |
| Entity | PurchaseReceive.java | ✅ 完整 |
| Controller | PurchaseOrderController.java | ⚠️ 简化 |

**差距**：
- 缺少 Mapper
- 缺少 Service
- 采购入库逻辑未实现

### 2.7 生产管理（aimrp-production）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Entity | ProductionOrder.java | ✅ 完整 |
| Entity | ProductionReport.java | ✅ 完整 |
| Entity | ProcessRoute.java | ✅ 完整 |
| Entity | ProcessRouteLine.java | ✅ 完整 |
| Entity | WorkCenter.java | ✅ 完整 |
| Entity | Resource.java | ✅ 完整 |
| Entity | MoOperation.java | ✅ 完整 |
| Service | SchedulerService.java | ✅ 完整 |
| Controller | ProductionController.java | ⚠️ 简化 |
| Controller | SchedulerController.java | ✅ 完整 |

**差距**：
- 缺少 Mapper
- 工艺路线CRUD未完善
- 排程需接入真实工单数据

---

## 三、Pro 阶段详细分析

### 3.1 MRP 计算（aimrp-mrp）

| 组件 | 文件 | 实现情况 | 数据来源 |
|------|------|----------|----------|
| Entity | MrpRun.java | ✅ 完整 | - |
| Entity | MrpSuggestion.java | ✅ 完整 | - |
| ValueObject | MrpContext.java | ✅ 完整 | - |
| ValueObject | MrpResult.java | ✅ 完整 | - |
| Service | MrpCalculator.java | ✅ 完整 | 模拟 |
| Service | BomExpander.java | ✅ 完整 | 模拟 |
| Service | DemandMerger.java | ✅ 完整 | 模拟 |
| Service | ImpactAnalysisService.java | ⚠️ 简化 | 模拟 |
| Service | ConflictDetectionService.java | ✅ 完整 | 模拟 |
| Service | MrpApplicationService.java | ⚠️ 直接调用 | 模拟 |

**关键问题**：
- `MrpApplicationService.loadItems()` - TODO: 从数据库查询
- `MrpApplicationService.loadBomMap()` - TODO: 从数据库查询
- `MrpApplicationService.loadInventory()` - TODO: 从数据库查询
- `MrpApplicationService.loadDemands()` - TODO: 从数据库查询
- `MrpApplicationService.saveResult()` - TODO: 保存到数据库

### 3.2 AI 预测（aimrp-forecast）

| 组件 | 文件 | 实现情况 | 数据来源 |
|------|------|----------|----------|
| Model | ForecastResult.java | ✅ 完整 | - |
| Service | DemandForecastService.java | ✅ 完整 | 模拟 |
| Service | SafetyStockService.java | ✅ 完整 | 模拟 |
| Controller | ForecastController.java | ✅ 完整 | - |
| Controller | SafetyStockController.java | ✅ 完整 | - |

**关键问题**：
- DemandForecastService 使用模拟历史数据
- SafetyStockService 使用模拟库存数据

### 3.3 风险监控（aimrp-risk）

| 组件 | 文件 | 实现情况 | 数据来源 |
|------|------|----------|----------|
| Model | RiskItem.java | ✅ 完整 | - |
| Service | RiskMonitorService.java | ✅ 完整 | 模拟 |
| Controller | RiskController.java | ✅ 完整 | - |
| Controller | RiskWarningController.java | ⚠️ 日志输出 | - |

**关键问题**：
- 风险检测使用模拟数据
- 预警发送只是日志打印，未接入消息服务

### 3.4 What-if（aimrp-whatif）

| 组件 | 文件 | 实现情况 | 数据来源 |
|------|------|----------|----------|
| Model | WhatIfScenario.java | ✅ 完整 | - |
| Model | WhatIfResult.java | ✅ 完整 | - |
| Service | WhatIfSimulationService.java | ✅ 框架 | 模拟 |
| Controller | WhatIfController.java | ✅ 完整 | - |

**关键问题**：
- 场景数据在内存中，未持久化
- 影响分析使用简化逻辑

### 3.5 AI 对话（aimrp-conversation）

| 组件 | 文件 | 实现情况 |
|------|------|----------|
| Service | IntentRecognitionService.java | ✅ 完整 |
| Service | EntityExtractionService.java | ✅ 完整 |
| Service | ExecutionRouter.java | ⚠️ 框架 |

**关键问题**：
- 执行路由未注册业务处理器

---

## 四、核心差距总结

### 4.1 分层架构差距

| 问题 | 影响 | 优先级 |
|------|------|--------|
| 缺少 Service 层的模块 | 不符合DDD，事务管理困难 | P0 |
| 缺少 Mapper 的模块 | 无法访问数据库 | P0 |
| 业务逻辑在 Controller | 代码耦合，难以测试 | P1 |

### 4.2 数据接入差距

| 模块 | Mapper | 数据源 | 状态 |
|------|--------|--------|------|
| demand | ✅ | 数据库 | 可用 |
| item | ✅ | 数据库 | 可用 |
| supplier | ✅ | 数据库 | 可用 |
| bom | ❌ | 无 | 需开发 |
| inventory | ❌ | 无 | 需开发 |
| purchase | ❌ | 无 | 需开发 |
| production | ❌ | 无 | 需开发 |
| mrp | ❌ | 模拟 | 需接入 |

### 4.3 业务流程差距

| 流程 | 当前状态 | 问题 |
|------|----------|------|
| MRP → 采购建议 | 模拟数据 | 未贯通 |
| 采购建议 → 采购订单 | 无 | 未实现 |
| MRP → 生产建议 | 模拟数据 | 未贯通 |
| 生产建议 → 生产工单 | 无 | 未实现 |
| 风险检测 → 预警通知 | 日志 | 未接入消息 |
| What-if → 应用 | 无 | 未实现 |

---

## 五、完善任务清单（更新版）

### 5.1 P0 - 必须完善

| # | 模块 | 任务 | 当前状态 | 工作量 |
|---|------|------|----------|--------|
| 1 | bom | 添加 Mapper | 无 | 1d |
| 2 | bom | 添加 Service 层 | 无 | 1d |
| 3 | inventory | 添加 Mapper | 无 | 1d |
| 4 | inventory | 添加 Service 层 | 无 | 1d |
| 5 | purchase | 添加 Mapper | 无 | 1d |
| 6 | purchase | 添加 Service 层 | 无 | 1d |
| 7 | production | 添加 Mapper | 无 | 2d |
| 8 | production | 添加 Service 层 | 无 | 2d |
| 9 | mrp | 接入物料数据 | 模拟 | 1d |
| 10 | mrp | 接入 BOM 数据 | 模拟 | 1d |
| 11 | mrp | 接入库存数据 | 模拟 | 1d |
| 12 | mrp | 接入订单数据 | 模拟 | 1d |
| 13 | mrp | 保存建议到数据库 | TODO | 1d |

### 5.2 P1 - 应该完善

| # | 模块 | 任务 | 当前状态 | 工作量 |
|---|------|------|----------|--------|
| 14 | production | 完善工艺路线 CRUD | 简化 | 2d |
| 15 | mrp | 完善影响分析逻辑 | 简化 | 2d |
| 16 | risk | 接入真实数据 | 模拟 | 2d |
| 17 | risk | 接入消息服务预警 | 日志 | 2d |
| 18 | forecast | 接入历史订单数据 | 模拟 | 2d |

### 5.3 P2 - 可以完善

| # | 模块 | 任务 | 当前状态 | 工作量 |
|---|------|------|----------|--------|
| 19 | conversation | 注册业务处理器 | 未注册 | 1d |
| 20 | whatif | 场景持久化 | 内存 | 2d |

---

## 六、总结

| 维度 | 统计 |
|------|------|
| 完整模块 | 3 (demand, item, supplier) |
| 中等模块 | 5 (production, mrp, forecast, risk, whatif) |
| 差模块 | 3 (bom, inventory, purchase) |
| Mapper 缺失 | 6/10 |
| Service 层缺失 | 6/10 |
| 使用模拟数据 | 7/10 |

**核心任务**：补充缺失的 Mapper + Service 层，接入真实数据

---

*备忘录更新完成 - 2026-03-09*
