# AI MRP Git 分支管理规范

> **版本**：1.2  
> **日期**：2026-03-09  
> **说明**：已更新Enterprise阶段规划（12周），Pro已发布

---

## 一、分支模型

采用 **Git Flow** + **Trunk-Based Development** 混合模型：

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Git Flow 分支模型                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   master (发布分支) ──────────────────────────────────────────────▶    │
│       ▲                                                                  │
│       │  merge                                                          │
│       │                                                                  │
│   release/v1.0 (预发布) ──────────────────────────────────────────▶    │
│       ▲                                                                  │
│       │  merge                                                          │
│       │                                                                  │
│   develop (开发主干) ─────────────────────────────────────────────▶    │
│     ▲  ▲  ▲  ▲  ▲  ▲  ▲                                                │
│     │  │  │  │  │  │  │                                                │
│     │  │  │  │  │  │  │                                                │
│  feature/xxx  feature/yyy  hotfix/zzz                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 二、分支类型

| 分支类型 | 命名规则 | 用途 | 生命周期 |
|----------|----------|------|----------|
| **master** | `master` | 生产发布 | 永久 |
| **develop** | `develop` | 开发主干 | 永久 |
| **release** | `release/v{x.y}` | 预发布 | 临时 |
| **hotfix** | `hotfix/xxx` | 紧急修复 | 临时 |
| **feature** | `feature/xxx` | 功能开发 | 临时 |
| **bugfix** | `bugfix/xxx` | Bug 修复 | 临时 |

---

## 三、功能分支命名

### 3.1 功能分支命名规范

```
feature/<模块>-<功能简述>

示例：
├── feature/demand-order-crud           # 需求-订单 CRUD
├── feature/demand-forecast              # 需求-销售预测
├── feature/bom-management               # BOM 管理
├── feature/inventory-stock              # 库存查询
├── feature/mrp-calculate                # MRP 计算
├── feature/mrp-purchase-suggestion      # MRP 采购建议
├── feature/ai-chat                     # AI 对话
├── feature/ai-intent                   # AI 意图识别
├── feature/ai-or-scheduler             # AI OR 排程
├── feature/ai-demand-predict            # AI 需求预测
├── feature/ai-risk-warning             # AI 风险预警
└── feature/impact-analysis             # 影响分析
```

### 3.2 完整功能分支清单

#### MVP 阶段（4 周）

| 分支 | 功能 | 优先级 | 周次 |
|------|------|--------|------|
| `feature/project-init` | 项目初始化 | P0 | W1 |
| `feature/database-design` | 数据库设计 | P0 | W1 |
| `feature/common-module` | 公共模块 | P0 | W1 |
| `feature/demand-order-crud` | 销售订单 CRUD | P0 | W2 |
| `feature/bom-management` | BOM 管理 | P0 | W2 |
| `feature/inventory-stock` | 库存管理 | P0 | W2 |
| `feature/mrp-calculate` | MRP 计算引擎 | P0 | W3 |
| `feature/mrp-purchase-suggestion` | 采购建议生成 | P0 | W3 |
| `feature/ai-chat-ui` | 对话窗 UI | P0 | W4 |
| `feature/ai-intent` | 意图识别 | P0 | W4 |
| `feature/ai-execute` | 执行路由 | P0 | W4 |

#### Pro 阶段（4 周）

| 分支 | 功能 | 优先级 | 周次 |
|------|------|--------|------|
| `feature/ai-demand-predict` | AI 需求预测 | P0 | W5-6 |
| `feature/ai-safety-stock` | AI 安全库存推荐 | P0 | W5-6 |
| `feature/ai-or-scheduler` | OR 智能排程 | P0 | W6-7 |
| `feature/mrp-gantt-chart` | 甘特图展示 | P1 | W7 |
| `feature/impact-analysis` | 插单影响分析 | P0 | W7-8 |
| `feature/ai-whatif` | What-if 模拟 | P0 | W7-8 |
| `feature/ai-risk-warning` | 风险预警 | P0 | W8 |

#### Enterprise 阶段（12 周）

##### 第一批次：基础架构（9-12周）

| 分支 | 功能 | 优先级 | 周次 |
|------|------|--------|------|
| `feature/org-structure` | 组织架构管理 | P0 | W9 |
| `feature/org-api` | 组织 API | P0 | W9 |
| `feature/multi-warehouse` | 多仓库支持 | P0 | W9-10 |
| `feature/multi-factory` | 多工厂/基地 | P0 | W10 |
| `feature/mps-master` | MPS 主生产计划 | P0 | W10-11 |
| `feature/supplier-portal` | 供应商门户 | P1 | W11-12 |

##### 第二批次：生产增强（13-16周）

| 分支 | 功能 | 优先级 | 周次 |
|------|------|--------|------|
| `feature/multi-production-line` | 多产线管理 | P0 | W13 |
| `feature/outsourcing` | 委外加工 | P0 | W13-14 |
| `feature/equipment-mgmt` | 设备管理 | P1 | W14 |
| `feature/quality-mgmt` | 质量管理 | P1 | W14-15 |
| `feature/cost-mgmt` | 成本管理 | P1 | W15-16 |

##### 第三批次：运营支撑（17-20周）

| 分支 | 功能 | 优先级 | 周次 |
|------|------|--------|------|
| `feature/drp` | DRP 配送需求计划 | P1 | W17 |
| `feature/budget-mgmt` | 预算管理 | P1 | W17-18 |
| `feature/approval-flow` | 审批流 | P1 | W18 |
| `feature/web-dashboard` | 运营仪表盘 | P1 | W18-19 |
| `feature/web-report` | 报表页面 | P1 | W19 |
| `feature/report-export` | 报表导出 | P1 | W19 |
| `feature/erp-integration` | ERP 对接 | P2 | W19-20 |
| `feature/open-api` | 开放平台 | P2 | W20 |

