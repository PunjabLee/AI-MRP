# AI MRP Python AI 服务集成开发指南

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、快速开始

### 1.1 服务地址

```
基础URL: http://localhost:8000
```

### 1.2 两种集成方式

| 方式 | 端点 | 适用场景 |
|------|------|----------|
| **统一网关** | `/integration/invoke` | 推荐，统一入口 |
| **独立API** | `/predict/demand` 等 | 简单场景 |

---

## 二、统一网关集成 (推荐)

### 2.1 请求格式

```json
{
  "type": "PREDICT_DEMAND",
  "data": {
    "item_code": "ITEM001",
    "forecast_days": 30,
    "method": "prophet"
  },
  "callback_url": "http://your-server.com/callback"
}
```

### 2.2 响应格式

```json
{
  "request_id": "req_1234567890",
  "code": 200,
  "message": "success",
  "success": true,
  "data": {...},
  "processing_time_ms": 1500
}
```

### 2.3 请求类型

| 类型 | 说明 |
|------|------|
| `PREDICT_DEMAND` | 需求预测 |
| `PREDICT_BATCH` | 批量预测 |
| `PREDICT_SAFETY_STOCK` | 安全库存计算 |
| `PREDICT_COMPARE` | 预测方法对比 |
| `SCHEDULE_OPTIMIZE` | 排程优化 |
| `SCHEDULE_FEASIBILITY` | 可行性检查 |
| `SCHEDULE_CAPACITY` | 产能分析 |
| `SCHEDULE_SCENARIOS` | 场景对比 |
| `CHAT_MESSAGE` | AI对话 |
| `WHATIF_SIMULATE` | What-if模拟 |
| `IMPACT_ANALYZE` | 影响分析 |

---

## 三、调用模式

### 3.1 同步调用

**请求**:
```json
POST /integration/invoke
{
  "type": "PREDICT_DEMAND",
  "data": {
    "item_code": "ITEM001",
    "forecast_days": 30
  }
}
```

**响应**: 立即返回结果

---

### 3.2 异步调用 (推荐大数据量场景)

**请求**:
```json
POST /integration/invoke/async
{
  "type": "PREDICT_BATCH",
  "data": {
    "item_codes": ["ITEM001", "ITEM002", ...],
    "forecast_days": 30
  }
}
```

**响应**:
```json
{
  "request_id": "req_abc123",
  "code": 202,
  "message": "Accepted - processing"
}
```

**获取结果**:
```bash
# 方式1: 轮询
GET /integration/result/{request_id}

# 方式2: 等待完成
GET /integration/result/{request_id}/wait?timeout=60
```

---

### 3.3 回调模式

**请求**:
```json
POST /integration/invoke
{
  "type": "PREDICT_DEMAND",
  "data": {...},
  "callback_url": "http://your-server.com/api/mrp/callback"
}
```

**回调通知**:
```json
{
  "request_id": "req_abc123",
  "status": "completed",
  "data": {...}
}
```

---

## 四、Java 集成示例

### 4.1 Maven 依赖

```xml
<!-- 无外部依赖，使用标准JDK HttpClient -->
```

### 4.2 快速开始

```java
import com.aimrp.ai.client.AIRMPClient;

public class Demo {
    public static void main(String[] args) throws Exception {
        // 创建客户端
        AIRMPClient client = new AIRMPClient("http://localhost:8000");
        
        // 需求预测
        Map<String, Object> result = client.predictDemand("ITEM001", 30, "prophet");
        
        // 提取数据
        String method = (String) result.get("method");
        List<Map> forecast = (List<Map>) result.get("forecast");
        
        System.out.println("预测方法: " + method);
    }
}
```

### 4.3 完整示例

