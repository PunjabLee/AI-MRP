-- ============================================
-- OR 排程优化 - 数据模型补充
-- ============================================

-- 工艺路线
CREATE TABLE m_process_route (
    id BIGSERIAL PRIMARY KEY,
    route_code VARCHAR(50) NOT NULL UNIQUE,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(100),
    route_name VARCHAR(100),
    version VARCHAR(20) DEFAULT 'V1',
    status VARCHAR(20) DEFAULT 'DRAFT',
    effective_date TIMESTAMP,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 工艺路线工序
CREATE TABLE m_process_route_line (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    operation_no INT NOT NULL,
    operation_name VARCHAR(100),
    work_center_code VARCHAR(50),
    std_hours DECIMAL(10,2) DEFAULT 0,
    setup_time INT DEFAULT 0,
    queue_hours DECIMAL(10,2) DEFAULT 0,
    priority INT DEFAULT 0,
    is_critical BOOLEAN DEFAULT FALSE,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES m_process_route(id)
);

-- 工作中心
CREATE TABLE m_work_center (
    id BIGSERIAL PRIMARY KEY,
    wc_code VARCHAR(50) NOT NULL UNIQUE,
    wc_name VARCHAR(100),
    capacity DECIMAL(10,2) DEFAULT 8,
    efficiency DECIMAL(5,2) DEFAULT 1.0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 资源
CREATE TABLE m_resource (
    id BIGSERIAL PRIMARY KEY,
    resource_code VARCHAR(50) NOT NULL UNIQUE,
    resource_name VARCHAR(100),
    work_center_code VARCHAR(50),
    resource_type VARCHAR(20) DEFAULT 'MACHINE',
    capacity DECIMAL(10,2) DEFAULT 8,
    efficiency DECIMAL(5,2) DEFAULT 1.0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (work_center_code) REFERENCES m_work_center(wc_code)
);

-- 工单工序明细
CREATE TABLE t_mo_operation (
    id BIGSERIAL PRIMARY KEY,
    mo_id BIGINT NOT NULL,
    operation_id BIGINT,
    operation_no INT NOT NULL,
    operation_name VARCHAR(100),
    work_center_code VARCHAR(50),
    std_hours DECIMAL(10,2),
    plan_qty DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    plan_start_date DATE,
    plan_end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    completed_qty DECIMAL(10,2) DEFAULT 0,
    scrapped_qty DECIMAL(10,2) DEFAULT 0,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mo_id) REFERENCES t_production_order(id)
);

-- 索引
CREATE INDEX idx_process_route_item ON m_process_route(item_code);
CREATE INDEX idx_process_route_line_route ON m_process_route_line(route_id);
CREATE INDEX idx_work_center_code ON m_work_center(wc_code);
CREATE INDEX idx_resource_wc ON m_resource(work_center_code);
CREATE INDEX idx_mo_operation_mo ON t_mo_operation(mo_id);
