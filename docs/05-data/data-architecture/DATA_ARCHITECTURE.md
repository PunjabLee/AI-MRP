# AI MRP 数据架构设计文档

> **版本**：1.1  
> **日期**：2026-03-09  
> **设计原则**：弱三范式，兼顾性能与规范

---

## 一、数据架构设计原则

### 1.1 弱三范式设计

```
传统三范式（严格）：
- 1NF：字段原子性
- 2NF：非主键字段完全依赖于主键
- 3NF：非主键字段之间不存在传递依赖

弱三范式（本文采用）：
- ✅ 保留 1NF/2NF 要求
- ⚡ 适当放宽 3NF，允许适度冗余
- ⚡ 核心业务表保持 3NF
- ⚡ 报表/统计表可适度反规范化
```

### 1.2 设计目标

| 目标 | 说明 |
|------|------|
| **数据完整性** | 主外键约束、唯一性约束 |
| **查询性能** | 适当冗余、预计算、分区 |
| **扩展性** | 支持多租户、多组织 |
| **可维护性** | 清晰的命名、规范的注释 |

---

## 二、整体数据架构

### 2.1 数据分层

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          数据分层架构                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    应用层 (Application Layer)                    │  │
│  │                                                                  │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                │  │
│  │  │    VO      │ │    DTO     │ │   Request  │                │  │
│  │  └─────────────┘ └─────────────┘ └─────────────┘                │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                     领域层 (Domain Layer)                        │  │
│  │                                                                  │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                │  │
│  │  │  聚合根     │ │   实体      │ │  值对象    │                │  │
│  │  └─────────────┘ └─────────────┘ └─────────────┘                │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                  基础设施层 (Infrastructure Layer)              │  │
│  │                                                                  │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                │  │
│  │  │   PO (Entity)│ │ Repository │ │   Mapper   │                │  │
│  │  └─────────────┘ └─────────────┘ └─────────────┘                │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                      数据存储层 (Data Layer)                     │  │
│  │                                                                  │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                │  │
│  │  │ PostgreSQL  │ │   Redis    │ │    MQ     │                │  │
│  │  │  (主存储)   │ │  (缓存)    │ │  (异步)   │                │  │
│  │  └─────────────┘ └─────────────┘ └─────────────┘                │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 数据分类

| 类别 | 存储 | 说明 |
|------|------|------|
| **主数据** | PostgreSQL | 物料、BOM、供应商、组织等相对稳定的数据 |
| **交易数据** | PostgreSQL | 订单、库存事务、MRP 结果等业务数据 |
| **缓存数据** | Redis | 会话、热点数据、计算结果缓存 |
| **消息数据** | RabbitMQ | 异步任务、事件通知 |
| **文件数据** | MinIO | 附件、报表导出文件 |

---

## 三、数据库设计规范

### 3.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| **表名** | 小写 + 下划线 | m_item, t_sales_order |
| **主键** | id | BIGSERIAL PRIMARY KEY |
| **外键** | xxx_id | order_id, item_id |
| **索引** | idx_xxx | idx_item_code |
| **唯一约束** | uk_xxx | uk_item_code |
| **时间戳** | created_at, updated_at | TIMESTAMP |

### 3.2 表分类前缀

| 前缀 | 含义 | 示例 |
|------|------|------|
| **m_** | 主数据 (Master) | m_item, m_bom, m_supplier |
| **t_** | 交易 (Transaction) | t_sales_order, t_mrp_run |
| **r_** | 报表 (Report) | r_daily_summary |
| **l_** | 日志 (Log) | l_audit_log |
| **s_** | 沙箱 (Sandbox) | s_sandbox_session |

### 3.3 字段设计原则

