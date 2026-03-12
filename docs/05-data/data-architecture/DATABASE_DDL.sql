# AI MRP 数据库设计文档

> **版本**：1.1  
> **日期**：2026-03-09  
> **数据库**：PostgreSQL 15+
> **说明**：核心表 DDL，补充表见 DATABASE_DDL_SUPPLEMENT.sql

---

## 一、表清单

### 1.1 主数据表 (m_)

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| m_item | 物料主数据 | 10,000+ |
| m_item_category | 物料分类 | 100+ |
| m_bom | BOM 主表 | 5,000+ |
| m_bom_line | BOM 明细 | 30,000+ |
| m_warehouse | 仓库 | 50+ |
| m_location | 库位 | 1,000+ |
| m_supplier | 供应商 | 500+ |
| m_supplier_item | 供应商物料 | 10,000+ |
| m_organization | 组织 | 50+ |
| m_unit | 单位 | 50+ |

### 1.2 交易表 (t_)

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| t_sales_order | 销售订单 | 100,000+ |
| t_sales_order_line | 订单明细 | 500,000+ |
| t_purchase_order | 采购订单 | 50,000+ |
| t_purchase_order_line | 采购明细 | 200,000+ |
| t_production_order | 生产工单 | 30,000+ |
| t_production_order_line | 工单明细 | 100,000+ |
| t_mrp_run | MRP 运行记录 | 10,000+ |
| t_mrp_suggestion | MRP 建议 | 100,000+ |
| t_inventory_transaction | 库存事务 | 1,000,000+ |

### 1.3 库存表 (m_)

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| m_inventory | 库存 | 100,000+ |
| m_inventory_allocation | 库存预留 | 50,000+ |

### 1.4 沙箱表 (s_)

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| s_sandbox_session | 沙箱会话 | 1,000+ |
| s_sandbox_mrp_result | 沙箱 MRP 结果 | 10,000+ |
| s_sandbox_ai_plan | 沙箱 AI 方案 | 5,000+ |
| s_sandbox_audit | 沙箱审计 | 10,000+ |

### 1.5 日志表 (l_)

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| l_audit_log | 审计日志 | 1,000,000+ |
| l_login_log | 登录日志 | 100,000+ |

### 1.6 需求预测模块 (m_ / t_) - 补充

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| m_forecast | 销售预测主表 | 1,000+ |
| t_forecast_detail | 预测明细 | 50,000+ |
| t_prediction_result | AI 预测结果 | 100,000+ |

### 1.7 生产报工模块 (t_) - 补充

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| t_production_report | 生产报工记录 | 50,000+ |
| t_production_defect | 生产不良记录 | 10,000+ |

### 1.8 风险预警模块 (t_) - 补充

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| t_risk_alert | 风险预警记录 | 10,000+ |
| t_supplier_risk | 供应商风险明细 | 5,000+ |

### 1.9 What-if 场景模块 (t_) - 补充

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| t_whatif_scenario | What-if 场景 | 1,000+ |
| t_whatif_comparison | 场景对比 | 5,000+ |

### 1.10 需求池模块 (t_) - 补充

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| t_demand_pool | 需求池 | 100,000+ |
| t_demand_source | 需求来源追踪 | 200,000+ |

### 1.11 AI 模型配置模块 (m_ / t_) - 补充

| 表名 | 说明 | 行数估算 |
|------|------|----------|
| m_ai_model | AI 模型配置 | 50+ |
| t_ai_training_log | AI 训练记录 | 100+ |
| m_safety_stock_config | 安全库存配置 | 10,000+ |

> **注意**：补充表 DDL 见 `DATABASE_DDL_SUPPLEMENT.sql`

## 二、完整 DDL

