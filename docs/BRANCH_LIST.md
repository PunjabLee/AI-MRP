# AI MRP 完整功能分支清单

> **版本**：1.0  
> **日期**：2026-03-08

---

## 一、分支统计

| 阶段 | 分支数 | 时间 |
|------|--------|------|
| MVP | 23 | W1-4 |
| Pro | 16 | W5-8 |
| Enterprise | 12 | W9-12 |
| **总计** | **51** | 12 周 |

---

## 二、MVP 阶段分支（22 个）

### 2.1 基础设施（4 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/project-init` | 项目初始化（骨架 + Docker） | 2d |
| `feature/database-design` | PostgreSQL 数据库设计 | 2d |
| `feature/common-module` | 公共模块（Response/Exception） | 2d |
| `feature/sandbox-mechanism` | 沙箱机制（数据/计算/AI/业务模拟） | 4d |

### 2.2 后端-需求模块（2 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/demand-order-crud` | 销售订单 CRUD | 3d |
| `feature/demand-order-api` | 订单列表/详情 API | 2d |

### 2.3 后端-BOM 模块（2 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/bom-management` | BOM 管理 CRUD | 3d |
| `feature/bom-expand-api` | BOM 展开 API | 2d |

### 2.4 后端-库存模块（2 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/inventory-stock` | 库存管理 CRUD | 3d |
| `feature/inventory-api` | 库存查询 API | 2d |

### 2.5 后端-MRP 模块（2 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/mrp-calculate` | MRP 计算引擎 | 4d |
| `feature/mrp-suggestion` | 采购/生产建议生成 | 2d |

### 2.6 前端-页面（5 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/web-layout` | 前端页面布局 | 2d |
| `feature/web-order-page` | 订单页面 | 2d |
| `feature/web-bom-page` | BOM 页面 | 2d |
| `feature/web-inventory-page` | 库存页面 | 2d |
| `feature/web-mrp-page` | MRP 页面 | 2d |

### 2.7 AI 对话模块（5 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/ai-chat-ui` | 对话窗 UI | 2d |
| `feature/ai-intent` | 意图识别 | 2d |
| `feature/ai-entity` | 实体提取 | 2d |
| `feature/ai-router` | 执行路由 | 2d |
| `feature/ai-response` | 对话结果展示 | 2d |

### 2.8 测试（1 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/mvp-integration` | MVP 集成测试 | 2d |

---

## 三、Pro 阶段分支（16 个）

### 3.1 AI 预测模块（4 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/ai-demand-predict` | AI 需求预测（Prophet） | 4d |
| `feature/ai-predict-api` | 预测 API + 页面 | 2d |
| `feature/ai-safety-stock` | AI 安全库存推荐 | 3d |
| `feature/ai-safety-stock-api` | 安全库存页面 | 2d |

### 3.2 AI 排程模块（3 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/ai-or-scheduler` | OR 排程优化（OR-Tools） | 4d |
| `feature/ai-or-model` | 排程数学模型 | 2d |
| `feature/web-gantt` | 甘特图展示 | 3d |

### 3.3 AI 影响分析模块（5 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/ai-impact-analysis` | 插单影响分析引擎 | 3d |
| `feature/ai-conflict-detect` | 冲突检测 | 2d |
| `feature/ai-cost-analysis` | 成本影响分析 | 2d |
| `feature/ai-whatif` | What-if 模拟 | 3d |
| `feature/ai-plan-compare` | 方案对比 | 2d |

### 3.4 AI 预警模块（3 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/ai-risk-monitor` | 风险监控 | 2d |
| `feature/ai-risk-warning` | 风险预警 | 2d |
| `feature/web-risk-page` | 风险预警页面 | 2d |

### 3.5 测试（1 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/pro-integration` | Pro 集成测试 | 2d |

---

## 四、Enterprise 阶段分支（12 个）

### 4.1 组织架构模块（3 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/org-structure` | 组织架构管理 | 3d |
| `feature/org-api` | 组织 API | 2d |
| `feature/multi-warehouse` | 多仓库支持 | 3d |

### 4.2 计划联动模块（1 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/mps-mrp-drp` | MPS+MRP+DRP 联动 | 4d |

### 4.3 供应商模块（3 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/supplier-portal` | 供应商门户 | 4d |
| `feature/supplier-api` | 供应商 API | 2d |
| `feature/erp-integration` | ERP 对接 | 3d |

### 4.4 开放平台模块（1 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/open-api` | 开放平台 | 3d |

### 4.5 报表模块（3 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/web-dashboard` | 运营仪表盘 | 3d |
| `feature/web-report` | 报表页面 | 3d |
| `feature/report-export` | 报表导出（Excel/PDF） | 2d |

