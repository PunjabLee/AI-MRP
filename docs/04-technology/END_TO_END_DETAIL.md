# AI MRP 完整端到端调用链详解

> **版本**: 1.0  
> **日期**: 2026-03-11  
> **说明**: 完整记录从前端→Java→Python的每一层调用

---

## 一、完整调用链路总览

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                              完整端到端调用链路                                          │
│                         前端 → Java → Python (完整流程)                               │
└──────────────────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   前端       │     │   Java       │     │   Java       │     │   Python     │
│   React      │     │   Controller │     │   Service    │     │   AI Service │
│              │     │              │     │              │     │              │
│  forecast.ts │────▶ Forecast     │────▶ Demand       │     │              │
│              │     │ Controller   │     │ Forecast     │     │              │
│              │     │              │     │ Service      │     │              │
│              │     │              │     │              │     │              │
│              │     │              │     │ +---------+  │     │              │
│              │     │              │     │ |Feign   │  │     │              │
│              │     │              │     │ |Client  │──┼────▶              │
│              │     │              │     │ +---------+  │     │              │
│              │     │              │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
     :80ms              :50ms               :100ms              :500ms

已实现: 前端 → Java Controller → Java Service
待实现: Java Feign Client → Python API
```

---

## 二、完整调用链：需求预测

### 2.1 第一段：前端 → Java Controller (已实现 ✅)

```
步骤1: 前端发起请求
    文件: src/api/forecast.ts
    方法: forecastApi.forecast()
    
    代码:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ forecast: (data) => request.post('/api/forecast/forecast', data),        │
    │                                                                         │
    │ // 调用                                                                  │
    │ const result = await forecastApi.forecast({                              │
    │   itemCode: 'A001',                                                    │
    │   historicalData: [                                                    │
    │     { date: '2024-01-01', qty: 100 },                                  │
    │     { date: '2024-01-02', qty: 120 }                                   │
    │   ],                                                                    │
    │   forecastDays: 30,                                                     │
    │   method: 'PROPHET'                                                    │
    │ });                                                                    │
    └────────────────────────────────────────────────────────────────────────────┘

    HTTP 请求:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ POST /api/forecast/forecast                                              │
    │ Host: localhost:8080                                                    │
    │ Content-Type: application/json                                          │
    │                                                                         │
    │ {                                                                       │
    │   "itemCode": "A001",                                                  │
    │   "historicalData": [{"date":"2024-01-01","qty":100},...],            │
    │   "forecastDays": 30,                                                  │
    │   "method": "PROPHET"                                                 │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤2: Java Controller 接收
    文件: aimrp-forecast/src/.../ForecastController.java
    行号: 30
    方法: @PostMapping("/forecast")
    
    代码:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ @PostMapping("/forecast")                                               │
    │ public List<ForecastResult> forecastDemand(@RequestBody ForecastRequest │
    │     request) {                                                          │
    │     log.info("接收预测请求 - itemCode: {}", request.getItemCode());    │
    │                                                                         │
    │     // 参数转换 DTO → Entity                                           │
    │     List<HistoricalData> historicalData = new ArrayList<>();           │
    │     for (ForecastRequest.HistoricalDataDTO dto : request.getHistorical │
    │         HistoricalData data = new HistoricalData();                      │
    │         data.setDate(dto.getDate());                                    │
    │         data.setQty(dto.getQty());                                      │
    │         historicalData.add(data);                                      │
    │     }                                                                   │
    │                                                                         │
    │     // 枚举转换                                                          │
    │     DemandForecastService.ForecastMethod method =                       │
    │         DemandForecastService.ForecastMethod.valueOf(request.getMethod│
    │                                                                         │
    │     // 调用 Service                                                     │
    │     return forecastService.forecast(                                    │
    │         request.getItemCode(),                                          │
    │         historicalData,                                                 │
    │         request.getForecastDays(),                                      │
    │         method                                                          │
    │     );                                                                  │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤3: Java Controller 返回
    HTTP 响应 (200 OK):
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ [                                                                          │
    │   {                                                                        │
    │     "id": 1,                                                             │
    │     "itemCode": "A001",                                                  │
    │     "forecastDate": "2024-02-01",                                       │
    │     "forecastQty": 110.00,                                               │
    │     "lowerBound": 93.50,                                                │
    │     "upperBound": 126.50,                                               │
    │     "forecastType": "DAILY"                                              │
    │   },                                                                      │
    │   ...                                                                    │
    │ ]                                                                          │
    └────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 第二段：Java Service → Python API (待实现 ⚠️)

```
步骤4: Java Service 决定调用策略
    文件: aimrp-forecast/src/.../DemandForecastService.java
    方法: forecast()
    
    代码:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ public List<ForecastResult> forecast(...) {                            │
    │     // 当前实现: Java 内置算法                                          │
    │     if (method == ForecastMethod.PROPHET) {                            │
    │         // TODO: 调用 Python AI Service                                  │
    │         return callPythonProphetAPI(itemCode, historicalData, days);   │
    │     } else {                                                             │
    │         // 使用 Java 内置移动平均算法                                    │
    │         return forecastSimpleMA(...);                                    │
    │     }                                                                    │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤5: Java Feign Client 调用 Python (需要实现)
    文件: aimrp-integration/src/.../feign/AIForecastClient.java
    方法: @PostMapping("/predict/demand")
    
    代码:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ @FeignClient(name = "ai-service", url = "${ai.service.url}")          │
    │ public interface AIForecastClient {                                    │
    │                                                                         │
    │     @PostMapping("/predict/demand")                                    │
    │     ApiResponse<PythonForecastResponse> forecast(                       │
    │         @RequestBody PythonPredictRequest request                       │
    │     );                                                                  │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

    HTTP 请求 (Java → Python):
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ POST /predict/demand                                                    │
    │ Host: localhost:8000                                                    │
    │ Content-Type: application/json                                        │
    │                                                                         │
    │ {                                                                       │
    │   "item_code": "A001",                                                 │
    │   "forecast_days": 30,                                                 │
    │   "method": "prophet",                                                │
    │   "confidence_level": 0.95,                                           │
    │   "historical_data": [                                                 │
    │     {"date": "2024-01-01", "qty": 100},                                │
    │     {"date": "2024-01-02", "qty": 120}                                 │
    │   ],                                                                    │
    │   "prophet_changepoint_prior_scale": 0.05,                            │
    │   "prophet_seasonality_mode": "multiplicative"                         │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤6: Python API 处理请求
    文件: app/router/predict.py
    方法: predict_demand()
    行号: 150
    
    代码流程:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ 1. 参数验证                                                             │
    │    if not historical_data:                                              │
    │        historical_data = _generate_mock_data(item_code)                 │
    │                                                                         │
    │ 2. 方法选择 (auto)                                                     │
    │    if method == "auto":                                                │
    │        method = auto_select_algorithm(historical_data)                  │
    │                                                                         │
    │ 3. 创建引擎                                                             │
    │    engine = create_forecast_engine(method, **engine_kwargs)           │
    │                                                                         │
    │ 4. 训练                                                                 │
    │    engine.fit(historical_data)                                          │
    │                                                                         │
    │ 5. 预测                                                                 │
    │    results = engine.forecast(forecast_days, confidence_level)           │
    │                                                                         │
    │ 6. 返回响应                                                             │
    │    return ApiResponse.success(data={...})                               │
    └────────────────────────────────────────────────────────────────────────────┘

    Python 调用链:
    │
    ▼ create_forecast_engine("prophet", changepoint_prior_scale=0.05, ...)
    │   文件: algorithms/forecast.py, 行号 780
    │
    ▼ ProphetForecast.__init__(changepoint_prior_scale=0.05, ...)
    │   文件: algorithms/forecast.py, 行号 55
    │
    ▼ ProphetForecast.fit(historical_data)
    │   文件: algorithms/forecast.py, 行号 78
    │
    ▼ ProphetForecast.forecast(days, confidence)
    │   文件: algorithms/forecast.py, 行号 95
    │
    ▼ 返回预测结果 List[Dict]

步骤7: Python 返回响应
    HTTP 响应 (200 OK):
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ {                                                                        │
    │   "code": 200,                                                          │
    │   "message": "success",                                                 │
    │   "success": true,                                                      │
    │   "data": {                                                             │
    │     "item_code": "A001",                                               │
    │     "method": "prophet",                                               │
    │     "forecast": [                                                        │
    │       {"date": "2024-02-01", "qty": 115.5, "lower": 95.0, "upper": ..│
    │     ],                                                                   │
    │     "confidence": 0.95,                                                  │
    │     "metrics": {"mape": 8.5, "mae": 10.2, "rmse": 12.8}             │
    │   },                                                                     │
    │   "processing_time_ms": 1500                                            │
    │ }                                                                        │
    └────────────────────────────────────────────────────────────────────────────┘

步骤8: Java Feign Client 接收响应
    代码:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ var response = client.forecast(aiRequest);                               │
    │                                                                         │
    │ // 转换 Python 响应到 Java 对象                                          │
    │ List<ForecastResult> results = convertResponse(response.getData());    │
    │                                                                         │
    │ return results;                                                         │
    └────────────────────────────────────────────────────────────────────────────┘

步骤9: 完整响应返回前端
    Java Controller 返回:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ HTTP/1.1 200 OK                                                        │
    │ Content-Type: application/json                                          │
    │                                                                         │
    │ [                                                                        │
    │   {                                                                      │
    │     "itemCode": "A001",                                                │
    │     "forecastDate": "2024-02-01",                                      │
    │     "forecastQty": 115.50,                                              │
    │     "lowerBound": 95.00,                                               │
    │     "upperBound": 136.00,                                              │
    │     "forecastType": "DAILY"                                             │
    │   },                                                                     │
    │   ...                                                                   │
    │ ]                                                                        │
    └────────────────────────────────────────────────────────────────────────────┘
```

---

## 三、完整调用链：排程优化

### 3.1 调用流程图

```
前端                    Java Controller           Java Service           Python
───────────────────────────────────────────────────────────────────────────────

production.ts    POST /api/production      ProductionSchedule    Feign Client
  optimizeSchedule()  /schedule/optimize     Service.optimize()    callPython()
       │                   │                      │                      │
       ▼                   ▼                      ▼                      ▼
  {orders:[],        ScheduleRequest      createScheduler()    /schedule/
   resources:[]}     → validate()         → _ortools_solve()    optimize
                                                                    │
                                                                    ▼
                                                            OR-Tools CP-SAT
                                                            Solver
                                                                    │
                                                                    ▼
                                                            ScheduleResult
                                                                    │
                                                                    ▼
       ▼                   ▼                      ▼                      ▼
   渲染甘特图        JSON Response         返回结果              JSON Response
```

### 3.2 完整调用链详解

```
步骤1: 前端发起
    文件: src/api/production.ts
    方法: productionApi.optimizeSchedule()
    
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ optimizeSchedule: (data) =>                                              │
    │   request.post('/api/production/schedule/optimize', data)               │
    │                                                                         │
    │ // 调用                                                                  │
    │ const result = await productionApi.optimizeSchedule({                   │
    │   orders: [                                                             │
    │     {id: 'O001', product: 'A', quantity: 100, priority: 1}           │
    │   ],                                                                    │
    │   resources: [                                                           │
    │     {id: 'WC01', name: '工作中心1', capacity: 8}                      │
    │   ],                                                                    │
    │   goal: 'makespan',                                                     │
    │   timeLimitSeconds: 30                                                   │
    │ });                                                                    │
    └────────────────────────────────────────────────────────────────────────────┘

步骤2: Java Controller
    文件: aimrp-production/src/.../controller/ProductionScheduleController.java
    方法: @PostMapping("/schedule/optimize")
    
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ @PostMapping("/schedule/optimize")                                        │
    │ public ScheduleResponse optimizeSchedule(@RequestBody ScheduleRequest req)│
    │     return scheduleService.optimize(req);                                │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤3: Java Service (当前实现)
    文件: aimrp-production/src/.../service/ProductionScheduleService.java
    
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ public ScheduleResponse optimize(ScheduleRequest request) {               │
    │     // TODO: 根据目标选择实现                                            │
    │     // 1. 如果需要高级优化 → 调用 Python AI Service                    │
    │     // 2. 否则使用 Java 内置简单算法                                    │
    │                                                                         │
    │     if (request.getGoal().equals("AI_OPTIMIZE")) {                     │
    │         return callPythonScheduler(request);                            │
    │     } else {                                                            │
    │         return javaLocalSchedule(request);                              │
    │     }                                                                    │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤4: Java Feign Client (待实现)
    文件: aimrp-integration/src/.../feign/AI ScheduleClient.java
    
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ @FeignClient(name = "ai-service", url = "${ai.service.url}")          │
    │ public interface AIScheduleClient {                                     │
    │                                                                         │
    │     @PostMapping("/schedule/optimize")                                 │
    │     ApiResponse<PythonScheduleResponse> optimize(                       │
    │         @RequestBody PythonScheduleRequest request                       │
    │     );                                                                  │
    │ }                                                                       │
    └────────────────────────────────────────────────────────────────────────────┘

步骤5: Python 处理
    文件: app/router/schedule.py
    方法: optimize_schedule()
    行号: 90
    
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ 1. 参数验证                                                             │
    │    if not orders: return error                                          │
    │    if not resources: return error                                       │
    │                                                                         │
    │ 2. 创建排程器                                                            │
    │    result = create_scheduler(                                            │
    │        orders=orders,                                                   │
    │        resources=resources,                                              │
    │        goal=goal,                                                       │
    │        time_limit_seconds=request.time_limit_seconds                     │
    │    )                                                                    │
    │                                                                         │
    │ 3. OR-Tools 求解                                                        │
    │    → ORToolsScheduler.optimize()                                        │
    │       → _ortools_solve()                                               │
    │          → solver = cp_model.CpSolver()                                │
    │          → solver.parameters.max_time_in_seconds = 30                   │
    │          → status = solver.Solve(model)                                 │
    │                                                                         │
    │ 4. 返回结果                                                              │
    │    return ScheduleResult(...)                                           │
    └────────────────────────────────────────────────────────────────────────────┘

步骤6: 完整响应
    Python 返回:
    ┌────────────────────────────────────────────────────────────────────────────┐
    │ {                                                                        │
    │   "schedule_id": "sch_20240201_001",                                    │
    │   "status": "optimal",                                                  │
    │   "makespan_hours": 48.5,                                              │
    │   "makespan_days": 6.06,                                               │
    │   "total_tardiness_hours": 0,                                          │
    │   "total_cost": 15000.0,                                               │
    │   "resource_utilization": {"WC01": 0.85, "WC02": 0.72},               │
    │   "gantt_data": [...]                                                  │
    │ }                                                                        │
    └────────────────────────────────────────────────────────────────────────────┘
```

---

## 四、待实现清单：Java → Python 集成

### 4.1 需要实现的代码

| # | 文件 | 类/接口 | 方法 | 说明 |
|---|------|---------|------|------|
| 1 | `aimrp-integration/.../feign/AIForecastClient.java` | `@FeignClient` | `forecast()` | 预测调用 |
| 2 | `aimrp-integration/.../feign/AIScheduleClient.java` | `@FeignClient` | `optimize()` | 排程调用 |
| 3 | `aimrp-integration/.../feign/AIChatClient.java` | `@FeignClient` | `chat()` | 对话调用 |
| 4 | `aimrp-forecast/.../service/AIForecastService.java` | `@Service` | `forecastWithAI()` | 预测服务 |
| 5 | `aimrp-production/.../service/AIScheduleService.java` | `@Service` | `optimizeWithAI()` | 排程服务 |
| 6 | `aimrp-integration/.../config/FeignConfig.java` | `@Configuration` | - | Feign配置 |

### 4.2 配置清单

```yaml
# application.yml
ai:
  service:
    url: http://localhost:8000
    timeout: 60000
    retry:
      enabled: true
      maxAttempts: 3
    fallback:
      enabled: true  # 降级到Java内置算法
```

### 4.3 完整示例代码

```java
// 1. Feign Client
@FeignClient(
    name = "ai-service",
    url = "${ai.service.url}",
    path = "/predict",
    configuration = FeignConfig.class
)
public interface AIForecastClient {
    
    @PostMapping("/demand")
    ApiResponse<PythonForecastResponse> forecast(@RequestBody PythonPredictRequest request);
    
    @PostMapping("/safety-stock")
    ApiResponse<PythonSafetyStockResponse> safetyStock(@RequestBody PythonSafetyStockRequest request);
}

// 2. Service
@Service
@RequiredArgsConstructor
public class AIForecastService {
    
    private final AIForecastClient forecastClient;
    
    public List<ForecastResult> forecastWithAI(PredictRequest request) {
        // 1. 转换为 Python 请求
        PythonPredictRequest pythonRequest = PythonPredictRequest.builder()
            .itemCode(request.getItemCode())
            .forecastDays(request.getForecastDays())
            .method("prophet")
            .historicalData(convertHistory(request.getHistory()))
            .build();
        
        // 2. 调用 Python API
        var response = forecastClient.forecast(pythonRequest);
        
        // 3. 转换结果
        return convertResponse(response.getData());
    }
    
    // 降级方案
    public List<ForecastResult> forecastWithJava(PredictRequest request) {
        // 使用 Java 内置移动平均算法
        return demandForecastService.forecast(...);
    }
}

// 3. Controller
@RestController
@RequestMapping("/api/forecast/ai")
@RequiredArgsConstructor
public class AIForecastController {
    
    private final AIForecastService forecastService;
    
    @PostMapping("/demand")
    public List<ForecastResult> forecast(@RequestBody PredictRequest request) {
        // 优先使用 AI 服务，失败则降级
        try {
            return forecastService.forecastWithAI(request);
        } catch (Exception e) {
            log.warn("AI服务调用失败，降级到Java算法: {}", e.getMessage());
            return forecastService.forecastWithJava(request);
        }
    }
}
```

---

## 五、调用耗时分析

### 5.1 各阶段耗时预估

| 阶段 | 耗时 | 说明 |
|------|------|------|
| 前端 → Java | ~50ms | 局域网HTTP |
| Java 处理 | ~20ms | 业务逻辑 |
| Java → Python | ~100ms | 跨服务HTTP |
| Python 预测 | ~500ms | Prophet模型 |
| Python → Java | ~100ms | 跨服务HTTP |
| Java → 前端 | ~50ms | 局域网HTTP |
| **总计** | **~820ms** | 端到端 |

### 5.2 优化建议

1. **连接池**: 使用 Feign HTTP 客户端连接池
2. **异步**: 使用 `@Async` 异步调用 Python API
3. **缓存**: 相同请求缓存预测结果
4. **降级**: Python 服务不可用时降级到 Java 内置算法

---

*文档版本: 1.0 - 完整端到端调用链*
