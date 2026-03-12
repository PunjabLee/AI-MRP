# AI MRP 预算管理模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、业务需求

### 1.1 核心功能

| 功能 | 说明 |
|------|------|
| 年度预算编制 | 按组织/部门编制年度预算 |
| 预算分解 | 季度/月度分解 |
| 预算控制 | 超出预算预警/阻断 |
| 预算分析 | 实际 vs 预算对比 |
| 预算调整 | 预算变更审批 |

### 1.2 预算类型

| 类型 | 说明 |
|------|------|
| 销售预算 | 销售收入目标 |
| 采购预算 | 采购成本预算 |
| 生产预算 | 生产成本预算 |
| 费用预算 | 管理/销售/研发费用 |

---

## 二、技术架构

### 2.1 模块结构

```
aimrp-budget/
├── src/main/java/com/aimrp/budget/
│   ├── controller/
│   │   ├── BudgetController.java      # 预算管理
│   │   ├── BudgetApplyController.java  # 预算申请
│   │   └── BudgetAnalysisController.java # 预算分析
│   ├── service/
│   │   ├── BudgetService.java          # 预算服务
│   │   ├── BudgetControlService.java   # 预算控制
│   │   └── BudgetAnalysisService.java  # 预算分析
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── Budget.java            # 预算实体
│   │   │   ├── BudgetItem.java        # 预算明细
│   │   │   └── BudgetAdjustment.java  # 预算调整
│   │   └── repository/
│   │       └── BudgetRepository.java
│   └── dto/
│       ├── BudgetRequest.java
│       └── BudgetResponse.java
```

### 2.2 数据库设计

```sql
-- 预算主表
CREATE TABLE t_budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_no VARCHAR(50) NOT NULL,       -- 预算编号
    budget_year INT NOT NULL,             -- 预算年度
    budget_type VARCHAR(20),            -- 预算类型
    org_id VARCHAR(50),                  -- 组织ID
    dept_id VARCHAR(50),                 -- 部门ID
    total_amount DECIMAL(18,2),         -- 预算总额
    used_amount DECIMAL(18,2) DEFAULT 0, -- 已使用
    remaining_amount DECIMAL(18,2),      -- 剩余
    status VARCHAR(20),                  -- DRAFT/APPROVED/CLOSED
    creator_id VARCHAR(50),
    create_time DATETIME,
    updater_id VARCHAR(50),
    update_time DATETIME
);

-- 预算明细表
CREATE TABLE t_budget_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_id BIGINT NOT NULL,
    item_name VARCHAR(100),              -- 预算项目
    category VARCHAR(50),                -- 类别
    budget_amount DECIMAL(18,2),        -- 预算金额
    used_amount DECIMAL(18,2) DEFAULT 0,
    quarter INT,                         -- 季度
    month INT,                          -- 月份
    remark VARCHAR(500)
);

-- 预算调整表
CREATE TABLE t_budget_adjustment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_id BIGINT NOT NULL,
    adjustment_no VARCHAR(50),          -- 调整单号
    adjust_type VARCHAR(20),            -- ADD/REDUCE/TRANSFER
    adjust_amount DECIMAL(18,2),
    reason VARCHAR(500),
    status VARCHAR(20),                  -- PENDING/APPROVED/REJECTED
    apply_time DATETIME,
    approver_id VARCHAR(50),
    approve_time DATETIME
);
```

---

## 三、核心功能

### 3.1 预算编制

```java
@Service
@RequiredArgsConstructor
public class BudgetService {

    /**
     * 创建年度预算
     */
    public Budget createAnnualBudget(BudgetRequest request) {
        // 1. 校验
        validateBudgetRequest(request);
        
        // 2. 生成预算编号
        String budgetNo = generateBudgetNo(request.getBudgetYear());
        
        // 3. 创建预算主表
        Budget budget = Budget.builder()
            .budgetNo(budgetNo)
            .budgetYear(request.getBudgetYear())
            .budgetType(request.getBudgetType())
            .orgId(request.getOrgId())
            .deptId(request.getDeptId())
            .totalAmount(request.getTotalAmount())
            .status("DRAFT")
            .build();
        
        budget = save(budget);
        
        // 4. 创建预算明细
        for (BudgetItemRequest item : request.getItems()) {
            BudgetItem itemEntity = convertToEntity(item);
            itemEntity.setBudgetId(budget.getId());
            budgetItemRepository.save(itemEntity);
        }
        
        return budget;
    }

    /**
     * 提交审批
     */
    public void submitForApproval(Long budgetId) {
        Budget budget = getById(budgetId);
        
        // 触发审批流
        processService.startProcess(
            "budget_approval",
            budget.getBudgetNo(),
            buildProcessVariables(budget)
        );
        
        budget.setStatus("PENDING_APPROVAL");
        save(budget);
    }
}
```

