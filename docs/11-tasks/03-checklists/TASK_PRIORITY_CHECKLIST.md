# MVP + Pro 阶段工作任务清单及优先级

> **日期**：2026-03-09  
> **版本**：1.0

---

## 一、MVP 阶段工作任务清单（已完成）

### 1.1 基础架构

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 1.1.1 | 项目初始化 | feature/project-init | P0 | ✅ |
| 1.1.2 | 数据库设计 | feature/database-design | P0 | ✅ |
| 1.1.3 | 公共模块 | feature/common-module | P0 | ✅ |

### 1.2 核心业务

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 1.2.1 | 销售订单CRUD | feature/demand-order-crud | P0 | ✅ |
| 1.2.2 | 订单API | feature/demand-order-api | P0 | ✅ |
| 1.2.3 | BOM管理 | feature/bom-management | P0 | ✅ |
| 1.2.4 | BOM展开API | feature/bom-expand-api | P0 | ✅ |
| 1.2.5 | 库存管理 | feature/inventory-stock | P0 | ✅ |
| 1.2.6 | 物料主数据 | feature/item-management | P0 | ✅ |
| 1.2.7 | 供应商管理 | feature/supplier-management | P0 | ✅ |
| 1.2.8 | MRP计算引擎 | feature/mrp-calculation | P0 | ✅ |
| 1.2.9 | 采购建议 | feature/purchase-suggestion | P0 | ✅ |

### 1.3 支撑功能

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 1.3.1 | 用户权限 | feature/user-auth | P1 | ✅ |
| 1.3.2 | 前端页面 | - | P1 | ✅ |

---

## 二、Pro 阶段工作任务清单（已完成）

### 2.1 AI 智能化

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.1.1 | AI需求预测 | feature/ai-demand-predict | P0 | ✅ |
| 2.1.2 | 预测API+页面 | feature/ai-predict-api | P0 | ✅ |
| 2.1.3 | AI安全库存推荐 | feature/ai-safety-stock | P0 | ✅ |
| 2.1.4 | 安全库存页面 | feature/ai-safety-stock-api | P0 | ✅ |

### 2.2 生产排程

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.2.1 | 工艺路线 | feature/process-route | P0 | ✅ |
| 2.2.2 | 工作中心 | feature/work-center | P0 | ✅ |
| 2.2.3 | 资源管理 | feature/resource | P0 | ✅ |
| 2.2.4 | 工单工序 | feature/mo-operation | P0 | ✅ |
| 2.2.5 | OR排程优化 | feature/ai-or-scheduler | P0 | ✅ |
| 2.2.6 | 排程数学模型 | feature/ai-or-model | P0 | ✅ |
| 2.2.7 | 甘特图展示 | feature/web-gantt | P1 | ✅ |

### 2.3 分析决策

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.3.1 | 插单影响分析 | feature/ai-impact-analysis | P0 | ✅ |
| 2.3.2 | 冲突检测 | feature/ai-conflict-detect | P0 | ✅ |
| 2.3.3 | 成本影响分析 | feature/ai-cost-analysis | P1 | ✅ |
| 2.3.4 | What-if模拟 | feature/ai-whatif | P0 | ✅ |
| 2.3.5 | 方案对比 | feature/ai-plan-compare | P1 | ✅ |

### 2.4 风险管理

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.4.1 | 风险监控 | feature/ai-risk-monitor | P0 | ✅ |
| 2.4.2 | 风险预警 | feature/ai-risk-warning | P0 | ✅ |
| 2.4.3 | 风险预警页面 | feature/web-risk-page | P1 | ✅ |

### 2.5 对话交互

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.5.1 | 对话窗UI | feature/ai-chat-ui | P1 | ✅ |
| 2.5.2 | 意图识别 | feature/ai-intent | P0 | ✅ |
| 2.5.3 | 实体提取 | feature/ai-entity-extract | P0 | ✅ |
| 2.5.4 | 执行路由 | feature/ai-execute | P0 | ✅ |

### 2.6 集成测试

| 序号 | 任务 | 分支 | 优先级 | 状态 |
|------|------|------|--------|------|
| 2.6.1 | MVP集成测试 | feature/mvp-integration | P0 | ✅ |
| 2.6.2 | Pro集成测试 | feature/pro-integration | P0 | ✅ |

---

## 三、优先级统计

### 3.1 MVP 阶段

| 优先级 | 数量 | 占比 |
|--------|------|------|
| P0 | 12 | 86% |
| P1 | 2 | 14% |
| P2 | 0 | 0% |

### 3.2 Pro 阶段

| 优先级 | 数量 | 占比 |
|--------|------|------|
| P0 | 17 | 71% |
| P1 | 5 | 21% |
| P2 | 0 | 0% |

---

## 四、代码质量任务（补充）

### 4.1 架构规范完善

| 序号 | 任务 | 优先级 | 状态 |
|------|------|--------|------|
| 4.1.1 | 补充 domain/service 层 | P0 | ✅ |
| 4.1.2 | 补充 application 层 | P0 | ✅ |
| 4.1.3 | 补充 mapper 层 | P0 | ✅ |
| 4.1.4 | 补充 domain/repository 接口 | P1 | ✅ |
| 4.1.5 | 补充 application/dto/command/query | P1 | ✅ |
| 4.1.6 | 补充 api/assembler | P2 | ✅ |

### 4.2 业务逻辑完善

| 序号 | 任务 | 优先级 | 状态 |
|------|------|--------|------|
| 4.2.1 | MRP接入物料数据 | P0 | ✅ |
| 4.2.2 | MRP接入BOM数据 | P0 | ✅ |
| 4.2.3 | MRP接入库存数据 | P0 | ✅ |
| 4.2.4 | MRP接入需求数据 | P0 | ✅ |
| 4.2.5 | conversation模块合并 | P0 | ✅ |
| 4.2.6 | 预测接入历史数据 | P1 | ✅ |
| 4.2.7 | 风险接入真实数据 | P1 | ✅ |
| 4.2.8 | What-if场景持久化 | P1 | ✅ |

---

## 五、总结

### 完成度

| 阶段 | 任务数 | P0 | P1 | P2 |
|------|--------|-----|-----|-----|
| MVP | 14 | 12 | 2 | 0 |
| Pro | 22 | 17 | 5 | 0 |
| 代码质量 | 14 | 8 | 4 | 2 |
| **总计** | **50** | **37** | **11** | **2** |

---

*清单整理完成 - 2026-03-09*
