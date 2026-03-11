<!--
---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: 5c8b84e774480e55f2ff127de8f8d526
    PropagateID: 5c8b84e774480e55f2ff127de8f8d526
    ReservedCode1: 304402203cb4e09cbd23045a64d1aeda3e10ecfacd3cf5734b8fbf51b2aaa68f18028717022017d5d2bbe18e8380f907a38f50e6111c76675682fcddfb13faaf274724cd9e90
    ReservedCode2: 3046022100b9f1a1ca5f810f8ccc94437472e56745ea797b8d42a1b861f6f1eb72dda59e65022100e6561b14336cc6b85bf407659753388525c3e31af7421b8f42b369dab349c973
---
-->
# 🚙 AI MRP 智能物料需求计划系统

> **项目代号**：AI-MRP  
> **版本**：v1.0.0-Pro  
> **创建日期**：2026-03-08

---

## 一、项目愿景

**让 MRP 计算从"人肉推算"升级为"AI 智能决策"**

传统 MRP 系统的核心痛点：
- ❌ 依赖人工经验调整参数
- ❌ 难以处理复杂约束（交期、产能、成本动态变化）
- ❌ 缺乏"what-if"场景模拟能力
- ❌ 供应链协同响应慢

**AI MRP 的核心价值**：
- ✅ LLM 理解自然语言需求，自动生成 MRP 计算模型
- ✅ OR 算法求解复杂约束下的最优排程方案
- ✅ "人机协同"——AI 建议 + 人工审核决策
- ✅ 数据驱动闭环，持续优化

---

## 二、市场定位

### 目标客户

| 客户类型 | 痛点 | 解决方案 |
|----------|------|----------|
| **中小制造企业** | 缺乏专职计划员，依赖老板经验 | AI 自动化 MRP，降低门槛 |
| **离散制造业** | BOM 复杂，多品种小批量 | OR 智能排程优化 |
| **供应链管理者** | 库存积压与缺料并存 | 需求驱动 + 动态优化 |
| **数字化转型企业** | 现有 ERP/MRP 智能化升级 | AI 插件/模块接入 |

### 竞争定位

```
传统 MRP（SAP, Oracle）          → 成熟但僵硬
新兴 SaaS MRP（金蝶，用友）       → 云化但缺乏 AI
AI MRP（我们）                    → LLM + OR 智能化
```

---

## 三、业务模块

### 3.1 需求管理

- **销售订单导入**：手动录入 / API 同步 ERP 订单
- **销售预测**：基于历史数据的 AI 预测（时序模型）
- **需求池**：订单 + 预测 → 合并需求清单
- **需求变更**：支持插单、减单、延期，触发 MRP 重算

### 3.2 BOM 管理

- **BOM 结构**：多层展开（父件 → 子件 → 原材料）
- **BOM 版本**：版本管理，支持 Engineering Change
- **BOM 变更**：变更记录，追溯历史
- **虚拟件 / 计划物料**：支持中间层抽象

### 3.3 库存管理

- **实时库存**：现有量、在途量、预留量
- **库存策略**：安全库存、订货点、最大最小库存
- **批次管理**：先进先出、有效期管理
- **多仓库**：支持多仓库调拨

### 3.4 MRP 计算引擎

**核心逻辑**：
```
需求 → BOM 展开 → 毛需求 → 净需求 → 计划订单 → 采购/生产建议
```

**AI 增强点**：
- LLM 解析自然语言需求描述
- OR 算法求解最优采购/生产批量
- 智能推荐安全库存策略
- 异常检测（缺料风险、库存积压预警）

### 3.5 采购管理

- **采购建议**：基于净需求生成采购订单建议
- **供应商管理**：供应商主数据、供货周期、价格
- **采购执行**：生成采购订单，跟踪到货
- **交期承诺**：AI 评估供应商交期能力

### 3.6 生产管理