### 4.6 测试（1 个）

| 分支 | 功能 | 预估 |
|------|------|------|
| `feature/enterprise-integration` | Enterprise 集成测试 | 3d |

---

## 五、分支总览表

| # | 分支 | 功能 | 阶段 | 预估 |
|---|------|------|------|------|
| 1 | `feature/project-init` | 项目初始化 | MVP | 2d |
| 2 | `feature/database-design` | 数据库设计 | MVP | 2d |
| 3 | `feature/common-module` | 公共模块 | MVP | 2d |
| 4 | `feature/demand-order-crud` | 销售订单 CRUD | MVP | 3d |
| 5 | `feature/demand-order-api` | 订单 API | MVP | 2d |
| 6 | `feature/bom-management` | BOM 管理 | MVP | 3d |
| 7 | `feature/bom-expand-api` | BOM 展开 API | MVP | 2d |
| 8 | `feature/inventory-stock` | 库存管理 | MVP | 3d |
| 9 | `feature/inventory-api` | 库存 API | MVP | 2d |
| 10 | `feature/mrp-calculate` | MRP 计算引擎 | MVP | 4d |
| 11 | `feature/mrp-suggestion` | 采购建议 | MVP | 2d |
| 12 | `feature/web-layout` | 前端布局 | MVP | 2d |
| 13 | `feature/web-order-page` | 订单页面 | MVP | 2d |
| 14 | `feature/web-bom-page` | BOM 页面 | MVP | 2d |
| 15 | `feature/web-inventory-page` | 库存页面 | MVP | 2d |
| 16 | `feature/web-mrp-page` | MRP 页面 | MVP | 2d |
| 17 | `feature/ai-chat-ui` | 对话窗 UI | MVP | 2d |
| 18 | `feature/ai-intent` | 意图识别 | MVP | 2d |
| 19 | `feature/ai-entity` | 实体提取 | MVP | 2d |
| 20 | `feature/ai-router` | 执行路由 | MVP | 2d |
| 21 | `feature/ai-response` | 对话结果 | MVP | 2d |
| 22 | `feature/mvp-integration` | MVP 测试 | MVP | 2d |
| 23 | `feature/ai-demand-predict` | AI 需求预测 | Pro | 4d |
| 24 | `feature/ai-predict-api` | 预测 API | Pro | 2d |
| 25 | `feature/ai-safety-stock` | AI 安全库存 | Pro | 3d |
| 26 | `feature/ai-safety-stock-api` | 安全库存页面 | Pro | 2d |
| 27 | `feature/ai-or-scheduler` | OR 排程 | Pro | 4d |
| 28 | `feature/ai-or-model` | 排程模型 | Pro | 2d |
| 29 | `feature/web-gantt` | 甘特图 | Pro | 3d |
| 30 | `feature/ai-impact-analysis` | 影响分析 | Pro | 3d |
| 31 | `feature/ai-conflict-detect` | 冲突检测 | Pro | 2d |
| 32 | `feature/ai-cost-analysis` | 成本分析 | Pro | 2d |
| 33 | `feature/ai-whatif` | What-if | Pro | 3d |
| 34 | `feature/ai-plan-compare` | 方案对比 | Pro | 2d |
| 35 | `feature/ai-risk-monitor` | 风险监控 | Pro | 2d |
| 36 | `feature/ai-risk-warning` | 风险预警 | Pro | 2d |
| 37 | `feature/web-risk-page` | 预警页面 | Pro | 2d |
| 38 | `feature/pro-integration` | Pro 测试 | Pro | 2d |
| 39 | `feature/org-structure` | 组织架构 | Ent | 3d |
| 40 | `feature/org-api` | 组织 API | Ent | 2d |
| 41 | `feature/multi-warehouse` | 多仓库 | Ent | 3d |
| 42 | `feature/mps-mrp-drp` | MPS+MRP+DRP | Ent | 4d |
| 43 | `feature/supplier-portal` | 供应商门户 | Ent | 4d |
| 44 | `feature/supplier-api` | 供应商 API | Ent | 2d |
| 45 | `feature/erp-integration` | ERP 对接 | Ent | 3d |
| 46 | `feature/open-api` | 开放平台 | Ent | 3d |
| 47 | `feature/web-dashboard` | 仪表盘 | Ent | 3d |
| 48 | `feature/web-report` | 报表页面 | Ent | 3d |
| 49 | `feature/report-export` | 报表导出 | Ent | 2d |
| 50 | `feature/enterprise-integration` | Enterprise 测试 | Ent | 3d |

---

*分支清单完成*
