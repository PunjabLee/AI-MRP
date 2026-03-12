# AI MRP 测试分支清单

> **版本**：1.1  
> **日期**：2026-03-09  
> **说明**：已更新测试状态

---

## 一、测试分支命名规范

```
test/<模块>-<类型>

类型：
- unit         # 单元测试
- integration  # 集成测试  
- e2e          # 端到端测试
- perf         # 性能测试
```

---

## 二、测试分支清单

### 2.1 MVP 阶段测试（10 个）

| 分支 | 类型 | 覆盖范围 | 状态 |
|------|------|----------|------|
| `test/project-setup` | unit | 项目配置测试 | ✅ |
| `test/common-module` | unit | 公共模块测试 | ✅ |
| `test/demand-unit` | unit | 需求领域单元测试 | ✅ |
| `test/demand-integration` | integration | 订单 CRUD 集成测试 | ✅ |
| `test/bom-unit` | unit | BOM 领域单元测试 | ✅ |
| `test/inventory-unit` | unit | 库存领域单元测试 | ✅ |
| `test/mrp-unit` | unit | MRP 核心算法测试 | ✅ |
| `test/mrp-integration` | integration | MRP 集成测试 | ✅ |
| `test/ai-unit` | unit | AI 对话单元测试 | ✅ |
| `test/mvp-e2e` | e2e | MVP 冒烟测试 | ⏳ |

### 2.2 Pro 阶段测试（8 个）

| 分支 | 类型 | 覆盖范围 | 状态 |
|------|------|----------|------|
| `test/ai-predict-unit` | unit | 预测算法单元测试 | ✅ |
| `test/ai-predict-integration` | integration | 预测集成测试 | ✅ |
| `test/ai-safety-stock-unit` | unit | 安全库存算法测试 | ✅ |
| `test/ai-or-unit` | unit | OR 求解器测试 | ✅ |
| `test/ai-or-integration` | integration | 排程集成测试 | ✅ |
| `test/ai-impact-unit` | unit | 影响分析算法测试 | ⏳ |
| `test/ai-risk-unit` | unit | 风险预警测试 | ⏳ |
| `test/pro-e2e` | e2e | Pro 端到端测试 | ⏳ |

### 2.3 Enterprise 阶段测试（5 个）

| 分支 | 类型 | 覆盖范围 | 状态 |
|------|------|----------|------|
| `test/org-unit` | unit | 组织架构测试 | ⏳ |
| `test/supplier-unit` | unit | 供应商模块测试 | ⏳ |
| `test/report-unit` | unit | 报表测试 | ⏳ |
| `test/enterprise-integration` | integration | 企业级集成测试 | ⏳ |
| `test/enterprise-e2e` | e2e | Enterprise E2E 测试 | ⏳ |

---

## 三、测试分支统计

| 阶段 | 单元测试 | 集成测试 | E2E | 已完成 |
|------|----------|----------|-----|--------|
| MVP | 6 | 3 | 1 | 9 |
| Pro | 5 | 2 | 1 | 5 |
| Enterprise | 3 | 1 | 1 | 0 |
| **总计** | **14** | **6** | **3** | **14** |

---

## 四、测试任务分解

### MVP 阶段

| 测试分支 | 功能分支对应 | 预估 |
|----------|--------------|------|
| `test/project-setup` | `feature/project-init` | 1d |
| `test/common-module` | `feature/common-module` | 1d |
| `test/demand-unit` | `feature/demand-order-crud` | 2d |
| `test/demand-integration` | `feature/demand-order-api` | 1d |
| `test/bom-unit` | `feature/bom-management` | 1d |
| `test/inventory-unit` | `feature/inventory-stock` | 1d |
| `test/mrp-unit` | `feature/mrp-calculate` | 2d |
| `test/mrp-integration` | `feature/mrp-suggestion` | 1d |
| `test/ai-unit` | `feature/ai-intent` | 1d |
| `test/mvp-e2e` | 所有 MVP 功能 | 2d |

### Pro 阶段

| 测试分支 | 功能分支对应 | 预估 |
|----------|--------------|------|
| `test/ai-predict-unit` | `feature/ai-demand-predict` | 2d |
| `test/ai-predict-integration` | `feature/ai-predict-api` | 1d |
| `test/ai-safety-stock-unit` | `feature/ai-safety-stock` | 1d |
| `test/ai-or-unit` | `feature/ai-or-scheduler` | 2d |
| `test/ai-or-integration` | `feature/ai-or-model` | 1d |
| `test/ai-impact-unit` | `feature/ai-impact-analysis` | 1d |
| `test/ai-risk-unit` | `feature/ai-risk-warning` | 1d |
| `test/pro-e2e` | 所有 Pro 功能 | 2d |

### Enterprise 阶段

| 测试分支 | 功能分支对应 | 预估 |
|----------|--------------|------|
| `test/org-unit` | `feature/org-structure` | 1d |
| `test/supplier-unit` | `feature/supplier-portal` | 1d |
| `test/report-unit` | `feature/web-report` | 1d |
| `test/enterprise-integration` | `feature/open-api` | 2d |
| `test/enterprise-e2e` | 所有 Enterprise 功能 | 2d |

---

*测试分支清单完成*
