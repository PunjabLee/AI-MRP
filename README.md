
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
# AI MRP 智能物料需求计划系统

> **项目代号**：AI-MRP
> **版本**：v1.0.0-Enterprise
> **更新日期**：2026-03-12

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
┌─────────────────────────────────────────────────────────────────────────────┐
│                           用户层 (Presentation Layer)                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Web Admin  │  │ Mobile App  │  │ API Gateway │  │ 桌面客户端  │     │
│  │ React/Vite │  │   Flutter  │  │   Gateway   │  │ Electron   │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
└─────────┼─────────────────┼─────────────────┼─────────────────┼───────────────┘
          │                 │                 │                 │
          └─────────────────┴────────┬────────┴─────────────────┘
                                    │ HTTP/REST
┌───────────────────────────────────▼────────────────────────────────────────┐
│                          Java Spring Boot 后端                            │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     Application Layer                             │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │   │
│  │  │ 需求管理 │ │ 库存管理 │ │ 生产管理 │ │ 预测模块  │        │   │
│  │  │ demand  │ │inventory │ │production│ │forecast  │        │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘        │   │
│  │       └────────────┴─────────────┴─────────────┘                │   │
│  │                            │                                     │   │
│  │                    ┌──────▼──────┐                              │   │
│  │                    │ aimrp-api   │ ← API 网关入口              │   │
│  │                    └──────┬──────┘                              │   │
│  └───────────────────────────┼─────────────────────────────────────┘   │
│                              │                                          │
│  ┌───────────────────────────▼────────────────────────────────────────┐  │
│  │                    Domain Layer                                   │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         │  │
│  │  │   BOM    │ │ 采购管理  │ │ 供应商   │ │ 质量管理  │         │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘         │  │
│  │                            │                                     │  │
│  │                    ┌──────▼──────┐                              │  │
│  │                    │ aimrp-integration │ ← Java-Python 集成    │  │
│  │                    └───────────────┘                              │  │
│  └───────────────────────────┼───────────────────────────────────────┘  │
│                              │ HTTP/REST                              │
└──────────────────────────────┼────────────────────────────────────────┘
                               │
