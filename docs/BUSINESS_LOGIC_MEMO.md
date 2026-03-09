# 业务逻辑实现备忘录

> **日期**：2026-03-09  
> **版本**：4.0  
> **状态**：✅ MVP + Pro 阶段完成，Enterprise 开发中

---

## 一、产品功能规划总览

### 1.1 迭代阶段

| 阶段 | 周期 | 目标 | 状态 |
|------|------|------|------|
| MVP | 4周 | 核心MRP流程跑通 | ✅ 完成 |
| Pro | 4周 | 智能化能力（AI预测/排程/风险） | ✅ 完成 |
| Enterprise | 12周 | 企业级能力（微服务/多仓库/报表） | ⏳ 规划中 |

### 1.2 MVP 阶段功能（已完成）

| 序号 | 功能 | 分支 | 状态 |
|------|------|------|------|
| 1 | 项目初始化 | feature/project-init | ✅ |
| 2 | 数据库设计 | feature/database-design | ✅ |
| 3 | 公共模块 | feature/common-module | ✅ |
| 4 | 销售订单CRUD | feature/demand-order-crud | ✅ |
| 5 | 订单API | feature/demand-order-api | ✅ |
| 6 | BOM管理 | feature/bom-management | ✅ |
| 7 | BOM展开API | feature/bom-expand-api | ✅ |
| 8 | 库存管理 | feature/inventory-stock | ✅ |
| 9 | 物料主数据 | feature/item-management | ✅ |
| 10 | 供应商管理 | feature/supplier-management | ✅ |
| 11 | MRP计算引擎 | feature/mrp-calculation | ✅ |
| 12 | 采购建议 | feature/purchase-suggestion | ✅ |
| 13 | 用户权限 | feature/user-auth | ✅ |
| 14 | 前端页面 | feature/mvp-frontend-pages | ✅ |

### 1.3 MVP 前端页面（已完成）

| 页面 | 功能 | 状态 |
|------|------|------|
| OrderPage | 销售订单 | ✅ |
| BomPage | BOM管理 | ✅ 新增 |
| InventoryPage | 库存管理（含出入库） | ✅ 新增 |
| ItemPage | 物料主数据 | ✅ 新增 |
| SupplierPage | 供应商管理 | ✅ 新增 |
| PurchasePage | 采购管理 | ✅ 新增 |
| MrpPage | MRP计算 | ✅ |
| RiskPage | 风险预警 | ✅ |
| WhatIfPage | What-if模拟 | ✅ |

### 1.4 Pro 阶段功能（已完成）

| 序号 | 功能 | 分支 | 状态 |
|------|------|------|------|
| 1 | AI需求预测 | feature/ai-demand-predict | ✅ |
| 2 | 预测API+页面 | feature/ai-predict-api | ✅ |
| 3 | AI安全库存 | feature/ai-safety-stock | ✅ |
| 4 | 安全库存页面 | feature/ai-safety-stock-api | ✅ |
| 5 | 工艺路线 | feature/process-route | ✅ |
| 6 | 工作中心 | feature/work-center | ✅ |
| 7 | 资源管理 | feature/resource | ✅ |
| 8 | 工单工序 | feature/mo-operation | ✅ |
| 9 | OR排程优化 | feature/ai-or-scheduler | ✅ |
| 10 | 排程模型 | feature/ai-or-model | ✅ |
| 11 | 甘特图 | feature/web-gantt | ✅ |
| 12 | 影响分析 | feature/ai-impact-analysis | ✅ |
| 13 | 冲突检测 | feature/ai-conflict-detect | ✅ |
| 14 | 成本分析 | feature/ai-cost-analysis | ✅ |
| 15 | What-if模拟 | feature/ai-whatif | ✅ |
| 16 | 方案对比 | feature/ai-plan-compare | ✅ |
| 17 | 风险监控 | feature/ai-risk-monitor | ✅ |
| 18 | 风险预警 | feature/ai-risk-warning | ✅ |
| 19 | 风险页面 | feature/web-risk-page | ✅ |
| 20 | Pro集成测试 | feature/pro-integration | ✅ |

### 1.4 Enterprise 阶段功能（开发中）

| 序号 | 功能 | 分支 | 状态 |
|------|------|------|------|
| 1 | 组织架构管理 | feature/enterprise-org-structure | ✅ 第一批次 |
| 2 | 多仓库支持 | feature/enterprise-org-structure | ✅ 第一批次 |
| 3 | MPS 主生产计划 | feature/enterprise-org-structure | ✅ 第一批次 |
| 4 | 供应商门户 | feature/enterprise-org-structure | ✅ 第一批次 |
| 5 | 设备管理 | feature/enterprise-batch2 | ✅ 第二批次 |
| 6 | 质量管理 | feature/enterprise-batch2 | ✅ 第二批次 |
| 7 | 成本管理 | feature/enterprise-batch2 | ✅ 第二批次 |
| 8 | 报表中心 | feature/enterprise-batch3 | ✅ 第三批次 |
| 9 | 系统集成 | feature/enterprise-batch3 | ✅ 第三批次 |
| 10 | 运营仪表盘 | - | ⏳ 待补充 |
| 11 | 预算管理 | - | ⏳ 待补充 |
| 12 | 审批流 | - | ⏳ 待补充 |

### 1.5 Enterprise 微服务架构（规划中）

| 序号 | 功能 | 预估工时 |
|------|------|----------|
| 1 | 组织架构管理 | 3d |
| 2 | 组织API | 2d |
| 3 | 多仓库支持 | 3d |
| 4 | MPS+MRP+DRP联动 | 4d |
| 5 | 供应商门户 | 4d |
| 6 | 供应商API | 2d |
| 7 | ERP对接 | 3d |
| 8 | 开放平台 | 3d |
| 9 | 运营仪表盘 | 3d |
| 10 | 报表页面 | 3d |
| 11 | 报表导出 | 2d |
| 12 | Enterprise集成测试 | 3d |

