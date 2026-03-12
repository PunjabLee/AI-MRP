# AI MRP 开发用户手册

> **版本**: 1.0  
> **日期**: 2026-03-11  
> **说明**: 完整整合开发设计文档，供开发人员使用

---

## 一、开发环境搭建

### 1.1 前置要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17+ | Java 17 LTS |
| **Node.js** | 18+ | 前端开发 |
| **Python** | 3.10+ | AI 服务 |
| **Maven** | 3.8+ | Java 构建 |
| **Docker** | 24+ | 容器化 |
| **PostgreSQL** | 14+ | 数据库 |
| **Redis** | 7+ | 缓存/消息队列 |

### 1.2 环境变量配置

```bash
# Java 后端 (.env 或 IDE 配置)
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=postgresql://localhost:5432/aimrp
DATABASE_USERNAME=aimrp
DATABASE_PASSWORD=your_password
REDIS_URL=redis://localhost:6379/0

# Python AI 服务 (.env)
PYTHON_ENV=development
LLM_PROVIDER=deepseek
DEEPSEEK_API_KEY=sk-xxx
DATABASE_URL=postgresql://...

# 前端 (.env)
VITE_API_BASE_URL=http://localhost:8080/api
```

### 1.3 项目启动

```bash
# 1. 克隆项目
git clone https://github.com/your-repo/AI-MRP.git
cd AI-MRP

# 2. 启动 Java 后端
cd code/backend
mvn clean install -DskipTests
mvn spring-boot:run

# 3. 启动 Python AI 服务
cd code/ai-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000

# 4. 启动前端
cd code/frontend/aimrp-admin
npm install
npm run dev

# 5. 访问
# 前端: http://localhost:5173
# Java后端: http://localhost:8080
# Python AI: http://localhost:8000
```

---

## 二、代码结构

### 2.1 项目目录

```
AI-MRP/
├── docs/                    # 项目文档
├── code/
│   ├── backend/           # Java Spring Boot 后端
│   │   ├── aimrp-api/    # API网关入口
│   │   ├── aimrp-core/   # 核心实体
│   │   ├── aimrp-common/ # 公共模块
│   │   ├── aimrp-demand/ # 需求管理
│   │   ├── aimrp-forecast/ # 预测模块
│   │   ├── aimrp-inventory/ # 库存管理
│   │   ├── aimrp-production/ # 生产管理
│   │   ├── aimrp-mrp/   # MRP计算
│   │   └── ...
│   │
│   ├── ai-service/       # Python FastAPI AI服务
│   │   ├── app/
│   │   │   ├── algorithms/ # 核心算法
│   │   │   ├── router/    # API路由
│   │   │   ├── integration/ # 集成层
│   │   │   ├── notification/ # 通知模块
│   │   │   ├── config/    # 配置
│   │   │   └── utils/    # 工具
│   │   └── requirements.txt
│   │
│   └── frontend/         # React 前端
│       └── aimrp-admin/
│           └── src/
│               ├── api/    # API调用
│               ├── apps/   # 页面组件
│               ├── components/ # 公共组件
│               └── ...
│
└── docker-compose.yml     # Docker编排
```

### 2.2 Java 后端模块

| 模块 | 职责 | 关键类 |
|------|------|--------|
| `aimrp-api` | API网关入口 | `ApiApplication` |
| `aimrp-core` | 核心实体/枚举 | `BaseEntity`, `Enums` |
| `aimrp-common` | 公共工具 | `ApiResponse`, `DateUtil` |
| `aimrp-forecast` | 需求预测 | `ForecastController`, `DemandForecastService` |
| `aimrp-production` | 生产管理 | `ProductionOrderController`, `ProductionScheduleService` |
| `aimrp-integration` | 外部集成 | `IntegrationController`, `FeignClients` |

### 2.3 Python AI 服务模块

| 模块 | 职责 | 关键文件 |
|------|------|----------|
| `algorithms/` | 核心算法 | `forecast.py`, `scheduler.py`, `llm.py` |
| `router/` | API路由 | `predict.py`, `schedule.py`, `chat.py` |
| `integration/` | 集成层 | `gateway.py`, `persistence.py` |
| `notification/` | 通知模块 | `manager.py` |
| `config/` | 配置 | `settings.py` |
| `utils/` | 工具 | `response.py`, `performance.py` |

---

## 三、核心业务开发

### 3.1 新增预测算法

**步骤1**: 创建预测引擎类

```java
// aimrp-forecast/src/main/java/.../NewForecastMethod.java
@Service
public class NewForecastMethod {
    
    public List<ForecastResult> forecast(String itemCode, 
                                          List<HistoricalData> data, 
                                          int days) {
        // 实现预测逻辑
        return results;
    }
}
```

**步骤2**: 注册到工厂

```java
// DemandForecastService.java
public List<ForecastResult> forecast(...) {
    switch (method) {
        case NEW_METHOD:
            return newForecastMethod.forecast(itemCode, data, days);
        // ...
    }
}
```

**Python 侧** (类似):

```python
# app/algorithms/forecast.py
class NewForecastEngine(ForecastEngine):
    def fit(self, historical_data):
        # 训练逻辑
        pass
    
    def forecast(self, days, confidence):
        # 预测逻辑
        return results

# 注册到工厂
def create_forecast_engine(method, **kwargs):
    engines = {
        "prophet": ProphetForecast,
        "new_engine": NewForecastEngine,  # 新增
    }
    return engines[method]()
```

