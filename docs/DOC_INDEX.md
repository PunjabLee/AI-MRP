# AI MRP 项目文档清单

> **版本**：6.0  
> **日期**：2026-03-12  
> **状态**：已完成模块设计分类

---

## 文档目录结构

```
docs/
├── 01-product/           # 产品规划 (4)
├── 02-business/          # 业务架构 (1)
├── 03-application/      # 应用架构 (17)
│   ├── 01-core-business/      # 核心业务层 (6)
│   ├── 02-intelligent-engine/ # 智能引擎层 (6)
│   └── 03-enterprise/         # Enterprise扩展 (5)
├── 04-technology/        # 技术架构 (10)
├── 05-data/             # 数据架构 (6)
├── 06-development/       # 开发规范 (5)
├── 07-api/              # API文档 (1)
├── 08-operations/       # 运维文档 (3)
├── 09-project/          # 项目管理
│   ├── 01-process/      # 流程规范 (3)
│   └── 02-planning/     # 规划计划 (8)
├── 10-reviews/          # Review报告 (12)
├── 11-tasks/            # 任务清单 (7)
└── 99-archive/          # 历史归档
```

---

## 一、产品规划 (01-product)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `PRD.md` | 产品需求文档完整版 | ✅ |
| 2 | `MARKET_RESEARCH.md` | 市场调研报告 | ✅ |
| 3 | `BUSINESS_LOGIC_MEMO.md` | 业务逻辑备忘录 | ✅ |
| 4 | `MRP_FACTORS.md` | MRP业务因子 | ✅ |

---

## 二、业务架构 (02-business)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `BUSINESS_FLOW.md` | 业务流程文档 | ✅ |

---

## 三、应用架构 (03-application) - 模块设计

### 03-1 核心业务层 (01-core-business)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `DEMAND_MANAGEMENT_DESIGN.md` | 需求管理模块 | ✅ |
| 2 | `BOM_MANAGEMENT_DESIGN.md` | BOM管理模块 | ✅ |
| 3 | `INVENTORY_MANAGEMENT_DESIGN.md` | 库存管理模块 | ✅ |
| 4 | `MRP_CALCULATION_DESIGN.md` | MRP计算模块 | ✅ |
| 5 | `PURCHASE_MANAGEMENT_DESIGN.md` | 采购管理模块 | ✅ |
| 6 | `PRODUCTION_MANAGEMENT_DESIGN.md` | 生产管理模块 | ✅ |

### 03-2 智能引擎层 (02-intelligent-engine)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `LLM_UNDERSTANDING_DESIGN.md` | LLM理解模块 | ✅ |
| 2 | `OR_SOLVER_DESIGN.md` | OR求解模块 | ✅ |
| 3 | `PREDICTION_ANALYSIS_DESIGN.md` | 预测分析模块 | ✅ |
| 4 | `ANOMALY_DIAGNOSIS_DESIGN.md` | 异常诊断模块 | ✅ |
| 5 | `RISK_WARNING_DESIGN.md` | 风险预警模块 | ✅ |
| 6 | `IMPACT_ANALYSIS_DESIGN.md` | 影响分析模块 | ✅ |

### 03-3 Enterprise扩展 (03-enterprise)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `APPROVAL_FLOW_DESIGN.md` | 审批流模块 | ✅ |
| 2 | `BUDGET_MODULE_DESIGN.md` | 预算管理模块 | ✅ |
| 3 | `DRP_MODULE_DESIGN.md` | DRP配送模块 | ✅ |
| 4 | `MOBILE_APP_DESIGN.md` | 移动端模块 | ✅ |
| 5 | `DASHBOARD_DESIGN.md` | Dashboard模块 | ✅ |

---

## 四、技术架构 (04-technology)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `TECHNICAL_ARCHITECTURE.md` | 技术架构设计 | ✅ |
| 2 | `CODE_FRAMEWORK.md` | 代码框架设计 | ✅ |
| 3 | `CODE_LAYER_DESIGN.md` | 代码分层设计(DDD) | ✅ |
| 4 | `MICROSERVICES_ARCHITECTURE_PLAN.md` | 微服务架构规划 | ✅ |
| 5 | `PYTHON_AI_SERVICE_ARCH.md` | Python AI服务架构 | ✅ |
| 6 | `MODULE_DEPENDENCY.md` | 模块依赖关系图 | ✅ |
| 7 | `CORE_FLOWCHARTS.md` | 核心流程图(完整方法链) | ✅ |
| 8 | `END_TO_END_ARCH.md` | 端到端架构图 | ✅ |
| 9 | `END_TO_END_DETAIL.md` | 端到端调用链详解 | ✅ |
| 10 | `END_TO_END_V2.md` | 超完整端到端技术手册 | ✅ |

---

## 五、数据架构 (05-data)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `DATA_ARCHITECTURE.md` | 数据架构设计 | ✅ |
| 2 | `DOMAIN_MODEL.md` | 领域模型 | ✅ |
| 3 | `DATABASE_DDL.sql` | 数据库DDL | ✅ |
| 4 | `DATABASE_DDL_SUPPLEMENT.sql` | 补充DDL | ✅ |
| 5 | `DATABASE_DDL_OR_SCHEDULER.sql` | OR排程DDL | ✅ |
| 6 | `README.md` | 数据架构说明 | ✅ |

