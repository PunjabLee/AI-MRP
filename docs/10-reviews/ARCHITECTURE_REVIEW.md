# AI-MRP 架构完整性 Review 报告

> **日期**：2026-03-09  
> **版本**：1.0  
> **目的**：自上而下分解到位，自下而上完整实现支撑

---

## 一、PRD 功能全景图

### 1.1 功能模块一览

| 层级 | 模块 | 子功能 |
|------|------|--------|
| **展示层** | 对话式交互 | AI 对话窗、意图理解、执行引擎、结果呈现 |
| **业务层** | 需求管理 | 销售订单、销售预测、需求池 |
| | BOM 管理 | BOM 维护、BOM 展开 |
| | 库存管理 | 库存查询、库存交易、库龄分析 |
| | MRP 计算 | MRP 执行、MRP 参数、MRP 结果 |
| | 采购管理 | 供应商、采购执行 |
| | 生产管理 | 生产计划、车间执行、报工 |
| **智能层** | LLM 理解 | 意图识别、实体提取、上下文管理 |
| | OR 求解 | 排程优化、批量优化 |
| | 预测分析 | 需求预测、安全库存推荐 |
| | 异常诊断 | 缺料诊断、延期诊断 |
| | 风险预警 | 供应商风险、库存风险、需求突变 |
| | 影响分析 | 插单影响、What-if 模拟 |
| **数据层** | 主数据 | 物料、BOM、供应商、仓库 |
| | 交易数据 | 订单、库存事务、MRP 结果 |
| | 分析数据 | 预测结果、风险记录 |

---

## 二、技术架构覆盖度分析

### 2.1 服务模块对照

| PRD 功能 | 对应服务 | 状态 | 说明 |
|----------|----------|------|------|
| 需求管理 | Demand Service | ✅ | |
| 销售预测 | Forecast Service | ✅ | |
| BOM 管理 | BOM Service | ✅ | |
| 库存管理 | Inventory Service | ✅ | |
| MRP 计算 | MRP Engine | ✅ | |
| 采购管理 | Purchase Service | ✅ | |
| 生产管理 | Production Service | ✅ | |
| AI 预测 | Predictor Engine | ✅ | |
| 风险预警 | Risk Service + Engine | ✅ | |
| What-if | WhatIf Service | ✅ | |
| 沙箱机制 | Sandbox Service | ✅ | |
| 对话式交互 | Conversation Service | ✅ | |
| 报表分析 | Report Service | ⚠️ | 需细化 |

### 2.2 技术架构缺失项

| 缺失项 | 影响 | 优先级 |
|--------|------|--------|
| 用户/权限模块 | 无法实现认证授权 | P0 |
| 通知模块 | 预警无法推送 | P1 |
| 系统参数模块 | MRP 参数无法配置 | P1 |
| 报表数据模型 | 报表无数据支撑 | P1 |

---

## 三、数据模型覆盖度分析

### 3.1 现有数据表统计

| 分类 | 表数量 | 状态 |
|------|--------|------|
| 主数据 (m_) | ~15 | ✅ |
| 交易数据 (t_) | ~15 | ✅ |
| 沙箱数据 (s_) | 4 | ✅ |
| 日志数据 (l_) | 2 | ✅ |
| 补充表 | ~13 | ✅ |

### 3.2 数据模型缺失项

| 缺失表 | 用途 | 优先级 |
|--------|------|--------|
| m_user / m_role / m_permission | 用户权限 | P0 |
| m_system_param | 系统参数配置 | P1 |
| t_notification | 通知记录 | P1 |
| t_import_task / t_export_task | 导入导出任务 | P2 |
| mv_xxx (物化视图) | 报表数据 | P1 |
| m_workstation / m_equipment | 车间设备 | P2 |
| m_production_line | 生产线 | P2 |

---

## 四、代码分层完整性分析

### 4.1 分层覆盖

| 层级 | 内容 | 状态 |
|------|------|------|
| Domain | 实体、值对象、聚合根、领域服务 | ✅ |
| Application | Command/Query、DTO、Handler | ✅ |
| Infrastructure | Repository、Cache、MQ、External | ✅ |
| API | Controller、Assembler | ✅ |

### 4.2 缺失设计

| 缺失项 | 说明 |
|--------|------|
| 用户权限模块设计 | 需要补充 |
| 通知模块设计 | 需要补充 |
| 导入导出模块设计 | 需要补充 |
| 报表查询设计 | 需要补充 |

---

## 五、补充设计清单

### 5.1 用户权限模块

```sql
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
    resource_type VARCHAR(20),  -- MENU/BUTTON/API
    resource_path VARCHAR(200),
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联
CREATE TABLE m_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY(user_id, role_id)
);

-- 角色权限关联
CREATE TABLE m_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY(role_id, permission_id)
);
```

### 5.2 系统参数模块

```sql
-- 系统参数表
CREATE TABLE m_system_param (
    id BIGSERIAL PRIMARY KEY,
    param_key VARCHAR(100) NOT NULL UNIQUE,
    param_value VARCHAR(500),
    param_type VARCHAR(20),  -- STRING/NUMBER/BOOLEAN/JSON
    param_group VARCHAR(50),  -- MRP/INVENTORY/PURCHASE
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
```