┌───────────────────────────────▼────────────────────────────────────────┐
│                        Python FastAPI AI 服务                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     Router Layer                                 │   │
│  │  /predict/*  /schedule/*  /chat/*  /model/*  /scenario/*     │   │
│  └────────────────────────────────┬────────────────────────────────┘   │
│                                   │                                   │
│  ┌────────────────────────────────▼────────────────────────────────┐   │
│  │                   Algorithm Layer                               │   │
│  │  forecast.py  scheduler.py  safety_stock.py  llm.py  whatif │   │
│  └────────────────────────────────┬────────────────────────────────┘   │
│                                   │                                   │
│  ┌────────────────────────────────▼────────────────────────────────┐   │
│  │                Infrastructure Layer                             │   │
│  │  notification/  persistence.py  performance.py                │   │
│  └───────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                               │
┌───────────────────────────────▼────────────────────────────────────────┐
│                         基础设施层 (Infrastructure)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │ PostgreSQL │  │    Redis    │  │  RabbitMQ  │  │    Kafka    │ │
│  │   主数据库   │  │   缓存/队列  │  │   消息队列   │  │  日志流    │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
│                                                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                  │
│  │   Nginx     │  │   Docker    │  │   Python    │                  │
│  │  反向代理   │  │  容器化部署  │  │   虚拟环境  │                  │
│  └─────────────┘  └─────────────┘  └─────────────┘                  │
└──────────────────────────────────────────────────────────────────────┘
```

### 4.2 AI + OR 技术栈

| 能力 | 技术方案 |
|------|----------|
| **LLM 理解层** | OpenAI / DeepSeek / Ollama |
| **OR 求解器** | Google OR-Tools (CP-SAT) |
| **时序预测** | Prophet / LSTM / ARIMA / XGBoost |
| **安全库存** | 统计法 / 蒙特卡洛模拟 |
| **通知通道** | RabbitMQ / Kafka / Redis / Email |

### 4.3 后端技术栈

- **框架**：Java 17 + Spring Boot 3
- **持久层**：MyBatis-Plus + PostgreSQL
- **缓存**：Redis 7
- **消息队列**：RabbitMQ
- **API**：Spring Cloud Gateway

### 4.4 前端技术栈

- **Web**：React 18 + TypeScript + Vite
- **状态管理**：Zustand
- **图表**：ECharts (甘特图)
- **UI**：Ant Design

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

- [x] AI 需求预测（时序模型）
  - [x] Prophet / ARIMA / LSTM / XGBoost
  - [x] 自动方法选择
  - [x] 置信区间计算
  - [x] 预测准确率评估 (MAPE/RMSE)
- [x] AI 安全库存推荐
  - [x] 统计法计算
  - [x] 服务水平优化
  - [x] EOQ 经济订货量
- [x] OR 排程优化（Google OR-Tools）
  - [x] 多种优化目标 (makespan/tardiness/cost/balanced)
  - [x] 复杂约束处理
  - [x] 甘特图可视化
- [x] 工艺路线/工作中心/资源管理
- [x] 多层 BOM 展开
- [x] 生产工单生成
- [x] What-if 场景模拟
- [x] 插单影响分析
- [x] 冲突检测
- [x] 成本影响分析
- [x] 风险监控与预警
- [x] LLM 对话式交互
  - [x] 意图识别
  - [x] 实体提取
  - [x] MRP 工具调用
- [x] Java-Python 集成网关
  - [x] 统一 API 入口
  - [x] 数据交换协议
  - [x] 结果持久化
  - [x] 回调机制
- [x] 模型/场景持久化
  - [x] 模型保存/加载 (pickle/joblib)
  - [x] 场景 CRUD + 对比
- [x] 监控指标
  - [x] 预测准确率追踪
  - [x] 排程效率统计
  - [x] 系统性能监控
- [x] 通知集成
  - [x] 多通道支持 (RabbitMQ/Kafka/Redis)

**交付**：智能排程 + 语音交互 + 风险预警 + 端到端集成

### Phase 3：Enterprise 企业级 ✅ 已完成

#### 已完成模块 ✅
- [x] **MPS 主生产计划** - aimrp-mps (8086)
- [x] **多仓库管理** - aimrp-warehouse (8091)
- [x] **组织架构管理** - aimrp-org (8090)
- [x] **成本管理** - aimrp-cost
- [x] **设备管理** - aimrp-equipment
- [x] **质量管理** - aimrp-quality
- [x] **报表模块** - aimrp-report (8093)
- [x] **供应商门户** - aimrp-supplier-portal (8092)
- [x] **DRP 配送需求计划** - aimrp-drp (8087)
- [x] **ERP 对接** - aimrp-integration (8094)
- [x] **AI 智能体编排** - aimrp-conversation (8095)
- [x] **AI 决策可解释性** - DecisionExplanationService

#### 微服务架构 ✅ 已完成
- [x] Nacos 注册/配置中心
- [x] Spring Cloud Gateway (8080)
- [x] Feign 跨模块调用
- [x] SkyWalking 链路追踪
- [x] Sentinel 熔断降级
- [x] Seata 分布式事务

### 迭代规划

| Iteration | 周期 | 内容 | 状态 |
|------------|------|------|------|
| 1 | 4周 | Enterprise基础 (MPS/仓库/组织) | ✅ |
| 2 | 4周 | 生产增强 (设备/质量) | ✅ |
| 3 | 4周 | 财务成本 (成本/报表) | ✅ |
| 4 | 4周 | 微服务架构/DRP/Dashboard | ✅ |
| 5 | 4周 | 预算/审批流 | ⏳ |
| 6 | 4周 | 移动端 | ⏳ |

**当前状态**：Enterprise 微服务架构已完成

---

## 七、版本规划

| 版本 | 形态 | 定位 | 状态 | 发布日期 |
|------|------|------|------|----------|
| **v1.0.0-MVP** | Web 应用 | 核心 MRP 流程 | ✅ 已发布 | 2026-01 |
| **v1.0.0-Pro** | Web 应用 | 智能化能力 | ✅ 已发布 | 2026-03 |
| **v1.0.0-Enterprise** | 微服务 | 企业级能力 | ✅ 已发布 | 2026-03 |

### 里程碑路线图

| 里程碑 | 预计日期 | 说明 |
|--------|----------|------|
| 微服务架构 | 2026-03-12 | ✅ 已完成 |
| Enterprise Phase 1 | 2026-03 | MPS/仓库/组织/设备/质量 ✅ |
| Enterprise Phase 2 | 2026-05 | 预算/审批流 |
| Enterprise Phase 3 | 2026-07 | 移动端 |
| v2.0.0-Enterprise | 2026-09 | 完整企业版 |

### v1.0.0-Pro 完成度

| 维度 | 评分 | 说明 |
|------|------|------|
| 算法实现 | 95% | Prophet/LSTM/ARIMA/OR-Tools |
| API丰富度 | 100% | 40+ API 端点 |
| 数据集成 | 90% | Java-Python 端到端 |
| 性能优化 | 90% | 缓存/并行/限流 |
| 端到端可用 | ✅ | 完整数据流 |

### Enterprise 模块清单

| 模块 | 功能 | 端口 | 状态 |
|------|------|------|------|
| aimrp-mps | MPS 主生产计划 | 8086 | ✅ |
| aimrp-drp | DRP 配送计划 | 8087 | ✅ |
| aimrp-warehouse | 多仓库管理 | 8091 | ✅ |
| aimrp-org | 组织架构管理 | 8090 | ✅ |
| aimrp-cost | 成本管理 | - | ✅ |
| aimrp-equipment | 设备管理 | - | ✅ |
| aimrp-quality | 质量管理 | - | ✅ |
| aimrp-report | 报表模块 | 8093 | ✅ |
| aimrp-supplier-portal | 供应商门户 | 8092 | ✅ |
| aimrp-integration | ERP 对接 | 8094 | ✅ |
| aimrp-conversation | AI 智能体编排 | 8095 | ✅ |

### 微服务架构清单

| 服务名 | 端口 | 说明 |
|--------|------|------|
| aimrp-gateway | 8080 | API 网关 |
| aimrp-mps | 8086 | 主生产计划 |
| aimrp-drp | 8087 | 配送需求计划 |
| aimrp-org | 8090 | 组织架构管理 |
| aimrp-warehouse | 8091 | 多仓库支持 |
| aimrp-supplier-portal | 8092 | 供应商门户 |
| aimrp-report | 8093 | 运营仪表盘 |
| aimrp-integration | 8094 | ERP 对接 |
| aimrp-conversation | 8095 | AI 智能体编排 |

---

## 八、项目结构

```
AI-MRP/
├── docs/                           # 项目文档 (50+ 文档)
│   ├── DEVELOPER_GUIDE.md         # 开发用户手册
│   ├── CODE_STANDARD.md          # 代码规范
│   ├── DEPLOYMENT_GUIDE.md      # 部署手册
│   ├── OPS_GUIDE.md             # 运维手册
│   ├── TROUBLESHOOTING_GUIDE.md # 故障排查
│   ├── BUSINESS_FLOW.md         # 业务流程
│   ├── END_TO_END_V2.md         # 端到端调用链
│   ├── PYTHON_AI_SERVICE_ARCH.md # Python架构
│   └── data-architecture/        # 数据架构 DDL
│
├── code/                           # 项目代码
│   ├── backend/                   # Java 后端 (Spring Boot)
│   │   ├── aimrp-api/           # API 入口
│   │   ├── aimrp-common/         # 公共模块
│   │   ├── aimrp-core/           # 核心域
│   │   ├── aimrp-demand/         # 需求管理
│   │   ├── aimrp-forecast/       # 预测模块 ✅ Pro
│   │   ├── aimrp-bom/           # BOM 管理
│   │   ├── aimrp-inventory/      # 库存管理
│   │   ├── aimrp-mrp/           # MRP 计算
│   │   ├── aimrp-purchase/      # 采购管理
│   │   ├── aimrp-production/     # 生产管理+排程 ✅ Pro
│   │   ├── aimrp-risk/          # 风险预警 ✅ Pro
│   │   ├── aimrp-whatif/        # What-if模拟 ✅ Pro
│   │   ├── aimrp-conversation/  # 对话服务 ✅ Pro
│   │   ├── aimrp-notification/  # 通知模块 ✅ Pro
│   │   ├── aimrp-integration/    # Java-Python集成 ✅ Pro
│   │   ├── aimrp-system/        # 用户权限
│   │   ├── aimrp-item/          # 物料主数据
│   │   └── aimrp-supplier/      # 供应商管理
│   │
│   ├── ai-service/               # Python AI 服务 (FastAPI)
│   │   ├── requirements.txt       # 依赖
│   │   ├── Dockerfile
│   │   └── app/
│   │       ├── main.py           # FastAPI 入口
│   │       ├── algorithms/       # 核心算法
│   │       │   ├── __init__.py
│   │       │   ├── forecast.py  # 预测算法 (Prophet/LSTM/ARIMA)
│   │       │   ├── scheduler.py # 排程优化 (OR-Tools)
│   │       │   ├── safety_stock.py # 安全库存
│   │       │   ├── llm.py       # LLM 客户端
│   │       │   ├── whatif.py    # What-if模拟
│   │       │   └── impact_analysis.py # 影响分析
│   │       ├── router/           # API 路由
│   │       │   ├── predict.py    # 需求预测 API
│   │       │   ├── schedule.py   # 排程优化 API
│   │       │   ├── chat.py      # 对话服务 API
│   │       │   ├── model.py     # 模型管理 API
│   │       │   ├── scenario.py   # 场景管理 API
│   │       │   └── metrics.py   # 监控指标 API
│   │       ├── integration/      # 集成层
│   │       │   ├── gateway.py   # 统一网关
│   │       │   ├── protocol.py  # 数据交换协议
│   │       │   ├── persistence.py # 结果持久化
│   │       │   ├── model_service.py # 模型服务
│   │       │   ├── scenario_service.py # 场景服务
│   │       │   └── metrics_service.py # 指标服务
│   │       ├── notification/    # 通知模块
│   │       │   └── manager.py  # 多通道通知
│   │       ├── config/           # 配置
│   │       │   └── settings.py  # Pydantic 配置
│   │       └── utils/            # 工具
│   │           ├── response.py  # 统一响应
│   │           └── performance.py # 性能优化
│   │
│   └── frontend/                 # React 前端
│       └── aimrp-admin/
│           └── src/
│               ├── api/         # API 调用
│               ├── apps/        # 页面组件
│               └── components/   # 公共组件
│
└── docker-compose.yml            # Docker 编排
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
| **What-if 模拟** | 场景对比分析，辅助决策 |
| **多通道通知** | RabbitMQ/Kafka/Redis 实时推送 |

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
| `aimrp-integration` | Java-Python集成网关 | `/api/integration/*` | ✅ |

### Enterprise 微服务模块

| 模块 | 功能 | 端口 | 状态 |
|------|------|------|------|
| `aimrp-mps` | MPS 主生产计划 | 8086 | ✅ |
| `aimrp-drp` | DRP 配送计划 | 8087 | ✅ |
| `aimrp-org` | 组织架构管理 | 8090 | ✅ |
| `aimrp-warehouse` | 多仓库支持 | 8091 | ✅ |
| `aimrp-supplier-portal` | 供应商门户 | 8092 | ✅ |
| `aimrp-report` | 运营仪表盘 | 8093 | ✅ |
| `aimrp-integration` | ERP 对接 | 8094 | ✅ |
| `aimrp-conversation` | AI 智能体编排 | 8095 | ✅ |

### Python AI Service API

| API | 功能 | 状态 |
|-----|------|------|
| `POST /predict/demand` | 需求预测 | ✅ |
| `POST /predict/safety-stock` | 安全库存计算 | ✅ |
| `POST /schedule/optimize` | 排程优化 | ✅ |
| `POST /chat/message` | LLM对话 | ✅ |
| `POST /model/save` | 模型保存 | ✅ |
| `GET /model/list` | 模型列表 | ✅ |
| `POST /scenario/save` | 场景保存 | ✅ |
| `GET /scenario/list` | 场景列表 | ✅ |
| `POST /scenario/compare` | 场景对比 | ✅ |
| `GET /metrics/dashboard` | 监控仪表盘 | ✅ |
| `POST /integration/invoke` | 统一调用入口 | ✅ |

### 待开发模块

| 模块 | 功能 |
|------|------|
| `aimrp-budget` | 预算管理 |
| `aimrp-approval` | 审批流 |
| 移动端 | Mobile App |

### API 响应规范

统一使用 `ApiResponse<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "success": true,
  "data": {...},
  "timestamp": 1709875200000
}
```

---

## 十一、分支策略

```
master           → v1.0.0-MVP (已发布)
release/pro      → Pro 发布分支 (v1.0.0-Pro) ✅
release/enterprise → Enterprise 发布分支 (v1.0.0-Enterprise) ✅
develop          → 开发主分支
```

| 分支 | 用途 | Tag |
|------|------|-----|
| `master` | 生产发布 | v1.0.0-MVP |
| `release/pro` | Pro发布 | v1.0.0-Pro |
| `release/enterprise` | Enterprise发布 | v1.0.0-Enterprise |
| `develop` | 开发主分支 | - |

---

*由 小jeep 🚙 整理*
*Punjab's AI Team © 2026*
