-- ============================================================
-- AI MRP 补充数据表设计
-- 版本：1.0
-- 日期：2026-03-09
-- 说明：补充 PRD 中缺失的数据表
-- ============================================================

-- ============================================================
-- 第六部分：需求预测模块 (m_ / t_)
-- ============================================================

-- 销售预测主表
CREATE TABLE m_forecast (
    id BIGSERIAL PRIMARY KEY,
    forecast_no VARCHAR(50) NOT NULL UNIQUE,
    forecast_name VARCHAR(200),
    forecast_type VARCHAR(20) NOT NULL,  -- MANUAL/AI/SYSTEM
    item_code VARCHAR(50),                -- 产品级预测（可选）
    category_id BIGINT,                   -- 分类级预测（可选）
    forecast_period VARCHAR(20) NOT NULL, -- WEEK/MONTH/QUARTER/YEAR
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',   -- DRAFT/ACTIVE/CANCELLED
    is_auto_refresh BOOLEAN DEFAULT FALSE, -- 是否自动刷新
    confidence_level DECIMAL(5,2),        -- 置信度
    model_config JSONB,                   -- 模型配置
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 预测明细表
CREATE TABLE t_forecast_detail (
    id BIGSERIAL PRIMARY KEY,
    forecast_id BIGINT NOT NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    forecast_qty DECIMAL(18,6) NOT NULL,
    actual_qty DECIMAL(18,6),             -- 实际销量（后续回填）
    variance_qty DECIMAL(18,6),            -- 差异
    variance_rate DECIMAL(5,4),            -- 差异率
    confidence DECIMAL(5,2),               -- 本条预测置信度
    upper_bound_qty DECIMAL(18,6),         -- 上界
    lower_bound_qty DECIMAL(18,6),         -- 下界
    source VARCHAR(20) DEFAULT 'MANUAL',  -- MANUAL/AI/HISTORY
    ext_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (forecast_id) REFERENCES m_forecast(id)
);

-- AI 预测结果表
CREATE TABLE t_prediction_result (
    id BIGSERIAL PRIMARY KEY,
    prediction_no VARCHAR(50) NOT NULL UNIQUE,
    prediction_type VARCHAR(20) NOT NULL,  -- DEMAND/SUPPLIER_RISK/INVENTORY
    item_code VARCHAR(50),
    prediction_period VARCHAR(20),         -- WEEK/MONTH
    prediction_date DATE NOT NULL,
    predicted_value DECIMAL(18,6) NOT NULL,
    confidence DECIMAL(5,2),
    upper_bound DECIMAL(18,6),
    lower_bound DECIMAL(18,6),
    model_name VARCHAR(50),               -- 模型名称：Prophet/XGBoost/LSTM
    model_version VARCHAR(20),
    features JSONB,                       -- 输入特征
    actual_value DECIMAL(18,6),           -- 实际值（回填）
    accuracy DECIMAL(5,2),                -- 准确率（回填后计算）
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING/VALIDATED/EXPIRED
    memo TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validated_at TIMESTAMP
);

-- 索引
CREATE INDEX idx_forecast_no ON m_forecast(forecast_no);
CREATE INDEX idx_forecast_status ON m_forecast(status);
CREATE INDEX idx_forecast_item ON m_forecast(item_code);
CREATE INDEX idx_forecast_detail_forecast ON t_forecast_detail(forecast_id);
CREATE INDEX idx_forecast_detail_item ON t_forecast_detail(item_code);
CREATE INDEX idx_forecast_detail_period ON t_forecast_detail(period_start);
CREATE INDEX idx_prediction_type ON t_prediction_result(prediction_type);
CREATE INDEX idx_prediction_item ON t_prediction_result(item_code);
CREATE INDEX idx_prediction_date ON t_prediction_result(prediction_date);

-- ============================================================
-- 第七部分：生产报工模块 (t_)
-- ============================================================

-- 生产报工记录表
CREATE TABLE t_production_report (
    id BIGSERIAL PRIMARY KEY,
    report_no VARCHAR(50) NOT NULL UNIQUE,
    mo_id BIGINT NOT NULL,                -- 关联工单
    mo_no VARCHAR(50) NOT NULL,
    workstation VARCHAR(50),              -- 工作站
    report_type VARCHAR(20) NOT NULL,     -- START/END/OUTPUT/SCRAP
    output_qty DECIMAL(18,6) DEFAULT 0,   -- 产出数量
    qualified_qty DECIMAL(18,6) DEFAULT 0,-- 合格数量
    scrap_qty DECIMAL(18,6) DEFAULT 0,    -- 报废数量
    rework_qty DECIMAL(18,6) DEFAULT 0,   -- 返工数量
    work_time DECIMAL(10,2) DEFAULT 0,    -- 实际工时（小时）
    machine_time DECIMAL(10,2) DEFAULT 0, -- 机时（小时）
    worker_count INT DEFAULT 1,            -- 人数
    start_time TIMESTAMP,                  -- 开工时间
    end_time TIMESTAMP,                   -- 完工时间
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/APPROVED/REJECTED
    approver VARCHAR(50),
    approved_at TIMESTAMP,
    ext_data JSONB,
    memo TEXT,
    reporter VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mo_id) REFERENCES t_production_order(id)
);