- **生产工单**：基于计划订单生成工单
- **生产排程**：OR 算法优化生产顺序与时间
- **产能平衡**：产线产能约束检查
- **车间执行**：报工、完工入库

### 3.7 报表与分析

- **MRP 结果报表**：需求来源、供应来源、缺口分析
- **库存分析**：周转率、呆滞料、库龄分析
- **需求 vs 供应对比**：供需平衡分析
- **What-if 模拟**：模拟需求变更对计划的影响

---

## 四、技术架构

### 4.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  Web Admin / Desktop Client / Mobile / API Gateway          │
├─────────────────────────────────────────────────────────────┤
│                      Application Layer                       │
│  MRP Engine / BOM Service / Inventory Service / ...        │
├─────────────────────────────────────────────────────────────┤
│                       Domain Layer                           │
│  Demand / BOM / Inventory / Purchase / Production           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              AI + OR Intelligence Layer             │    │
│  │  LLM Planner │ OR Solver │ Prediction Engine │ ...   │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                       │
│  MySQL / Redis / MinIO / Yjs CRDT / Message Queue          │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 AI + OR 技术栈

| 能力 | 技术方案 |
|------|----------|
| **LLM 理解层** | OpenAI API / 本地 LLM（可选） |
| **OR 求解器** | Google OR-Tools, Python-MIP |
| **时序预测** | Prophet, ARIMA, LSTM |
| **向量存储** | Milvus / Qdrant（知识库） |
| **Agent 框架** | LangChain, AutoGen |

### 4.3 后端技术栈

- **框架**：Java 17 + Spring Boot 3
- **持久层**：MyBatis-Plus + MySQL 8.0
- **缓存**：Redis 7
- **消息队列**：RabbitMQ
- **工作流**：Flowable（审批流）

### 4.4 前端技术栈

- **Web**：React + TypeScript + Vite
- **桌面端**：Electron + React
- **状态管理**：Zustand
- **图表**：ECharts

---

## 五、核心算法模型

### 5.1 MRP 展开算法

```
输入：需求清单（订单/预测）、BOM、库存
输出：采购建议、生产建议

步骤：
1. 按时间维度排序需求
2. 对每个需求项，从顶层 BOM 向下展开
3. 计算毛需求 = 需求数量 × BOM 用量
4. 计算净需求 = 毛需求 - 现有量 - 在途量 - 预留量
5. 生成计划订单（考虑批量规则、最小起订量）
6. 输出采购/生产建议
```

### 5.2 OR 优化模型

**目标函数**：
```
min TotalCost = Σ(库存持有成本 + 缺货成本 + 换线成本 + 运输成本)
```

**约束条件**：
- 产能约束：Σ(工时) ≤ 产能
- 交期约束：完成时间 ≤ 客户交期
- 库存约束：现有量 ≥ 安全库存
- 批量约束：订单量 ≥ MOQ
- 资源约束：每道工序需要特定设备/人员

**求解策略**：
- 启发式算法：贪心、遗传算法
- 精确算法：分支定界、割平面
- 强化学习：动态排程优化

### 5.3 LLM 应用场景

| 场景 | Prompt 示例 |
|------|-------------|
| **需求解析** | "客户说下周一要1000件A产品，BOM里A需要什么材料？" |
| **异常解释** | "为什么C材料会缺料？帮我分析原因" |
| **计划调整** | "如果D订单延期5天，对其他订单有什么影响？" |
| **知识问答** | "我们的安全库存策略是什么？" |

---

## 六、实施路径

### Phase 1：MVP（4 周）✅ 完成

- [x] 需求管理（订单/预测）
- [x] BOM 管理（单层展开）
- [x] 库存管理（基础）
- [x] MRP 计算引擎（批量展开）
- [x] 采购建议生成

**交付**：Web 版 MVP，可处理简单场景

### Phase 2：Pro 智能化（4 周）✅ 已完成

