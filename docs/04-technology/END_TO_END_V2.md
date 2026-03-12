# AI MRP 超完整端到端技术手册

> **版本**: 2.0  
> **日期**: 2026-03-11  
> **说明**: 完整记录每个类、每个方法、每个数据转换细节

---

## 一、完整架构层级

```
用户浏览器 (React)
    ↓ onClick / useEffect
src/api/ (Axios/Fetch)
    ↓ HTTP/1.1
Java Spring Boot 后端 (localhost:8080)
    ↓ @RestController
    ↓ @Service
    ↓ @Autowired
    ↓ Feign Client (TODO)
    ↓ HTTP/1.1
Python FastAPI AI 服务 (localhost:8000)
    ↓ @router.post()
    ↓ Service Layer
    ↓ Algorithm Layer
    ↓ Infrastructure (Redis/RabbitMQ)
```

---

## 二、前端 → Java Controller 完整调用链

### 2.1 React 组件层

```
文件: src/apps/admin/pages/forecast/ForecastPage.tsx (行号: 45)

const handleForecast = async () => {
    setLoading(true);
    try {
        // 1. 调用 API
        const result = await forecastApi.forecast({
            itemCode: selectedItem,
            historicalData: historyData,
            forecastDays: days,
            method: selectedMethod
        });
        
        // 2. 处理响应
        setForecastResult(result);
        
        // 3. 更新图表
        updateChart(result);
    } catch (error) {
        setError(error.message);
    } finally {
        setLoading(false);
    }
};
```

### 2.2 API 封装层

```
文件: src/api/forecast.ts (行号: 10)

export const forecastApi = {
    forecast: (data: ForecastRequest): Promise<ForecastResult[]> =>
        request.post('/api/forecast/forecast', data),
        
    recommendSafetyStock: (data: SafetyStockRequest): Promise<SafetyStockResult> =>
        request.post('/api/forecast/safety-stock', data),
        
    getHistory: (params) => 
        request.get('/api/forecast/history', { params }),
        
    getResults: (params) => 
        request.get('/api/forecast/results', { params })
};
```

### 2.3 Java Controller 层

```
文件: aimrp-forecast/src/.../ForecastController.java (行号: 30)

@PostMapping("/forecast")
public List<ForecastResult> forecastDemand(@RequestBody ForecastRequest req) {
    log.info("【Controller】接收预测请求 - itemCode: {}, method: {}", 
        req.getItemCode(), req.getMethod());
    
    // 参数校验
    if (req.getItemCode() == null || req.getItemCode().isBlank()) {
        throw new IllegalArgumentException("itemCode不能为空");
    }
    
    // DTO → Entity 转换
    List<HistoricalData> historicalData = req.getHistoricalData().stream()
        .map(dto -> {
            HistoricalData entity = new HistoricalData();
            entity.setDate(dto.getDate());
            entity.setQty(dto.getQty());
            return entity;
        })
        .collect(Collectors.toList());
    
    // 枚举转换
    ForecastMethod method = ForecastMethod.WEIGHTED_MA;
    if (req.getMethod() != null) {
        try {
            method = ForecastMethod.valueOf(req.getMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不支持的方法: {}, 使用默认", req.getMethod());
        }
    }
    
    // 调用 Service
    return forecastService.forecast(
        req.getItemCode(),
        historicalData,
        req.getForecastDays(),
        method
    );
}
```

---

## 三、Java Service 完整调用链

### 3.1 DemandForecastService

```
文件: aimrp-forecast/src/.../DemandForecastService.java (行号: 30)

public List<ForecastResult> forecast(String itemCode, 
                                     List<HistoricalData> historicalData, 
                                     int forecastDays,
                                     ForecastMethod method) {
    
    // 1. 数据校验
    if (historicalData == null || historicalData.isEmpty()) {
        historicalData = generateDefaultData(itemCode);
    }
    
    // 2. 数据排序
    historicalData.sort(Comparator.comparing(HistoricalData::getDate));
    
    // 3. 选择算法执行
    List<ForecastResult> results;
    switch (method) {
        case PROPHET:
            // TODO: 调用 Python AI Service
            results = forecastWeightedMA(itemCode, historicalData, forecastDays);
            break;
        case EXPONENTIAL:
            results = forecastExponential(itemCode, historicalData, forecastDays);
            break;
        case WEIGHTED_MA:
        default:
            results = forecastWeightedMA(itemCode, historicalData, forecastDays);
            break;
    }
    
    // 4. 保存结果
    forecastRepository.saveAll(results);
    
    return results;
}

// 加权移动平均 (行号: 115)
private List<ForecastResult> forecastWeightedMA(String itemCode, 
                                               List<HistoricalData> data, 
                                               int days) {
    int windowSize = Math.min(7, data.size());
    
    BigDecimal weightedSum = BigDecimal.ZERO;
    int weightSum = 0;
    
    for (int i = 0; i < windowSize; i++) {
        int idx = data.size() - windowSize + i;
        int weight = windowSize - i;
        weightedSum = weightedSum.add(
            data.get(idx).getQty().multiply(BigDecimal.valueOf(weight))
        );
        weightSum += weight;
    }
    
    BigDecimal forecastQty = weightedSum
        .divide(BigDecimal.valueOf(weightSum), 2, RoundingMode.HALF_UP);
    
    // 生成结果
    List<ForecastResult> results = new ArrayList<>();
    LocalDate startDate = LocalDate.now().plusDays(1);
    
    for (int i = 0; i < days; i++) {
        ForecastResult result = new ForecastResult();
        result.setItemCode(itemCode);
        result.setForecastDate(startDate.plusDays(i));
        result.setForecastQty(forecastQty);
        result.setLowerBound(forecastQty.multiply(BigDecimal.valueOf(0.85)));
        result.setUpperBound(forecastQty.multiply(BigDecimal.valueOf(1.15)));
        results.add(result);
    }
    
    return results;
}
```