-- 生产不良记录表
CREATE TABLE t_production_defect (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    defect_type VARCHAR(50) NOT NULL,     -- 不良类型
    defect_code VARCHAR(50),              -- 不良代码
    defect_qty DECIMAL(18,6) NOT NULL,
    defect_level VARCHAR(20),              -- MAJOR/MINOR
    cause_analysis TEXT,
    solution TEXT,
    photo_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'OPEN',   -- OPEN/RESOLVED
    resolver VARCHAR(50),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES t_production_report(id)
);

-- 索引
CREATE INDEX idx_production_report_mo ON t_production_report(mo_id);
CREATE INDEX idx_production_report_no ON t_production_report(report_no);
CREATE INDEX idx_production_report_date ON t_production_report(created_at);
CREATE INDEX idx_production_defect_report ON t_production_defect(report_id);

-- ============================================================
-- 第八部分：风险预警模块 (t_)
-- ============================================================

-- 风险预警记录表
CREATE TABLE t_risk_alert (
    id BIGSERIAL PRIMARY KEY,
    alert_no VARCHAR(50) NOT NULL UNIQUE,
    alert_type VARCHAR(20) NOT NULL,      -- SUPPLIER/INVENTORY/DEMAND/DELIVERY
    alert_level VARCHAR(20) NOT NULL,      -- LOW/MEDIUM/HIGH/CRITICAL
    alert_title VARCHAR(200) NOT NULL,
    alert_content TEXT,
    item_code VARCHAR(50),                -- 关联物料
    supplier_code VARCHAR(50),            -- 关联供应商
    order_no VARCHAR(50),                 -- 关联订单
    current_value DECIMAL(18,6),          -- 当前值
    threshold_value DECIMAL(18,6),        -- 阈值
    risk_score DECIMAL(5,2),              -- 风险评分 0-100
    status VARCHAR(20) DEFAULT 'OPEN',    -- OPEN/ACKNOWLEDGED/RESOLVED/IGNORED
    resolve_method VARCHAR(20),           -- 解决方式
    resolved_by VARCHAR(50),
    resolved_at TIMESTAMP,
    resolved_memo TEXT,
    acknowledged_by VARCHAR(50),
    acknowledged_at TIMESTAMP,
    notify_channels JSONB,                -- 通知渠道
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_time TIMESTAMP,
    expired_at TIMESTAMP                  -- 过期时间
);

-- 供应商风险明细表
CREATE TABLE t_supplier_risk (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    risk_type VARCHAR(20) NOT NULL,       -- DELAY/QUALITY/PAYMENT/CAPACITY
    risk_score DECIMAL(5,2) NOT NULL,
    delay_days INT,                       -- 延迟天数
    delay_rate DECIMAL(5,4),              -- 延迟率
    quality_issue_count INT,              -- 质量问题次数
    on_time_rate DECIMAL(5,4),           -- 准时交货率
    assessment_date DATE NOT NULL,
    next_review_date DATE,
    risk_trend VARCHAR(20),               -- UP/DOWN/STABLE
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES m_supplier(id)
);

-- 索引
CREATE INDEX idx_risk_alert_type ON t_risk_alert(alert_type);
CREATE INDEX idx_risk_alert_level ON t_risk_alert(alert_level);
CREATE INDEX idx_risk_alert_status ON t_risk_alert(status);
CREATE INDEX idx_risk_alert_item ON t_risk_alert(item_code);
CREATE INDEX idx_risk_alert_created ON t_risk_alert(created_at);
CREATE INDEX idx_supplier_risk_supplier ON t_supplier_risk(supplier_id);
CREATE INDEX idx_supplier_risk_date ON t_supplier_risk(assessment_date);

