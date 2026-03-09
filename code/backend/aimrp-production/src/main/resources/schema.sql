-- 生产模块数据表
CREATE TABLE t_production_order (
    id BIGSERIAL PRIMARY KEY,
    mo_no VARCHAR(50) NOT NULL UNIQUE,
    item_code VARCHAR(50),
    item_name VARCHAR(200),
    plan_qty DECIMAL(18,6),
    completed_qty DECIMAL(18,6),
    scrapped_qty DECIMAL(18,6),
    start_date DATE,
    end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    priority INT DEFAULT 5,
    source VARCHAR(20),
    suggestion_id BIGINT,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_production_report (
    id BIGSERIAL PRIMARY KEY,
    report_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT,
    mo_no VARCHAR(50),
    report_type VARCHAR(20),
    output_qty DECIMAL(18,6),
    qualified_qty DECIMAL(18,6),
    scrapped_qty DECIMAL(18,6),
    rework_qty DECIMAL(18,6),
    labor_hours DECIMAL(10,2),
    machine_hours DECIMAL(10,2),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES t_production_order(id)
);

CREATE INDEX idx_production_order_no ON t_production_order(mo_no);
CREATE INDEX idx_production_order_status ON t_production_order(status);
CREATE INDEX idx_production_order_item ON t_production_order(item_code);
CREATE INDEX idx_production_report_order ON t_production_report(order_id);

SELECT 'Production module tables created!' AS message;