```java
package com.example;

import com.aimrp.ai.client.AIRMPClient;
import java.util.*;

public class AIRMPDemo {
    public static void main(String[] args) {
        try {
            AIRMPClient client = new AIRMPClient("http://localhost:8000");
            
            // ===== 需求预测 =====
            Map<String, Object> predictResult = client.predictDemand("ITEM001", 30, "prophet");
            System.out.println("预测完成: " + predictResult.get("method"));
            
            // ===== 批量预测 =====
            List<String> items = Arrays.asList("ITEM001", "ITEM002", "ITEM003");
            Map<String, Object> batchResult = client.batchPredict(items, 30);
            
            // ===== 安全库存 =====
            Map<String, Object> ssResult = client.calculateSafetyStock("ITEM001", 7, 0.95);
            System.out.println("安全库存: " + ssResult.get("safety_stock"));
            
            // ===== 排程优化 =====
            List<Map<String, Object>> orders = new ArrayList<>();
            orders.add(Map.of(
                "id", "PO001",
                "quantity", 100,
                "unit_time", 5,
                "priority", 1
            ));
            
            List<Map<String, Object>> resources = new ArrayList<>();
            resources.add(Map.of(
                "id", "R001",
                "name", "生产线A",
                "capacity", 8
            ));
            
            Map<String, Object> scheduleResult = client.optimizeSchedule(orders, resources, "makespan");
            System.out.println("排程完成: " + scheduleResult.get("schedule_id"));
            
            // ===== 异步调用 =====
            String requestId = client.invokeAsync("PREDICT_BATCH", 
                Map.of("item_codes", items, "forecast_days", 30));
            
            // 轮询等待结果
            Map<String, Object> asyncResult = client.waitResult(requestId, 60);
            System.out.println("异步结果: " + asyncResult.get("status"));
            
            client.close();
            
        } catch (AIRMPClient.AIRMPException e) {
            System.err.println("错误: " + e.getMessage());
            System.err.println("详情: " + e.getError());
        }
    }
}
```

### 4.4 Spring Boot 集成

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aimrp.ai.client.AIRMPClient;

@Configuration
public class AIRMPConfig {
    
    @Bean
    public AIRMPClient airMRPClient() {
        String baseUrl = "http://localhost:8000";
        // 可选: 添加认证
        // return new AIRMPClient(baseUrl, "appId", "appSecret");
        return new AIRMPClient(baseUrl);
    }
}

---

package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aimrp.ai.client.AIRMPClient;
import java.util.Map;

@Service
public class MRPForecastService {
    
    @Autowired
    private AIRMPClient client;
    
    public Map<String, Object> forecast(String itemCode, int days) {
        try {
            return client.predictDemand(itemCode, days, "auto");
        } catch (Exception e) {
            throw new RuntimeException("预测失败", e);
        }
    }
}
```

---

## 五、C# 集成示例

### 5.1 NuGet 依赖

```bash
# 无需额外依赖，使用 System.Net.Http.Json
```

### 5.2 快速开始

```csharp
using AIRMP.Client;

var client = new AIRMPClient("http://localhost:8000");

// 需求预测
var result = await client.PredictDemandAsync("ITEM001", 30, "prophet");

Console.WriteLine($"预测方法: {result["method"]}");
```

### 5.3 完整示例

```csharp
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

class AIRMPDemo
{
    static async Task Main(string[] args)
    {
        var client = new AIRMPClient("http://localhost:8000");
        
        try
        {
            // ===== 需求预测 =====
            var predictResult = await client.PredictDemandAsync("ITEM001", 30, "prophet");
            Console.WriteLine($"预测方法: {predictResult["method"]}");
            
            // ===== 批量预测 =====
            var items = new List<string> { "ITEM001", "ITEM002", "ITEM003" };
            var batchResult = await client.BatchPredictAsync(items, 30);
            
            // ===== 安全库存 =====
            var ssResult = await client.CalculateSafetyStockAsync("ITEM001", 7, 0.95);
            Console.WriteLine($"安全库存: {ssResult["safety_stock"]}");
            
            // ===== 排程优化 =====
            var orders = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    { "id", "PO001" },
                    { "quantity", 100 },
                    { "unit_time", 5 },
                    { "priority", 1 }
                }
            };
            
            var resources = new List<Dictionary<string, object>>
            {
                new Dictionary<string, object>
                {
                    { "id", "R001" },
                    { "name", "生产线A" },
                    { "capacity", 8 }
                }
            };
            
            var scheduleResult = await client.OptimizeScheduleAsync(orders, resources, "makespan");
            Console.WriteLine($"排程ID: {scheduleResult["schedule_id"]}");
            
