# AI MRP MPS+MRP+DRP 联动设计

> **版本**: 1.0
> **日期**: 2026-03-12

---

## 一、业务流程

### 1.1 联动流程图

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                         MPS + MRP + DRP 业务闭环                              │
└────────────────────────────────────────────────────────────────────────────────┘

  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │   需求来源    │     │     MPS      │     │     MRP      │     │     DRP      │
  │              │     │ 主生产计划    │     │ 物料需求计划  │     │ 配送需求计划  │
  └──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
         │                    │                    │                    │
         ▼                    ▼                    ▼                    ▼
  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │ • 销售订单   │────▶│ • 毛需求计算 │────▶│ • 毛需求汇总 │────▶│ • 配送需求 │
  │ • 预测数据   │     │ • 产能检查   │     │ • 库存匹配   │     │ • 仓库分配 │
  │ • 独立需求   │     │ • 建议生成   │     │ • 建议生成   │     │ • 运输计划 │
  └──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
                              │                    │                    │
                              │                    ▼                    │
                              │            ┌──────────────┐            │
                              │            │   采购/生产   │            │
                              │            │    建议单     │            │
                              │            └──────────────┘            │
                              │                    │                    │
                              ▼                    ▼                    ▼
                       ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
                       │  生产订单    │     │  采购订单    │     │  配送订单    │
                       └──────────────┘     └──────────────┘     └──────────────┘
```

### 1.2 数据流向

```
需求输入层
    │
    ├── 销售订单 ─────────────────────────┐
    │                                      │
    ├── 需求预测 ──────▶ MPS ──▶ MRP ──┼──▶ 采购建议 ──▶ 采购订单
    │                         │         │
    │                         │         │
    └── 独立需求 ─────────────┘         │
                                       │
                              MRP产出 ─┘
                                       │
                              生产建议 ──▶ 生产订单

                                              │
MRP产出 ◀────────────────────────────────────┘
    │
    ├── 采购建议 ──▶ 采购 ──▶ 入库 ──▶ 库存
    │
    └── 生产建议 ──▶ 生产 ──▶ 成品 ──▶ 库存 ──▶ DRP ──▶ 配送 ──▶ 客户
```

---

## 二、MPS 模块增强

### 2.1 功能清单

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 需求汇总 | 汇总销售订单+预测+独立需求 | P0 |
| 毛需求计算 | 按时间维度计算总需求 | P0 |
| 产能检查 | 检查生产线产能是否满足 | P1 |
| MPS建议生成 | 生成生产计划建议 | P0 |
| 建议审核 | 人工审核后下达 | P1 |
| MRP联动 | 将MPS结果传递给MRP | P0 |

### 2.2 数据模型

```java
// MPS 计划
public class MpsPlan {
    private Long id;
    private String planNo;           // MPS计划编号
    private LocalDate planStartDate; // 计划开始日期
    private LocalDate planEndDate;   // 计划结束日期
    private String status;           // DRAFT/RELEASED/COMPLETED
    private String runType;          // MANUAL/AUTO
    private Integer itemCount;        // 物料种类数
    private Integer totalQty;         // 总生产量
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// MPS 建议
public class MpsSuggestion {
    private Long id;
    private Long planId;             // 关联MPS计划
    private Long itemId;             // 物料ID
    private String itemCode;         // 物料编码
    private BigDecimal suggestedQty; // 建议生产量
    private LocalDate dueDate;       // 需求日期
    private Integer priority;         // 优先级
    private String sourceType;       // 需求来源类型
    private String sourceNo;         // 来源单号
    private String status;           // PENDING/APPROVED/REJECTED
}
```

### 2.3 API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/mps/run` | POST | 执行MPS运算 |
| `/api/mps/suggestions` | GET | 获取MPS建议列表 |
| `/api/mps/suggestions/{id}/approve` | POST | 审核建议 |
| `/api/mps/suggestions/convert-to-mrp` | POST | 转换为MRP输入 |
| `/api/mps/to-mrp` | POST | 联动执行MPS+MRP |

---

## 三、MRP 模块联动

### 3.1 输入源扩展

| 来源 | 说明 | 优先级 |
|------|------|--------|
| MPS产出 | MPS审核后的生产建议 | P0 |
| 销售订单 | 独立需求 | P0 |
| 预测数据 | 预测需求 | P1 |
| 安全库存 | 补货需求 | P1 |

### 3.2 MRP 运行参数扩展

```java
public class MrpRunRequest {
    private String runType;              // MANUAL/MPS_AUTO
    private LocalDate planStartDate;     // 计划开始
    private LocalDate planEndDate;      // 计划结束
    private List<Long> mpsPlanIds;      // 关联的MPS计划ID
    private boolean enableDrpInput;      // 是否将MRP结果输入DRP
}
```

### 3.3 API 扩展

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/mrp/run-with-mps` | POST | MPS+MRP联动运行 |
| `/api/mrp/to-drp` | POST | MRP结果输入DRP |

---

## 四、DRP 模块设计

### 4.1 配送网络

```
┌─────────────────────────────────────────────────────────────────┐
│                      配送网络结构                                │
└─────────────────────────────────────────────────────────────────┘

