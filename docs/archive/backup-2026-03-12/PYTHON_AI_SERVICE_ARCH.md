# AI MRP Python AI Service 架构设计

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、项目概述

### 1.1 服务定位

AI MRP Python AI Service 是整个 AI MRP 系统的智能核心，负责：
- **需求预测**：基于历史数据进行时序预测
- **排程优化**：使用 OR-Tools 进行生产排程优化
- **安全库存**：智能计算安全库存策略
- **What-if 模拟**：场景模拟与影响分析
- **LLM 对话**：自然语言交互与智能问答

### 1.2 技术栈

| 层级 | 技术选型 |
|------|----------|
| **Web 框架** | FastAPI (异步高性能) |
| **AI/ML** | Prophet, LSTM, ARIMA, XGBoost |
| **优化求解** | Google OR-Tools |
| **LLM 集成** | OpenAI, DeepSeek, Ollama |
| **消息队列** | RabbitMQ, Kafka, Redis |
| **部署** | Docker, Uvicorn |

---

## 二、目录结构

```
ai-service/
├── app/
│   ├── main.py                    # FastAPI 应用入口
│   │
│   ├── algorithms/                # 核心算法引擎
│   │   ├── __init__.py           # 模块导出
│   │   ├── forecast.py           # 预测算法 (Prophet/LSTM/ARIMA/XGBoost)
│   │   ├── scheduler.py          # 排程优化 (OR-Tools)
│   │   ├── safety_stock.py       # 安全库存计算
│   │   ├── llm.py                # LLM 客户端封装
│   │   ├── whatif.py             # What-if 模拟引擎
│   │   └── impact_analysis.py    # 插单影响分析
│   │
│   ├── router/                   # API 路由层
│   │   ├── predict.py            # 需求预测 API
│   │   ├── schedule.py           # 排程优化 API
│   │   ├── chat.py               # 对话服务 API
│   │   ├── delivery_predict.py   # 交付预测 API
│   │   ├── inventory_predict.py  # 库存预测 API
│   │   ├── model.py              # 模型管理 API
│   │   ├── scenario.py           # 场景管理 API
│   │   └── metrics.py            # 监控指标 API
│   │
│   ├── integration/               # 集成层
│   │   ├── gateway.py            # 统一网关入口
│   │   ├── protocol.py           # 数据交换协议
│   │   ├── persistence.py        # 结果持久化
│   │   ├── model_service.py      # 模型持久化服务
│   │   ├── scenario_service.py    # 场景持久化服务
│   │   └── metrics_service.py    # 监控指标服务
│   │
│   ├── notification/              # 通知模块
│   │   ├── manager.py            # 通知管理器 (多通道)
│   │   └── __init__.py
│   │
│   ├── config/                   # 配置层
│   │   └── settings.py           # 应用配置 (Pydantic)
│   │
│   └── utils/                    # 工具层
│       ├── response.py           # 统一响应结构
│       ├── performance.py        # 性能优化工具
│       └── __init__.py
│
├── requirements.txt               # Python 依赖
├── .env.example                  # 环境变量示例
└── Dockerfile                    # Docker 构建文件
```

---

## 三、模块架构