### 5.3 通知模块

```sql
-- 通知记录表
CREATE TABLE t_notification (
    id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(20) NOT NULL,  -- RISK/WORKFLOW/SYSTEM
    title VARCHAR(200) NOT NULL,
    content TEXT,
    receiver_id BIGINT NOT NULL,
    channel VARCHAR(20),  -- IN_APP/EMAIL/SMS/WECHAT
    status VARCHAR(20) DEFAULT 'UNREAD',  -- UNREAD/READ/SENT
    sent_time TIMESTAMP,
    read_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 通知订阅表
CREATE TABLE m_notification_subscription (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    channel VARCHAR(20) DEFAULT 'IN_APP',
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.4 导入导出模块

```sql
-- 导入任务表
CREATE TABLE t_import_task (
    id BIGSERIAL PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    import_type VARCHAR(20) NOT NULL,  -- ORDER/ITEM/BOM
    file_name VARCHAR(200),
    file_url VARCHAR(500),
    total_rows INT,
    success_rows INT DEFAULT 0,
    fail_rows INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/PROCESSING/COMPLETED/FAILED
    error_detail JSONB,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 导出任务表
CREATE TABLE t_export_task (
    id BIGSERIAL PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE,
    export_type VARCHAR(20) NOT NULL,  -- ORDER/INVENTORY/REPORT
    file_name VARCHAR(200),
    file_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);
```

### 5.5 报表物化视图

```sql
-- 订单汇总物化视图
CREATE MATERIALIZED VIEW mv_order_summary AS
SELECT 
    DATE_TRUNC('day', order_date) AS order_date,
    customer_code,
    item_code,
    COUNT(*) AS order_count,
    SUM(qty) AS total_qty,
    SUM(total_amount) AS total_amount,
    COUNT(DISTINCT order_no) AS order_no_count
FROM t_sales_order
GROUP BY DATE_TRUNC('day', order_date), customer_code, item_code;

-- 库存周转物化视图
CREATE MATERIALIZED VIEW mv_inventory_turnover AS
SELECT 
    item_code,
    warehouse_code,
    SUM(CASE WHEN trans_type = 'IN' THEN trans_qty ELSE 0 END) AS total_in_qty,
    SUM(CASE WHEN trans_type = 'OUT' THEN trans_qty ELSE 0 END) AS total_out_qty,
    AVG(on_hand_qty) AS avg_on_hand,
    CASE 
        WHEN SUM(CASE WHEN trans_type = 'OUT' THEN trans_qty ELSE 0 END) > 0
        THEN AVG(on_hand_qty) * 365 / SUM(CASE WHEN trans_type = 'OUT' THEN trans_qty ELSE 0 END)
        ELSE 0 
    END AS turnover_days
FROM m_inventory
LEFT JOIN t_inventory_transaction ON m_inventory.item_code = t_inventory_transaction.item_code
GROUP BY item_code, warehouse_code;

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
```

---

## 六、补充代码分层设计

### 6.1 用户权限模块结构

```
aimrp-common/
├── security/
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java
│   ├── handler/
│   │   └── AccessDeniedHandler.java
│   └── service/
│       ├── UserDetailsService.java
│       └── JwtService.java
│
aimrp-system/                        # 新增模块
├── domain/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── Permission.java
│   ├── valueobject/
│   │   ├── UserStatus.java
│   │   └── Password.java
│   └── repository/
│       ├── UserRepository.java
│       └── RoleRepository.java
│
├── application/
│   ├── command/
│   │   ├── CreateUserCommand.java
│   │   └── AssignRoleCommand.java
│   ├── query/
│   │   ├── UserQuery.java
│   │   └── RoleQuery.java
│   └── service/
│       ├── UserApplicationService.java
│       └── AuthApplicationService.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── po/
│   │   │   ├── UserPO.java
│   │   │   └── RolePO.java
│   │   └── mapper/
│   │       └── UserMapper.java
│   └── security/
│       └── JwtTokenProvider.java
│
└── api/
    ├── controller/
    │   ├── AuthController.java
    │   └── UserController.java
    └── dto/
        ├── LoginRequest.java
        └── LoginResponse.java
```

### 6.2 通知模块结构

```
aimrp-notification/                   # 新增模块
├── domain/
│   ├── entity/
│   │   ├── Notification.java
│   │   └── Subscription.java
│   └── repository/
│       └── NotificationRepository.java
│
├── application/
│   ├── service/
│   │   ├── NotificationApplicationService.java
│   │   └── SubscriptionService.java
│   └── handler/
│       └── RiskAlertHandler.java     # 监听风险事件发送通知
│
├── infrastructure/
│   ├── mq/
│   │   └── NotificationConsumer.java
│   ├── channel/
│   │   ├── EmailChannel.java
│   │   ├── SmsChannel.java
│   │   └── WechatChannel.java
│   └── persistence/
│       └── NotificationMapper.java
│
└── api/
    └── controller/
        └── NotificationController.java
```

---

## 七、版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2026-03-09 | 初始版本 - 架构完整性 Review |

---

*Review 完成*