### 3.2 预算控制

```java
@Service
@RequiredArgsConstructor
public class BudgetControlService {

    /**
     * 预算前置校验
     */
    public BudgetCheckResult checkBeforeExpense(String deptId, String budgetType, BigDecimal amount) {
        // 1. 获取当前可用预算
        Budget budget = getAvailableBudget(deptId, budgetType);
        
        if (budget == null) {
            return BudgetCheckResult.builder()
                .pass(false)
                .message("该部门暂无预算")
                .build();
        }
        
        // 2. 计算剩余
        BigDecimal remaining = budget.getTotalAmount()
            .subtract(budget.getUsedAmount());
        
        // 3. 控制策略
        ControlStrategy strategy = getControlStrategy(budgetType);
        
        switch (strategy) {
            case STRICT:      // 严格控制：不能超出
                return BudgetCheckResult.builder()
                    .pass(amount.compareTo(remaining) <= 0)
                    .remaining(remaining)
                    .message(amount.compareTo(remaining) > 0 ? "预算不足" : "OK")
                    .build();
                    
            case WARNING:      // 警告控制：超出提醒
                boolean overBudget = amount.compareTo(remaining) > 0;
                return BudgetCheckResult.builder()
                    .pass(true)
                    .warning(overBudget)
                    .remaining(remaining)
                    .message(overBudget ? "超出预算" : "OK")
                    .build();
                    
            default:
                return BudgetCheckResult.builder().pass(true).build();
        }
    }

    /**
     * 占用预算（实际消费时）
     */
    public void occupyBudget(String deptId, String budgetType, BigDecimal amount) {
        Budget budget = getAvailableBudget(deptId, budgetType);
        budget.setUsedAmount(budget.getUsedAmount().add(amount));
        save(budget);
    }
}
```

### 3.3 预算分析

```java
@Service
@RequiredArgsConstructor
public class BudgetAnalysisService {

    /**
     * 预算执行分析
     */
    public BudgetAnalysisResult analyze(Long budgetId) {
        Budget budget = getById(budgetId);
        List<BudgetItem> items = budgetItemRepository.findByBudgetId(budgetId);
        
        // 计算执行率
        BigDecimal totalBudget = budget.getTotalAmount();
        BigDecimal used = budget.getUsedAmount();
        BigDecimal executionRate = used.divide(totalBudget, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        // 按月分析
        List<MonthlyAnalysis> monthly = analyzeMonthly(items);
        
        // 趋势分析
        TrendAnalysis trend = analyzeTrend(budgetId);
        
        return BudgetAnalysisResult.builder()
            .budgetNo(budget.getBudgetNo())
            .totalBudget(totalBudget)
            .usedAmount(used)
            .remainingAmount(totalBudget.subtract(used))
            .executionRate(executionRate)
            .monthlyAnalysis(monthly)
            .trendAnalysis(trend)
            .build();
    }
}
```

---

## 四、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/budget/create` | POST | 创建预算 |
| `/api/budget/submit` | POST | 提交审批 |
| `/api/budget/approve` | POST | 审批通过 |
| `/api/budget/adjust` | POST | 预算调整 |
| `/api/budget/check` | POST | 预算校验 |
| `/api/budget/occupy` | POST | 占用预算 |
| `/api/budget/analyze` | GET | 预算分析 |
| `/api/budget/report` | GET | 预算报表 |

---

## 五、实施计划

| 周次 | 内容 |
|------|------|
| 第1周 | 预算编制、审批流集成 |
| 第2周 | 预算控制、占用释放 |
| 第3周 | 预算分析、报表 |
| 第4周 | 测试优化 |

**预估工时**: 80小时

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