            // ===== 异步调用 =====
            var requestId = await client.InvokeAsync("PREDICT_BATCH",
                new Dictionary<string, object>
                {
                    { "item_codes", items },
                    { "forecast_days", 30 }
                });
            
            // 等待结果
            var asyncResult = await client.WaitResultAsync(requestId, 60);
            Console.WriteLine($"异步结果: {asyncResult["status"]}");
            
        }
        catch (AIRMPClient.AIRMPException ex)
        {
            Console.WriteLine($"错误: {ex.Message}");
            Console.WriteLine($"详情: {ex.Error}");
        }
        finally
        {
            client.Dispose();
        }
    }
}
```

### 5.4 .NET Core 依赖注入

```csharp
// Program.cs
builder.Services.AddSingleton<AIRMPClient>(sp => 
    new AIRMPClient("http://localhost:8000"));

// Service
public class ForecastService
{
    private readonly AIRMPClient _client;
    
    public ForecastService(AIRMPClient client)
    {
        _client = client;
    }
    
    public async Task<Dictionary<string, object>> Forecast(string itemCode, int days)
    {
        return await _client.PredictDemandAsync(itemCode, days, "auto");
    }
}
```

---

## 六、错误处理

### 6.1 错误响应格式

```json
{
  "code": 400,
  "message": "Bad Request",
  "success": false,
  "error": {
    "type": "VALIDATION_ERROR",
    "field": "item_code",
    "reason": "物料编码不能为空"
  }
}
```

### 6.2 Java 异常处理

```java
try {
    client.predictDemand("ITEM001", 30, "prophet");
} catch (AIRMPClient.AIRMPException e) {
    System.err.println("错误: " + e.getMessage());
    
    Map<String, Object> error = e.getError();
    if (error != null) {
        System.err.println("类型: " + error.get("type"));
        System.err.println("原因: " + error.get("reason"));
    }
}
```

### 6.3 C# 异常处理

```csharp
try {
    await client.PredictDemandAsync("ITEM001", 30, "prophet");
} catch (AIRMPClient.AIRMPException ex) {
    Console.WriteLine($"错误: {ex.Message}");
    
    if (ex.Error != null) {
        Console.WriteLine($"类型: {ex.Error.GetValueOrDefault("type")}");
        Console.WriteLine($"原因: {ex.Error.GetValueOrDefault("reason")}");
    }
}
```

### 6.4 常见错误码

| code | message | 说明 |
|------|---------|------|
| 400 | Bad Request | 请求参数错误 |
| 404 | Not Found | 资源不存在 |
| 422 | Validation Error | 验证失败 |
| 500 | Internal Server Error | 服务器内部错误 |
| 503 | Service Unavailable | 服务不可用 |

---

## 七、最佳实践

### 7.1 超时设置

```java
// Java - 推荐超时设置
AIRMPClient client = new AIRMPClient("http://localhost:8000");
// 默认超时60秒，大数据量可延长
```

```csharp
// C# - 已内置60秒超时，可调整
var client = new AIRMPClient("http://localhost:8000");
```

### 7.2 重试机制

```java
// Java - 建议自行实现重试
int maxRetries = 3;
for (int i = 0; i < maxRetries; i++) {
    try {
        return client.predictDemand(itemCode, days, method);
    } catch (AIRMPClient.AIRMPException e) {
        if (i == maxRetries - 1) throw e;
        Thread.sleep(1000 * (i + 1)); // 指数退避
    }
}
```

### 7.3 连接池

```java
// Java - HttpClient 默认复用连接
// 如需高并发，可配置连接池
```

### 7.4 日志记录

```java
// 建议记录关键信息
logger.info("预测请求: itemCode={}, method={}", itemCode, method);
logger.info("预测结果: requestId={}, time={}ms", 
    result.get("request_id"), 
    result.get("processing_time_ms"));
```

---

## 八、完整 API 参考

详见 [API_REFERENCE.md](./API_REFERENCE.md)

---

## 九、获取帮助

- 问题反馈: GitHub Issues
- 技术支持: 团队内部

---

*文档版本: 1.0*  
*更新时间: 2026-03-11*