-- ============================================================
-- 第九部分：What-if 场景模块 (t_)
-- ============================================================

-- What-if 场景表
CREATE TABLE t_whatif_scenario (
    id BIGSERIAL PRIMARY KEY,
    scenario_no VARCHAR(50) NOT NULL UNIQUE,
    scenario_name VARCHAR(200) NOT NULL,
    scenario_type VARCHAR(20) NOT NULL,   -- DEMAND_CHANGE/SUPPLIER_CHANGE/BUFFER_CHANGE
    description TEXT,
    base_data JSONB NOT NULL,            -- 基准数据快照
    change_data JSONB NOT NULL,          -- 变更数据
    result_data JSONB,                   -- 分析结果
    impact_analysis JSONB,               -- 影响分析结果
    status VARCHAR(20) DEFAULT 'DRAFT',   -- DRAFT/RUNNING/COMPLETED/CANCELLED
    total_cost_before DECIMAL(18,2),     -- 变更前总成本
    total_cost_after DECIMAL(18,2),      -- 变更后总成本
    cost_variance DECIMAL(18,2),         -- 成本差异
    delivery_rate_before DECIMAL(5,4),  -- 变更前交付率
    delivery_rate_after DECIMAL(5,4),   -- 变更后交付率
    risk_score_before DECIMAL(5,2),     -- 变更前风险
    risk_score_after DECIMAL(5,2),      -- 变更后风险
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- What-if 场景对比表
CREATE TABLE t_whatif_comparison (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL,
    compare_type VARCHAR(20) NOT NULL,   -- COST/DELIVERY/INVENTORY/RISK
    metric_name VARCHAR(50) NOT NULL,
    baseline_value DECIMAL(18,6),
    scenario_value DECIMAL(18,6),
    variance DECIMAL(18,6),
    variance_rate DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scenario_id) REFERENCES t_whatif_scenario(id)
);

-- 索引
CREATE INDEX idx_whatif_scenario_no ON t_whatif_scenario(scenario_no);
CREATE INDEX idx_whatif_scenario_type ON t_whatif_scenario(scenario_type);
CREATE INDEX idx_whatif_scenario_status ON t_whatif_scenario(status);
CREATE INDEX idx_whatif_comparison_scenario ON t_whatif_comparison(scenario_id);

-- ============================================================
-- 第十部分：需求池模块 (t_)
-- ============================================================

-- 需求池表
CREATE TABLE t_demand_pool (
    id BIGSERIAL PRIMARY KEY,
    demand_no VARCHAR(50) NOT NULL UNIQUE,
    demand_type VARCHAR(20) NOT NULL,   -- ORDER/FORECAST/PROJECT/SPARE
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(200),
    demand_source VARCHAR(50),           -- 来源：ORDER_NO/FORECAST_NO
    source_id BIGINT,
    source_line_id BIGINT,
    qty DECIMAL(18,6) NOT NULL,
    due_date DATE,
    priority INT DEFAULT 5,              -- 1-10
    status VARCHAR(20) DEFAULT 'PENDING',-- PENDING/CONFIRMED/EXECUTED/CANCELLED
    demand_date DATE,                    -- 需求日期
    warehouse_id BIGINT,                 -- 需求仓库
    is_satisfied BOOLEAN DEFAULT FALSE,  -- 是否已满足
    satisfied_qty DECIMAL(18,6) DEFAULT 0,
    satisfied_date DATE,
    allocated_qty DECIMAL(18,6) DEFAULT 0,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES m_warehouse(id)
);

-- 需求来源追踪表
CREATE TABLE t_demand_source (
    id BIGSERIAL PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,    -- SALES_ORDER/PURCHASE_ORDER/FORECAST
    source_id BIGINT NOT NULL,
    source_line_id BIGINT,
    original_qty DECIMAL(18,6) NOT NULL,
    remaining_qty DECIMAL(18,6) NOT NULL,
    allocated_qty DECIMAL(18,6) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (demand_id) REFERENCES t_demand_pool(id),
    UNIQUE(source_type, source_id, source_line_id)
);

-- 索引
CREATE INDEX idx_demand_pool_no ON t_demand_pool(demand_no);
CREATE INDEX idx_demand_pool_item ON t_demand_pool(item_code);
CREATE INDEX idx_demand_pool_status ON t_demand_pool(status);
CREATE INDEX idx_demand_pool_due_date ON t_demand_pool(due_date);
CREATE INDEX idx_demand_pool_priority ON t_demand_pool(priority);
CREATE INDEX idx_demand_source_demand ON t_demand_source(demand_id);
CREATE INDEX idx_demand_source_source ON t_demand_source(source_type, source_id);

