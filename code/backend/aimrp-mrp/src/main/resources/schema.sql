-- ============================================================
-- MRP 模块数据表
-- 版本：1.0
-- 日期：2026-03-09
-- ============================================================

-- MRP 运行记录表
CREATE TABLE t_mrp_run (
    id BIGSERIAL PRIMARY KEY,
    run_no VARCHAR(50) NOT NULL UNIQUE,
    run_type VARCHAR(20) DEFAULT 'MANUAL',  -- MANUAL/AUTO
    status VARCHAR(20) DEFAULT 'PENDING',   -- PENDING/RUNNING/COMPLETED/FAILED
    plan_start_date DATE,
    plan_end_date DATE,
    item_count INT DEFAULT 0,
    demand_count INT DEFAULT 0,
    suggestion_count INT DEFAULT 0,
    purchase_suggestion_count INT DEFAULT 0,
    production_suggestion_count INT DEFAULT 0,
    run_time_ms BIGINT,
    error_message TEXT,
    run_log TEXT,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MRP 建议表
CREATE TABLE t_mrp_suggestion (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL,
    suggestion_type VARCHAR(20) NOT NULL,  -- PURCHASE/PRODUCTION
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    suggest_qty DECIMAL(18,6) NOT NULL,
    need_date DATE,
    suggest_order_date DATE,
    suggest_finish_date DATE,
    demand_source VARCHAR(20),  -- ORDER/FORECAST/POOL
    demand_id BIGINT,
    priority INT DEFAULT 5,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/ACCEPTED/REJECTED/CONVERTED
    reject_reason VARCHAR(500),
    converted_order_id BIGINT,
    converted_order_no VARCHAR(50),
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (run_id) REFERENCES t_mrp_run(id)
);

-- MRP 参数配置表
CREATE TABLE t_mrp_parameter (
    id BIGSERIAL PRIMARY KEY,
    param_key VARCHAR(100) NOT NULL UNIQUE,
    param_value VARCHAR(500),
    param_type VARCHAR(20),  -- GLOBAL/MATERIAL
    item_code VARCHAR(50),
    param_group VARCHAR(50),
    description VARCHAR(200),
    is_editable BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(param_type, item_code, param_key)
);

-- 初始化 MRP 全局参数
INSERT INTO t_mrp_parameter (param_key, param_value, param_type, param_group, description, is_editable) VALUES
('planning.horizon', '90', 'GLOBAL', 'PLANNING', '计划展望期（天）', TRUE),
('planning.time-bucket', 'DAY', 'GLOBAL', 'PLANNING', '时间段精度', TRUE),
('planning.allow-negative', 'false', 'GLOBAL', 'PLANNING', '是否允许负库存', TRUE),
('calculation.batch-size', '1000', 'GLOBAL', 'CALCULATION', '批处理大小', TRUE),
('calculation.timeout', '300000', 'GLOBAL', 'CALCULATION', '计算超时（毫秒）', TRUE);

-- 创建索引
CREATE INDEX idx_mrp_run_no ON t_mrp_run(run_no);
CREATE INDEX idx_mrp_run_status ON t_mrp_run(status);
CREATE INDEX idx_mrp_suggestion_run ON t_mrp_suggestion(run_id);
CREATE INDEX idx_mrp_suggestion_type ON t_mrp_suggestion(suggestion_type);
CREATE INDEX idx_mrp_suggestion_status ON t_mrp_suggestion(status);
CREATE INDEX idx_mrp_suggestion_item ON t_mrp_suggestion(item_code);
CREATE INDEX idx_mrp_parameter_type ON t_mrp_parameter(param_type, item_code);

SELECT 'MRP 模块数据表创建完成!' AS message;
