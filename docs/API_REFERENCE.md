# AI MRP Python API 接口文档

> **版本**: 2.0  
> **日期**: 2026-03-11  
> **基础URL**: `http://localhost:8000`

---

## 目录

1. [预测服务 API](#一预测服务-api)
2. [排程服务 API](#二排程服务-api)
3. [安全库存 API](#三安全库存-api)
4. [对话服务 API](#四对话服务-api)
5. [What-if 模拟 API](#五what-if-模拟-api)
6. [插单影响分析 API](#六插单影响分析-api)
7. [通知服务 API](#七通知服务-api)

---

## 一、预测服务 API

### 1.1 需求预测

**接口说明**: 基于历史数据进行需求预测，支持多种算法

**请求地址**: `/api/v1/predict/demand`

**请求方式**: `POST`

**请求头**:
```http
Content-Type: application/json
```

**请求体 (JSON)**:
```json
{
  "item_code": "ITEM001",
  "forecast_days": 30,
  "method": "prophet",
  "confidence_level": 0.95,
  "historical_data": [
    {"date": "2026-01-01", "qty": 100},
    {"date": "2026-01-02", "qty": 110}
  ],
  "prophet_changepoint_prior_scale": 0.05,
  "prophet_seasonality_mode": "multiplicative",
  "lstm_sequence_length": 30,
  "lstm_epochs": 50,
  "arima_p": 5,
  "arima_d": 1,
  "arima_q": 0
}
```

**请求参数说明**:

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| item_code | string | 是 | 物料编码 | ITEM001 |
| forecast_days | integer | 否 | 预测天数(1-365) | 30 |
| method | string | 否 | 预测方法 | prophet |
| confidence_level | float | 否 | 置信度(0.5-0.99) | 0.95 |
| historical_data | array | 否 | 历史数据 | [...] |
| prophet_changepoint_prior_scale | float | 否 | Prophet趋势灵敏度 | 0.05 |
| prophet_seasonality_mode | string | 否 | Prophet季节模式 | multiplicative |
| lstm_sequence_length | integer | 否 | LSTM序列长度 | 30 |
| lstm_epochs | integer | 否 | LSTM训练轮数 | 50 |
| arima_p | integer | 否 | ARIMA p阶数 | 5 |
| arima_d | integer | 否 | ARIMA d阶数 | 1 |
| arima_q | integer | 否 | ARIMA q阶数 | 0 |

**method 可选值**:
- `prophet` - Facebook Prophet
- `lstm` - LSTM 深度学习
- `arima` - ARIMA 时间序列
- `xgboost` - XGBoost
- `moving_average` - 移动平均
- `exponential_smoothing` - 指数平滑
- `auto` - 自动选择

**返回示例 (JSON)**:
```json
{
  "item_code": "ITEM001",
  "method": "prophet",
  "forecast": [
    {
      "date": "2026-03-12",
      "qty": 125.5,
      "lower": 110.2,
      "upper": 140.8,
      "trend": 0.5,
      "seasonal": 1.1
    }
  ],
  "confidence": 0.95,
  "metrics": {
    "mape": 8.5,
    "mae": 12.3,
    "rmse": 15.7
  },
  "parameters": {
    "forecast_days": 30,
    "method": "prophet",
    "confidence_level": 0.95
  },
  "visualization_data": {
    "chart_type": "forecast",
    "time_series": {...}
  }
}
```

**返回字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| item_code | string | 物料编码 |
| method | string | 使用的预测方法 |
| forecast | array | 预测结果列表 |
| forecast[].date | string | 预测日期 |
| forecast[].qty | float | 预测数量 |
| forecast[].lower | float | 置信区间下限 |
| forecast[].upper | float | 置信区间上限 |
| forecast[].trend | float | 趋势值 |
| forecast[].seasonal | float | 季节性因子 |
| metrics | object | 模型评估指标 |
| metrics.mape | float | 平均绝对百分比误差 |
| metrics.mae | float | 平均绝对误差 |
| metrics.rmse | float | 均方根误差 |
| visualization_data | object | 可视化数据 |

---

### 1.2 批量预测

**接口说明**: 批量预测多个物料的需求

**请求地址**: `/api/v1/predict/batch`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "item_codes": ["ITEM001", "ITEM002", "ITEM003"],
  "forecast_days": 30,
  "method": "auto",
  "include_safety_stock": true,
  "parallel": true
}
```

**返回示例 (JSON)**:
```json
{
  "forecasts": [
    {
      "item_code": "ITEM001",
      "method": "prophet",
      "forecast": [...],
      "latest_qty": 125.5
    }
  ],
  "summary": {
    "total_items": 3,
    "methods_used": {
      "ITEM001": "prophet",
      "ITEM002": "arima"
    },
    "forecast_days": 30
  },
  "visualization_data": {
    "chart_type": "batch_forecast"
  }
}
```

---

### 1.3 预测方法对比

**接口说明**: 对比多种预测方法的结果

**请求地址**: `/api/v1/predict/compare`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "item_code": "ITEM001",
  "methods": ["prophet", "lstm", "arima"],
  "forecast_days": 30
}
```

**返回示例 (JSON)**:
```json
{
  "item_code": "ITEM001",
  "comparisons": [
    {
      "method": "prophet",
      "forecast": [...],
      "metrics": {"mape": 8.5, "mae": 12.3}
    }
  ],
  "best_method": "prophet",
  "recommendation": "基于MAPE指标，推荐使用 prophet 方法"
}
```

---

### 1.4 获取预测方法列表

**请求地址**: `/api/v1/predict/methods`

**请求方式**: `GET`

**返回示例 (JSON)**:
```json
{
  "methods": [
    {
      "name": "prophet",
      "display_name": "Facebook Prophet",
      "description": "支持季节性和趋势的时间序列预测",
      "parameters": {
        "changepoint_prior_scale": {
          "type": "float",
          "default": 0.05,
          "range": [0.001, 0.5]
        }
      },
      "pros": ["自动检测季节性", "处理节假日"],
      "cons": ["计算较慢"]
    }
  ]
}
```

---

## 二、排程服务 API

### 2.1 智能排程优化

**接口说明**: 使用OR-Tools进行生产排程优化

**请求地址**: `/api/v1/schedule/optimize`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "orders": [
    {
      "id": "PO001",
      "product": "产品A",
      "quantity": 100,
      "unit_time": 5,
      "priority": 1,
      "deadline": "2026-03-15T00:00:00",
      "process_type": "assembly",
      "setup_time": 30
    }
  ],
  "resources": [
    {
      "id": "R001",
      "name": "生产线A",
      "capacity": 8,
      "available_hours": 8,
      "hourly_cost": 100,
      "overtime_cost": 150,
      "supported_processes": ["assembly", "testing"]
    }
  ],
  "goal": "makespan",
  "constraints": {
    "resource_lock": {"R001": ["PO001"]},
    "sequence": [{"order": "PO001", "before": "PO002"}]
  },
  "optimize_level": "normal",
  "time_limit_seconds": 30
}
```

**goal 可选值**:
- `makespan` - 最小化总完工时间
- `tardiness` - 最小化总延迟时间
- `cost` - 最小化总成本
- `balanced` - 平衡模式

**返回示例 (JSON)**:
```json
{
  "schedule_id": "SCH_20260311103000",
  "status": "optimal",
  "makespan_hours": 48.5,
  "makespan_days": 6.1,
  "total_tardiness": 2.5,
  "total_cost": 4850.0,
  "resource_utilization": {
    "生产线A": 85.5,
    "生产线B": 72.3
  },
  "schedule_details": [
    {
      "order_id": "PO001",
      "resource_id": "R001",
      "resource_name": "生产线A",
      "start_time": "2026-03-11T08:00:00",
      "end_time": "2026-03-11T16:30:00",
      "duration_hours": 8.5,
      "quantity": 100,
      "priority": 1,
      "tardiness_hours": 0,
      "cost": 850.0
    }
  ],
  "gantt_data": [
    {
      "order_id": "PO001",
      "resource": "生产线A",
      "start": "2026-03-11T08:00:00",
      "end": "2026-03-11T16:30:00",
      "color": "#3b82f6"
    }
  ],
  "metrics": {
    "total_orders": 5,
    "total_resources": 2,
    "goal": "makespan"
  },
  "visualization_data": {
    "chart_type": "gantt",
    "resources": ["生产线A", "生产线B"]
  }
}
```

**返回字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| schedule_id | string | 排程ID |
| status | string | 优化状态 (optimal/feasible/suboptimal) |
| makespan_hours | float | 总完工时间(小时) |
| makespan_days | float | 总完工时间(天) |
| total_tardiness | float | 总延迟时间(小时) |
| total_cost | float | 总成本 |
| resource_utilization | object | 资源利用率 |
| schedule_details | array | 详细排程列表 |
| gantt_data | array | 甘特图数据 |
| visualization_data | object | 可视化数据 |

---

### 2.2 排程可行性检查

**请求地址**: `/api/v1/schedule/feasibility-check`

**请求方式**: `POST`

**返回示例 (JSON)**:
```json
{
  "feasible": true,
  "total_work_minutes": 2400,
  "total_capacity_minutes": 3840,
  "utilization_percent": 62.5,
  "issues": {
    "process": [],
    "deadline": []
  },
  "suggestions": ["方案可行，产能利用率适中"],
  "visualization_data": {
    "capacity_chart": {
      "labels": ["已用", "剩余"],
      "values": [2400, 1440]
    }
  }
}
```

---

### 2.3 场景对比

**请求地址**: `/api/v1/schedule/scenarios`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "scenarios": [
    {
      "name": "方案A-最短时间",
      "orders": [...],
      "resources": [...],
      "goal": "makespan"
    },
    {
      "name": "方案B-最低成本",
      "orders": [...],
      "resources": [...],
      "goal": "cost"
    }
  ],
  "comparison_metrics": ["makespan", "tardiness", "cost", "utilization"]
}
```

---

### 2.4 产能分析

**请求地址**: `/api/v1/schedule/capacity-analysis`

**请求方式**: `POST`

**返回示例 (JSON)**:
```json
{
  "resource_loads": [
    {
      "id": "R001",
      "name": "生产线A",
      "total_capacity": 64,
      "orders": [...],
      "utilization": 85.5
    }
  ],
  "bottlenecks": [],
  "summary": {
    "total_resources": 2,
    "avg_utilization": 78.9,
    "bottleneck_count": 0
  }
}
```

---

### 2.5 获取优化目标列表

**请求地址**: `/api/v1/schedule/goals`

**请求方式**: `GET`

---

### 2.6 获取约束类型

**请求地址**: `/api/v1/schedule/constraints`

**请求方式**: `GET`

---

## 三、安全库存 API

### 3.1 安全库存计算

**接口说明**: 计算物料的安全库存、再订货点、经济订货量

**请求地址**: `/api/v1/predict/safety-stock`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "item_code": "ITEM001",
  "service_level": 0.95,
  "lead_time_days": 7,
  "lead_time_std": 1.5,
  "method": "auto",
  "unit_cost": 50.0,
  "holding_cost_rate": 0.2,
  "order_cost": 100.0,
  "historical_demand": [
    {"date": "2026-01-01", "qty": 100},
    {"date": "2026-01-02", "qty": 110}
  ]
}
```

**method 可选值**:
- `auto` - 自动选择
- `statistical` - 统计法
- `service` - 服务水平法
- `ml` - 机器学习法
- `montecarlo` - 蒙特卡洛模拟

**返回示例 (JSON)**:
```json
{
  "item_code": "ITEM001",
  "safety_stock": 85.5,
  "reorder_point": 185.5,
  "optimal_order_qty": 150.0,
  "service_level_achieved": 95.0,
  "method": "ml",
  "parameters": {
    "z_value": 1.65,
    "demand_avg": 100.0,
    "demand_std": 20.5,
    "lead_time_days": 7
  },
  "analysis": {
    "formula": "SS = Z × σ × √LT × 趋势因子 × 波动因子",
    "trend": "stable",
    "seasonality": 1.1
  },
  "recommendations": [
    "✅ 安全库存设置合理"
  ],
  "visualization_data": {
    "chart_type": "safety_stock",
    "gauges": [...]
  }
}
```

---

## 四、对话服务 API

### 4.1 AI 对话

**接口说明**: 智能对话接口，支持规则匹配和LLM两种模式

**请求地址**: `/api/v1/chat`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "message": "帮我查一下物料 ITEM001 的库存",
  "session_id": "user123",
  "context": {},
  "use_llm": false
}
```

**use_llm 说明**:
- `false` (默认): 规则匹配模式，响应快速
- `true`: LLM理解模式，需要配置LLM

**返回示例 (JSON)**:
```json
{
  "message": "物料 ITEM001 当前库存：150 件\n安全库存：50 件\n可用数量：100 件",
  "session_id": "user123",
  "intent": "query_stock",
  "entities": {
    "item_code": "ITEM001"
  },
  "suggestions": ["查看订单", "运行 MRP"],
  "action": {
    "type": "API",
    "endpoint": "/inventory/query",
    "params": {"item_code": "ITEM001"}
  },
  "confidence": 0.9,
  "mode": "rule"
}
```

---

### 4.2 获取对话历史

**请求地址**: `/api/v1/chat/history/{session_id}`

**请求方式**: `GET`

**参数**:
- `session_id` - 会话ID
- `limit` - 返回条数(默认10)

---

### 4.3 获取支持的意图

**请求地址**: `/api/v1/chat/intents`

**请求方式**: `GET`

---

### 4.4 获取意图推荐

**请求地址**: `/api/v1/chat/recommendations`

**请求方式**: `GET`

**参数**:
- `session_id` - 会话ID

---

## 五、What-if 模拟 API

### 5.1 What-if 场景模拟

**接口说明**: 进行What-if场景模拟分析

**请求地址**: `/api/v1/whatif/simulate`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "baseline": {
    "metrics": {
      "monthly_demand": 1000,
      "monthly_capacity": 1500,
      "unit_cost": 50,
      "lead_time_days": 7,
      "safety_stock": 100,
      "profit_margin": 0.2
    }
  },
  "scenarios": [
    {
      "name": "需求增长20%",
      "type": "demand_increase",
      "parameters": {
        "demand_change_rate": 0.2
      }
    },
    {
      "name": "成本上升10%",
      "type": "price_change", 
      "parameters": {
        "cost_change_rate": 0.1
      }
    }
  ]
}
```

**返回示例 (JSON)**:
```json
{
  "scenarios": [
    {
      "name": "需求增长20%",
      "type": "demand_increase",
      "risk_level": "HIGH",
      "metrics": {
        "capacity_utilization": 120,
        "total_cost": 60000,
        "profit": 12000
      },
      "changes": {
        "capacity_utilization": 0.2,
        "total_cost": 0.2,
        "profit": 0.2
      },
      "recommendations": [
        "📈 需求增长超过20%，建议提前备货或增加产能"
      ]
    }
  ],
  "best_recommendations": {
    "cost": "方案B-低成本",
    "speed": "方案A-最短时间",
    "risk": "方案C-低风险"
  }
}
```

---

## 六、插单影响分析 API

### 6.1 插单影响分析

**接口说明**: 分析插入新订单对现有计划的影响

**请求地址**: `/api/v1/impact/analyze`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "new_order": {
    "order_no": "PO_NEW",
    "item_code": "ITEM001",
    "quantity": 100,
    "priority": 1,
    "required_date": "2026-03-15T00:00:00",
    "unit_price": 60.0,
    "unit_cost": 40.0,
    "work_center": "生产线A"
  },
  "existing_plans": [
    {
      "plan_no": "PLAN001",
      "order_no": "PO001",
      "item_code": "ITEM001",
      "quantity": 50,
      "start_date": "2026-03-11T08:00:00",
      "end_date": "2026-03-11T12:00:00",
      "work_center": "生产线A",
      "priority": 3
    }
  ],
  "resources": [
    {
      "id": "R001",
      "name": "生产线A",
      "capacity_hours": 8,
      "hourly_cost": 100
    }
  ],
  "materials": {
    "ITEM001": {
      "quantity_required": 1,
      "current_inventory": 200,
      "on_order_qty": 0,
      "safety_stock": 50,
      "lead_time_days": 7
    }
  }
}
```

**返回示例 (JSON)**:
```json
{
  "new_order_no": "PO_NEW",
  "is_feasible": true,
  "total_impact_score": 45.5,
  "risk_level": "MEDIUM",
  "affected_orders": [
    {
      "order_no": "PO001",
      "original_end_date": "2026-03-11T12:00:00",
      "new_end_date": "2026-03-11T16:00:00",
      "delay_days": 0.17
    }
  ],
  "capacity_analysis": {
    "score": 65,
    "description": "产能利用率65%，需加班1小时",
    "financial_impact": 150
  },
  "material_analysis": {
    "score": 20,
    "description": "物料充足"
  },
  "cost_analysis": {
    "score": 30,
    "description": "额外成本 ¥150 (5%)"
  },
  "timeline_analysis": {
    "score": 25,
    "description": "影响1个订单，最大延期0.17天"
  },
  "recommendations": [
    "✅ 插单影响可控，可以执行"
  ],
  "alternative_solutions": [
    {
      "name": "分批交货",
      "description": "将订单分批交付，缓解产能压力",
      "pros": ["降低产能压力"],
      "cons": ["增加物流成本"],
      "impact_reduction": "30%"
    }
  ]
}
```

---

## 七、通知服务 API

### 7.1 发送通知

**接口说明**: 发送预警通知

**请求地址**: `/api/v1/notification/send`

**请求方式**: `POST`

**请求体 (JSON)**:
```json
{
  "type": "risk_warning",
  "title": "库存预警",
  "content": "物料 ITEM001 库存不足",
  "recipients": ["user@example.com"],
  "priority": "HIGH",
  "metadata": {
    "item_code": "ITEM001",
    "current_qty": 30,
    "safety_stock": 50
  }
}
```

**type 可选值**:
- `risk_warning` - 风险预警
- `inventory_alert` - 库存预警
- `order_update` - 订单更新
- `mrp_result` - MRP结果
- `schedule_change` - 排程变更

**priority 可选值**:
- `LOW`, `NORMAL`, `HIGH`, `URGENT`

---

## Java 调用示例

### 7.1 HttpClient 示例

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AirMRPClient {
    private final String baseUrl = "http://localhost:8000/api/v1";
    private final HttpClient client = HttpClient.newHttpClient();
    
    // 需求预测
    public String predictDemand(String itemCode, int days) throws Exception {
        String json = String.format("""
            {
                "item_code": "%s",
                "forecast_days": %d,
                "method": "prophet",
                "confidence_level": 0.95
            }
            """, itemCode, days);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/predict/demand"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    // 排程优化
    public String optimizeSchedule(Object orders, Object resources) throws Exception {
        // 构建请求JSON
        String json = buildScheduleRequest(orders, resources);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/schedule/optimize"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    // 安全库存计算
    public String calculateSafetyStock(String itemCode) throws Exception {
        String json = String.format("""
            {
                "item_code": "%s",
                "service_level": 0.95,
                "lead_time_days": 7,
                "method": "auto"
            }
            """, itemCode);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/predict/safety-stock"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    // AI对话
    public String chat(String message, String sessionId) throws Exception {
        String json = String.format("""
            {
                "message": "%s",
                "session_id": "%s",
                "use_llm": false
            }
            """, message, sessionId);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/chat"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    // 插单影响分析
    public String analyzeImpact(Object newOrder, Object existingPlans) throws Exception {
        String json = buildImpactRequest(newOrder, existingPlans);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/impact/analyze"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
```

### 7.2 OkHttp 示例

```java
import okhttp3.*;

public class AirMRPClient {
    private final OkHttpClient client = new OkHttpClient();
    private final String baseUrl = "http://localhost:8000/api/v1";
    
    public String predictDemand(String itemCode, int days) {
        JSONObject json = new JSONObject();
        json.put("item_code", itemCode);
        json.put("forecast_days", days);
        json.put("method", "prophet");
        json.put("confidence_level", 0.95);
        
        RequestBody body = RequestBody.create(
            json.toString(), 
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(baseUrl + "/predict/demand")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 7.3 RestTemplate 示例

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class AirMRPClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8000/api/v1";
    
    public Map<String, Object> predictDemand(String itemCode, int days) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> request = new HashMap<>();
        request.put("item_code", itemCode);
        request.put("forecast_days", days);
        request.put("method", "prophet");
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/predict/demand",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        return response.getBody();
    }
}
```

---

## C# 调用示例

### 7.4 HttpClient 示例

```csharp
using System;
using System.Net.Http;
using System.Text;
using System.Text.Json;

public class AirMRPClient
{
    private readonly string _baseUrl = "http://localhost:8000/api/v1";
    private readonly HttpClient _client = new HttpClient();
    
    // 需求预测
    public async Task<string> PredictDemandAsync(string itemCode, int days)
    {
        var request = new
        {
            item_code = itemCode,
            forecast_days = days,
            method = "prophet",
            confidence_level = 0.95
        };
        
        var json = JsonSerializer.Serialize(request);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        var response = await _client.PostAsync($"{_baseUrl}/predict/demand", content);
        return await response.Content.ReadAsStringAsync();
    }
    
    // 排程优化
    public async Task<string> OptimizeScheduleAsync(object orders, object resources)
    {
        var request = new
        {
            orders = orders,
            resources = resources,
            goal = "makespan"
        };
        
        var json = JsonSerializer.Serialize(request);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        var response = await _client.PostAsync($"{_baseUrl}/schedule/optimize", content);
        return await response.Content.ReadAsStringAsync();
    }
    
    // 安全库存计算
    public async Task<string> CalculateSafetyStockAsync(string itemCode)
    {
        var request = new
        {
            item_code = itemCode,
            service_level = 0.95,
            lead_time_days = 7,
            method = "auto"
        };
        
        var json = JsonSerializer.Serialize(request);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        var response = await _client.PostAsync($"{_baseUrl}/predict/safety-stock", content);
        return await response.Content.ReadAsStringAsync();
    }
    
    // AI对话
    public async Task<string> ChatAsync(string message, string sessionId)
    {
        var request = new
        {
            message = message,
            session_id = sessionId,
            use_llm = false
        };
        
        var json = JsonSerializer.Serialize(request);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        var response = await _client.PostAsync($"{_baseUrl}/chat", content);
        return await response.Content.ReadAsStringAsync();
    }
}
```

### 7.5 RestSharp 示例

```csharp
using RestSharp;

public class AirMRPClient
{
    private readonly RestClient _client;
    
    public AirMRPClient()
    {
        _client = new RestClient("http://localhost:8000/api/v1");
    }
    
    public string PredictDemand(string itemCode, int days)
    {
        var request = new RestRequest("/predict/demand", Method.Post);
        request.AddJsonBody(new
        {
            item_code = itemCode,
            forecast_days = days,
            method = "prophet",
            confidence_level = 0.95
        });
        
        var response = _client.Execute(request);
        return response.Content;
    }
    
    public string OptimizeSchedule(object orders, object resources)
    {
        var request = new RestRequest("/schedule/optimize", Method.Post);
        request.AddJsonBody(new
        {
            orders = orders,
            resources = resources,
            goal = "makespan"
        });
        
        var response = _client.Execute(request);
        return response.Content;
    }
}
```

---

## 错误响应

所有API的错误响应格式如下:

```json
{
  "error": "错误信息",
  "code": "ERROR_CODE",
  "details": {}
}
```

**常见错误码**:
- `VALIDATION_ERROR` - 参数验证错误
- `NOT_FOUND` - 资源不存在
- `INTERNAL_ERROR` - 内部错误
- `SERVICE_UNAVAILABLE` - 服务不可用

---

## 附录

### A. 响应状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

### B. 数据类型对应

| JSON类型 | Java类型 | C#类型 |
|----------|----------|---------|
| string | String | string |
| number | Double/Float | double/float |
| integer | Integer/Long | int/long |
| boolean | Boolean | bool |
| array | List | List/Array |
| object | Map/Object | object/dynamic |

---

*文档版本: 2.0*  
*更新时间: 2026-03-11*