         ┌──────────────┐
         │  总仓(DC)    │ ◀── 生产基地
         └──────┬───────┘
                │
        ┌───────┼───────┐
        ▼       ▼       ▼
   ┌────────┐┌────────┐┌────────┐
   │RDC区域 ││RDC区域 ││RDC区域 │ ◀── 区域配送中心
   │ 配送  ││ 配送  ││ 配送  │
   └────┬───┘└────┬───┘└────┬───┘
        │        │        │
   ┌────┴┐  ┌───┴┐   ┌───┴┐
   │门店A│  │门店B│   │门店C│
   └─────┘  └────┘   └────┘
```

### 4.2 DRP 运算

| 输入 | 说明 |
|------|------|
| 库存需求 | 门店要货需求 |
| 库存数据 | 各仓库可用库存 |
| 配送策略 | 就近/最低成本/平衡 |
| 运输约束 | 车辆装载/时间窗 |

| 输出 | 说明 |
|------|------|
| 配送计划 | 从哪个仓库配送多少 |
| 运输计划 | 运输路线和时间 |
| 成本预估 | 配送成本估算 |

### 4.3 数据模型

```java
// 配送网络
public class DistributionNetwork {
    private Long id;
    private String networkCode;
    private String networkName;
    private String networkType;   // DC/RDC/STORE
    private Long parentId;        // 上级网络ID
    private Long warehouseId;     // 关联仓库
    private String address;
    private Boolean isActive;
}

// 配送需求
public class DrpDemand {
    private Long id;
    private String demandNo;
    private Long networkId;       // 门店ID
    private Long itemId;
    private BigDecimal demandQty;
    private LocalDate demandDate;
    private String status;        // PENDING/ALLOCATED/SHIPPED
}

// 配送计划
public class DistributionPlan {
    private Long id;
    private String planNo;
    private Long networkId;       // 目的门店
    private Long warehouseId;     // 配送仓库
    private Long itemId;
    private BigDecimal planQty;
    private BigDecimal shippedQty;
    private LocalDate planDate;
    private String status;
}
```

### 4.4 API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/drp/run` | POST | 执行DRP运算 |
| `/api/drp/plans` | GET | 获取配送计划 |
| `/api/drp/plans/{id}/ship` | POST | 发货确认 |

---

## 五、联动实现

### 5.1 MPS → MRP 联动

```java
@Service
@RequiredArgsConstructor
public class MpsMrpIntegrationService {

    private final MpsApplicationService mpsService;
    private final MrpApplicationService mrpService;

    /**
     * MPS + MRP 一体化运行
     */
    public MrpResult runMpsToMrp(MpsRunRequest mpsRequest) {
        // 1. 执行MPS
        MpsResult mpsResult = mpsService.runMps(mpsRequest);

        // 2. 获取MPS建议
        List<MpsSuggestion> approvedSuggestions =
            mpsSuggestionMapper.selectByStatus("APPROVED");

        // 3. 转换为MRP输入
        MrpRunRequest mrpRequest = convertToMrpRequest(approvedSuggestions);
        mrpRequest.setRunType("MPS_AUTO");

        // 4. 执行MRP
        MrpResult mrpResult = mrpService.runMrp(mrpRequest);

        return mrpResult;
    }
}
```

### 5.2 MRP → DRP 联动

```java
@Service
@RequiredArgsConstructor
public class MrpDrpIntegrationService {

    private final MrpApplicationService mrpService;
    private final DrpApplicationService drpService;

    /**
     * MRP + DRP 一体化运行
     */
    public DrpResult runMrpToDrp(Long mrpRunId) {
        // 1. 获取MRP成品建议
        List<MrpSuggestion> productionSuggestions =
            mrpSuggestionMapper.selectByRunIdAndType(mrpRunId, "PRODUCTION");

        // 2. 转换为DRP需求
        List<DrpDemand> demands = convertToDrpDemands(productionSuggestions);

        // 3. 执行DRP
        DrpRunRequest drpRequest = DrpRunRequest.builder()
            .demands(demands)
            .strategy(AllocateStrategy.BALANCED)
            .build();

        DrpResult drpResult = drpService.runDrp(drpRequest);

        return drpResult;
    }
}
```

### 5.3 完整闭环

```java
@Service
@RequiredArgsConstructor
public class SupplyChainPlanningService {

    /**
     * 完整供应链计划闭环
     * MPS → MRP → DRP
     */
    public SupplyChainResult runFullChain(SupplyChainRequest request) {
        // Phase 1: MPS
        MpsResult mpsResult = runMps(request);

        // Phase 2: MRP (输入包含MPS产出)
        MrpResult mrpResult = runMrpWithMps(request, mpsResult);

        // Phase 3: DRP (输入包含MRP产出)
        DrpResult drpResult = runDrpWithMrp(mrpResult);

        // 汇总结果
        return SupplyChainResult.builder()
            .mpsResult(mpsResult)
            .mrpResult(mrpResult)
            .drpResult(drpResult)
            .build();
    }
}
```

---

## 六、实施计划

| 阶段 | 任务 | 优先级 | 工时 |
|------|------|--------|------|
| 1 | MPS 增强 - 需求汇总 | P0 | 2d |
| 2 | MPS 增强 - 建议生成 | P0 | 2d |
| 3 | MPS-MRP 联动 | P0 | 1d |
| 4 | DRP 模块创建 | P0 | 5d |
| 5 | MRP-DRP 联动 | P1 | 1d |
| 6 | 完整闭环测试 | P1 | 2d |

**预估总工时**: 13天

---

*文档版本: 1.0*
*2026-03-12*