---

## 六、开发规范 (06-development)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `DEVELOPER_GUIDE.md` | 开发用户手册 | ✅ |
| 2 | `CODE_STANDARD.md` | 代码规范 | ✅ |
| 3 | `INTEGRATION_GUIDE.md` | Java集成指南 | ✅ |
| 4 | `ALGORITHM_REVIEW.md` | 算法Review | ✅ |
| 5 | `DEVELOPMENT_ENVIRONMENT.md` | 开发环境 | ✅ |

---

## 七、API文档 (07-api)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `API_REFERENCE.md` | API接口文档 | ✅ |

---

## 八、运维文档 (08-operations)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `DEPLOYMENT_GUIDE.md` | 部署手册(Docker/K8s/微服务) | ✅ |
| 2 | `OPS_GUIDE.md` | 运维手册 | ✅ |
| 3 | `TROUBLESHOOTING_GUIDE.md` | 故障排查手册 | ✅ |

---

## 九、项目管理 (09-project)

### 09-1 流程规范 (01-process)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `GIT_WORKFLOW.md` | Git工作流 | ✅ |
| 2 | `BRANCH_LIST.md` | 功能分支清单 | ✅ |
| 3 | `TEST_BRANCH_LIST.md` | 测试分支清单 | ✅ |

### 09-2 规划计划 (02-planning)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `ITERATION_PLAN.md` | 迭代规划 | ✅ |
| 2 | `DEVELOPMENT_PLAN.md` | 开发计划 | ✅ |
| 3 | `ACTION_PLAN.md` | 行动计划 | ✅ |
| 4 | `ENTERPRISE_ITERATION_PLAN.md` | Enterprise迭代规划 | ✅ |
| 5 | `ENTERPRISE_PLAN_SUGGESTION.md` | Enterprise规划建议 | ✅ |
| 6 | `ENTERPRISE_REVIEW.md` | Enterprise Review | ✅ |
| 7 | `OR_SCHEDULER_PLAN.md` | OR排程规划 | ✅ |
| 8 | `M3_DELIVERY.md` | M3交付物 | ✅ |

---

## 十、Review报告 (10-reviews)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `MVP_REVIEW.md` | MVP Review报告 | ✅ |
| 2 | `MVP_PRO_REVIEW.md` | MVP+Pro Review报告 | ✅ |
| 3 | `COMPREHENSIVE_REVIEW.md` | 全面Review报告 | ✅ |
| 4 | `COMPREHENSIVE_REVIEW_V2.md` | 全面Review报告V2 | ✅ |
| 5 | `COMPREHENSIVE_REVIEW_V3.md` | 全面Review报告V3 | ✅ |
| 6 | `FINAL_REVIEW.md` | 最终Review报告 | ✅ |
| 7 | `OVERALL_REVIEW.md` | 整体完成度Review | ✅ |
| 8 | `ARCHITECTURE_REVIEW.md` | 架构Review | ✅ |
| 9 | `ARCHITECTURE_REVIEW_JAVA.md` | Java架构Review | ✅ |
| 10 | `CONTROLLER_VO_REVIEW.md` | Controller VO Review | ✅ |
| 11 | `PRODUCT_REVIEW.md` | 产品Review | ✅ |
| 12 | `MILESTONE_REVIEW.md` | 里程碑Review | ✅ |

---

## 十一、任务清单 (11-tasks)

| # | 文档 | 说明 | 状态 |
|---|------|------|------|
| 1 | `MVP_TASKS.md` | MVP任务清单 | ✅ |
| 2 | `PRO_TASKS_ANALYSIS.md` | Pro任务分析 | ✅ |
| 3 | `NEXT_PLAN.md` | 下一步计划 | ✅ |
| 4 | `NEXT_STEPS.md` | 后续步骤 | ✅ |
| 5 | `REMAINING_TASKS.md` | 剩余任务 | ✅ |
| 6 | `TASK_CHECKLIST.md` | 任务检查清单 | ✅ |
| 7 | `TASK_PRIORITY_CHECKLIST.md` | 任务优先级清单 | ✅ |

---

## 十二、历史归档 (99-archive)

| 目录 | 说明 |
|------|------|
| `backup-2026-03-12/` | 历史备份 |

---

## 文档统计

| 目录 | 数量 |
|------|------|
| 01-product | 4 |
| 02-business | 1 |
| 03-application | 17 |
| 04-technology | 10 |
| 05-data | 6 |
| 06-development | 5 |
| 07-api | 1 |
| 08-operations | 3 |
| 09-project | 11 |
| 10-reviews | 12 |
| 11-tasks | 7 |
| **总计** | **77** |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-03-09 | 初始版本 |
| 2.0 | 2026-03-11 | 完整更新 |
| 3.0 | 2026-03-12 | 新增5个Enterprise模块设计 |
| 4.0 | 2026-03-12 | 目录结构整理完成 |
| 5.0 | 2026-03-12 | 补充12个核心模块设计 |
| 6.0 | 2026-03-12 | 模块设计分类完成 |

---

*由 小jeep 🚙 整理*
*Punjab's AI Team © 2026*