```sql
-- ============================================================
-- AI MRP 数据库设计
-- 版本：1.0
-- 数据库：PostgreSQL 15+
-- ============================================================

-- 创建数据库
CREATE DATABASE aimrp WITH ENCODING = 'UTF8';

-- 连接数据库
\c aimrp;

-- ============================================================
-- 第一部分：主数据表 (m_)
-- ============================================================

-- 单位表
CREATE TABLE m_unit (
    id BIGSERIAL PRIMARY KEY,
    unit_code VARCHAR(20) NOT NULL UNIQUE,
    unit_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 物料分类表
CREATE TABLE m_item_category (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(200) NOT NULL,
    parent_id BIGINT,
    level INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES m_item_category(id)
);

-- 物料主数据表
CREATE TABLE m_item (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL UNIQUE,
    item_name VARCHAR(200) NOT NULL,
    item_type VARCHAR(20) NOT NULL,  -- 成品/半成品/原材料/虚拟件
    category_id BIGINT,
    spec VARCHAR(200),                -- 规格
    unit VARCHAR(20),                 -- 单位
    weight DECIMAL(10,2),            -- 重量
    length DECIMAL(10,2),             -- 长度
    width DECIMAL(10,2),              -- 宽度
    height DECIMAL(10,2),             -- 高度
    standard_cost DECIMAL(18,2),       -- 标准成本
    sale_price DECIMAL(18,2),         -- 销售价
    tax_rate DECIMAL(5,4),            -- 税率
    safety_stock DECIMAL(18,6) DEFAULT 0,
    moq DECIMAL(18,6) DEFAULT 1,      -- 最小起订量
    lead_time INT DEFAULT 0,           -- 提前期（天）
    is_trace_batch BOOLEAN DEFAULT FALSE,  -- 是否批次管理
    is_enable_serial BOOLEAN DEFAULT FALSE,  -- 是否序列号管理
    shelf_life INT DEFAULT 0,          -- 保质期（天）
    status VARCHAR(20) DEFAULT 'ACTIVE',
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES m_item_category(id)
);

-- 索引
CREATE INDEX idx_item_code ON m_item(item_code);
CREATE INDEX idx_item_type ON m_item(item_type);
CREATE INDEX idx_item_category ON m_item(category_id);
CREATE INDEX idx_item_status ON m_item(status);

-- 仓库表
CREATE TABLE m_warehouse (
    id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_name VARCHAR(200) NOT NULL,
    warehouse_type VARCHAR(20),        -- 原材料/半成品/成品
    org_id BIGINT,                     -- 组织ID
    is_default BOOLEAN DEFAULT FALSE,
    address VARCHAR(500),
    contact VARCHAR(50),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 库位表
CREATE TABLE m_location (
    id BIGSERIAL PRIMARY KEY,
    location_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL,
    location_type VARCHAR(20),         -- 存储/拣货/暂存
    row VARCHAR(10),                   -- 排
    col VARCHAR(10),                   -- 列
    level VARCHAR(10),                  -- 层
    capacity DECIMAL(18,2),            -- 容量
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES m_warehouse(id)
);

-- 组织表
CREATE TABLE m_organization (
    id BIGSERIAL PRIMARY KEY,
    org_code VARCHAR(50) NOT NULL UNIQUE,
    org_name VARCHAR(200) NOT NULL,
    org_type VARCHAR(20),             -- 公司/工厂/部门
    parent_id BIGINT,
    level INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES m_organization(id)
);

-- 供应商表
CREATE TABLE m_supplier (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(200) NOT NULL,
    supplier_type VARCHAR(20),         -- 原材料/设备/服务
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address VARCHAR(500),
    payment_days INT DEFAULT 30,       -- 付款天数
    tax_no VARCHAR(50),                -- 税号
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    rating INT DEFAULT 3,              -- 评级 1-5
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 供应商物料关联表
CREATE TABLE m_supplier_item (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    supplier_item_code VARCHAR(50),    -- 供应商物料编码
    supplier_item_name VARCHAR(200),
    price DECIMAL(18,2),              -- 供应价
    moq DECIMAL(18,6) DEFAULT 1,
    lead_time INT DEFAULT 7,            -- 供货周期
    is_preferred BOOLEAN DEFAULT FALSE, -- 是否首选
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES m_supplier(id),
    UNIQUE(supplier_id, item_code)
);

-- BOM 主表
CREATE TABLE m_bom (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL,
    version INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'DRAFT',  -- DRAFT/ACTIVE/OBSOLETE
    effective_date DATE,
    expire_date DATE,
    total_cost DECIMAL(18,2),          -- BOM 总成本（冗余）
    total_weight DECIMAL(10,2),        -- BOM 总重量（冗余）
    yield_rate DECIMAL(5,4) DEFAULT 1,  -- 成品率
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(item_code, version)
);

-- BOM 明细表
CREATE TABLE m_bom_line (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    child_code VARCHAR(50) NOT NULL,
    child_name VARCHAR(200),
    qty DECIMAL(18,6) NOT NULL,
    loss_rate DECIMAL(5,4) DEFAULT 0,
    unit VARCHAR(20),
    is_optional BOOLEAN DEFAULT FALSE,  -- 是否可选件
    sequence INT DEFAULT 0,
    work_center VARCHAR(50),
    process_time DECIMAL(10,2),        -- 加工时间（分钟）
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bom_id) REFERENCES m_bom(id)
);

-- 索引
CREATE INDEX idx_bom_item ON m_bom(item_code);
CREATE INDEX idx_bom_status ON m_bom(status);
CREATE INDEX idx_bom_line_bom ON m_bom_line(bom_id);
CREATE INDEX idx_bom_line_child ON m_bom_line(child_code);

-- ============================================================
-- 第二部分：交易表 (t_)
-- ============================================================

-- 销售订单表
CREATE TABLE t_sales_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(200),
    order_date DATE NOT NULL,
    due_date DATE,
    priority INT DEFAULT 5,              -- 1-10，1 最高
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/CONFIRMED/PRODUCING/SHIPPED/COMPLETED/CANCELLED
    total_qty DECIMAL(18,6),
    total_amount DECIMAL(18,2),
    currency VARCHAR(10) DEFAULT 'CNY',
    sales_person VARCHAR(50),
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 销售订单明细表
CREATE TABLE t_sales_order_line (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    item_spec VARCHAR(200),
    unit VARCHAR(20),
    qty DECIMAL(18,6) NOT NULL,
    unit_price DECIMAL(18,2),
    line_amount DECIMAL(18,2),
    delivered_qty DECIMAL(18,6) DEFAULT 0,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES t_sales_order(id)
);

-- 索引
CREATE INDEX idx_sales_order_no ON t_sales_order(order_no);
CREATE INDEX idx_sales_order_customer ON t_sales_order(customer_code);
CREATE INDEX idx_sales_order_status ON t_sales_order(status);
CREATE INDEX idx_sales_order_date ON t_sales_order(order_date);
CREATE INDEX idx_sales_order_line_order ON t_sales_order_line(order_id);
CREATE INDEX idx_sales_order_line_item ON t_sales_order_line(item_code);

-- 采购订单表
CREATE TABLE t_purchase_order (
    id BIGSERIAL PRIMARY KEY,
    po_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(200),
    order_date DATE NOT NULL,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/APPROVED/RECEIVING/RECEIVED/CANCELLED
    total_qty DECIMAL(18,6),
    total_amount DECIMAL(18,2),
    payment_status VARCHAR(20) DEFAULT 'UNPAID',  -- UNPAID/PARTIAL/PAID
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES m_supplier(id)
);

-- 采购订单明细表
CREATE TABLE t_purchase_order_line (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    unit VARCHAR(20),
    qty DECIMAL(18,6) NOT NULL,
    unit_price DECIMAL(18,2),
    line_amount DECIMAL(18,2),
    received_qty DECIMAL(18,6) DEFAULT 0,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES t_purchase_order(id)
);

-- 生产工单表
CREATE TABLE t_production_order (
    id BIGSERIAL PRIMARY KEY,
    mo_no VARCHAR(50) NOT NULL UNIQUE,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    qty DECIMAL(18,6) NOT NULL,
    finished_qty DECIMAL(18,6) DEFAULT 0,
    order_date DATE NOT NULL,
    due_date DATE,
    start_date DATE,
    end_date DATE,
    priority INT DEFAULT 5,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/SCHEDULED/PRODUCING/COMPLETED/CANCELLED
    warehouse_id BIGINT,                     -- 入库仓库
    ext_data JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 生产工单明细表
CREATE TABLE t_production_order_line (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    unit VARCHAR(20),
    qty DECIMAL(18,6) NOT NULL,
    item_type VARCHAR(20),  -- 原材料/半成品
    ext_data JSONB,
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES t_production_order(id)
);

-- MRP 运行记录表
CREATE TABLE t_mrp_run (
    id BIGSERIAL PRIMARY KEY,
    run_no VARCHAR(50) NOT NULL UNIQUE,
    run_type VARCHAR(20) NOT NULL,  -- FULL/INCREMENTAL
    status VARCHAR(20) DEFAULT 'RUNNING',  -- RUNNING/COMPLETED/FAILED
    total_demands INT DEFAULT 0,
    total_suggestions INT DEFAULT 0,
    run_time_ms BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    error_msg TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MRP 建议表
CREATE TABLE t_mrp_suggestion (
    id BIGSERIAL PRIMARY KEY,
    mrp_run_id BIGINT NOT NULL,
    demand_type VARCHAR(20) NOT NULL,  -- ORDER/FORECAST
    demand_id BIGINT,
    demand_line_id BIGINT,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    suggestion_type VARCHAR(20) NOT NULL,  -- PO/MO/TRANSFER
    suggested_qty DECIMAL(18,6) NOT NULL,
    due_date DATE,
    priority INT DEFAULT 5,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/EXECUTED/IGNORED
    source_order_no VARCHAR(50),
    estimated_cost DECIMAL(18,2),
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mrp_run_id) REFERENCES t_mrp_run(id)
);

-- 索引
CREATE INDEX idx_mrp_run_status ON t_mrp_run(status);
CREATE INDEX idx_mrp_run_date ON t_mrp_run(start_time);
CREATE INDEX idx_mrp_suggestion_run ON t_mrp_suggestion(mrp_run_id);
CREATE INDEX idx_mrp_suggestion_item ON t_mrp_suggestion(item_code);
CREATE INDEX idx_mrp_suggestion_type ON t_mrp_suggestion(suggestion_type);

-- 库存表
CREATE TABLE m_inventory (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    location_id BIGINT,
    on_hand_qty DECIMAL(18,6) DEFAULT 0,
    allocated_qty DECIMAL(18,6) DEFAULT 0,
    frozen_qty DECIMAL(18,6) DEFAULT 0,
    safety_stock DECIMAL(18,6) DEFAULT 0,
    max_stock DECIMAL(18,6),
    last_in_qty DECIMAL(18,6) DEFAULT 0,
    last_out_qty DECIMAL(18,6) DEFAULT 0,
    last_trans_date TIMESTAMP,
    avg_cost DECIMAL(18,2),             -- 加权平均成本
    ext_data JSONB,
    memo TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(item_code, warehouse_id, location_id),
    FOREIGN KEY (warehouse_id) REFERENCES m_warehouse(id),
    FOREIGN KEY (location_id) REFERENCES m_location(id)
);

CREATE INDEX idx_inventory_item ON m_inventory(item_code);
CREATE INDEX idx_inventory_warehouse ON m_inventory(warehouse_id);

-- 库存事务表
CREATE TABLE t_inventory_transaction (
    id BIGSERIAL PRIMARY KEY,
    trans_no VARCHAR(50) NOT NULL UNIQUE,
    trans_type VARCHAR(20) NOT NULL,  -- IN/OUT/ALLOC/FREEZE/UNFREEZE/TRANSFER
    item_code VARCHAR(50) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    location_id BIGINT,
    trans_qty DECIMAL(18,6) NOT NULL,
    before_qty DECIMAL(18,6) NOT NULL,
    after_qty DECIMAL(18,6) NOT NULL,
    unit_cost DECIMAL(18,2),
    source_type VARCHAR(20),  -- SALES/PURCHASE/MO/ADJUST
    source_id BIGINT,
    source_line_id BIGINT,
    batch_no VARCHAR(50),
    trans_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    operator VARCHAR(50),
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES m_warehouse(id)
);

CREATE INDEX idx_trans_item ON t_inventory_transaction(item_code);
CREATE INDEX idx_trans_date ON t_inventory_transaction(trans_date);
CREATE INDEX idx_trans_source ON t_inventory_transaction(source_type, source_id);

-- ============================================================
-- 第三部分：沙箱表 (s_)
-- ============================================================

-- 沙箱会话表
CREATE TABLE s_sandbox_session (
    id BIGSERIAL PRIMARY KEY,
    session_no VARCHAR(50) NOT NULL UNIQUE,
    session_type VARCHAR(20) NOT NULL,  -- SANDBOX_MRP/SANDBOX_AI/SANDBOX_SIMULATION
    status VARCHAR(20) DEFAULT 'RUNNING',  -- RUNNING/CONFIRMED/CANCELLED/EXPIRED
    source_data JSONB NOT NULL,         -- 源数据快照
    param_data JSONB,                    -- 参数变更
    result_data JSONB,                   -- 执行结果
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    expired_at TIMESTAMP
);

CREATE INDEX idx_sandbox_session_status ON s_sandbox_session(status);
CREATE INDEX idx_sandbox_session_type ON s_sandbox_session(session_type);
CREATE INDEX idx_sandbox_session_expired ON s_sandbox_session(expired_at);

-- 沙箱 MRP 结果表
CREATE TABLE s_sandbox_mrp_result (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    suggestion_type VARCHAR(20) NOT NULL,
    suggested_qty DECIMAL(18,6) NOT NULL,
    due_date DATE,
    estimated_cost DECIMAL(18,2),
    risk_level VARCHAR(20),  -- LOW/MEDIUM/HIGH
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES s_sandbox_session(id)
);

CREATE INDEX idx_sandbox_mrp_session ON s_sandbox_mrp_result(session_id);

-- 沙箱 AI 方案表
CREATE TABLE s_sandbox_ai_plan (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    plan_no VARCHAR(20) NOT NULL,
    plan_name VARCHAR(100),
    plan_type VARCHAR(20),  -- COST_MIN/DELIVERY_MIN/BALANCED/CUSTOM
    total_cost DECIMAL(18,2),
    delivery_days INT,
    risk_score DECIMAL(5,2),
    plan_data JSONB,
    is_selected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES s_sandbox_session(id)
);

CREATE INDEX idx_sandbox_ai_plan_session ON s_sandbox_ai_plan(session_id);

-- 沙箱审计日志表
CREATE TABLE s_sandbox_audit (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,  -- CREATE/PREVIEW/CONFIRM/CANCEL/EXPIRE
    operator VARCHAR(50),
    detail JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES s_sandbox_session(id)
);

-- ============================================================
-- 第四部分：日志表 (l_)
-- ============================================================

-- 审计日志表
CREATE TABLE l_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    module VARCHAR(50),
    action VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_module ON l_audit_log(module);
CREATE INDEX idx_audit_entity ON l_audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_date ON l_audit_log(created_at);

-- 登录日志表
CREATE TABLE l_login_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50),
    login_type VARCHAR(20),  -- PASSWORD/WECHAT/DINGTALK
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(20),  -- SUCCESS/FAILED
    error_msg VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_user ON l_login_log(username);
CREATE INDEX idx_login_date ON l_login_log(created_at);

-- ============================================================
-- 第五部分：初始化数据
-- ============================================================

-- 初始化单位
INSERT INTO m_unit (unit_code, unit_name) VALUES 
('PCS', '件'), ('KG', '千克'), ('G', '克'), 
('M', '米'), ('CM', '厘米'), ('L', '升'),
('ML', '毫升'), ('BOX', '箱'), ('SET', '套');

-- 初始化组织
INSERT INTO m_organization (org_code, org_name, org_type) VALUES 
('HQ', '总部', 'COMPANY'),
('FACTORY1', '一号工厂', 'FACTORY');

-- 初始化仓库
INSERT INTO m_warehouse (warehouse_code, warehouse_name, warehouse_type, org_id) VALUES 
('WH01', '原材料仓', 'RAW', 2),
('WH02', '半成品仓', 'WIP', 2),
('WH03', '成品仓', 'FG', 2);

-- ============================================================
-- 完成
-- ============================================================
SELECT 'AI MRP 数据库初始化完成!' AS message;
```

---

*数据库设计完成*
