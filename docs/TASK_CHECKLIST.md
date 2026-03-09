# 待完善任务清单（整合版）

> **日期**：2026-03-09  
> **版本**：2.0  
> **说明**：整合架构规范与业务逻辑差距

---

## 一、架构规范完善（基于编码规范 v1.2）

### P0 - 必须完善

| # | 任务 | 模块 | 差距 | 工时 |
|---|------|------|------|------|
| A1 | 补充 domain/service 层 | bom/inventory/purchase | 缺失 | 3d |
| A2 | 补充 application/service 层 | bom/inventory/purchase | 缺失 | 3d |
| A3 | 补充 infrastructure/mapper 层 | bom/inventory/purchase/production | 缺失 | 3d |
| A4 | 添加 domain/repository 接口 | 所有模块 | 建议 | 2d |

### P1 - 应该完善

| # | 任务 | 模块 | 差距 | 工时 |
|---|------|------|------|------|
| A5 | 添加 application/dto | demand/item/supplier | 缺失 | 1d |
| A6 | 添加 application/command | demand/item/supplier | 缺失 | 1d |
| A7 | 添加 application/query | demand/item/supplier | 缺失 | 1d |
| A8 | 添加 api/assembler | 所有模块 | 缺失 | 2d |
| A9 | 添加 domain/valueobject | mrp/forecast | 缺失 | 1d |

---

## 二、业务逻辑完善（基于 BUSINESS_LOGIC_MEMO）

### P0 - 必须完善

| # | 任务 | 模块 | 差距 | 工时 |
|---|------|------|------|------|
| B1 | MRP 接入物料数据 | mrp | 模拟数据 | 1d |
| B2 | MRP 接入 BOM 数据 | mrp | 模拟数据 | 1d |
| B3 | MRP 接入库存数据 | mrp | 模拟数据 | 1d |
| B4 | MRP 接入订单数据 | mrp | 模拟数据 | 1d |
| B5 | conversation 模块合并 | conversation | 仅在master | 1d |

### P1 - 应该完善

| # | 任务 | 模块 | 差距 | 工时 |
|---|------|------|------|------|
| B6 | 预测接入历史订单 | forecast | 模拟数据 | 2d |
| B7 | 风险接入真实数据 | risk | 模拟数据 | 2d |
| B8 | 风险预警接入消息 | risk | 日志输出 | 2d |
| B9 | What-if 场景持久化 | whatif | 内存 | 2d |
| B10 | 完善事务管理 | 所有模块 | 无 | 3d |

---

## 三、Enterprise 阶段（后期）

### P1 - 建议开发

| # | 任务 | 预估 |
|---|------|------|
| C1 | 组织架构管理 | 3d |
| C2 | 多仓库支持 | 3d |
| C3 | 运营仪表盘 | 3d |
| C4 | MPS+MRP+DRP 联动 | 4d |
| C5 | 供应商门户 | 4d |
| C6 | ERP 对接 | 3d |

---

## 四、任务优先级汇总

### 4.1 P0 - 必须完成

| # | 类型 | 任务 | 总工时 |
|---|------|------|----------|
| A1-A4 | 架构 | 补充缺失分层 | 11d |
| B1-B5 | 业务 | 数据接入+合并 | 6d |
| **合计** | | | **17d** |

### 4.2 P1 - 应该完成

| # | 类型 | 任务 | 总工时 |
|---|------|------|----------|
| A5-A9 | 架构 | 规范完善 | 8d |
| B6-B10 | 业务 | 逻辑完善 | 11d |
| C1-C6 | Enterprise | 规划功能 | 20d |
| **合计** | | | **39d** |

---

## 五、执行顺序建议

### 5.1 第一阶段：架构规范（1-2周）

```
Week 1:
├── A1: bom/inventory/purchase domain/service
├── A2: bom/inventory/purchase application/service
└── A3: bom/inventory/purchase mapper

Week 2:
├── A4: domain/repository 接口
├── A5-A7: dto/command/query（可选）
└── 同步：B1-B4 MRP数据接入
```

### 5.2 第二阶段：业务完善（3-4周）

```
Week 3:
├── B6: 预测数据接入
├── B7: 风险数据接入
└── B8: 风险预警消息

Week 4:
├── B9: What-if 持久化
├── B10: 事务管理
└── A8-A9: assembler/valueobject（可选）
```

### 5.3 第三阶段：Enterprise（5-8周）

```
Week 5-6: C1-C3 组织/仓库/仪表盘
Week 7-8: C4-C6 联动/门户/对接
```

---

## 六、总结

| 阶段 | P0 任务 | P1 任务 | 总工时 |
|------|----------|----------|----------|
| 架构规范 | 11d | 8d | 19d |
| 业务完善 | 6d | 11d | 17d |
| Enterprise | - | 20d | 20d |
| **总计** | **17d** | **39d** | **56d** |

---

*清单整理完成 - 2026-03-09*
