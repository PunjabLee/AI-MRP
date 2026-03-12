# 待完善任务清单（整合版）

> **日期**：2026-03-12
> **版本**：3.0
> **说明**：已完成微服务架构改造和Enterprise核心模块

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

## 三、Enterprise 阶段（已完成 ✅ 2026-03-12）

### P0 - 已完成

| # | 任务 | 状态 | 端口 | 说明 |
|---|------|------|------|------|
| C1 | 组织架构管理 | ✅ 完成 | 8090 | aimrp-org 微服务 |
| C2 | 多仓库支持 | ✅ 完成 | 8091 | aimrp-warehouse 微服务 |
| C3 | 运营仪表盘 | ✅ 完成 | 8093 | aimrp-report 微服务 |
| C4 | MPS+MRP+DRP 联动 | ✅ 完成 | 8086/8087 | aimrp-mps/aimrp-drp |
| C5 | 供应商门户 | ✅ 完成 | 8092 | aimrp-supplier-portal 微服务 |
| C6 | ERP 对接 | ✅ 完成 | 8094 | aimrp-integration 微服务 |
| C7 | AI智能体编排 | ✅ 完成 | 8095 | aimrp-conversation 微服务 |
| C8 | AI决策可解释性 | ✅ 完成 | 8095 | DecisionExplanationService |

### 微服务架构（已完成 ✅ 2026-03-12）

| # | 任务 | 优先级 | 状态 | 说明 |
|---|------|--------|------|------|
| D1 | Nacos 注册/配置中心 | P0 | ✅ 完成 | 所有模块已配置 |
| D2 | Spring Cloud Gateway | P0 | ✅ 完成 | 路由所有微服务 |
| D3 | Feign 调用改造 | P1 | ✅ 完成 | 跨模块调用 |
| D4 | SkyWalking 链路追踪 | P2 | ✅ 完成 | APM接入 |
| D5 | Sentinel 熔断降级 | P2 | ✅ 完成 | 流量控制 |
| D6 | ELK 日志接入 | P2 | ⏳ 待接入 | 日志收集待配置 |

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

### 4.3 P2 - 微服务架构

| # | 类型 | 任务 | 总工时 |
|---|------|------|----------|
| D1-D2 | 基础设施 | Nacos + Gateway | 6d |
| D3 | 调用改造 | Feign | 5d |
| D4-D6 | 可观测性 | SkyWalking + Sentinel + ELK | 7d |
| **合计** | | | **18d** |

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
| Enterprise | ✅ 完成 | ✅ 完成 | 20d |
| 微服务架构 | ✅ 完成 | ✅ 完成 | 18d |
| **总计** | **17d** | **39d** | **56d** |

---

## 七、已完成微服务清单（2026-03-12）

| 服务名 | 端口 | 说明 | 状态 |
|--------|------|------|------|
| aimrp-gateway | 8080 | API网关 | ✅ |
| aimrp-mps | 8086 | 主生产计划 | ✅ |
| aimrp-drp | 8087 | 配送需求计划 | ✅ |
| aimrp-org | 8090 | 组织架构管理 | ✅ |
| aimrp-warehouse | 8091 | 多仓库支持 | ✅ |
| aimrp-supplier-portal | 8092 | 供应商门户 | ✅ |
| aimrp-report | 8093 | 运营仪表盘 | ✅ |
| aimrp-integration | 8094 | ERP对接 | ✅ |
| aimrp-conversation | 8095 | AI智能体编排 | ✅ |

---

*清单整理完成 - 2026-03-12 v3.0 更新*
