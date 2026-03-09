---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: b845f619d465e1c0c0174d0ee26af3cf
    PropagateID: b845f619d465e1c0c0174d0ee26af3cf
    ReservedCode1: 30450220694500c8392bed6de0d87764ecccdf8dcce95a191f82ab1512dc53eb1f6b3747022100d73a130400bce9a6aaf59746efa277299c38327245fb9e4f11aa2f27bfb955a5
    ReservedCode2: 304502210092cd51acc0348d680e2aadaeace93f02fbce012a22a126135b3404fee51fe9c30220668734fbf60b8087e1b85840fc1d8bc3fd4fc7fdc98d621c1c826b0585f7ccd2
---

# AI MRP 迭代规划（含测试）

> **版本**：1.0  
> **日期**：2026-03-08

---

## 一、迭代原则

### 1.1 MVP 优先

- **最小可用产品**：每个迭代交付可运行的系统
- **快速验证**：小步快跑，及时反馈
- **价值驱动**：优先实现高价值功能

### 1.2 迭代周期

| 阶段 | 周期 | 目标 |
|------|------|------|
| **MVP** | 4 周 | 核心流程跑通 |
| **Pro** | 4 周 | 智能化能力 |
| **Enterprise** | 4 周 | 企业级能力 |

---

## 二、功能分支清单

### MVP 阶段（第 1-4 周）

| 分支 | 功能 | 预估 | 负责人 | 状态 |
|------|------|------|--------|------|
| `feature/project-init` | 项目初始化（骨架 + Docker） | 2d | - | ✅ |
| `feature/database-design` | PostgreSQL 数据库设计 | 2d | - | ✅ |
| `feature/common-module` | 公共模块（Response/Exception） | 2d | - | ✅ |
| `feature/demand-order-crud` | 销售订单 CRUD | 3d | - | ✅ |
| `feature/demand-order-api` | 订单列表/详情 API | 2d | - | ✅ |
| `feature/bom-management` | BOM 管理 CRUD | 3d | - | ✅ |
| `feature/bom-expand-api` | BOM 展开 API | 2d | - | ✅ |
| `feature/inventory-stock` | 库存管理 CRUD | 3d | - | ✅ |
| `feature/inventory-api` | 库存查询 API | 2d | - | ✅ |
| `feature/mrp-calculate` | MRP 计算引擎 | 4d | - | ✅ |
| `feature/mrp-suggestion` | 采购/生产建议生成 | 2d | - | ✅ |
| `feature/web-layout` | 前端页面布局 | 2d | - | ✅ |
| `feature/web-order-page` | 订单页面 | 2d | - | ✅ |
| `feature/web-bom-page` | BOM 页面 | 2d | - | ✅ |
| `feature/web-inventory-page` | 库存页面 | 2d | - | ✅ |
| `feature/web-mrp-page` | MRP 页面 | 2d | - | ✅ |
| `feature/ai-chat-ui` | 对话窗 UI | 2d | - | ✅ |
| `feature/ai-intent` | 意图识别 | 2d | - | ✅ |
| `feature/ai-entity` | 实体提取 | 2d | - | ✅ |
| `feature/ai-router` | 执行路由 | 2d | - | ✅ |
| `feature/ai-response` | 对话结果展示 | 2d | - | ✅ |
| `feature/mvp-integration` | MVP 集成测试 | 2d | - | ⏳ |

**MVP 里程碑**：第 4 周末交付可运行的对话式 MRP 原型

---

### Pro 阶段（第 5-8 周）

