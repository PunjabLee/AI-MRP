# AI MRP Python Service 模块依赖关系

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、整体依赖关系图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              main.py                                        │
│                          (应用入口)                                         │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Router Layer                                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ predict │ │ schedule│ │  chat  │ │ model  │ │scenario│ │metrics │  │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘  │
└───────┼──────────┼──────────┼──────────┼──────────┼──────────┼──────────┘
        │          │          │          │          │          │
        ▼          ▼          ▼          ▼          ▼          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Service Layer                                      │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                    integration/gateway.py                           │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────────┐  │   │
│  │  │ persistence │  │model_service │  │   scenario_service     │  │   │
│  │  └─────────────┘  └──────────────┘  └─────────────────────────┘  │   │
│  └────────────────────────────────────────────────────────────────────┘   │
└───────┼──────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Algorithm Layer                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │ forecast │ │ scheduler│ │safety_   │ │   llm   │ │ whatif  │        │
│  │          │ │          │ │  stock   │ │          │ │         │        │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘        │
└───────┬──────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                     │
│  │ config/  │ │  utils/  │ │notification│ │ protocol │                     │
│  │settings  │ │response  │ │ manager  │ │         │                     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 二、模块调用关系

### 2.1 Router → Service

| Router | 调用 Service | 说明 |
|--------|-------------|------|
| `predict.py` | `algorithms/forecast.py` | 创建预测引擎 |
| `schedule.py` | `algorithms/scheduler.py` | 执行排程优化 |
| `chat.py` | `algorithms/llm.py` | LLM 对话 |
| `model.py` | `integration/model_service.py` | 模型 CRUD |
| `scenario.py` | `integration/scenario_service.py` | 场景 CRUD |
| `metrics.py` | `integration/metrics_service.py` | 指标记录 |
| `gateway.py` | `persistence.py` + 算法模块 | 统一入口 |

### 2.2 Service → Algorithm

| Service | 依赖 Algorithm | 说明 |
|---------|---------------|------|
| `gateway.py` | forecast, scheduler, llm, whatif | 路由请求到算法 |
| `model_service.py` | - | 纯服务，无算法依赖 |
| `scenario_service.py` | whatif | 使用 What-if 模拟 |

### 2.3 Algorithm 内部依赖

| Algorithm | 依赖 | 说明 |
|-----------|------|------|
| `whatif.py` | forecast, scheduler | 模拟需要预测和排程 |
| `impact_analysis.py` | scheduler | 影响分析需要排程结果 |

---

## 三、数据流关系

### 3.1 预测请求数据流

```
HTTP Request (/predict/demand)
    │
    ▼
predict.py (参数验证)
    │
    ▼
create_forecast_engine() [factory]
    │
    ▼
ProphetForecast.fit() → ProphetForecast.forecast()
    │
    ▼
返回预测结果
    │
    ▼
ApiResponse.success()
    │
    ▼
HTTP Response
```

### 3.2 排程请求数据流

```
HTTP Request (/schedule/optimize)
    │
    ▼
schedule.py (参数验证)
    │
    ▼
create_scheduler() [factory]
    │
    ▼
ORToolsScheduler.set_orders() → set_resources() → optimize()
    │
    ▼
OR-Tools CP-SAT 求解
    │
    ▼
ScheduleResult
    │
    ▼
ApiResponse.success()
    │
    ▼
HTTP Response
```

### 3.3 Java 集成数据流

```
Java HTTP Client
    │
    ▼
/integration/invoke (Gateway)
    │
    ▼
AIIntegrationService.create_pending_result()
    │
    ├──────────────┬──────────────┬──────────────┐
    ▼              ▼              ▼              ▼
PREDICT_DEMAND  SCHEDULE_       CHAT_        WHATIF_
               OPTIMIZE         MESSAGE       SIMULATE
    │              │              │              │
    ▼              ▼              ▼              ▼
forecast.py    scheduler.py    llm.py       whatif.py
    │              │              │              │
    └──────────────┴──────────────┴──────────────┘
                         │
                         ▼
            AIIntegrationService.complete_result()
                         │
                         ▼
            回调 Java (callback_url) [可选]
```

---

## 四、共享依赖

### 4.1 共享工具

| 工具 | 使用模块 |
|------|----------|
| `utils/response.py` | 所有 Router |
| `utils/performance.py` | predict.py, schedule.py |
| `config/settings.py` | main.py, 各模块 |

### 4.2 共享数据结构

```python
# algorithms/__init__.py 统一导出
from app.algorithms.forecast import ForecastEngine
from app.algorithms.scheduler import ScheduleResult
from app.algorithms.safety_stock import SafetyStockResult

# 所有 Router 导入方式
from app.algorithms import create_forecast_engine, create_scheduler
```

---

## 五、循环依赖检查

```
✓ 无循环依赖

main.py → router/* → (service) → algorithms/* → utils/*
                                           ↓
                                    notification/*
                                    config/*
```

---

## 六、扩展点说明

### 6.1 新增 Router

1. 创建 `router/new_feature.py`
2. 导入 `ApiResponse` from `utils/response`
3. 导入算法 from `algorithms`
4. 在 `main.py` 注册 router

```python
# main.py
from app.router import new_feature
app.include_router(new_feature.router, prefix="/new", tags=["NewFeature"])
```

### 6.2 新增 Algorithm

1. 创建 `algorithms/new_algorithm.py`
2. 继承基类 (如 `ForecastEngine`)
3. 在 `algorithms/__init__.py` 导出
4. 在工厂函数中注册

---

*文档版本: 1.0*