-- ============================================================
-- 第十一部分：AI 模型配置模块 (m_)
-- ============================================================

-- AI 模型配置表
CREATE TABLE m_ai_model (
    id BIGSERIAL PRIMARY KEY,
    model_code VARCHAR(50) NOT NULL UNIQUE,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(20) NOT NULL,     -- DEMAND_PREDICTION/SAFETY_STOCK/OR_SCHEDULING
    provider VARCHAR(20),                 -- OPENAI/DEEPSEEK/OLLAMA/LOCAL
    model_version VARCHAR(20),
    endpoint VARCHAR(500),
    api_key_encrypted VARCHAR(500),
    parameters JSONB,                    -- 模型参数配置
    is_active BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    last_test_date TIMESTAMP,
    last_test_result JSONB,
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AI 训练记录表
CREATE TABLE t_ai_training_log (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL,
    training_type VARCHAR(20) NOT NULL,  -- INITIAL/RETRAINING
    training_data_range JSONB,           -- 训练数据范围
    training_samples INT,
    accuracy_before DECIMAL(5,4),
    accuracy_after DECIMAL(5,4),
    mape_before DECIMAL(5,4),
    mape_after DECIMAL(5,4),
    status VARCHAR(20) DEFAULT 'RUNNING',-- RUNNING/COMPLETED/FAILED
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    error_msg TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES m_ai_model(id)
);

-- 索引
CREATE INDEX idx_ai_model_code ON m_ai_model(model_code);
CREATE INDEX idx_ai_model_type ON m_ai_model(model_type);
CREATE INDEX idx_ai_training_model ON t_ai_training_log(model_id);
CREATE INDEX idx_ai_training_status ON t_ai_training_log(status);

-- ============================================================
-- 第十二部分：安全库存配置表 (m_)
-- ============================================================

-- 安全库存配置表
CREATE TABLE m_safety_stock_config (
    id BIGSERIAL PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL,
    warehouse_id BIGINT,
    config_type VARCHAR(20) NOT NULL,   -- MANUAL/AI/CALCULATED
    method VARCHAR(50),                  -- 方法：FIXED/PERCENTAGE/ROI/AI
    safety_stock_qty DECIMAL(18,6),      -- 手工设置数量
    safety_stock_percent DECIMAL(5,4),  -- 百分比
    service_level DECIMAL(5,4),         -- 服务水平
    lead_time_variance DECIMAL(5,4),    -- 交期波动系数
    demand_variance DECIMAL(5,4),       -- 需求波动系数
    calculated_safety_stock DECIMAL(18,6), -- 计算值
    ai_recommended_qty DECIMAL(18,6),  -- AI 推荐值
    ai_confidence DECIMAL(5,2),         -- AI 推荐置信度
    is_auto_update BOOLEAN DEFAULT FALSE,-- 是否自动更新
    effective_from DATE,
    effective_to DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    memo TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(item_code, warehouse_id, status)
);

CREATE INDEX idx_safety_stock_item ON m_safety_stock_config(item_code);
CREATE INDEX idx_safety_stock_warehouse ON m_safety_stock_config(warehouse_id);

-- ============================================================
-- 补充表创建完成
-- ============================================================

SELECT 'AI MRP 补充数据表创建完成!' AS message;

-- ============================================================
-- 第十三部分：用户权限模块 (m_) - 补充
-- ============================================================

-- 用户表
CREATE TABLE m_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE m_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE m_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(100),
    resource_type VARCHAR(20),
    resource_path VARCHAR(200),
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联
CREATE TABLE m_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES m_user(id),
    FOREIGN KEY (role_id) REFERENCES m_role(id)
);

-- 角色权限关联
CREATE TABLE m_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY(role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES m_role(id),
    FOREIGN KEY (permission_id) REFERENCES m_permission(id)
);

-- 索引
CREATE INDEX idx_user_username ON m_user(username);
CREATE INDEX idx_user_status ON m_user(status);
CREATE INDEX idx_role_code ON m_role(role_code);

-- ============================================================
-- 第十四部分：系统参数模块 (m_) - 补充
-- ============================================================