| 分支 | 功能 | 预估 | 负责人 | 状态 |
|------|------|------|--------|------|
| `feature/ai-demand-predict` | AI 需求预测（Prophet） | 4d | - | ✅ |
| `feature/ai-predict-api` | 预测 API + 页面 | 2d | - | ✅ |
| `feature/ai-safety-stock` | AI 安全库存推荐 | 3d | - | ✅ |
| `feature/ai-safety-stock-api` | 安全库存页面 | 2d | - | ✅ |
| `feature/process-route` | 工艺路线管理 | 3d | - | ✅ |
| `feature/work-center` | 工作中心管理 | 2d | - | ✅ |
| `feature/resource` | 资源管理 | 2d | - | ✅ |
| `feature/mo-operation` | 工单工序明细 | 2d | - | ✅ |
| `feature/ai-or-scheduler` | OR 排程优化（OR-Tools） | 4d | - | ✅ |
| `feature/ai-or-model` | 排程数学模型 | 2d | - | ✅ |
| `feature/web-gantt` | 甘特图展示 | 3d | - | ✅ |
| `feature/ai-impact-analysis` | 插单影响分析引擎 | 3d | - | ⏳ |
| `feature/ai-conflict-detect` | 冲突检测 | 2d | - | ⏳ |
| `feature/ai-cost-analysis` | 成本影响分析 | 2d | - | ⏳ |
| `feature/ai-whatif` | What-if 模拟 | 3d | - | ⏳ |
| `feature/ai-plan-compare` | 方案对比 | 2d | - | ⏳ |
| `feature/ai-risk-monitor` | 风险监控 | 2d | - | ⏳ |
| `feature/ai-risk-warning` | 风险预警 | 2d | - | ⏳ |
| `feature/web-risk-page` | 风险预警页面 | 2d | - | ⏳ |
| `feature/pro-integration` | Pro 集成测试 | 2d | - | ⏳ |

### 补充实现（额外完成）

| 分支 | 功能 | 预估 | 状态 |
|------|------|------|------|
| `feature/user-auth` | 用户权限认证（JWT） | 2d | ✅ |
| `feature/item-management` | 物料主数据管理 | 2d | ✅ |
| `feature/supplier-management` | 供应商管理 | 2d | ✅ |
| `feature/purchase-management` | 采购管理（订单+入库） | 3d | ✅ |
| `feature/production-management` | 生产管理（工单+报工） | 3d | ✅ |
| `feature/purchase-suggestion` | 采购建议生成 | 2d | ✅ |

**Pro 里程碑**：第 8 周末交付智能化 MRP 系统

---

### Enterprise 阶段（第 9-12 周）

| 分支 | 功能 | 预估 | 负责人 |
|------|------|------|--------|
| `feature/org-structure` | 组织架构管理 | 3d | - |
| `feature/org-api` | 组织 API | 2d | - |
| `feature/multi-warehouse` | 多仓库支持 | 3d | - |
| `feature/mps-mrp-drp` | MPS+MRP+DRP 联动 | 4d | - |
| `feature/supplier-portal` | 供应商门户 | 4d | - |
| `feature/supplier-api` | 供应商 API | 2d | - |
| `feature/erp-integration` | ERP 对接 | 3d | - |
| `feature/open-api` | 开放平台 | 3d | - |
| `feature/web-dashboard` | 运营仪表盘 | 3d | - |
| `feature/web-report` | 报表页面 | 3d | - |
| `feature/report-export` | 报表导出（Excel/PDF） | 2d | - |
| `feature/enterprise-integration` | Enterprise 集成测试 | 3d | - |

**Enterprise 里程碑**：第 12 周末交付企业级 MRP

---

## 三、迭代计划

### Sprint 1: 基础框架（第 1-2 周）

**目标**：搭建项目骨架 + 基础数据模型

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 项目初始化（Spring Boot + React） | 架构 | 后端 | 1d |
| PostgreSQL 数据库设计 | 设计 | 后端 | 1d |
| 物料主数据 CRUD | 后端 | 后端 | 2d |
| BOM 管理 CRUD | 后端 | 后端 | 2d |
| 库存管理 CRUD | 后端 | 后端 | 2d |
| 前端基础框架搭建 | 前端 | 前端 | 2d |
| 物料列表页面 | 前端 | 前端 | 1d |
| BOM 维护页面 | 前端 | 前端 | 1d |

