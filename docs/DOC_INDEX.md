# AI MRP 项目文档清单

> **版本**：1.1  
> **日期**：2026-03-09  
> **状态**：已完成

---

## 一、文档清单

| # | 文档 | 状态 | 说明 |
|---|------|------|------|
| 1 | `README.md` | ✅ | 项目概述 |
| 2 | `docs/PRD.md` | ✅ | 产品需求文档 |
| 3 | `docs/MARKET_RESEARCH.md` | ✅ | 市场调研报告 |
| 4 | `docs/TECHNICAL_ARCHITECTURE.md` | ✅ | 技术架构设计 |
| 5 | `docs/CODE_FRAMEWORK.md` | ✅ | 代码框架设计 |
| 6 | `docs/CODE_LAYER_DESIGN.md` | ✅ | 代码分层设计（DDD） |
| 7 | `docs/GIT_WORKFLOW.md` | ✅ | Git 分支管理 |
| 8 | `docs/ITERATION_PLAN.md` | ✅ | 迭代规划 |
| 9 | `docs/BRANCH_LIST.md` | ✅ | 功能分支清单 |
| 10 | `docs/TEST_BRANCH_LIST.md` | ✅ | 测试分支清单 |
| 11 | `docs/DEVELOPMENT_PLAN.md` | ✅ | 开发计划 |
| 12 | `docs/OR_SCHEDULER_PLAN.md` | ✅ | OR排程规划 |
| 13 | `docs/MVP_REVIEW.md` | ✅ | MVP Review报告 |
| 14 | `docs/MVP_FULL_REVIEW.md` | ✅ | MVP全面Review报告 |
| 15 | `docs/NEXT_STEPS.md` | ✅ | 下一步工作清单 |
| **16** | **`docs/data-architecture/`** | ✅ | **数据架构（DDL）** |

---

## 二、核心特性总结

### 2.1 产品定位

- **产品名称**：AI MRP（智能物料需求计划系统）
- **核心特色**：可对话式配置的智能 MRP
- **目标用户**：中小制造企业、计划主管、供应链管理

### 2.2 核心功能

| 模块 | 功能 |
|------|------|
| **对话式交互** | 自然语言对话、意图识别、实体提取、执行路由 |
| **需求管理** | 销售订单、需求预测、需求池 |
| **BOM 管理** | BOM 维护、BOM 展开、多层展开 |
| **库存管理** | 库存查询、库存交易、库龄分析 |
| **MRP 计算** | MRP 引擎、采购建议、生产建议 |
| **AI 智能** | 需求预测、安全库存推荐、OR 排程 |
| **影响分析** | 插单影响、What-if 模拟、成本分析 |
| **风险预警** | 供应商风险、库存风险、需求突变 |
| **沙箱机制** | 数据隔离、计算预览、业务模拟、参数调优 |

### 2.3 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Java 17 + Spring Boot 3 + PostgreSQL |
| AI 服务 | Python + LangChain + OR-Tools + Prophet |
| 前端 | React + TypeScript + Vite |
| 部署 | Docker + Kubernetes |
| 数据库 | PostgreSQL 15+ (主从/读写分离) |

### 2.4 沙箱机制

| 方案 | 架构 | 场景 |
|------|------|------|
| 独立数据库 | aimrp_prod + aimrp_sandbox | 生产环境 |
| 容器化沙箱 | 独立 PostgreSQL 容器 | 开发/测试 |
| Schema 隔离 | 同一库不同 schema | 低成本 |

---

## 三、迭代规划

### 3.1 阶段划分

| 阶段 | 时间 | 交付 | 规划数 | 已完成 |
|------|------|------|--------|--------|
| MVP | W1-4 | 对话式 MRP 原型 | 22 | 21 |
| Pro | W5-8 | 智能化 MRP | 20 | 11 |
| Enterprise | W9-12 | 企业级 MRP | 12 | 0 |
| **总计** | **12 周** | | **54** | **32** |

### 3.2 里程碑

| 里程碑 | 时间 | 交付 | 状态 |
|--------|------|------|------|
| M1 | W2 末 | 项目骨架 + 基础 CRUD | ✅ |
| M2 | W4 末 | 对话式 MRP 原型 | ✅ |
| M3 | W8 末 | 智能化 MRP | ⏳ 进行中 |
| M4 | W12 末 | 企业级 MRP | ⏳ |

---

## 四、文档索引

### 4.1 产品

- `README.md` - 项目概述
- `docs/PRD.md` - 产品需求文档（完整功能清单）

### 4.2 技术

- `docs/TECHNICAL_ARCHITECTURE.md` - 技术架构（DDD + 沙箱）
- `docs/CODE_FRAMEWORK.md` - 代码框架（Java/Python/前端）

### 4.3 流程

- `docs/GIT_WORKFLOW.md` - Git 分支管理规范
- `docs/ITERATION_PLAN.md` - 迭代规划（12 周）
- `docs/DEVELOPMENT_PLAN.md` - 开发计划

### 4.4 实施

- `docs/BRANCH_LIST.md` - 功能分支清单（51 个）
- `docs/TEST_BRANCH_LIST.md` - 测试分支清单（23 个）
- `docs/MARKET_RESEARCH.md` - 市场调研报告

---

## 五、版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0 | 2026-03-08 | 初始版本 |
| 1.1 | 2026-03-09 | 添加OR排程规划，更新MVP/Pro状态 |

---

*文档清单完成*