---

## 四、Java → Python 集成 (待实现)

### 4.1 Feign Client

```
文件: aimrp-integration/src/.../feign/AIForecastClient.java

@FeignClient(
    name = "ai-forecast-client",
    url = "${ai.service.url:http://localhost:8000}",
    path = "/predict",
    configuration = FeignConfig.class
)
public interface AIForecastClient {
    
    @PostMapping("/demand")
    AIApiResponse<AIForecastResponse> forecast(@RequestBody AIPredictRequest request);
    
    @PostMapping("/safety-stock")
    AIApiResponse<AISafetyStockResponse> safetyStock(@RequestBody AISafetyStockRequest request);
    
    @PostMapping("/batch")
    AIApiResponse<AIBatchForecastResponse> batchForecast(@RequestBody AIBatchForecastRequest request);
}
```

### 4.2 数据映射

```
Java → Python 请求映射:
itemCode → item_code (String → str)
forecastDays → forecast_days (Integer → int)
method → method (String → str)
confidenceLevel → confidence_level (Double → float)
historicalData → historical_data (List → List[Dict])
  └─ getDate() → ["date"] (LocalDate → str)
  └─ getQty() → ["qty"] (BigDecimal → int/float)

Python → Java 响应映射:
item_code → itemCode (str → String)
forecast → forecast (List[Dict] → List<ForecastResult>)
  └─ ["date"] → getForecastDate() (str → LocalDate)
  └─ ["qty"] → getForecastQty() (float → BigDecimal)
metrics → metrics (Dict → Map)
  └─ ["mape"] → getMape() (float → Double)
```

---

## 五、Python Router → Algorithm 完整调用链

### 5.1 predict_demand 完整方法链

```
文件: app/router/predict.py (行号: 150)

async def predict_demand(request: PredictRequest) -> ApiResponse:
    
    # Step 1: 参数提取
    item_code = request.item_code
    forecast_days = request.forecast_days
    method = request.method
    confidence_level = request.confidence_level
    
    # Step 2: 获取历史数据
    historical_data = request.historical_data
    if not historical_data:
        historical_data = _generate_mock_data(item_code)
    
    if not historical_data:
        return ApiResponse.bad_request(message="无历史数据")
    
    # Step 3: 方法选择 (auto)
    if method == "auto":
        method = auto_select_algorithm(historical_data)
        # 实现 (forecast.py:800):
        #   n = len(historical_data)
        #   if n < 30: return "moving_average"
        #   elif n < 100: return "exponential_smoothing"
        #   else: return "prophet"
    
    # Step 4: 构建引擎参数
    engine_kwargs = {}
    if method == "prophet":
        engine_kwargs["prophet_changepoint_prior_scale"] = (
            request.prophet_changepoint_prior_scale or 0.05
        )
        engine_kwargs["prophet_seasonality_mode"] = (
            request.prophet_seasonality_mode or "multiplicative"
        )
    
    # Step 5: 创建引擎
    engine = create_forecast_engine(method, **engine_kwargs)
    # 实现 (forecast.py:780):
    #   engines = {
    #       "prophet": lambda: ProphetForecast(...),
    #       "arima": lambda: ARIMAForecast(...),
    #       ...
    #   }
    #   return engines[method.lower()]()
    
    # Step 6: 训练
    engine.fit(historical_data)
    # 实现 (forecast.py:78):
    #   self.historical_df = pd.DataFrame([
    #       {"ds": self._parse_date(d["date"]), "y": d["qty"]}
    #       for d in historical_data
    #   ])
    #   self.model = Prophet(
    #       changepoint_prior_scale=self.changepoint_prior_scale,
    #       seasonality_mode=self.seasonality_mode
    #   )
    #   self.model.fit(self.historical_df)
    #   self._fitted = True
    
    # Step 7: 预测
    forecast_results = engine.forecast(forecast_days, confidence_level)
    # 实现 (forecast.py:95):
    #   self.future = self.model.make_future_dataframe(periods=days)
    #   self.forecast_df = self.model.predict(self.future)
    #   z_score = self._get_z_score(confidence)  # 0.95→1.96
    #   results = [{"date": row['ds'], "qty": max(0, row['yhat']), ...}]
    
    # Step 8: 获取指标
    metrics = engine.get_metrics()
    # 实现 (forecast.py:120):
    #   merged = self.forecast_df.merge(self.historical_df, on='ds')
    #   mae = np.mean(np.abs(merged['y'] - merged['yhat']))
    #   rmse = np.sqrt(np.mean((merged['y'] - merged['yhat'])**2))
    #   mape = np.mean(np.abs((merged['y'] - merged['yhat']) / merged['y'])) * 100
    #   return {"mape": round(mape, 2), "mae": round(mae, 2), "rmse": round(rmse, 2)}
    
    # Step 9: 构建响应
    data = {
        "item_code": item_code,
        "method": method,
        "forecast": forecast_results,
        "confidence": confidence_level,
        "metrics": metrics,
        "parameters": engine_kwargs,
        "visualization_data": _build_forecast_visualization(forecast_results, metrics)
    }
    
    return ApiResponse.success(data=data, message="预测成功")
```