-- 系统参数表
CREATE TABLE m_system_param (
    id BIGSERIAL PRIMARY KEY,
    param_key VARCHAR(100) NOT NULL UNIQUE,
    param_value VARCHAR(500),
    param_type VARCHAR(20),
    param_group VARCHAR(50),
    description VARCHAR(200),
    is_editable BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化 MRP 参数
INSERT INTO m_system_param (param_key, param_value, param_type, param_group, description) VALUES
('mrp.batch.size', '1000', 'NUMBER', 'MRP', 'MRP 批量计算大小'),
('mrp.lead.time.default', '7', 'NUMBER', 'MRP', '默认提前期（天）'),
('mrp.safety.stock.method', 'SERVICE_LEVEL', 'STRING', 'MRP', '安全库存计算方法'),
('inventory.alert.threshold', '0.2', 'NUMBER', 'INVENTORY', '库存预警阈值'),
('purchase.moq.enabled', 'true', 'BOOLEAN', 'PURCHASE', '是否启用 MOQ');

-- ============================================================
-- 第十五部分：通知模块 (t_) - 补充
-- ============================================================

-- 通知记录表
CREATE TABLE t_notification (
    id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    receiver_id BIGINT NOT NULL,
    channel VARCHAR(20),
    status VARCHAR(20) DEFAULT 'UNREAD',
    sent_time TIMESTAMP,
    read_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (receiver_id) REFERENCES m_user(id)
);

-- 通知订阅表
CREATE TABLE m_notification_subscription (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    channel VARCHAR(20) DEFAULT 'IN_APP',
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES m_user(id)
);

-- 索引
CREATE INDEX idx_notification_receiver ON t_notification(receiver_id);
CREATE INDEX idx_notification_status ON t_notification(status);
CREATE INDEX idx_notification_type ON t_notification(notification_type);

-- ============================================================
-- 第十六部分：导入导出模块 (t_) - 补充
-- ============================================================

-- 导入任务表
CREATE TABLE t_import_task (
    id BIGSERIAL PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    import_type VARCHAR(20) NOT NULL,
    file_name VARCHAR(200),
    file_url VARCHAR(500),
    total_rows INT,
    success_rows INT DEFAULT 0,
    fail_rows INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    error_detail JSONB,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 导出任务表
CREATE TABLE t_export_task (
    id BIGSERIAL PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    export_type VARCHAR(20) NOT NULL,
    file_name VARCHAR(200),
    file_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 索引
CREATE INDEX idx_import_task_type ON t_import_task(import_type);
CREATE INDEX idx_import_task_status ON t_import_task(status);
CREATE INDEX idx_export_task_type ON t_export_task(export_type);
CREATE INDEX idx_export_task_status ON t_export_task(status);

-- ============================================================
-- 第十七部分：报表物化视图 - 补充
-- ============================================================

-- 订单汇总物化视图
CREATE MATERIALIZED VIEW mv_order_summary AS
SELECT 
    DATE_TRUNC('day', order_date) AS order_date,
    customer_code,
    item_code,
    COUNT(*) AS order_count,
    SUM(qty) AS total_qty,
    SUM(total_amount) AS total_amount
FROM t_sales_order
GROUP BY DATE_TRUNC('day', order_date), customer_code, item_code;

-- 库存周转物化视图
CREATE MATERIALIZED VIEW mv_inventory_turnover AS
SELECT 
    i.item_code,
    i.warehouse_code,
    COALESCE(SUM(CASE WHEN t.trans_type = 'IN' THEN t.trans_qty ELSE 0 END), 0) AS total_in_qty,
    COALESCE(SUM(CASE WHEN t.trans_type = 'OUT' THEN t.trans_qty ELSE 0 END), 0) AS total_out_qty,
    AVG(i.on_hand_qty) AS avg_on_hand
FROM m_inventory i
LEFT JOIN t_inventory_transaction t ON i.item_code = t.item_code AND i.warehouse_id = t.warehouse_id
GROUP BY i.item_code, i.warehouse_code;

-- MRP 执行汇总
CREATE MATERIALIZED VIEW mv_mrp_execution_summary AS
SELECT 
    DATE_TRUNC('day', created_at) AS run_date,
    run_type,
    status,
    COUNT(*) AS execution_count,
    SUM(total_demands) AS total_demands,
    SUM(total_suggestions) AS total_suggestions,
    AVG(run_time_ms) AS avg_run_time_ms
FROM t_mrp_run
GROUP BY DATE_TRUNC('day', created_at), run_type, status;

-- ============================================================
-- 补充表创建完成
-- ============================================================

SELECT 'AI MRP 补充数据表（第二轮）创建完成!' AS message;