**交付**：
- 基础项目骨架
- 核心数据表
- 物料/BOM/库存管理页面

---

#### Sprint 2: 订单 + MRP 核心（第 3 周）

**目标**：销售订单管理 + MRP 计算引擎

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 销售订单 CRUD | 后端 | 后端 | 2d |
| 订单列表页面 | 前端 | 前端 | 1d |
| MRP 计算引擎 | 后端 | 后端 | 3d |
| 采购建议生成 | 后端 | 后端 | 1d |
| MRP 结果展示页面 | 前端 | 前端 | 1d |

**交付**：
- 销售订单管理
- MRP 计算引擎（支持批量展开）
- 采购建议展示

---

#### Sprint 3: 对话式交互（第 4 周）

**目标**：实现基础对话式交互

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 对话窗 UI | 前端 | 前端 | 1d |
| 意图识别模块 | 后端 | 后端 | 2d |
| 实体提取 | 后端 | 后端 | 1d |
| 执行路由 | 后端 | 后端 | 2d |
| 对话结果展示 | 前端 | 前端 | 1d |
| MVP 集成测试 | 测试 | 全员 | 1d |

**交付**：
- 对话式交互界面
- 简单对话意图执行
- MVP 版本发布

---

### Pro 迭代（5-8 周）

#### Sprint 4: AI 预测 + 安全库存（第 5-6 周）

**目标**：AI 预测与安全库存优化

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 历史数据接入 | 后端 | 后端 | 2d |
| 时序预测模型 | 算法 | 算法 | 3d |
| 预测 API | 后端 | 后端 | 1d |
| 安全库存推荐算法 | 算法 | 算法 | 2d |
| 预测结果展示页面 | 前端 | 前端 | 2d |

**交付**：
- 需求预测功能
- 安全库存 AI 推荐

---

#### Sprint 5: OR 排程优化（第 6-7 周）

**目标**：OR 算法驱动的智能排程

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 排程问题建模 | 算法 | 算法 | 2d |
| OR-Tools 集成 | 后端 | 后端 | 2d |
| 排程求解服务 | 后端 | 后端 | 2d |
| 甘特图展示 | 前端 | 前端 | 2d |
| 排程优化入口 | 前端 | 前端 | 1d |

**交付**：
- OR 智能排程
- 甘特图可视化

---

#### Sprint 6: 影响分析 + 预警（第 7-8 周）

**目标**：插单影响分析 + 风险预警

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 影响分析引擎 | 后端 | 后端 | 3d |
| What-if 模拟 | 后端 | 后端 | 2d |
| 风险预警规则 | 后端 | 后端 | 2d |
| 预警通知 | 后端 | 前端 | 1d |
| 影响分析展示 | 前端 | 前端 | 2d |
| Pro 版本集成测试 | 测试 | 全员 | 1d |

**交付**：
- 插单影响分析
- What-if 模拟
- 风险预警
- Pro 版本发布

---

### Enterprise 迭代（9-12 周）

#### Sprint 7: 多组织架构（第 9-10 周）

**目标**：多工厂/多仓库支持

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 组织架构管理 | 后端 | 后端 | 2d |
| 多仓库支持 | 后端 | 后端 | 2d |
| MPS + MRP + DRP 联动 | 后端 | 后端 | 3d |
| 组织架构页面 | 前端 | 前端 | 2d |

**交付**：
- 多工厂/多仓库
- 统一计划体系

---

#### Sprint 8: 供应链协同（第 10-11 周）

**目标**：供应商协同 + 外部集成

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 供应商门户 | 前端 | 前端 | 3d |
| 供应商 API | 后端 | 后端 | 2d |
| ERP 对接（API） | 后端 | 后端 | 3d |
| 开放平台 | 后端 | 后端 | 2d |