---

## 六、数据库操作链

### 6.1 JPA Repository

```
文件: aimrp-forecast/src/.../repository/DemandForecastRepository.java

public interface DemandForecastRepository 
    extends JpaRepository<ForecastResult, Long> {
    
    List<ForecastResult> findByItemCode(String itemCode);
    
    List<ForecastResult> findByItemCodeAndForecastDateBetween(
        String itemCode, LocalDate startDate, LocalDate endDate);
    
    List<ForecastResult> findByItemCodeAndForecastType(
        String itemCode, ForecastType forecastType);
    
    long countByItemCode(String itemCode);
    
    void deleteByForecastDateBefore(LocalDate date);
}
```

### 6.2 生成的 SQL

```
-- findByItemCode
SELECT * FROM t_demand_forecast 
WHERE item_code = 'A001' 
ORDER BY forecast_date ASC;

-- findByItemCodeAndForecastDateBetween
SELECT * FROM t_demand_forecast 
WHERE item_code = 'A001' 
  AND forecast_date BETWEEN '2024-02-01' AND '2024-02-28';

-- saveAll (批量插入)
INSERT INTO t_demand_forecast 
(item_code, forecast_date, forecast_qty, ...)
VALUES 
('A001', '2024-02-01', 110.00, ...),
('A001', '2024-02-02', 110.00, ...);
```

---

## 七、异常处理链

### 7.1 Java

```
@ExceptionHandler
public Map<String, Object> handleException(Exception e) {
    if (e instanceof IllegalArgumentException) {
        return Map.of("code", 400, "message", "参数错误: " + e.getMessage());
    }
    if (e instanceof BusinessException) {
        return Map.of("code", ((BusinessException)e).getCode(), "message", e.getMessage());
    }
    return Map.of("code", 500, "message", "服务器内部错误");
}
```

### 7.2 Python

```
@router.exception_handler(Exception)
async def global_exception_handler(request, exc):
    if isinstance(exc, ValueError):
        return ApiResponse.validation_error(message=f"参数错误: {str(exc)}")
    if isinstance(exc, HTTPException):
        return ApiResponse.error(code=exc.status_code, message=str(exc.detail))
    return ApiResponse.server_error(message="服务器内部错误")
```

---

## 八、完整 HTTP 示例

### 8.1 请求

```
POST /api/forecast/forecast HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Request-ID: req-20240311-001

{
  "itemCode": "A001",
  "forecastDays": 30,
  "method": "PROPHET",
  "confidenceLevel": 0.95,
  "historicalData": [
    {"date": "2024-01-01", "qty": 100},
    {"date": "2024-01-02", "qty": 120}
  ]
}
```

### 8.2 响应

```
HTTP/1.1 200 OK
Content-Type: application/json
X-Request-ID: req-20240311-001
X-Processing-Time: 150ms

{
  "code": 200,
  "message": "success",
  "success": true,
  "data": [
    {
      "itemCode": "A001",
      "forecastDate": "2024-02-01",
      "forecastQty": 115.50,
      "lowerBound": 95.00,
      "upperBound": 136.00,
      "forecastType": "DAILY"
    }
  ]
}
```

---

*文档版本: 2.0 - 超完整端到端技术手册*