### 1.5 Enterprise 微服务架构（规划中）

| 序号 | 功能 | 预估工时 |
|------|------|----------|
| 1 | Nacos注册/配置中心 | 3d |
| 2 | Spring Cloud Gateway | 3d |
| 3 | Feign调用改造 | 5d |
| 4 | SkyWalking链路追踪 | 2d |
| 5 | Sentinel熔断降级 | 2d |
| 6 | ELK日志接入 | 3d |

---

## 二、业务逻辑实现状态

### 2.1 模块分层结构（100%完成）

| 模块 | domain/service | application | mapper | 状态 |
|------|----------------|-------------|--------|------|
| demand | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| item | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| supplier | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| bom | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| inventory | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| purchase | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| production | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| mrp | ✅ 6 | ✅ 1 | ✅ 4 | ✅ |
| forecast | ✅ 2 | ✅ 1 | ✅ 1 | ✅ |
| risk | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| whatif | ✅ 1 | ✅ 1 | ✅ 1 | ✅ |
| conversation | ✅ 3 | ✅ 1 | - | ✅ |

### 2.2 MRP数据接入（100%完成）

| 数据类型 | Mapper | 状态 |
|----------|--------|------|
| 物料数据 | ItemMapper | ✅ |
| BOM数据 | BomMapper | ✅ |
| 库存数据 | InventoryMapper | ✅ |
| 需求数据 | SalesOrderMapper | ✅ |
| 在途采购 | MrpPurchaseOnWayMapper | ✅ |
| 在制生产 | MrpProductionOnWayMapper | ✅ |

### 2.3 业务功能实现

| 模块 | 功能 | 实现方式 | 状态 |
|------|------|----------|------|
| demand | 订单CRUD | DomainService + Mapper | ✅ |
| item | 物料CRUD | DomainService + Mapper | ✅ |
| supplier | 供应商CRUD | DomainService + Mapper | ✅ |
| bom | BOM管理+BOM展开 | BomDomainService + BomApplicationService | ✅ |
| inventory | 出入库 | InventoryDomainService + ApplicationService | ✅ |
| purchase | 采购订单 | PurchaseDomainService + ApplicationService | ✅ |
| production | 生产排程 | SchedulerService + ApplicationService | ✅ |
| mrp | MRP计算 | MrpCalculator + BomExpander + DemandMerger | ✅ |
| forecast | 需求预测+安全库存 | DemandForecastService + SafetyStockService | ✅ |
| risk | 风险监控+预警 | RiskMonitorService + ApplicationService | ✅ |
| whatif | 场景模拟 | WhatIfSimulationService + 持久化 | ✅ |
| conversation | 对话服务 | IntentRecognition + EntityExtraction + Router | ✅ |

---

## 三、集成测试状态

### 3.1 已完成测试

| 测试类型 | 文件 | 覆盖范围 |
|----------|------|----------|
| MVP集成测试 | MvpIntegrationTest.java | BOM展开、需求合并、净需求计算 |
| Pro集成测试 | ProIntegrationTest.java | 需求预测、安全库存、联动测试 |
| 对话集成测试 | ConversationIntegrationTest.java | 对话流程 |

### 3.2 测试覆盖说明

- MVP测试：覆盖完整MRP计算流程
- Pro测试：覆盖AI预测+安全库存联动
- 对话测试：覆盖意图识别→实体提取→路由执行

---

## 四、技术架构说明

### 4.1 模块调用方式

| 阶段 | 调用方式 | 说明 |
|------|----------|------|
| MVP/Pro | Maven依赖 + Spring DI | 同JVM内调用，高性能 |
| Enterprise | Feign | 跨服务调用，微服务架构 |

### 4.2 分层架构

```
api层        → 外部接口（REST）
application层 → 业务流程编排、事务管理
domain层     → 业务逻辑、领域服务
infrastructure层 → 数据访问、第三方集成
```

### 4.3 编码规范（v1.2）

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| Entity | 业务名 | SalesOrder |
| DomainService | 业务名+DomainService | BomDomainService |
| ApplicationService | 业务名+ApplicationService | BomApplicationService |
| Mapper | 表名+Mapper | SalesOrderMapper |
| Controller | 业务名+Controller | SalesOrderController |

---

## 五、版本演进记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2026-03-08 | 初始版本，记录Pro阶段分析 |
| 2.0 | 2026-03-08 | 更新核心差距总结 |
| 2.1 | 2026-03-09 | 集成测试添加 |
| 2.2 | 2026-03-09 | P0任务完成更新 |
| 2.3 | 2026-03-09 | P1任务第一部分完成 |
| 2.4 | 2026-03-09 | 剩余任务更新 |
| 3.0 | 2026-03-09 | 最终版，100%完成 |
| 4.0 | 2026-03-09 | 全面更新，产品规划+迭代计划 |

---

## 六、待完成任务

### 6.1 Enterprise阶段

#### 业务功能
- 组织架构管理
- 多仓库支持
- MPS+MRP+DRP联动
- 供应商门户
- ERP对接
- 运营仪表盘
- 报表导出

#### 微服务架构
- Nacos部署
- Gateway配置
- Feign改造
- SkyWalking/Sentinel/ELK

### 6.2 优化任务
- 事务管理完善
- 异常处理规范
- 日志规范

---

*本备忘录仅做增量更新，不删除历史内容*

*更新完成 - 2026-03-09*
