---
AIGC:
    ContentProducer: Minimax Agent AI
    ContentPropagator: Minimax Agent AI
    Label: AIGC
    ProduceID: 0f14883a2c3a29854dfc40ff0272b87c
    PropagateID: 0f14883a2c3a29854dfc40ff0272b87c
    ReservedCode1: 3045022100d0cec7f9abc325b63bb88fd7caa0fa85270fcebc5f9ab4e59b9776cb4c47eae9022050bae1e8236e546dc6770b5378943b11270d2c0b28a90bbfdddbbc5346770438
    ReservedCode2: 3045022100d4a7a95d5ad2b0de43a12de60d2a1238f7521b20e25a2bbc9938d6d934a4c10d02207d232afc540c7debb4985c698379b364b9f4cfedf546f04dc673d974c5e447e6
---

# AI MRP 数据架构文档

> **版本**：1.0  
> **日期**：2026-03-08

---

## 文档清单

| 文档 | 说明 |
|------|------|
| `DOMAIN_MODEL.md` | 领域模型设计文档 |
| `DATA_ARCHITECTURE.md` | 数据架构设计文档 |
| `DATABASE_DDL.sql` | 完整 DDL 建表脚本 |

---

## 一、领域模型设计 (DOMAIN_MODEL.md)

### 内容概要

- **领域划分**：核心领域 + 支持领域
- **核心领域**：Demand、BOM、Inventory、MRP、Purchase、Production
- **支持领域**：Supplier、Organization、Sandbox
- **聚合根**：各领域核心实体
- **领域服务**：业务逻辑封装
- **领域事件**：业务事件驱动

---

## 二、数据架构设计 (DATA_ARCHITECTURE.md)

### 内容概要

- **设计原则**：弱三范式，兼顾性能与规范
- **数据分层**：应用层 → 领域层 → 基础设施层 → 数据存储层
- **命名规范**：表名、字段名、索引命名规范
- **PostgreSQL 特性**：JSONB、分区、物化视图

---

## 三、数据库设计 (DATABASE_DDL.sql)

### 表分类

| 分类 | 前缀 | 数量 |
|------|------|------|
| 主数据 | m_ | 约 15 张 |
| 交易数据 | t_ | 约 10 张 |
| 沙箱数据 | s_ | 约 4 张 |
| 日志数据 | l_ | 约 2 张 |

### 核心表

| 表名 | 说明 |
|------|------|
| m_item | 物料主数据 |
| m_bom / m_bom_line | BOM 主/明细 |
| m_inventory | 库存 |
| t_sales_order / t_sales_order_line | 销售订单 |
| t_purchase_order | 采购订单 |
| t_production_order | 生产工单 |
| t_mrp_run / t_mrp_suggestion | MRP 运行/建议 |
| s_sandbox_session | 沙箱会话 |

---

## 四、设计亮点

### 4.1 弱三范式

- 核心业务表保持 3NF
- 允许适度冗余（如：总计数字段）
- JSONB 存储扩展属性

### 4.2 性能优化

- 合理索引设计
- 分区表支持（大表）
- 物化视图（报表）

### 4.3 可扩展性

- ext_data (JSONB) 扩展字段
- 审计日志
- 沙箱机制支持

---

## 五、快速开始

```bash
# 1. 执行 DDL
psql -U postgres -d aimrp -f DATABASE_DDL.sql

# 2. 验证表
psql -d aimrp -c "\dt"
```

---

*文档版本：1.0*