```sql
-- 标准字段设计
CREATE TABLE example (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(50) NOT NULL,        -- 业务编码
    name            VARCHAR(200),                -- 名称
    type            VARCHAR(20),                 -- 类型
    status          VARCHAR(20) DEFAULT 'ACTIVE', -- 状态
    qty             DECIMAL(18,6) DEFAULT 0,    -- 数量（精度）
    amount          DECIMAL(18,2) DEFAULT 0,     -- 金额（2位精度）
    ext_data        JSONB,                      -- 扩展数据（JSON）
    memo            TEXT,                        -- 备注（长文本）
    created_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 四、核心表结构设计

### 4.1 主数据表（弱三范式示例）

#### 4.1.1 物料主数据表

```sql
-- 物料主数据（规范设计）
CREATE TABLE m_item (
    id              BIGSERIAL PRIMARY KEY,
    item_code       VARCHAR(50) NOT NULL UNIQUE,
    item_name       VARCHAR(200) NOT NULL,
    item_type       VARCHAR(20) NOT NULL,  -- 成品/半成品/原材料
    category_id     BIGINT,                -- 分类（冗余，允许局部更新）
    spec            VARCHAR(200),          -- 规格
    unit            VARCHAR(20) NOT NULL,  -- 单位
    weight          DECIMAL(10,2),        -- 重量
    dimension       VARCHAR(50),            -- 尺寸
    standard_cost   DECIMAL(18,2),        -- 标准成本（冗余，便于查询）
    sale_price      DECIMAL(18,2),        -- 销售价（冗余）
    safety_stock    DECIMAL(18,6) DEFAULT 0,
    moq             DECIMAL(18,6) DEFAULT 1,  -- 最小起订量
    lead_time       INT DEFAULT 0,         -- 提前期（天）
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    ext_data        JSONB,                 -- 扩展属性
    memo            TEXT,
    created_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_item_type ON m_item(item_type);
CREATE INDEX idx_item_category ON m_item(category_id);
CREATE INDEX idx_item_status ON m_item(status);
```

**弱三范式说明**：
- category_id 冗余存储分类 ID（规范）
- standard_cost、sale_price 冗余存储（性能优化，避免每次计算）

#### 4.1.2 BOM 表

```sql
-- BOM 主表
CREATE TABLE m_bom (
    id              BIGSERIAL PRIMARY KEY,
    item_code       VARCHAR(50) NOT NULL,  -- 父物料
    version         INT DEFAULT 1,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    effective_date  DATE,
    expire_date     DATE,
    total_cost      DECIMAL(18,2),  -- BOM 总成本（冗余，展开计算）
    total_weight    DECIMAL(10,2),  -- BOM 总重量（冗余）
    ext_data        JSONB,
    memo            TEXT,
    created_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(item_code, version)
);

-- BOM 明细
CREATE TABLE m_bom_line (
    id              BIGSERIAL PRIMARY KEY,
    bom_id          BIGINT NOT NULL REFERENCES m_bom(id),
    line_no         INT NOT NULL,
    child_code      VARCHAR(50) NOT NULL,  -- 子物料（冗余，便于查询）
    child_name      VARCHAR(200),            -- 子物料名称（冗余）
    qty             DECIMAL(18,6) NOT NULL,
    loss_rate       DECIMAL(5,4) DEFAULT 0,
    unit            VARCHAR(20),
    sequence        INT DEFAULT 0,           -- 工序序号
    work_center     VARCHAR(50),             -- 车间（冗余）
    ext_data        JSONB,
    memo            TEXT,
    created_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_bom_item ON m_bom(item_code);
CREATE INDEX idx_bom_line_bom ON m_bom_line(bom_id);
CREATE INDEX idx_bom_line_child ON m_bom_line(child_code);
```

### 4.2 交易表（规范设计）

#### 4.2.1 销售订单

```sql
-- 销售订单
CREATE TABLE t_sales_order (
    id              BIGSERIAL PRIMARY KEY,
    order_no        VARCHAR(50) NOT NULL UNIQUE,
    customer_code   VARCHAR(50) NOT NULL,
    customer_name   VARCHAR(200),  -- 冗余
    order_date      DATE NOT NULL,
    due_date        DATE,
    priority        INT DEFAULT 5,
    status          VARCHAR(20) DEFAULT 'PENDING',
    total_qty       DECIMAL(18,6),  -- 冗余汇总
    total_amount    DECIMAL(18,2),   -- 冗余汇总
    ext_data        JSONB,
    memo            TEXT,
    created_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 销售订单明细
CREATE TABLE t_sales_order_line (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES t_sales_order(id),
    line_no         INT NOT NULL,
    item_code       VARCHAR(50) NOT NULL,
    item_name       VARCHAR(200),   -- 冗余
    qty             DECIMAL(18,6) NOT NULL,
    unit_price      DECIMAL(18,2),
    line_amount     DECIMAL(18,2),  -- 冗余
    delivered_qty   DECIMAL(18,6) DEFAULT 0,
    due_date        DATE,
    status          VARCHAR(20) DEFAULT 'PENDING',
    ext_data        JSONB,
    memo            TEXT,
    created_by      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_order_no ON t_sales_order(order_no);
CREATE INDEX idx_order_customer ON t_sales_order(customer_code);
CREATE INDEX idx_order_status ON t_sales_order(status);
CREATE INDEX idx_order_date ON t_sales_order(order_date);
CREATE INDEX idx_order_line_order ON t_sales_order_line(order_id);
CREATE INDEX idx_order_line_item ON t_sales_order_line(item_code);
```

### 4.3 库存表（考虑性能）

```sql
-- 库存表
CREATE TABLE m_inventory (
    id              BIGSERIAL PRIMARY KEY,
    item_code       VARCHAR(50) NOT NULL,
    warehouse_code  VARCHAR(50) NOT NULL,
    location_code   VARCHAR(50),  -- 库位
    on_hand_qty     DECIMAL(18,6) DEFAULT 0,
    allocated_qty   DECIMAL(18,6) DEFAULT 0,  -- 已分配
    frozen_qty      DECIMAL(18,6) DEFAULT 0,   -- 冻结
    safety_stock    DECIMAL(18,6) DEFAULT 0,
    max_stock       DECIMAL(18,6),
    last_in_qty     DECIMAL(18,6) DEFAULT 0,  -- 上次入库数量（冗余）
    last_out_qty    DECIMAL(18,6) DEFAULT 0,   -- 上次出库数量（冗余）
    last_in_date    TIMESTAMP,                   -- 上次入库时间
    last_out_date   TIMESTAMP,                   -- 上次出库时间
    ext_data        JSONB,
    memo            TEXT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(item_code, warehouse_code, location_code)
);

-- 库存事务表
CREATE TABLE t_inventory_transaction (
    id              BIGSERIAL PRIMARY KEY,
    trans_no        VARCHAR(50) NOT NULL UNIQUE,
    item_code       VARCHAR(50) NOT NULL,
    warehouse_code  VARCHAR(50) NOT NULL,
    trans_type      VARCHAR(20) NOT NULL,  -- IN/OUT/ALLOC/FREEZE
    trans_qty       DECIMAL(18,6) NOT NULL,
    before_qty      DECIMAL(18,6) NOT NULL,
    after_qty       DECIMAL(18,6) NOT NULL,
    source_type     VARCHAR(20),  -- 来源类型：ORDER/PURCHASE/MO
    source_id      BIGINT,        -- 来源ID
    batch_no       VARCHAR(50),   -- 批次号
    trans_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    operator       VARCHAR(50),
    memo            TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_inv_item_wh ON m_inventory(item_code, warehouse_code);
CREATE INDEX idx_trans_item ON t_inventory_transaction(item_code);
CREATE INDEX idx_trans_date ON t_inventory_transaction(trans_date);
CREATE INDEX idx_trans_source ON t_inventory_transaction(source_type, source_id);
```

---

## 五、PostgreSQL 特性使用

### 5.1 JSONB 使用场景

```sql
-- 扩展属性使用 JSONB
ALTER TABLE m_item ADD COLUMN ext_data JSONB;

-- 查询示例
SELECT * FROM m_item WHERE ext_data->>'color' = 'RED';
SELECT ext_data->>'brand' AS brand FROM m_item WHERE item_code = 'A001';
```

### 5.2 分区表（大数据量）

```sql
-- 库存事务表分区（按月）
CREATE TABLE t_inventory_transaction (
    id              BIGSERIAL,
    trans_no        VARCHAR(50) NOT NULL,
    item_code       VARCHAR(50) NOT NULL,
    ...
    trans_date      TIMESTAMP NOT NULL
) PARTITION BY RANGE (trans_date);

-- 创建月度分区
CREATE TABLE t_inventory_transaction_2026_01 PARTITION OF t_inventory_transaction
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TABLE t_inventory_transaction_2026_02 PARTITION OF t_inventory_transaction
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
```

### 5.3 物化视图（统计报表）

```sql
-- 库存汇总物化视图
CREATE MATERIALIZED VIEW mv_inventory_summary AS
SELECT 
    item_code,
    warehouse_code,
    SUM(on_hand_qty) AS total_qty,
    COUNT(*) AS location_count,
    MAX(updated_at) AS last_update
FROM m_inventory
GROUP BY item_code, warehouse_code;

-- 刷新
REFRESH MATERIALIZED VIEW mv_inventory_summary;
```

---

## 六、新增表清单（2026-03-09 补充）

### 6.1 需求预测模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `m_forecast` | 销售预测主表 | 管理预测任务 |
| `t_forecast_detail` | 预测明细 | 存储预测值与实际值 |
| `t_prediction_result` | AI 预测结果 | 存储各类 AI 预测结果 |

### 6.2 生产报工模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `t_production_report` | 生产报工记录 | 记录报工、完工、不良品 |
| `t_production_defect` | 生产不良记录 | 详细不良原因分析 |

### 6.3 风险预警模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `t_risk_alert` | 风险预警记录 | 供应商/库存/需求/交付风险 |
| `t_supplier_risk` | 供应商风险明细 | 供应商风险评估 |

### 6.4 What-if 场景模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `t_whatif_scenario` | What-if 场景 | 模拟分析场景管理 |
| `t_whatif_comparison` | 场景对比 | 方案对比分析 |

### 6.5 需求池模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `t_demand_pool` | 需求池 | 统一管理订单/预测需求 |
| `t_demand_source` | 需求来源追踪 | 需求来源追溯 |

### 6.6 AI 模型配置模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `m_ai_model` | AI 模型配置 | 管理 AI 模型配置 |
| `t_ai_training_log` | AI 训练记录 | 记录模型训练历史 |

### 6.7 安全库存配置模块

| 表名 | 说明 | 用途 |
|------|------|------|
| `m_safety_stock_config` | 安全库存配置 | 手工/AI 安全库存管理 |

---

## 七、ER 关系图（核心）

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          核心表关系图                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│    m_item ─────┐                                                      │
│         │      │                                                      │
│         ▼      │      t_sales_order                                   │
│    m_bom ─────┼──► t_sales_order_line                                │
│         │      │           │                                          │
│         ▼      │           ▼                                          │
│  m_bom_line ───┼──►  (物料来源)                                       │
│         │      │                                                      │
│         │      │      t_mrp_run                                       │
│         ▼      │           │                                          │
│    (库存) ◄────┼─── t_mrp_suggestion                                 │
│         │      │           │                                          │
│    m_inventory ◄───── t_inventory_transaction                        │
│                                                                         │
│    m_warehouse                                                      │
│         │                                                            │
│         ▼                                                            │
│    m_supplier ──► t_purchase_order                                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 八、版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2026-03-08 | 初始版本：核心业务表（订单/BOM/库存/MRP/沙箱） |
| 1.1 | 2026-03-09 | 补充表：需求预测、生产报工、风险预警、What-if、需求池、AI模型配置 |

*数据架构设计完成*
