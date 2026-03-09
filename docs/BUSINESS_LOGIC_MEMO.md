

# 业务逻辑实现备忘录（增量更新 - Pro 阶段完成后）

> **日期**：2026-03-09  
> **版本**：2.1  
> **更新类型**：增量更新

---

## Pro 阶段完成后整体分析

### 模块架构总览（更新版）

| 模块 | Java文件 | Mapper | Service | API | 业务逻辑 | 状态 |
|------|----------|--------|---------|-----|----------|------|
| demand | 4 | ✅ | ❌ | ✅ | 直接调用Mapper | 简化 |
| item | 3 | ✅ | ❌ | ✅ | 直接调用Mapper | 简化 |
| supplier | 3 | ✅ | ❌ | ✅ | 直接调用Mapper | 简化 |
| bom | 2 | ❌ | ❌ | ✅ | 无 | 差 |
| inventory | 2 | ❌ | ❌ | ✅ | 无 | 差 |
| purchase | 4 | ❌ | ❌ | ✅ | 无 | 差 |
| production | 10 | ❌ | ✅ | ✅ | 排程完整 | 中 |
| mrp | 14 | ❌ | ✅ | ✅ | 成本新增 | 中 |
| forecast | 5 | ❌ | ✅ | ✅ | 预测/安全库存 | 中 |
| risk | 4 | ❌ | ✅ | ✅ | 监控/预警 | 中 |
| whatif | 4 | ❌ | ✅ | ✅ | 模拟/对比 | 中 |
| conversation | 0* | ❌ | ❌ | ❌ | 仅在master | 缺失 |

> *注：conversation 模块文件仅存在于 master 分支，develop 分支未合并

---

## Pro 阶段新增功能分析

### 2.1 成本影响分析（新增）

| 组件 | 状态 | 说明 |
|------|------|------|
| CostImpactAnalysisService.java | ✅ 完整 | 直接成本、间接成本、ROI分析 |
| CostAnalysisController.java | ✅ 完整 | REST API |
| 业务逻辑差距 | ⚠️ | 需接入真实成本数据 |

### 2.2 累计新增 Service

| 模块 | Service | 功能 | 数据来源 |
|------|---------|------|----------|
| mrp | CostImpactAnalysisService | 成本分析 | 输入参数 |
| mrp | ImpactAnalysisService | 影响分析 | 模拟 |
| mrp | ConflictDetectionService | 冲突检测 | 模拟 |
| whatif | WhatIfSimulationService | 模拟 | 内存 |
| risk | RiskMonitorService | 风险监控 | 模拟 |
| risk | RiskWarningController | 预警 | 日志 |
| production | SchedulerService | 排程 | 模拟 |

---

## 核心差距总结（更新版）

### 3.1 Mapper 层缺失

| 模块 | Mapper | 状态 |
|------|--------|------|
| bom | ❌ | 需开发 |
| inventory | ❌ | 需开发 |
| purchase | ❌ | 需开发 |
| production | ❌ | 需开发 |
| mrp | ❌ | 需开发 |
| forecast | ❌ | 需开发 |
| risk | ❌ | 需开发 |

### 3.2 Service 层缺失

| 模块 | Service | 状态 |
|------|---------|------|
| demand | ❌ | 需开发 |
| item | ❌ | 需开发 |
| supplier | ❌ | 需开发 |
| bom | ❌ | 需开发 |
| inventory | ❌ | 需开发 |
| purchase | ❌ | 需开发 |

### 3.3 数据接入差距

| 模块/功能 | 数据来源 | 目标 | 状态 |
|-----------|----------|------|------|
| MRP计算 | 模拟 | 数据库 | 需接入 |
| 需求预测 | 模拟 | 历史订单 | 需接入 |
| 安全库存 | 模拟 | 库存数据 | 需接入 |
| 风险监控 | 模拟 | 库存/供应商 | 需接入 |
| 排程 | 模拟 | 工单数据 | 需接入 |
| What-if | 内存 | 持久化 | 需开发 |

### 3.4 特殊问题

| 问题 | 说明 | 状态 |
|------|------|------|
| conversation 模块 | 仅存在于 master 分支 | 需合并 |
| 预警通知 | 仅日志输出 | 需接入消息 |

---

## 待完善任务清单（更新版）

### P0 - 必须完善

| # | 模块 | 任务 | 差距 | 工时 |
|---|------|------|------|------|
| 1 | bom | Mapper + Service | 无 | 2d |
| 2 | inventory | Mapper + Service | 无 | 2d |
| 3 | purchase | Mapper + Service | 无 | 2d |
| 4 | production | Mapper | 无 | 2d |
| 5 | mrp | 接入物料/BOM/库存数据 | 模拟 | 2d |
| 6 | conversation | 合并到develop | 仅在master | 1d |

### P1 - 应该完善

| # | 模块 | 任务 | 差距 | 工时 |
|---|------|------|------|------|
| 7 | forecast | 接入历史订单数据 | 模拟 | 2d |
| 8 | risk | 接入真实数据+消息 | 模拟+日志 | 3d |
| 9 | whatif | 场景持久化 | 内存 | 2d |
| 10 | 所有模块 | 完善事务管理 | 无 | 3d |

---

## Pro 阶段功能对照

### 已完成（18项）

| 功能 | 状态 | 业务逻辑 |
|------|------|----------|
| AI 需求预测 | ✅ | 简化 |
| AI 安全库存 | ✅ | 简化 |
| 工艺路线/资源 | ✅ | 简化 |
| OR 排程 | ✅ | 简化 |
| 甘特图 | ✅ | 可用 |
| 影响分析 | ✅ | 简化 |
| 冲突检测 | ✅ | 简化 |
| What-if | ✅ | 简化 |
| 风险监控 | ✅ | 简化 |
| 风险预警 | ⚠️ | 日志 |
| 成本分析 | ✅ | 简化 |

---

## 集成测试待办

| 任务 | 状态 |
|------|------|
| MVP 集成测试 | ⏳ |
| Pro 集成测试 | ⏳ |

---

*本备忘录仅做增量更新，不删除历史内容*

*更新完成 - 2026-03-09*