**交付**：
- 供应商协同
- 外部系统对接

---

#### Sprint 9: 高级分析 + 优化（第 11-12 周）

**目标**：完整分析 + 性能优化

**任务**：

| 任务 | 类型 | 负责 | 预估 |
|------|------|------|------|
| 运营报表 | 前端 | 前端 | 3d |
| 仪表盘 | 前端 | 前端 | 2d |
| 性能优化 | 后端 | 后端 | 2d |
| 安全加固 | 后端 | 后端 | 1d |
| Enterprise 集成测试 | 测试 | 全员 | 2d |

**交付**：
- 完整报表
- 性能优化
- Enterprise 版本发布

---

## 四、功能迭代地图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         功能迭代地图                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Week 1-2     Week 3       Week 4    Week 5-6    Week 7-8   Week 9-12│
│  ─────────    ──────       ──────    ─────────    ────────   ─────────│
│                                                                         │
│  基础框架      销售订单      对话交互   AI 预测     影响分析   多工厂    │
│  ├── 项目初始化│ ├── CRUD   │ ├── Chat  │ ├── 预测    │ ├── 插单   │ ├── 组织 │
│  ├── 数据库    │ ├── MRP    │ ├── Intent│ ├── 安全   │ ├── What-if│ ├── 仓库 │
│  ├── 物料     │ └── 采购建议│ └── 执行  │           │ ├── 预警   │ │       │
│  ├── BOM      │             │          │ OR 排程   │           │ 供应商  │
│  └── 库存     │             │          │ ├── GA    │           │ ├── 门户  │
│               │             │          │ ├── 求解   │           │ └── API  │
│               │             │          │ └── Gantt  │           │         │
│               │             │          │            │           │ 报表    │
│               │             │          │            │           │ ├── 仪表盘│
│               │             │          │            │           │ └── 报表 │
│                                                                         │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌───────┐│
│  │ Alpha   │ │ Beta    │ │  1.0   │ │  1.1   │ │  1.2   │ │  2.0  ││
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └───────┘│
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 五、里程碑

| 里程碑 | 时间 | 交付 | 状态 |
|--------|------|------|------|
| **M1: 骨架** | 第 2 周末 | 项目骨架 + 基础 CRUD | ✅ |
| **M2: MVP** | 第 4 周末 | 对话式 MRP 原型 | ✅ |
| **M3: Pro** | 第 8 周末 | 智能化 MRP | ⏳ 进行中 |
| **M4: GA** | 第 12 周末 | 企业级 MRP | ⏳ |

---

## 六、资源估算

### 6.1 人力投入

| 角色 | MVP (4周) | Pro (4周) | Enterprise (4周) |
|------|-----------|-----------|------------------|
| 后端开发 | 2 | 2 | 2 |
| 前端开发 | 1 | 1 | 1 |
| 算法工程师 | 0 | 1 | 1 |
| 测试 | 1 | 1 | 1 |
| 产品 | 1 | 1 | 0.5 |

### 6.2 服务器资源

| 环境 | 规格 | 数量 |
|------|------|------|
| 开发 | 2C4G | 1 台 |
| 测试 | 4C8G | 1 台 |
| 生产 | 8C16G | 3+ 台 |

---

## 七、测试策略

### 7.1 测试分层

