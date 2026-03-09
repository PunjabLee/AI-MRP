-- 采购模块数据表
CREATE TABLE t_purchase_order (
    id BIGSERIAL PRIMARY KEY,
    po_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_code VARCHAR(50),
    supplier_name VARCHAR(200),
    order_date DATE,
    expect_date DATE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    total_amount DECIMAL(18,2),
    received_amount DECIMAL(18,2),
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE t_purchase_order_line (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_code VARCHAR(50),
    item_name VARCHAR(200),
    order_qty DECIMAL(18,6),
    received_qty DECIMAL(18,6),
    unit_price DECIMAL(18,6),
    amount DECIMAL(18,2),
    expect_date DATE,
    receive_date DATE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    source VARCHAR(20),
    suggestion_id BIGINT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES t_purchase_order(id)
);

CREATE TABLE t_purchase_receive (
    id BIGSERIAL PRIMARY KEY,
    receive_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT,
    po_no VARCHAR(50),
    receive_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_purchase_order_no ON t_purchase_order(po_no);
CREATE INDEX idx_purchase_order_status ON t_purchase_order(status);
CREATE INDEX idx_purchase_order_supplier ON t_purchase_order(supplier_code);
CREATE INDEX idx_purchase_order_line_order ON t_purchase_order_line(order_id);
CREATE INDEX idx_purchase_receive_order ON t_purchase_receive(order_id);

SELECT 'Purchase module tables created!' AS message;
