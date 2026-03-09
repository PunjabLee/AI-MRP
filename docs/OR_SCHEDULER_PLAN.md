# OR 排程优化 - 整体规划

> **日期**：2026-03-09  
> **版本**：1.0

---

## 一、目标

实现基于运筹学（OR）的生产排程优化，自动生成高效的生产计划。

---

## 二、现有数据模型

### 2.1 生产工单（已存在）

| 字段 | 说明 |
|------|------|
| moNo | 工单编号 |
| itemCode | 物料编码 |
| planQty | 计划数量 |
| startDate | 计划开始 |
| endDate | 计划结束 |
| priority | 优先级 |
| status | 状态 |

### 2.2 物料主数据（已存在）

| 字段 | 说明 |
|------|------|
| itemCode | 物料编码 |
| source | 来源（MAKE/BUY） |
| leadTime | 采购/生产周期 |

---

## 三、需要补充的数据模型

### 3.1 工艺路线（Process Route）

```
m_process_route
├── route_id          主键
├── item_code         物料编码
├── route_name        路线名称
├── version           版本
├── status            状态
└── lines             工序明细 []
    ├── line_id       工序ID
    ├── operation_no  工序号
    ├── operation_name 工序名称
    ├── work_center   工作中心
    ├── std_hours     标准工时（小时）
    ├── setup_time    准备时间（分钟）
    ├── queue_time    排队时间（小时）
```

### 3.2 工作中心（Work Center）

```
m_work_center
├── wc_id             主键
├── wc_code           编码
├── wc_name           名称
├── capacity          产能（小时/天）
├── efficiency        效率系数
├── status            状态
```

### 3.3 资源主数据（Resource）

```
m_resource
├── resource_id       主键
├── resource_code     编码
├── resource_name     名称
├── work_center_id    所属工作中心
├── resource_type     类型（机器/人工）
├── capacity          产能
├── status            状态
```

### 3.4 工单工序明细

```
t_mo_operation
├── mo_id             工单ID
├── operation_id      工序ID
├── operation_no      工序号
├── status            状态
├── plan_start_date   计划开始
├── plan_end_date     计划结束
├── actual_start_date 实际开始
├── actual_end_date   实际结束
```

---

## 四、OR 排程模型

### 4.1 数学模型

**目标函数**：
```
最小化总完工时间（Makespan）
或
最小化延期订单数量
```

**约束条件**：
1. 工序顺序约束（必须按工艺路线顺序）
2. 资源能力约束（不能超过工作中心产能）
3. 优先级约束（高优先级工单优先排程）
4. 交期约束（尽量满足交期）

### 4.2 求解算法

| 算法 | 适用场景 | 复杂度 |
|------|----------|--------|
| 启发式（规则） | 简单排程 | O(n log n) |
| 遗传算法 | 中等规模 | O(n² log n) |
| 整数规划 | 精确求解 | NP难 |

---

## 五、开发任务分解

### 5.1 数据模型补充

| 任务 | 分支 | 工作量 |
|------|------|--------|
| 工艺路线管理 | feature/process-route | 2d |
| 工作中心管理 | feature/work-center | 1d |
| 资源管理 | feature/resource | 1d |
| 工单工序明细 | feature/mo-operation | 2d |

### 5.2 OR 排程引擎

| 任务 | 分支 | 工作量 |
|------|------|--------|
| 排程模型定义 | feature/or-scheduler | 2d |
| 启发式排程 | feature/or-scheduler | 2d |
| 遗传算法优化 | feature/or-scheduler | 3d |
| 排程结果输出 | feature/or-scheduler | 1d |

### 5.3 集成与页面

| 任务 | 分支 | 工作量 |
|------|------|--------|
| 排程 API | feature/or-scheduler | 1d |
| 甘特图页面 | feature/web-gantt | 3d |
| 集成测试 | feature/pro-integration | 2d |

---

## 六、执行顺序

```
第一阶段：数据模型
├── 1. 工艺路线管理
├── 2. 工作中心管理
├── 3. 资源管理
└── 4. 工单工序明细

第二阶段：排程引擎
├── 1. 排程模型
├── 2. 启发式排程
├── 3. 遗传算法（可选）
└── 4. 排程 API

第三阶段：前端集成
├── 1. 甘特图
└── 2. 集成测试
```

---

## 七、总结

| 项目 | 数量 |
|------|------|
| 新增数据表 | 4 |
| 新增功能模块 | 3 |
| 预计工时 | 15d |

---

*规划完成*