- [x] AI 需求预测（时序模型）- Prophet/ARIMA
- [x] AI 安全库存推荐
- [x] OR 排程优化（Google OR-Tools）
- [x] 工艺路线/工作中心/资源管理
- [x] 多层 BOM 展开
- [x] 生产工单生成
- [x] 甘特图展示
- [x] What-if 场景模拟
- [x] 插单影响分析
- [x] 冲突检测
- [x] 成本影响分析
- [x] 风险监控与预警
- [x] LLM 对话式交互
- [x] Java-Python 集成网关
- [x] 预测结果持久化 + 回调机制

**交付**：智能排程 + 语音交互 + 风险预警 + 端到端集成

### Phase 3：Enterprise 企业级（规划中）

- [ ] 多工厂/多基地
- [ ] 多仓库
- [ ] 多产线
- [ ] MPS+MRP+DRP 联动
- [ ] 供应商协同平台
- [ ] ERP 对接
- [ ] 运营仪表盘
- [ ] 高级报表
- [ ] 微服务架构（Nacos/Gateway/SkyWalking）

**交付**：完整企业版

---

## 七、版本规划

| 版本 | 形态 | 定位 | 状态 |
|------|------|------|------|
| **v1.0.0-MVP** | Web 应用 | 核心 MRP 流程 | ✅ 已发布 |
| **v1.0.0-Pro** | Web 应用 | 智能化能力 | ✅ 已发布 (2026-03) |
| **v1.0.0-Enterprise** | Web 应用 | 企业级能力 | ⏳ 规划中 |

---

## 八、项目结构

```
AI-MRP/
├── docs/                           # 项目文档
│   ├── PRD.md                     # 产品需求文档
│   ├── TECHNICAL_ARCHITECTURE.md  # 技术架构设计
│   ├── CODE_LAYER_DESIGN.md      # 代码分层设计
│   ├── DATA_ARCHITECTURE.md      # 数据架构设计
│   ├── DEVELOPMENT_ENVIRONMENT.md # 开发环境配置
│   ├── GIT_WORKFLOW.md          # Git 分支规范
│   └── data-architecture/        # DDL 脚本
│       ├── DATABASE_DDL.sql
│       └── DATABASE_DDL_SUPPLEMENT.sql
│
├── code/                           # 项目代码
│   ├── backend/                   # Java 后端（Maven 多模块）
│   │   ├── pom.xml              # 主 POM
│   │   ├── aimrp-common/        # 公共模块
│   │   ├── aimrp-core/          # 核心域模块
│   │   ├── aimrp-demand/        # 需求管理
│   │   ├── aimrp-forecast/      # 预测模块 ✅ Pro
│   │   ├── aimrp-bom/           # BOM 管理
│   │   ├── aimrp-inventory/     # 库存管理
│   │   ├── aimrp-mrp/           # MRP 计算
│   │   ├── aimrp-purchase/      # 采购管理
│   │   ├── aimrp-production/    # 生产管理+排程 ✅ Pro
│   │   ├── aimrp-risk/          # 风险预警 ✅ Pro
│   │   ├── aimrp-whatif/        # What-if模拟 ✅ Pro
│   │   ├── aimrp-conversation/  # 对话服务 ✅ Pro
│   │   ├── aimrp-notification/  # 通知模块 ✅ Pro
│   │   ├── aimrp-integration/   # Java-Python集成网关 ✅ Pro
│   │   ├── aimrp-system/        # 用户权限
│   │   ├── aimrp-item/          # 物料主数据
│   │   ├── aimrp-supplier/      # 供应商管理
│   │   └── aimrp-api/           # API 入口
│   │
│   ├── ai-service/               # Python AI 微服务
│   │   ├── requirements.txt     # 依赖
│   │   └── app/
│   │       ├── main.py          # FastAPI 入口
│   │       ├── config/          # 配置
│   │       ├── router/          # 路由
│   │       ├── agents/          # Agent 编排
│   │       ├── llm/             # LLM 调用
│   │       ├── or_solver/       # OR 求解器
│   │       ├── predictor/       # 预测引擎
│   │       └── knowledge/       # RAG 知识库
│   │
│   └── frontend/                 # React 前端
│       └── aimrp-admin/         # 管理后台
│           ├── src/
│           │   ├── pages/       # 页面
│           │   ├── components/  # 组件
│           │   ├── hooks/      # Hooks
│           │   ├── services/   # API 服务
│           │   └── stores/    # 状态管理
│           └── package.json
│
└── .gitignore                    # Git 忽略配置
```

