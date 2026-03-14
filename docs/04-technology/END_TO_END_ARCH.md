# AI MRP 完整端到端架构与调用链

> **版本**: 1.0  
> **日期**: 2026-03-11  
> **说明**: 完整记录从前端到AI服务的完整调用链路

---

## 一、系统整体架构

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                    用户浏览器                                        │
│                              (http://localhost:5173)                               │
└─────────────────────────────────────┬───────────────────────────────────────────────┘
                                      │ HTTP/REST
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              Java Spring Boot 后端                                 │
│                              (http://localhost:8080)                              │
│                                                                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ aimrp-api   │  │ aimrp-      │  │ aimrp-      │  │ aimrp-      │             │
│  │ (API网关)   │──│ forecast    │──│ production  │──│ integration │             │
│  └─────────────┘  │ (预测模块)   │  │ (生产模块)  │  │ (集成模块)   │             │
│                   └─────────────┘  └─────────────┘  └──────┬──────┘             │
│                                                             │                     │
│                                                             │ HTTP/REST          │
│                                                             │ (TODO: 实现)        │
└─────────────────────────────────────────────────────────────┼─────────────────────┘
                                                              │
                                                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            Python FastAPI AI 服务                                   │
│                            (http://localhost:8000)                               │
│                                                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                           Router Layer                                      │   │
│  │  /predict/*   /schedule/*   /chat/*   /model/*   /scenario/*   /metrics/* │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                            │
│                                      ▼                                            │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                        Algorithm Layer                                       │   │
│  │  forecast.py   scheduler.py   llm.py   whatif.py   safety_stock.py       │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 二、前端 → Java 后端 完整调用链

### 2.1 需求预测完整调用链

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         前端 → Java 需求预测调用链                                │
└─────────────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整调用链路
================================================================================

【前端】 React aimrp-admin
    │
    │ 文件: src/api/forecast.ts
    │ 方法: forecastApi.forecast()
    │
    ▼ POST /api/forecast/forecast

【Java】 ForecastController
    │
    │ 文件: aimrp-forecast/src/.../ForecastController.java
    │ 行号: ~30
    │ 方法: @PostMapping("/forecast")
    │
    └─ → forecastDemand(ForecastRequest request) → List<ForecastResult>
    
    输入参数:
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │ ForecastRequest (Java)                                                   │
    ├─────────────────────────────────────────────────────────────────────────────┤
    │ - itemCode: String                   # 物料编码                         │
    │ - historicalData: List<HistoricalDataDTO>  # 历史数据                      │
    │   └── [{date: LocalDate, qty: BigDecimal}, ...]                         │
    │ - forecastDays: Integer = 30         # 预测天数                          │
    │ - method: String = "WEIGHTED_MA"     # 预测方法                         │
    └─────────────────────────────────────────────────────────────────────────────┘

    步骤1: 参数转换
        ├─ DTO → Entity 转换
        └─ historicalData: List<HistoricalDataDTO> → List<HistoricalData>

    步骤2: 调用服务
        └─ → forecastService.forecast(itemCode, historicalData, forecastDays, method)
    
    步骤3: 返回结果
        └─ List<ForecastResult>

【Java】 DemandForecastService
    │
    │ 文件: aimrp-forecast/src/.../DemandForecastService.java
    │ 行号: ~30
    │ 方法: forecast()
    │
    └─ forecast(itemCode, historicalData, forecastDays, ForecastMethod)
    
    ForecastMethod 枚举值:
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │ SIMPLE_MA      # 简单移动平均                                            │
    │ WEIGHTED_MA    # 加权移动平均                                            │
    │ EXPONENTIAL    # 指数平滑                                                │
    └─────────────────────────────────────────────────────────────────────────────┘

    [forecastSimpleMA 实现] 行号: ~95
    步骤1: 取最近N天数据
        windowSize = Math.min(7, data.size())
    
    步骤2: 计算平均值
        avg = Σ(qty) / N
    
    步骤3: 生成预测结果
        FOR i IN 1..forecastDays:
            result.forecastDate = today + i
            result.forecastQty = avg
            result.lowerBound = avg * 0.8
            result.upperBound = avg * 1.2

    [forecastWeightedMA 实现] 行号: ~115
    步骤1: 计算加权平均
        weightedSum = Σ(qty[i] * weight[i])  # 近期权重更高
        weightSum = Σ(weight)
        avg = weightedSum / weightSum
    
    步骤2: 生成预测结果 (置信区间更窄: 0.85-1.15)

    [forecastExponential 实现] 行号: ~155
    步骤1: 指数平滑计算
        level[0] = data[0]
        FOR i IN 1..N:
            level[i] = alpha * data[i] + (1-alpha) * level[i-1]
    
    步骤2: 预测
        forecast[i] = level[last]

    返回: List<ForecastResult>

【Java】 ForecastResult (Domain Model)
    │
    │ 文件: aimrp-forecast/src/.../domain/model/ForecastResult.java
    │
    └─ 实体结构:
        ┌─────────────────────────────────────────────────────────────────────────────┐
        │ ForecastResult                                                           │
        ├─────────────────────────────────────────────────────────────────────────────┤
        │ - id: Long                                                              │
        │ - itemCode: String              # 物料编码                                │
        │ - forecastDate: LocalDate     # 预测日期                                │
        │ - forecastQty: BigDecimal      # 预测数量                                │
        │ - lowerBound: BigDecimal       # 下界                                    │
        │ - upperBound: BigDecimal       # 上界                                    │
        │ - forecastType: String         # 预测类型 (DAILY/WEEKLY/MONTHLY)         │
        │ - confidenceLevel: Double       # 置信度                                  │
        │ - createdAt: LocalDateTime     # 创建时间                                │
        └─────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           REST API 对应关系
================================================================================

前端调用                    Java Controller              Java Service              算法
────────────────────────────────────────────────────────────────────────────────────
POST /api/forecast    →  ForecastController.     → DemandForecastService.  → 移动平均
/demand                     forecastDemand()            forecast()             算法

请求体:
{
  "itemCode": "A001",
  "historicalData": [
    {"date": "2024-01-01", "qty": 100},
    {"date": "2024-01-02", "qty": 120}
  ],
  "forecastDays": 30,
  "method": "WEIGHTED_MA"
}

响应体:
[
  {
    "itemCode": "A001",
    "forecastDate": "2024-02-01",
    "forecastQty": 110.00,
    "lowerBound": 93.50,
    "upperBound": 126.50,
    "forecastType": "DAILY"
  },
  ...
]
```

### 2.2 排程优化调用链

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         前端 → Java 排程优化调用链                                │
└─────────────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整调用链路
================================================================================

【前端】
    │
    │ 文件: src/api/production.ts
    │ 方法: productionApi.optimizeSchedule()
    │
    ▼ POST /api/production/schedule/optimize

【Java】 ProductionScheduleController
    │
    │ 文件: aimrp-production/src/.../controller/ProductionScheduleController.java
    │ 方法: @PostMapping("/schedule/optimize")
    │
    └─ optimizeSchedule(ScheduleRequest request) → ScheduleResponse

【Java】 ProductionScheduleService
    │
    │ 文件: aimrp-production/src/.../service/ProductionScheduleService.java
    │ 方法: optimize()
    │
    └─ → OR-Tools 或启发式算法求解

【Java】 OR-Tools 调度器 (待实现)
    │
    │ TODO: 集成 Python AI Service
    │ 
    │ 计划调用:
    │ POST http://localhost:8000/schedule/optimize
    │ {
    │   "orders": [...],
    │   "resources": [...],
    │   "goal": "makespan",
    │   "time_limit_seconds": 30
    │ }

================================================================================
                           数据模型
================================================================================

ScheduleRequest (Java)
├── orders: List<ProductionOrder>
│   └── [{orderId: "O001", product: "A", quantity: 100, priority: 1, ...}]
├── resources: List<Resource>
│   └── [{resourceId: "WC01", name: "工作中心1", capacity: 8, ...}]
├── goal: String = "makespan"     # makespan | tardiness | cost | balanced
├── constraints: Map<String, Object>
└── timeLimitSeconds: Integer = 30

ScheduleResponse (Java)
├── scheduleId: String
├── status: String                # optimal | feasible | suboptimal
├── makespanDays: Double
├── totalCost: BigDecimal
├── scheduleDetails: List<ScheduleDetail>
│   └── [{orderId, resourceId, startTime, endTime, ...}]
└── ganttData: List<GanttItem>
```

### 2.3 安全库存计算调用链

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                       前端 → Java 安全库存计算调用链                              │
└─────────────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整调用链路
================================================================================

【前端】
    │
    │ 文件: src/api/forecast.ts
    │ 方法: forecastApi.recommendSafetyStock()
    │
    ▼ POST /api/forecast/safety-stock

【Java】 SafetyStockController (待实现)
    │
    │ 计划方法: @PostMapping("/safety-stock")
    │
    └─ recommendSafetyStock(SafetyStockRequest) → SafetyStockResponse

【Java】 SafetyStockService
    │
    │ 文件: aimrp-forecast/src/.../domain/service/SafetyStockService.java
    │ 方法: recommend()
    │
    └─ recommend(itemCode, avgDemand, demandHistory, leadTime, method, serviceLevel)

    [实现] 行号: ~30
    步骤1: 计算需求标准差
        demandStd = sqrt(Σ(qty - avg)² / N)
    
    步骤2: 计算安全库存 (统计法)
        z = getZScore(serviceLevel)  # 0.95 → 1.65
        safetyStock = z * demandStd * sqrt(leadTime)
    
    步骤3: 计算再订货点
        reorderPoint = avgDemand * leadTime + safetyStock
    
    步骤4: 计算经济订货量 (EOQ)
        eoq = sqrt(2 * annualDemand * orderCost / (holdingCostRate * unitCost))

    返回: SafetyStockResult

================================================================================
                           数据模型
================================================================================

SafetyStockRequest
├── itemCode: String
├── avgDemand: BigDecimal
├── demandHistory: List<BigDecimal>
├── leadTime: Integer = 7                    # 提前期(天)
├── method: String = "FIXED_DAYS"            # FIXED_DAYS | STATISTICAL | EOQ
└── serviceLevel: Double = 0.95

SafetyStockResult
├── recommendedSafetyStock: BigDecimal
├── reorderPoint: BigDecimal
├── optimalOrderQty: BigDecimal
├── explanation: String
├── standardDeviation: BigDecimal
└── zScore: Double
```

---

## 三、Java → Python AI Service (待实现)

### 3.1 集成方案设计

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                        Java → Python 集成架构                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           集成架构设计
================================================================================

┌──────────────┐                    ┌──────────────┐
│   Java       │                    │   Python     │
│   后端       │                    │   AI Service │
│              │                    │              │
│  ┌────────┐  │   HTTP/REST       │  ┌────────┐  │
│  │ Feign  │──┼────────────────────┼──│ Router │  │
│  │ Client │  │   JSON Request     │  │        │  │
│  └────────┘  │                    │  └────────┘  │
│      │       │                    │      │        │
│      │       │   JSON Response    │      │        │
│      ▼       │ ◄───────────────────┼───────▲      │
│  ┌────────┐  │                    │  ┌─────┴────┐ │
│  │ Service │  │   回调(可选)       │  │Algorithm │ │
│  │ Layer  │  │ ◄───────────────────┼──│ Layer   │ │
│  └────────┘  │                    │  └──────────┘ │
└──────────────┘                    └──────────────┘

================================================================================
                           需要实现的功能
================================================================================

【1. Feign 客户端】 aimrp-integration 模块

    文件: aimrp-integration/src/.../feign/AIForecastClient.java
    
    @FeignClient(
        name = "ai-service",
        url = "${ai.service.url:http://localhost:8000}",
        path = "/predict"
    )
    public interface AIForecastClient {
        
        @PostMapping("/demand")
        ApiResponse<ForecastResponse> forecastDemand(@Body PredictRequest request);
        
        @PostMapping("/safety-stock")
        ApiResponse<SafetyStockResponse> calculateSafetyStock(@Body SafetyStockRequest request);
    }

【2. 配置】 application.yml
    
    ai:
      service:
        url: http://localhost:8000
        timeout: 60000
        retry:
          enabled: true
          maxAttempts: 3

【3. 服务封装】 AimrpForecastService.java
    
    @Service
    @RequiredArgsConstructor
    public class AimrpForecastService {
        
        private final AIForecastClient forecastClient;
        
        public List<ForecastResult> forecastWithAI(PredictRequest request) {
            // 1. 调用Python服务
            var response = forecastClient.forecastDemand(request);
            
            // 2. 转换结果
            return convertToForecastResults(response.getData());
        }
    }

【4. 回调处理】 CallbackController.java
    
    @PostMapping("/integration/callback")
    public void handleCallback(@RequestBody AICallbackRequest request) {
        // 处理异步结果
        var result = resultService.findByRequestId(request.getRequestId());
        result.setData(request.getData());
        result.setStatus(ProcessStatus.COMPLETED);
    }
```

### 3.2 数据交换协议

```
================================================================================
                           请求协议 (Java → Python)
================================================================================

【PredictRequest】→ POST /predict/demand

{
  "item_code": "A001",
  "forecast_days": 30,
  "method": "prophet",
  "confidence_level": 0.95,
  "historical_data": [
    {"date": "2024-01-01", "qty": 100},
    {"date": "2024-01-02", "qty": 120}
  ],
  "prophet_changepoint_prior_scale": 0.05,
  "prophet_seasonality_mode": "multiplicative"
}

【ScheduleRequest】→ POST /schedule/optimize

{
  "orders": [
    {
      "id": "O001",
      "product": "A",
      "quantity": 100,
      "unit_time": 60,
      "priority": 1,
      "deadline": "2024-02-01T00:00:00"
    }
  ],
  "resources": [
    {
      "id": "WC01",
      "name": "工作中心1",
      "capacity": 8,
      "hourly_cost": 100
    }
  ],
  "goal": "makespan",
  "time_limit_seconds": 30
}

================================================================================
                           响应协议 (Python → Java)
================================================================================

【PredictResponse】

{
  "code": 200,
  "message": "success",
  "success": true,
  "data": {
    "item_code": "A001",
    "method": "prophet",
    "forecast": [
      {
        "date": "2024-02-01",
        "qty": 115.5,
        "lower": 95.0,
        "upper": 136.0
      }
    ],
    "confidence": 0.95,
    "metrics": {
      "mape": 8.5,
      "mae": 10.2,
      "rmse": 12.8
    }
  },
  "processing_time_ms": 1500
}

【ScheduleResponse】

{
  "code": 200,
  "message": "success",
  "data": {
    "schedule_id": "sch_20240201_001",
    "status": "optimal",
    "makespan_hours": 48.5,
    "makespan_days": 6.06,
    "total_tardiness_hours": 0,
    "total_cost": 15000.0,
    "resource_utilization": {
      "WC01": 0.85,
      "WC02": 0.72
    },
    "gantt_data": [...]
  }
}
```

---

## 四、前端完整调用清单

### 4.1 前端 API 调用文件结构

```
src/api/
├── index.ts              # API 基类 (request)
├── forecast.ts          # 预测相关
├── production.ts        # 生产/排程相关
├── inventory.ts         # 库存相关
├── order.ts            # 订单相关
├── purchase.ts         # 采购相关
├── mrp.ts              # MRP相关
├── whatif.ts           # What-if相关
├── risk.ts             # 风险相关
└── ...                 # 其他模块
```

### 4.2 完整 API 调用列表

| 功能 | 前端方法 | Java端点 | Python端点 | 状态 |
|------|----------|----------|------------|------|
| 需求预测 | `forecastApi.forecast()` | POST /api/forecast/forecast | POST /predict/demand | Java已实现 |
| 安全库存 | `forecastApi.recommendSafetyStock()` | POST /api/forecast/safety-stock | POST /predict/safety-stock | Java待完善 |
| 排程优化 | `productionApi.optimizeSchedule()` | POST /api/production/schedule/optimize | POST /schedule/optimize | Java待完善 |
| 对话服务 | `chatApi.sendMessage()` | POST /api/conversation/chat | POST /chat/message | Java待完善 |
| What-if | `whatifApi.simulate()` | POST /api/whatif/simulate | POST /whatif/simulate | Java待完善 |

### 4.3 完整调用链示例

```
【需求预测 - 完整调用链】

前端 (React)
    │
    ▼
forecast.ts::forecastApi.forecast(data)
    │
    ▼ POST /api/forecast/forecast
    │  Body: {itemCode, historicalData, forecastDays, method}
    │
    ▼
Java ForecastController::forecastDemand()
    │
    ▼ DemandForecastService.forecast()
    │
    ├─ IF method == "WEIGHTED_MA":
    │     └─ forecastWeightedMA() → 加权移动平均
    │
    ├─ IF method == "EXPONENTIAL":
    │     └─ forecastExponential() → 指数平滑
    │
    └─ IF method == "SIMPLE_MA":
          └─ forecastSimpleMA() → 简单移动平均
    
    │
    ▼
ForecastResult (List)
    │
    ▼ JSON Response
    │
前端 (渲染预测图表)
```

---

## 五、Java 后端模块清单

### 5.1 核心模块

| 模块 | 职责 | 关键类 |
|------|------|--------|
| **aimrp-api** | API网关入口 | ApiApplication, WebConfig |
| **aimrp-core** | 核心实体/枚举 | BaseEntity, Enums |
| **aimrp-common** | 公共工具 | ApiResponse, DateUtil |
| **aimrp-item** | 物料主数据 | Item, ItemService |
| **aimrp-supplier** | 供应商管理 | Supplier, SupplierService |
| **aimrp-demand** | 需求管理 | Demand, DemandService |
| **aimrp-bom** | BOM管理 | Bom, BomService |
| **aimrp-inventory** | 库存管理 | Inventory, InventoryService |
| **aimrp-mrp** | MRP计算 | MrpCalculator, MrpService |
| **aimrp-forecast** | 预测模块 | ForecastController, DemandForecastService |
| **aimrp-production** | 生产管理 | ProductionOrder, ProductionScheduleService |
| **aimrp-purchase** | 采购管理 | PurchaseOrder, PurchaseService |
| **aimrp-schedule** | 排程管理 | Schedule, ScheduleService |
| **aimrp-whatif** | What-if模拟 | WhatIfScenario, WhatIfService |
| **aimrp-conversation** | 对话服务 | Conversation, ChatService |
| **aimrp-risk** | 风险预警 | RiskAlert, RiskService |
| **aimrp-integration** | 集成模块 | IntegrationController, FeignClients |

### 5.2 各层典型调用

```
Controller → Service → Domain/Repository

示例:
ForecastController 
    → ForecastApplicationService 
        → DemandForecastService 
            → ForecastResult (Domain)

ProductionScheduleController
    → ProductionScheduleService
        → OR-Tools Scheduler (TODO: 集成Python)
```

---

## 六、待完善功能清单

### 6.1 Java → Python 集成 (高优先级)

| # | 功能 | Java端点 | Python端点 | 状态 |
|---|------|----------|------------|------|
| 1 | 高级预测 | /api/forecast/ai-forecast | /predict/demand | TODO |
| 2 | 智能排程 | /api/production/ai-schedule | /schedule/optimize | TODO |
| 3 | LLM对话 | /api/chat/ai-message | /chat/message | TODO |
| 4 | What-if模拟 | /api/whatif/ai-simulate | /whatif/simulate | TODO |

### 6.2 需要实现的Java代码

```java
// 1. Feign Client
// 文件: aimrp-integration/src/main/java/.../feign/AIForecastClient.java

@FeignClient(name = "ai-service", url = "${ai.service.url}")
public interface AIForecastClient {
    @PostMapping("/predict/demand")
    ApiResponse<AIForecastResponse> forecast(@RequestBody AIPredictRequest request);
}

// 2. Service
// 文件: aimrp-forecast/src/main/java/.../service/AIForecastService.java

@Service
public class AIForecastService {
    @Autowired
    private AIForecastClient client;
    
    public List<ForecastResult> forecastWithProphet(PredictRequest request) {
        var response = client.forecast(toAIRequest(request));
        return convertResponse(response.getData());
    }
}

// 3. Controller
// 文件: aimrp-forecast/src/main/java/.../controller/AIForecastController.java

@RestController
@RequestMapping("/api/forecast/ai")
public class AIForecastController {
    @PostMapping("/demand")
    public List<ForecastResult> forecast(@RequestBody PredictRequest request) {
        return aiForecastService.forecastWithProphet(request);
    }
}
```

---

*文档版本: 1.0 - 完整端到端调用链*