### 3.2 新增 API 接口

**Java 后端**:

```java
// 1. 创建 Controller
@RestController
@RequestMapping("/api/xxx")
@RequiredArgsConstructor
public class XxxController {
    
    @PostMapping("/action")
    public ApiResponse<XxxResponse> action(@RequestBody XxxRequest request) {
        // 业务逻辑
        return ApiResponse.ok(result);
    }
}

// 2. 创建 Service
@Service
@RequiredArgsConstructor
public class XxxService {
    
    public XxxResult doAction(XxxRequest request) {
        // 业务逻辑
        return result;
    }
}

// 3. 创建 Repository (如需数据库)
@Repository
public interface XxxRepository extends JpaRepository<XxxEntity, Long> {
}
```

**Python AI 服务**:

```python
# 1. 创建 Router
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()

class XxxRequest(BaseModel):
    param1: str
    param2: int

@router.post("/action")
async def xxx_action(request: XxxRequest):
    # 业务逻辑
    return {"code": 200, "data": {...}}

# 2. 注册到 main.py
app.include_router(router, prefix="/xxx", tags=["Xxx"])
```

### 3.3 新增通知通道

```python
# app/notification/manager.py
class NewChannel(NotificationChannel):
    @property
    def name(self) -> str:
        return "new_channel"
    
    async def send(self, notification: Notification) -> bool:
        # 发送逻辑
        return True

# 注册
manager.register_channel("new_channel", NewChannel(...))
```

---

## 四、集成开发

### 4.1 Java 调用 Python AI 服务

**步骤1**: 添加 Feign Client

```java
// aimrp-integration/src/.../feign/AIForecastClient.java
@FeignClient(
    name = "ai-service",
    url = "${ai.service.url}",
    path = "/predict"
)
public interface AIForecastClient {
    
    @PostMapping("/demand")
    ApiResponse<PythonForecastResponse> forecast(@RequestBody PythonPredictRequest request);
}
```

**步骤2**: 配置

```yaml
# application.yml
ai:
  service:
    url: http://localhost:8000
    timeout: 60000
```

**步骤3**: 调用

```java
@Service
@RequiredArgsConstructor
public class ForecastIntegrationService {
    
    private final AIForecastClient aiClient;
    
    public List<ForecastResult> forecastWithAI(PredictRequest request) {
        PythonPredictRequest pythonRequest = convertToPython(request);
        var response = aiClient.forecast(pythonRequest);
        return convertToJava(response.getData());
    }
}
```

### 4.2 回调机制

```python
# Python 回调 Java
result = {
    "request_id": "req_123",
    "status": "completed",
    "data": {...}
}

# 发送到 Java 回调 URL
import requests
requests.post(
    "http://localhost:8080/api/integration/callback",
    json=result,
    headers={"Authorization": "Bearer token"}
)
```

---

## 五、测试

### 5.1 单元测试

```java
// Java JUnit 5
@SpringBootTest
class DemandForecastServiceTest {
    
    @Autowired
    private DemandForecastService service;
    
    @Test
    void testForecast() {
        // 准备数据
        List<HistoricalData> data = Arrays.asList(
            new HistoricalData(LocalDate.now().minusDays(7), BigDecimal.valueOf(100)),
            new HistoricalData(LocalDate.now().minusDays(6), BigDecimal.valueOf(110))
        );
        
        // 执行
        List<ForecastResult> results = service.forecast("A001", data, 7, ForecastMethod.SIMPLE_MA);
        
        // 断言
        assertNotNull(results);
        assertEquals(7, results.size());
    }
}
```

```python
# Python pytest
import pytest
from app.algorithms.forecast import ProphetForecast

def test_prophet_forecast():
    engine = ProphetForecast()
    historical_data = [
        {"date": "2024-01-01", "qty": 100},
        {"date": "2024-01-02", "qty": 110}
    ]
    
    engine.fit(historical_data)
    results = engine.forecast(7, 0.95)
    
    assert len(results) == 7
    assert all("qty" in r for r in results)
```

### 5.2 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ForecastControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testForecastEndpoint() throws Exception {
        mockMvc.perform(post("/api/forecast/forecast")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemCode\":\"A001\",\"forecastDays\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 六、调试

### 6.1 日志配置

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.aimrp: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

```python
# Python
import logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)
logger.debug("调试信息: {}", variable)
```

### 6.2 断点调试

**IntelliJ IDEA**:
1. 在代码行左侧点击设置断点
2. 右键 → Debug 'Application'
3. 调试窗口查看变量

**VS Code (Python)**:
1. 安装 Python 扩展
2. 在代码行左侧点击设置断点
3. 按 F5 开始调试

---

## 七、代码提交

### 7.1 Git 工作流

```bash
# 1. 创建功能分支
git checkout -b feature/xxx

# 2. 开发并提交
git add .
git commit -m "feat: 新功能描述"

# 3. 推送到远程
git push origin feature/xxx

# 4. 创建 Pull Request
# 合并到 develop 分支
```

### 7.2 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 代码重构
test: 测试
chore: 构建/工具
```

---

*文档版本: 1.0*