##### 微服务架构（贯穿）

| 分支 | 功能 | 优先级 | 周次 |
|------|------|--------|------|
| `feature/nacos-deploy` | Nacos 注册/配置中心 | P0 | W9-11 |
| `feature/gateway` | Spring Cloud Gateway | P0 | W11-12 |
| `feature/feign-migration` | Feign 调用改造 | P1 | W13-15 |
| `feature/skywalking` | SkyWalking 链路追踪 | P2 | W15-16 |
| `feature/sentinel` | Sentinel 熔断降级 | P2 | W16-17 |
| `feature/elk` | ELK 日志接入 | P2 | W17-18 |

---

## 四、功能开发标准流程功能开发标准流程

#4.1 开发步骤

```
1. 从 develop 创建功能分支
   git checkout develop
   git checkout -b feature/xxx

2. 开发功能代码
   - 编写 Entity/Mapper/Service/Controller
   - 编写单元测试

3. 开发完成后更新业务逻辑备忘录（必须）
   - 更新 docs/BUSINESS_LOGIC_MEMO.md
   - 增量更新，不删除历史内容
   - 记录实现差距

4. 提交代码
   git add -A
   git commit -m "feat: 功能描述"

5. 推送并创建 PR
   git push -u origin feature/xxx

6. 合并到 develop
   - 通过 PR 合并或直接合并
```

#4.2 业务逻辑备忘录更新规范

每次功能开发完成后，必须更新 `docs/BUSINESS_LOGIC_MEMO.md`：

| 更新内容 | 说明 |
|----------|------|
| 新增 Service | 记录功能实现 |
| 业务逻辑差距 | 标注简化/模拟/待接入 |
| 数据接入状态 | 模拟/数据库/待开发 |
| Mapper/Service 缺失 | 记录待补充 |

#4.3 更新模板

```markdown
### [模块名] 功能名

| 组件 | 文件 | 实现程度 | 数据来源 | 状态 |
|------|------|----------|----------|------|
| Service | XxxService.java | 框架完整 | 模拟 | 待接入 |
```

---

## 七、版本发布分支

#4.1 发布分支命名

```
release/v<major>.<minor>

示例：
├── release/v1.0           # v1.0 发布
├── release/v1.1           # v1.1 发布
└── release/v2.0           # v2.0 发布
```

#4.2 版本号规范

```
<major>.<minor>.<patch>

示例：1.0.0

- major: 主版本，不兼容变更
- minor: 次版本，向后兼容功能新增
- patch: 补丁版本，向后兼容 bug 修复
```

#4.3 发布流程

```
1. 从 develop 创建 release/v1.0 分支
2. 在 release 分支进行测试和修复
3. 合并到 master 打标签 v1.0.0
4. 合并回 develop
5. 删除 release 分支
```

---

## 七、Hotfix 分支

### 5.1 命名规范

```
hotfix/<问题描述>

示例：
├── hotfix/order-crash              # 订单模块崩溃
├── hotfix/mrp-calculate-error     # MRP 计算错误
└── hotfix/security-vulnerability  # 安全漏洞
```

### 5.2 处理流程

```
1. 从 master 创建 hotfix/xxx
2. 修复并测试
3. 合并到 master 打标签
4. 合并到 develop
5. 删除 hotfix 分支
```

---

## 七、分支操作命令

### 6.1 开始新功能

```bash
# 1. 更新 develop
git checkout develop
git pull origin develop

# 2. 创建功能分支
git checkout -b feature/demand-order-crud develop

# 3. 开发完成后，提交 PR 到 develop
git push origin feature/demand-order-crud
```

### 6.2 发布版本

```bash
# 1. 创建 release 分支
git checkout -b release/v1.0 develop

# 2. 测试修复...

# 3. 合并到 master
git checkout master
git merge release/v1.0
git tag v1.0.0 -m "Release v1.0.0"

# 4. 合并回 develop
git checkout develop
git merge release/v1.0

# 5. 删除 release 分支
git branch -d release/v1.0
```

### 6.3 紧急修复

```bash
# 1. 从 master 创建 hotfix 分支
git checkout -b hotfix/fix-bug master

# 2. 修复后合并
git checkout master
git merge hotfix/fix-bug
git tag v1.0.1 -m "Hotfix v1.0.1"

git checkout develop
git merge hotfix/fix-bug

# 3. 删除 hotfix 分支
git branch -d hotfix/fix-bug
```

---

## 七、PR 规范

### 7.1 PR 标题格式

```
[<分支类型>] <功能描述>

示例：
[feature] 添加销售订单 CRUD 功能
[bugfix] 修复 MRP 计算边界问题
[hotfix] 修复库存负数问题
```

### 7.2 PR 内容模板

```markdown
## 变更描述
<!-- 简要说明本次变更 -->

## 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 重构
- [ ] 文档更新

## 关联 Issue
<!-- 关联的需求或任务 -->

## 测试情况
- [ ] 单元测试通过
- [ ] 本地测试通过
- [ ] 需要协助测试

##  reviewers
<!-- 指定 reviewers -->
```

---

## 八、分支保护规则

### 8.1 保护分支

| 分支 | 保护规则 |
|------|----------|
| `master` | 必须 PR + 至少 1 人 review + CI 通过 |
| `develop` | 必须 PR + 至少 1 人 review + CI 通过 |
| `release/*` | 必须 PR + 至少 1 人 review |

### 8.2 CI/CD 配置

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop, 'release/**']
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build
        run: ./mvnw clean package
      - name: Test
        run: ./mvnw test
```

---

*Git 分支管理规范完成*