---

## 九、核心突破点总结

| 突破点 | 价值 |
|--------|------|
| **LLM + OR 融合** | 自然语言驱动 MRP，降低使用门槛 |
| **复杂约束求解** | 交期/产能/成本多目标优化 |
| **人机协同** | AI 建议 + 人工审核，兼顾效率与灵活 |
| **数据闭环** | 实际执行数据回流，持续优化模型 |
| **Java-Python 集成** | 端到端数据流，预测→MRP→排程一体化 |

---

## 十、开发状态

### 已完成模块

| 模块 | 功能 | API | 状态 |
|------|------|-----|------|
| `aimrp-system` | 用户权限认证 | `/api/auth/*` | ✅ |
| `aimrp-item` | 物料主数据 | `/api/items` | ✅ |
| `aimrp-supplier` | 供应商管理 | `/api/suppliers` | ✅ |
| `aimrp-demand` | 销售订单 | `/api/orders` | ✅ |
| `aimrp-bom` | BOM管理 | `/api/boms` | ✅ |
| `aimrp-inventory` | 库存管理 | `/api/inventory` | ✅ |
| `aimrp-mrp` | MRP计算引擎 | `/api/mrp/*` | ✅ |
| `aimrp-purchase` | 采购管理 | `/api/purchase-orders/*` | ✅ |
| `aimrp-production` | 生产管理+排程 | `/api/production-orders/*` | ✅ |
| `aimrp-forecast` | AI需求预测+安全库存 | `/api/forecast/*` | ✅ |
| `aimrp-risk` | 风险监控+预警 | `/api/risk/*` | ✅ |
| `aimrp-conversation` | AI对话 | `/api/conversation/*` | ✅ |
| `aimrp-whatif` | What-if模拟 | `/api/whatif/*` | ✅ |
| `aimrp-notification` | 通知模块 | `/api/notify/*` | ✅ |
| `aimrp-integration` | Java-Python集成网关 | `/api/ai/*` | ✅ |
| `aimrp-model` | 模型持久化服务 | `/api/model/*` | ✅ |
| `aimrp-scenario` | What-if场景管理 | `/api/scenario/*` | ✅ |

### 待开发模块（Enterprise）

| 模块 | 功能 |
|------|------|
| `aimrp-organization` | 组织架构管理 |
| `aimrp-multi-warehouse` | 多仓库支持 |
| `aimrp-mps` | MPS 主生产计划 |
| `aimrp-drp` | DRP 配送需求计划 |
| `aimrp-supplier-portal` | 供应商门户 |
| `aimrp-report` | 报表模块 |

### API 响应规范

统一使用 `ApiResponse<T>` 结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {...},
  "timestamp": 1709875200000
}
```

---

## 十一、分支策略

```
master           → v1.0.0-MVP (已发布)
release/mvp-1.0  → MVP 发布分支
release/pro      → Pro 发布分支 (v1.0.0-Pro) ✅
develop          → Enterprise 开发中
```

| 分支 | 用途 | Tag |
|------|------|-----|
| `master` | 生产发布 | v1.0.0-MVP |
| `release/pro` | Pro发布 | v1.0.0-Pro |
| `develop` | 开发主分支 | - |

---

*由 小jeep 🚙（天蓝色）整理*
*AI MRP Team © 2026*