### 3.1 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Router Layer (路由层)                      │
│  /predict/*, /schedule/*, /chat/*, /model/*, /scenario/*  │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                   Service Layer (服务层)                     │
│  gateway.py, model_service.py, scenario_service.py,         │
│  metrics_service.py, persistence.py                          │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                 Algorithm Layer (算法层)                     │
│  forecast.py, scheduler.py, safety_stock.py, llm.py,        │
│  whatif.py, impact_analysis.py                              │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                Infrastructure Layer (基础设施层)              │
│  notification/, config/, utils/, integration/               │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 核心模块说明

| 模块 | 职责 | 关键类/函数 |
|------|------|-------------|
| **router/** | HTTP API 入口 | FastAPI Router |
| **algorithms/** | 核心业务算法 | ForecastEngine, ORToolsScheduler |
| **integration/** | 外部集成 | Gateway, Protocol, Persistence |
| **notification/** | 消息通知 | NotificationManager |
| **config/** | 配置管理 | Settings (Pydantic) |
| **utils/** | 公共工具 | ApiResponse, MemoryCache |

---

## 四、数据流架构

### 4.1 请求处理流程

```
HTTP Request
    │
    ▼
┌─────────────────┐
│  Router Layer   │ ← 验证请求参数
│  (FastAPI)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Service Layer  │ ← 业务逻辑编排
│  (Gateway)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Algorithm Layer │ ← 核心算法执行
│ (ML/OR/LLM)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Persistence    │ ← 结果存储
│  (Optional)    │
└────────┬────────┘
         │
         ▼
    HTTP Response
```

### 4.2 Java-Python 集成流程

```
┌─────────────┐     HTTP/REST      ┌─────────────┐
│  Java后端   │ ─────────────────→ │ Python AI   │
│             │   JSON Request     │   Service   │
│  - 调用预测 │                    │  - 路由入口 │
│  - 获取结果 │ ←───────────────── │  - 算法执行 │
│  - 回调接收 │   JSON Response    │  - 结果存储 │
└─────────────┘                    └─────────────┘
```

---

## 五、关键设计模式

### 5.1 工厂模式

```python
# algorithms/__init__.py
from app.algorithms.forecast import create_forecast_engine

# 创建预测引擎
engine = create_forecast_engine("prophet", changepoint_prior_scale=0.1)
```

### 5.2 单例模式

```python
# integration/persistence.py
_integration_service: Optional[AIIntegrationService] = None

def get_integration_service() -> AIIntegrationService:
    global _integration_service
    if _integration_service is None:
        _integration_service = AIIntegrationService()
    return _integration_service
```

### 5.3 策略模式

```python
# algorithms/scheduler.py
class OptimizationGoal(Enum):
    MAKESPAN = "makespan"      # 最小化总完工时间
    TARDINESS = "tardiness"   # 最小化延迟
    COST = "cost"             # 最小化成本
    BALANCED = "balanced"      # 平衡模式
```

---

## 六、配置管理

### 6.1 环境变量

通过 `.env` 文件或环境变量配置：

```bash
# 服务配置
HOST=0.0.0.0
PORT=8000
DEBUG=false

# LLM 配置
LLM_PROVIDER=deepseek
DEEPSEEK_API_KEY=sk-xxx

# 数据库
DATABASE_URL=postgresql://...

# Redis
REDIS_URL=redis://localhost:6379/0

# 消息队列
RABBITMQ_HOST=localhost
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### 6.2 配置类

```python
# config/settings.py
class Settings(BaseSettings):
    llm_provider: str = "deepseek"
    deepseek_api_key: str = ""
    # ...
```

---

## 七、扩展性设计

### 7.1 新增预测算法

1. 在 `algorithms/forecast.py` 中创建新类继承 `ForecastEngine`
2. 在 `create_forecast_engine()` 工厂函数中注册

```python
class NewForecast(ForecastEngine):
    def fit(self, historical_data): ...
    def forecast(self, days, confidence): ...

# 注册
engines = {
    "prophet": ProphetForecast,
    "new_forecast": NewForecast,  # 新增
}
```

### 7.2 新增通知通道

1. 继承 `NotificationChannel` 基类
2. 实现 `send()` 和 `batch_send()` 方法
3. 注册到 `NotificationManager`

```python
class NewChannel(NotificationChannel):
    async def send(self, notification): ...
    async def batch_send(self, notifications): ...

manager.register_channel("new_channel", NewChannel(...))
```

---

## 八、部署架构

### 8.1 Docker 部署

```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 8.2 服务依赖

```
┌──────────────┐     ┌──────────────┐
│   Python AI  │────→│  RabbitMQ    │
│   Service    │     │  / Kafka     │
└──────────────┘     └──────────────┘
        │
        ▼
┌──────────────┐     ┌──────────────┐
│   Redis      │     │  PostgreSQL  │
│  (缓存/队列) │     │  (数据持久化) │
└──────────────┘     └──────────────┘
```

---

*文档版本: 1.0*
