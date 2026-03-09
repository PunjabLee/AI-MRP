# 下一步工作清单

> **基于 MVP Review 报告**  
> **日期**：2026-03-09

---

## 一、MVP 收尾工作

### 1.1 已完成 ✅

| 任务 | 状态 |
|------|------|
| 前端 BOM 页面 | ✅ |
| 前端库存页面 | ✅ |
| AI 意图识别 | ✅ |
| AI 实体提取 | ✅ |
| AI 执行路由 | ✅ |
| 技术架构文档更新 | ✅ |

### 1.2 待完成 ⏳

| 任务 | 优先级 | 说明 |
|------|--------|------|
| 合并到 master | P0 | MVP 版本发布 |
| 集成测试运行 | P1 | 需要 Java 环境 |
| 版本号打标签 | P1 | v1.0.0-MVP |

---

## 二、Pro 阶段（5-8周）工作清单

### 2.1 AI 智能预测

| 任务 | 分支 | 功能 |
|------|------|------|
| AI 需求预测 | feature/ai-demand-predict | Prophet 预测 |
| 预测 API | feature/ai-predict-api | 预测接口+页面 |
| 安全库存推荐 | feature/ai-safety-stock | AI 安全库存 |
| 安全库存页面 | feature/ai-safety-stock-api | 页面展示 |

### 2.2 优化排程

| 任务 | 分支 | 功能 |
|------|------|------|
| OR 排程优化 | feature/ai-or-scheduler | OR-Tools |
| 排程模型 | feature/ai-or-model | 数学模型 |
| 甘特图 | feature/web-gantt | 可视化 |

### 2.3 分析能力

| 任务 | 分支 | 功能 |
|------|------|------|
| 插单影响分析 | feature/ai-impact-analysis | 影响评估 |
| 冲突检测 | feature/ai-conflict-detect | 资源冲突 |
| 成本分析 | feature/ai-cost-analysis | 成本影响 |

### 2.4 What-if 模拟

| 任务 | 分支 | 功能 |
|------|------|------|
| What-if 模拟 | feature/ai-whatif | 场景模拟 |
| 方案对比 | feature/ai-plan-compare | 方案比较 |

### 2.5 风险预警

| 任务 | 分支 | 功能 |
|------|------|------|
| 风险监控 | feature/ai-risk-monitor | 风险监测 |
| 风险预警 | feature/ai-risk-warning | 预警通知 |
| 风险页面 | feature/web-risk-page | 预警页面 |

### 2.6 Pro 集成

| 任务 | 分支 | 功能 |
|------|------|------|
| Pro 集成测试 | feature/pro-integration | 端到端测试 |

---

## 三、工作顺序建议

```
当前阶段：MVP 收尾
├── 1. 合并到 master
├── 2. 打标签 v1.0.0-MVP
└── 3. 准备 Pro 开发

第一周（Pro）
├── 1. AI 需求预测
├── 2. 预测 API + 页面
└── 3. 安全库存推荐

第二周（Pro）
├── 1. OR 排程优化
├── 2. 排程模型
└── 3. 甘特图

第三周（Pro）
├── 1. 插单影响分析
├── 2. 冲突检测
├── 3. 成本分析
└── 4. What-if 模拟

第四周（Pro）
├── 1. 风险监控
├── 2. 风险预警
├── 3. 风险页面
└── 4. Pro 集成测试
```

---

## 四、立即可执行的任务

| 优先级 | 任务 | 说明 |
|--------|------|------|
| P0 | 合并 master | git checkout master && git merge release/mvp-1.0 |
| P0 | 创建 tag | git tag v1.0.0-MVP |
| P1 | 启动 Pro 第一轮 | feature/ai-demand-predict |

---

## 五、决策点

1. **是否现在发布 MVP？** - 需要确认集成测试通过
2. **Pro 阶段优先级？** - 建议从 AI 需求预测开始
3. **团队分工？** - 需要分配负责人

---

*工作清单完成*