```
┌─────────────────────────────────────────────────────────────┐
│                        测试金字塔                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                        ▲ E2E 测试 (少量)                     │
│                       ▲▲▲                                    │
│                      ▲▲▲▲▲ 集成测试 (中量)                   │
│                     ▲▲▲▲▲▲▲                                 │
│                    ▲▲▲▲▲▲▲▲▲ 单元测试 (大量)                │
│                                                             │
│  目标：70% 单元测试 + 20% 集成测试 + 10% E2E                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 测试技术栈

| 类型 | 工具 | 覆盖范围 |
|------|------|----------|
| **单元测试** | JUnit 5 + Mockito | Java 业务逻辑 |
| **单元测试** | Pytest | Python AI 服务 |
| **单元测试** | Vitest | 前端组件 |
| **集成测试** | Spring Boot Test | Controller/Repository |
| **集成测试** | Testcontainers | PostgreSQL/MySQL |
| **E2E 测试** | Playwright | 端到端流程 |
| **Mock 服务** | WireMock | 外部 API 模拟 |
| **性能测试** | JMeter / k6 | 接口性能 |

### 7.3 测试分支策略

每个功能开发完成后，需创建对应的测试分支：

| 功能分支 | 对应测试分支 |
|----------|--------------|
| `feature/demand-order-crud` | `test/demand-order-crud` |
| `feature/mrp-calculate` | `test/mrp-calculate` |
| `feature/ai-intent` | `test/ai-intent` |
| ... | ... |

**测试分支命名规范：**
```
test/<功能模块>-<测试类型>

示例：
test/demand-unit        # 需求模块单元测试
test/mrp-integration    # MRP 集成测试
test/ai-e2e            # AI 模块 E2E 测试
```

### 7.4 测试覆盖要求

| 模块 | 单元测试 | 集成测试 | E2E |
|------|----------|----------|-----|
| 领域模型 | ✅ 覆盖率 > 80% | - | - |
| Application Service | ✅ 覆盖率 > 70% | ✅ | - |
| Controller | - | ✅ | - |
| MRP 计算引擎 | ✅ 核心算法 100% | ✅ | - |
| AI 对话 | - | ✅ | ✅ 关键路径 |
| 排程算法 | ✅ 边界条件全覆盖 | ✅ | - |

### 7.5 CI/CD 测试流程

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop, 'feature/**', 'test/**']
  pull_request:
    branches: [main, develop]

jobs:
  # 1. 单元测试
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Java Unit Test
        run: ./mvnw test
      - name: Python Unit Test
        run: pytest tests/unit/
      - name: Frontend Unit Test
        run: npm run test:unit

  # 2. 集成测试
  integration-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Start Testcontainers
        run: docker-compose -f docker-compose.test.yml up -d
      - name: Run Integration Tests
        run: ./mvnw verify -Pintegration-test

  # 3. E2E 测试
  e2e-test:
    runs-on: ubuntu-latest
    needs: [unit-test, integration-test]
    steps:
      - uses: actions/checkout@v3
      - name: Install Playwright
        run: npx playwright install
      - name: Run E2E Tests
        run: npm run test:e2e
```

### 7.6 测试里程碑

| 里程碑 | 测试目标 | 完成标准 |
|--------|----------|----------|
| M1.5 | 基础 CRUD 测试覆盖 | 核心 Entity 单元测试 > 80% |
| M2 | MVP 测试完成 | 集成测试 + E2E 冒烟通过 |
| M3 | Pro 测试完成 | AI 模块测试覆盖 |
| M4 | Enterprise 测试完成 | 全模块测试覆盖 |

---

## 八、风险与对策

| 风险 | 影响 | 对策 |
|------|------|------|
| OR 算法复杂度 | 排程可能无法在合理时间内求解 | 限制问题规模 + 启发式降级 |
| LLM 响应不稳定 | 对话体验受影响 | 实现多 provider 切换 |
| 数据质量差 | 预测效果差 | 数据清洗 + 人工干预 |
| 需求变更 | 影响进度 | 敏捷响应 + 优先级重排 |

---

## 九、版本发布计划

```
v1.0.0-Alpha   (第 4 周末)
    └── MVP 版本，对话式 MRP 原型

v1.0.0-Beta    (第 8 周末)  
    └── Pro 版本，AI + OR 能力

v1.0.0         (第 12 周末)
    └── Enterprise 版本，企业级能力
```

---

*迭代规划完成*

