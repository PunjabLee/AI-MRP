# AI MRP Python Service 核心流程图与完整方法链

> **版本**: 2.0  
> **日期**: 2026-03-11  
> **说明**: 完整记录每个业务流涉及的所有方法调用链

---

## 一、预测流程

### 1.1 需求预测完整流程

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           需求预测流程                                        │
│                      POST /predict/demand                                   │
└──────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整方法调用链
================================================================================

步骤1: Router 入口
    predict.py::predict_demand(request: PredictRequest) → ApiResponse
    文件: app/router/predict.py, 行号 ~150

步骤2: 获取历史数据
    _generate_mock_data(item_code: str) → List[Dict]
    文件: predict.py, 行号 540
    返回: [{"date": datetime, "qty": float}, ...] 90条数据

步骤3: 方法选择 (如果 method == "auto")
    auto_select_algorithm(historical_data: List[Dict]) → str
    文件: algorithms/forecast.py, 行号 800
    实现:
        n = len(historical_data)
        if n < 30: return "moving_average"
        elif n < 100: return "exponential_smoothing"
        else: return "prophet"

步骤4: 创建预测引擎
    create_forecast_engine(method: str, **kwargs) → ForecastEngine
    文件: algorithms/forecast.py, 行号 780
    
    参数映射:
    ┌──────────────┬─────────────────────────────────────────────────────────┐
    │ method       │ kwargs 参数                                             │
    ├──────────────┼─────────────────────────────────────────────────────────┤
    │ prophet      │ prophet_changepoint_prior_scale, seasonality_mode,    │
    │              │ yearly_seasonality, weekly_seasonality                │
    ├──────────────┼─────────────────────────────────────────────────────────┤
    │ arima        │ order (tuple)                                         │
    ├──────────────┼─────────────────────────────────────────────────────────┤
    │ lstm         │ sequence_length, epochs                               │
    ├──────────────┼─────────────────────────────────────────────────────────┤
    │ xgboost      │ (无额外参数)                                           │
    ├──────────────┼─────────────────────────────────────────────────────────┤
    │ moving_average│ window                                               │
    └──────────────┴─────────────────────────────────────────────────────────┘

步骤5: 训练模型
    engine.fit(historical_data: List[Dict]) → self
    
    [ProphetForecast.fit 实现]
    文件: algorithms/forecast.py, 行号 78
    实现:
        1. 转换数据格式:
           self.historical_df = pd.DataFrame([
               {"ds": self._parse_date(d["date"]), "y": d["qty"]}
               for d in historical_data
           ])
        2. 创建并训练模型:
           self.model = Prophet(
               yearly_seasonality=self.yearly_seasonality,
               weekly_seasonality=self.weekly_seasonality,
               seasonality_mode=self.seasonality_mode,
               changepoint_prior_scale=self.changepoint_prior_scale
           )
           self.model.fit(self.historical_df)
           self._fitted = True

步骤6: 执行预测
    engine.forecast(forecast_days: int, confidence: float) → List[Dict]
    
    [ProphetForecast.forecast 实现]
    文件: algorithms/forecast.py, 行号 95
    实现:
        1. 创建未来日期:
           self.future = self.model.make_future_dataframe(periods=days)
        2. 预测:
           self.forecast_df = self.model.predict(self.future)
        3. 构建结果:
           z_score = self._get_z_score(confidence)  # 0.95→1.96
           results = [{"date": row['ds'], "qty": max(0, row['yhat']),
                      "lower": ..., "upper": ..., "trend": ..., "seasonal": ...}]

步骤7: 获取评估指标
    engine.get_metrics() → Dict[str, float]
    文件: algorithms/forecast.py, 行号 120
    实现:
        merged = self.forecast_df.merge(self.historical_df, on='ds', how='inner')
        mae = np.mean(np.abs(merged['y'] - merged['yhat']))
        rmse = np.sqrt(np.mean((merged['y'] - merged['yhat'])**2))
        mape = np.mean(np.abs((merged['y'] - merged['yhat']) / merged['y'])) * 100
        return {"mape": round(mape, 2), "mae": round(mae, 2), "rmse": round(rmse, 2)}

步骤8: 构建可视化数据
    _build_forecast_visualization(forecast: List[Dict], metrics: Dict) → Dict
    文件: predict.py, 行号 390

步骤9: 返回响应
    ApiResponse.success(data: dict, message: str) → ApiResponse
    文件: utils/response.py
    返回结构:
        {
            "code": 200,
            "message": "预测成功",
            "success": true,
            "data": {
                "item_code": str,
                "method": str,
                "forecast": [...],
                "confidence": float,
                "metrics": {...},
                "parameters": {...},
                "visualization_data": {...}
            }
        }

================================================================================
                           完整数据模型
================================================================================

【输入模型】

PredictRequest (pydantic.BaseModel)
├── item_code: str                              # 物料编码 (必需)
├── forecast_days: int = 30                     # 预测天数 [1-365]
├── method: str = "auto"                        # 预测方法
├── confidence_level: float = 0.95               # 置信度 [0.5-0.99]
├── historical_data: Optional[List[Dict]]      # 历史数据
│   └── [{"date": "2024-01-01", "qty": 100}, ...]
├── prophet_changepoint_prior_scale: Optional[float]   # [0.001-0.5]
├── prophet_seasonality_mode: Optional[str]            # additive | multiplicative
├── prophet_yearly_seasonality: Optional[bool]
├── prophet_weekly_seasonality: Optional[bool]
├── lstm_sequence_length: Optional[int]          # [7-180]
├── lstm_epochs: Optional[int]                  # [10-500]
├── arima_p: Optional[int]                      # [0-10]
├── arima_d: Optional[int]                      # [0-2]
└── arima_q: Optional[int]                      # [0-10]

【算法引擎】

ForecastEngine (ABC) - algorithms/forecast.py
├── fit(historical_data: List[Dict]) → self
├── forecast(days: int, confidence: float) → List[Dict]
└── get_metrics() → Dict[str, float]

ProphetForecast (继承 ForecastEngine)
├── __init__(changepoint_prior_scale=0.05, seasonality_mode='multiplicative',
│           yearly_seasonality=True, weekly_seasonality=True)
├── _parse_date(date_input) → datetime
├── _get_z_score(confidence: float) → float     # {0.90:1.645, 0.95:1.96, 0.99:2.576}
├── _calculate_seasonal_factor(values) → Dict
└── _fallback_forecast(days, confidence) → List[Dict]  # Prophet不可用时备选

SafetyStockEngine - algorithms/safety_stock.py
└── calculate(item_code, historical_demand, lead_time_days, ...) → SafetyStockResult

ScheduleResult - algorithms/scheduler.py
├── schedule_id: str
├── status: str                     # optimal | feasible | suboptimal
├── makespan_hours: float
├── makespan_days: float
├── total_tardiness_hours: float
├── total_cost: float
├── resource_utilization: Dict[str, float]
├── schedule_details: List[Dict]
├── gantt_data: List[Dict]
└── metrics: Dict[str, Any]
```

---

## 二、排程流程

### 2.1 排程优化完整流程

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           排程优化流程                                        │
│                     POST /schedule/optimize                                 │
└──────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整方法调用链
================================================================================

步骤1: Router 入口
    schedule.py::optimize_schedule(request: ScheduleRequest) → ApiResponse
    文件: app/router/schedule.py, 行号 ~90

步骤2: 参数验证
    IF orders IS EMPTY: return ApiResponse.bad_request("生产订单不能为空")
    IF resources IS EMPTY: return ApiResponse.bad_request("资源不能为空")

步骤3: 创建排程器并优化
    create_scheduler(orders, resources, goal, constraints, time_limit_seconds)
    → ScheduleResult
    文件: algorithms/scheduler.py, 行号 598
    
    实现:
        scheduler = ORToolsScheduler(time_limit_seconds=time_limit_seconds)
        scheduler.set_orders(orders)
        scheduler.set_resources(resources)
        scheduler.set_goal(goal)
        if constraints: scheduler.set_constraints(constraints)
        return scheduler.optimize()

步骤4: ORToolsScheduler.optimize()
    文件: algorithms/scheduler.py, 行号 180
    实现:
        IF NOT (self.orders AND self.resources):
            RETURN self._create_empty_result()
        TRY:
            RETURN self._ortools_solve()    # 优先使用OR-Tools
        EXCEPT:
            RETURN self._heuristic_solve()  # 回退到启发式

步骤5: ORToolsScheduler._ortools_solve()
    文件: algorithms/scheduler.py, 行号 187
    实现:
        1. 导入: from ortools.sat.python import cp_model
        2. 创建模型: model = cp_model.CpModel()
        3. 创建决策变量:
           - start_time[o, r]: 订单o在资源r上的开始时间
           - assign[o, r]: 订单o是否分配给资源r
        4. 添加约束:
           - 每个订单必须分配且只能分配给一个资源
           - 资源容量约束 (Cumulative)
           - 优先级约束
           - 截止时间约束
        5. 设置目标函数:
           IF goal == MAKESPAN: model.Minimize(makespan)
           ELIF goal == TARDINESS: model.Minimize(sum(tardiness))
           ELIF goal == COST: model.Minimize(sum(cost))
        6. 求解:
           solver = cp_model.CpSolver()
           solver.parameters.max_time_in_seconds = self._time_limit_seconds  # ← 透传
           solver.parameters.num_workers = 4
           status = solver.Solve(model)
        7. 解析结果: return self._parse_solution(solver, status, ...)

步骤6: ORToolsScheduler._parse_solution()
    文件: algorithms/scheduler.py, 行号 318
    实现:
        FOR o IN range(num_orders):
            FOR r IN range(num_resources):
                IF solver.Value(assign[o][r]):
                    start = solver.Value(start_time[(o, r)])
                    duration = orders[o].quantity * orders[o].unit_process_time
                    end = start + duration
                    schedule_details.append({...})
        makespan = max([d["end_minutes"] for d in schedule_details])
        RETURN ScheduleResult(schedule_id=..., status=..., makespan_hours=..., ...)

步骤7: 构建可视化数据
    _build_gantt_visualization(gantt_data, schedule_details)
    + _build_utilization_chart(resource_utilization)
    + _build_metrics_summary(result)

步骤8: 返回响应
    ApiResponse.success(data=data, message="排程优化完成")

================================================================================
                           完整数据模型
================================================================================

【输入模型】

ScheduleRequest (pydantic.BaseModel)
├── orders: List[Dict]                    # 必需
│   └── [{"id": "ORDER001", "product": "A", "quantity": 100,
│         "unit_time": 60, "priority": 1, "deadline": "2024-01-15", ...}]
├── resources: List[Dict]               # 必需
│   └── [{"id": "WC01", "name": "工作中心1", "capacity": 8,
│         "hourly_cost": 100, ...}]
├── goal: str = "makespan"             # makespan | tardiness | cost | balanced
├── constraints: Dict = {}              # 约束条件
├── optimize_level: str = "normal"     # fast | normal | deep
├── time_limit_seconds: int = 30        # [1-300] ← 关键参数
└── enable_caching: bool = True

【核心类】

ORToolsScheduler - algorithms/scheduler.py
├── __init__(time_limit_seconds=30)
├── set_orders(orders) → self
├── set_resources(resources) → self
├── set_goal(goal) → self
├── set_constraints(constraints) → self
├── optimize() → ScheduleResult
├── _ortools_solve() → ScheduleResult        # OR-Tools实现
├── _heuristic_solve() → ScheduleResult      # 启发式备选
├── _parse_solution(...) → ScheduleResult
├── _calculate_horizon() → int
└── _create_empty_result() → ScheduleResult

OptimizationGoal (Enum)
├── MAKESPAN     # 最小化总完工时间
├── TARDINESS    # 最小化总延迟时间
├── COST         # 最小化总成本
└── BALANCED     # 平衡模式

ProductionOrder (dataclass)
├── id: str
├── product: str
├── quantity: float
├── unit_process_time: float      # 分钟/件
├── priority: int = 1            # 1=最高
├── deadline: Optional[datetime]
├── available_after: Optional[datetime]
├── process_type: str
├── setup_time: float = 0
└── color: str

Resource (dataclass)
├── id: str
├── name: str
├── capacity: float = 8          # 每天可用小时
├── available_hours: float = 8
├── supported_processes: List[str]
├── hourly_cost: float = 100
├── overtime_cost: float = 150
└── maintenance_windows: List[Tuple[datetime, datetime]]
```

---

## 三、对话流程

### 3.1 LLM 对话完整流程

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           对话流程                                            │
│                       POST /chat/message                                     │
└──────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整方法调用链
================================================================================

步骤1: Router 入口
    chat.py::chat(request: ChatRequest) → ApiResponse
    文件: app/router/chat.py, 行号 ~220

步骤2: 会话管理
    conversation_context.add_message(session_id, role, content, intent, entities)
    文件: chat.py, 行号 70
    实现:
        if session_id not in self.sessions:
            self.sessions[session_id] = []
        self.sessions[session_id].append({role, content, intent, entities, timestamp})
        if len > max_history: 保持最新10条

步骤3: 模式选择

【规则匹配模式】 (use_llm = False)

    步骤3a-1: 意图识别
        _recognize_intent(message: str) → (intent, entities, confidence)
        文件: chat.py, 行号 225
        实现:
            FOR intent, patterns IN INTENT_PATTERNS:
                FOR pattern IN patterns:
                    IF re.search(pattern, message):
                        RETURN intent, extracted_entities, 0.9
            RETURN "unknown", {}, 0.0

    步骤3a-2: 动作执行
        _execute_action(intent, entities, context) → response_message
        文件: chat.py, 行号 280
        意图-动作映射:
        ┌─────────────────┬───────────────────────────────────────────────────┐
        │ intent          │ 动作                                            │
        ├─────────────────┼───────────────────────────────────────────────────┤
        │ greeting        │ 返回问候语                                      │
        │ query_stock     │ 查询库存 → 调用 inventory API                   │
        │ run_mrp         │ 运行MRP → 调用 mrp API                        │
        │ query_forecast  │ 查询预测 → 调用 predict API                   │
        │ create_order    │ 创建订单 → 调用 order API                      │
        │ schedule        │ 排程优化 → 调用 schedule API                  │
        │ whatif          │ What-if模拟 → 调用 whatif API                 │
        └─────────────────┴───────────────────────────────────────────────────┘

【LLM模式】 (use_llm = True)

    步骤3b-1: 获取LLM客户端
        get_llm_client() → Optional[LLMClient]
        文件: chat.py, 行号 75
        实现:
            IF _llm_client IS None:
                config = LLMConfig()  # 从settings读取
                IF config.api_key:
                    _llm_client = LLMClient(config)
            RETURN _llm_client

    步骤3b-2: 获取MRP工具定义
        get_mrp_tools() → List[ToolDefinition]
        文件: algorithms/llm.py, 行号 200
        返回工具列表:
        ┌──────────────────────────┬────────────────────────────────────────┐
        │ 工具名称                 │ 描述                                   │
        ├──────────────────────────┼────────────────────────────────────────┤
        │ get_inventory            │ 查询物料库存                           │
        │ run_mrp_calculation      │ 运行MRP计算                           │
        │ create_order             │ 创建订单                               │
        │ query_forecast           │ 查询需求预测                           │
        │ optimize_schedule        │ 优化生产排程                          │
        │ calculate_safety_stock   │ 计算安全库存                          │
        └──────────────────────────┴────────────────────────────────────────┘

    步骤3b-3: 构建消息列表
        messages = [
            Message(role="system", content=SYSTEM_PROMPT),
            ...conversation_context.get_history(session_id),
            Message(role="user", content=message)
        ]

    步骤3b-4: 调用LLM
        llm_client.chat(messages, tools, ...) → LLMResponse
        文件: algorithms/llm.py, 行号 80
        实现:
            payload = {
                "model": config.model,
                "messages": [...],
                "tools": [...],
                "temperature": 0.7
            }
            response = httpx.post(f"{config.base_url}/chat/completions",
                                  json=payload, headers={...})
            RETURN self._parse_response(response)

    步骤3b-5: 处理工具调用 (如果LLM返回了工具调用)
        IF response.tool_calls:
            FOR tool_call IN response.tool_calls:
                result = _execute_tool(tool_call.function.name,
                                      tool_call.function.arguments)
                messages.append(Message(role="assistant", content=result))
            final_response = llm_client.chat(messages)  # 再次调用生成最终回复

步骤4: 返回响应
    ApiResponse.success(data=ChatResponse, message="success")

================================================================================
                           完整数据模型
================================================================================

【输入模型】

ChatRequest
├── message: str                    # 用户消息 (必需)
├── session_id: Optional[str]      # 会话ID
├── context: Optional[Dict]         # 额外上下文
└── use_llm: bool = False         # 是否使用LLM

ChatResponse
├── message: str                   # 回复消息
├── session_id: str               # 会话ID
├── intent: Optional[str]          # 识别的意图
├── entities: Optional[Dict]       # 提取的实体
├── suggestions: Optional[List[str]]  # 建议操作
├── action: Optional[Dict]         # 执行的动作
├── confidence: Optional[float]    # 置信度
└── mode: str                     # "rule" | "llm"

【核心类】

ConversationContext - chat.py
├── __init__()
├── add_message(session_id, role, content, intent, entities) → None
├── get_history(session_id) → List[Dict]
└── clear(session_id) → None

LLMClient - algorithms/llm.py
├── __init__(config: LLMConfig)
├── chat(messages, tools, ...) → LLMResponse
├── _parse_response(response) → LLMResponse
└── _call_api(payload) → Dict

LLMConfig - algorithms/llm.py
├── provider: str                  # "openai" | "deepseek" | "ollama"
├── api_key: str
├── base_url: str
├── model: str
├── temperature: float = 0.7
└── max_tokens: int = 2000

Message (dataclass)
├── role: str                     # "system" | "user" | "assistant"
├── content: str
└── tool_calls: Optional[List]

ToolDefinition (dataclass)
├── name: str
├── description: str
└── parameters: Dict
```

---

## 四、集成网关流程

### 4.1 Java-Python 集成流程

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     Java-Python 集成流程                                      │
│                  POST /integration/invoke                                   │
└──────────────────────────────────────────────────────────────────────────────┘

================================================================================
                           完整方法调用链
================================================================================

步骤1: Router 入口
    gateway.py::invoke(request: GatewayRequest) → GatewayResponse
    文件: app/integration/gateway.py, 行号 ~50

步骤2: 生成请求ID
    request_id = request.request_id OR f"req_{datetime.now().strftime('%Y%m%d%H%M%S')}"

步骤3: 解析请求类型
    RequestType(request.type) → RequestType
    文件: integration/protocol.py, 行号 20
    
    RequestType 枚举值:
    ┌────────────────────────┬────────────────────────────────────────────────┐
    │ 类型                    │ 说明                                           │
    ├────────────────────────┼────────────────────────────────────────────────┤
    │ PREDICT_DEMAND         │ 需求预测                                       │
    │ PREDICT_SAFETY_STOCK   │ 安全库存计算                                   │
    │ PREDICT_BATCH          │ 批量预测                                       │
    │ PREDICT_COMPARE        │ 预测方法对比                                   │
    │ SCHEDULE_OPTIMIZE      │ 排程优化                                       │
    │ SCHEDULE_FEASIBILITY   │ 可行性检查                                     │
    │ CHAT_MESSAGE           │ AI对话                                         │
    │ WHATIF_SIMULATE        │ What-if模拟                                    │
    │ IMPACT_ANALYZE         │ 影响分析                                       │
    └────────────────────────┴────────────────────────────────────────────────┘

步骤4: 创建待处理结果
    _integration.create_pending_result(request_id, request_type, callback_url)
    → AIResult
    文件: integration/persistence.py, 行号 230
    实现:
        result = AIResult(request_id=..., request_type=...,
                         status=ResultStatus.PENDING, callback_url=...)
        self.save_result(result)
        RETURN result

步骤5: 处理请求 (核心分发)
    await _process_request(req_type: RequestType, data: Dict) → Dict
    文件: integration/gateway.py, 行号 315
    
    [_process_request 实现]
    
    IF req_type == PREDICT_DEMAND:
        1. 获取参数: item_code, forecast_days, method
        2. 获取历史数据: _generate_mock_data(item_code)
        3. 方法选择: auto_select_algorithm(historical_data)
        4. 创建引擎: create_forecast_engine(method)
        5. 训练: engine.fit(historical_data)
        6. 预测: engine.forecast(forecast_days, 0.95)
        7. 返回: {"item_code": ..., "method": ..., "forecast": [...]}
    
    ELIF req_type == PREDICT_SAFETY_STOCK:
        1. 获取参数: item_code, lead_time_days, service_level
        2. 获取需求数据: _generate_demand_data(item_code)
        3. 创建引擎: engine = SafetyStockEngine()
        4. 计算: engine.calculate(item_code=..., lead_time_days=..., service_level=...)
        5. 返回: {"item_code": ..., "safety_stock": ..., "reorder_point": ..., ...}
    
    ELIF req_type == SCHEDULE_OPTIMIZE:
        1. 获取参数: orders, resources, goal
        2. 创建排程器: create_scheduler(orders, resources, goal)
        3. 执行优化: result = scheduler.optimize()
        4. 返回: {"schedule_id": ..., "status": ..., "makespan_hours": ..., ...}
    
    ELIF req_type == CHAT_MESSAGE:
        1. 转发到 chat.py::chat()
        2. 返回对话结果
    
    ELSE:
        RAISE ValueError(f"Unsupported request type: {req_type}")

步骤6: 标记结果完成
    _integration.complete_result(request_id, data, processing_time_ms) → bool
    文件: integration/persistence.py, 行号 260
    实现:
        result = self.update_result(request_id, status=ResultStatus.COMPLETED,
                                   data=data, completed_at=datetime.now(), ...)
        IF result AND result.callback_url:
            self.trigger_callback(result)

步骤7: 触发回调 (可选)
    _callback_manager.trigger_callback(result)
    文件: integration/persistence.py, 行号 180
    实现:
        IF NOT result.callback_url: RETURN
        self._queue.put(result)
        IF NOT self._running: self._start_worker()

步骤8: 返回响应
    GatewayResponse(request_id=..., code=200, success=True, data=data, ...)

================================================================================
                           完整数据模型
================================================================================

【输入模型】

GatewayRequest
├── type: str                       # 请求类型 (必需)
├── data: Dict[str, Any] = {}     # 请求数据
├── callback_url: Optional[str]     # 回调URL
└── request_id: Optional[str]     # 请求ID

GatewayResponse
├── request_id: str
├── code: int                       # 200=成功, 400=错误, 500=服务器错误
├── message: str
├── success: bool
├── data: Optional[Dict]
├── error: Optional[Dict]
├── processing_time_ms: int
└── status: Optional[str]          # pending | processing | completed | failed

【协议模型】

AIRequest - integration/protocol.py
├── request_id: str
├── timestamp: str
├── type: RequestType
├── data: Dict[str, Any]
├── callback: Optional[CallbackConfig]
├── app_id: Optional[str]
├── signature: Optional[str]
├── timeout: int = 60
├── priority: int = 5
└── metadata: Optional[Dict]

CallbackConfig
├── url: str                        # 回调URL
├── method: str = "POST"
├── token: Optional[str]             # 认证Token
├── retry: int = 3                # 重试次数 [0-5]
└── timeout: int = 30             # 超时秒数 [5-60]

【持久化模型】

AIResult - integration/persistence.py
├── request_id: str
├── request_type: str
├── status: ResultStatus            # PENDING | PROCESSING | COMPLETED | FAILED | TIMEOUT
├── data: Optional[Dict]
├── error: Optional[Dict]
├── created_at: datetime
├── updated_at: datetime
├── completed_at: Optional[datetime]
├── processing_time_ms: int
├── callback_url: Optional[str]
├── callback_status: Optional[str]
├── retry_count: int
└── metadata: Dict[str, Any]

ResultStatus (Enum)
├── PENDING       # 待处理
├── PROCESSING    # 处理中
├── COMPLETED     # 已完成
├── FAILED        # 失败
└── TIMEOUT       # 超时
```

---

## 五、模型持久化流程

### 5.1 模型保存流程 POST /model/save

```
步骤1: Router 入口
    model.py::save_model(request: SaveModelRequest) → ApiResponse
    文件: app/router/model.py, 行号 ~60

步骤2: 解码模型数据
    base64.b64decode(request.model_data) → model_bytes

步骤3: 反序列化模型
    pickle.loads(model_bytes) 或 joblib.load(buffer) → model

步骤4: 保存模型
    store.save_model(model, model_name, ModelType(model_type), item_code, ...)
    → model_id
    文件: integration/model_service.py, 行号 130
    实现:
        1. 生成唯一ID: model_id = f"{model_type.value}_{item_code}_{uuid}"
        2. 保存文件: joblib.dump(model, file_path)
        3. 计算校验和: checksum = md5(file_path)
        4. 创建元数据: metadata = ModelMetadata(...)
        5. 保存: self._metadata[model_id] = metadata
        6. 持久化: self._save_metadata()

步骤5: 返回响应
    ApiResponse.success(data={"model_id": model_id}, message="模型保存成功")
```

### 5.2 模型加载流程 POST /model/load/{model_id}

```
步骤1: Router 入口
    model.py::load_model(model_id: str) → ApiResponse
    文件: app/router/model.py, 行号 ~90

步骤2: 加载模型
    store.load_model(model_id) → model
    文件: integration/model_service.py, 行号 170
    实现:
        1. 获取元数据: metadata = self._metadata[model_id]
        2. 验证文件: os.path.exists(metadata.file_path)
        3. 验证校验和: checksum == metadata.checksum
        4. 加载: model = joblib.load(file_path) 或 pickle.load(file)
        5. 更新访问时间: metadata.updated_at = datetime.now()

步骤3: 序列化返回
    pickle.dumps(model) 或 joblib.dump(buffer) → model_bytes
    base64.b64encode(model_bytes) → model_data

步骤4: 返回响应
    ApiResponse.success(data={model_id, model_data, metadata: {...}}, message="...")
```

---

## 六、场景管理流程

### 6.1 场景保存流程 POST /scenario/save

```
步骤1: Router 入口
    scenario.py::save_scenario(request: SaveScenarioRequest) → ApiResponse
    文件: app/router/scenario.py, 行号 ~60

步骤2: 构建场景数据
    scenario = WhatIfScenarioData(
        name=request.name,
        description=request.description,
        category=request.category,
        baseline_params=request.baseline_params,
        changed_params=request.changed_params,
        orders=request.orders,
        resources=request.resources,
        results=request.results,
        impact_analysis=request.impact_analysis
    )

步骤3: 保存场景
    store.save_scenario(scenario, tags, created_by) → scenario_id
    文件: integration/scenario_service.py, 行号 130
    实现:
        1. 生成ID: scenario_id = f"scenario_{uuid}"
        2. 保存文件: json.dump(scenario_data, file_path)
        3. 创建元数据: metadata = ScenarioMetadata(...)
        4. 持久化: self._metadata[scenario_id] = metadata

步骤4: 返回响应
    ApiResponse.success(data={"scenario_id": scenario_id}, message="场景保存成功")
```

### 6.2 场景对比流程 POST /scenario/compare

```
步骤1: Router 入口
    scenario.py::compare_scenarios(request: CompareScenariosRequest) → ApiResponse
    文件: app/router/scenario.py, 行号 ~200

步骤2: 对比场景
    store.compare_scenarios(scenario_ids) → comparison
    文件: integration/scenario_service.py, 行号 320
    实现:
        1. 加载所有场景: scenarios = [load_scenario(id) for id in scenario_ids]
        2. 构建对比数据:
           comparison = {
               "scenarios": [
                   {"scenario_id": ..., "name": ..., "results": ..., ...}
                   for ...
               ],
               "count": len(scenarios)
           }
        3. 更新状态: metadata.status = ScenarioStatus.COMPARED.value

步骤3: 返回响应
    ApiResponse.success(data=comparison, message="对比成功")
```

---

## 七、监控指标流程

### 7.1 记录指标 POST /metrics/forecast/record

```
步骤1: Router 入口
    metrics.py::record_forecast_metric(request) → ApiResponse
    文件: app/router/metrics.py, 行号 ~40

步骤2: 记录指标
    record_forecast_accuracy(item_code, forecast_date, actual_qty, predicted_qty, ...)
    文件: integration/metrics_service.py, 行号 350
    实现:
        1. 创建指标: metric = ForecastAccuracyMetric(...)
        2. 计算误差: error = actual_qty - predicted_qty
           mape = abs(error / actual_qty * 100)
        3. 保存: store.record_forecast_accuracy(metric)

步骤3: 返回响应
    ApiResponse.success(message="记录成功")
```

### 7.2 获取仪表盘 GET /metrics/dashboard

```
步骤1: Router 入口
    metrics.py::get_dashboard(days: int = 7) → ApiResponse
    文件: app/router/metrics.py, 行号 ~180

步骤2: 聚合数据
    store.get_forecast_accuracy(days)     # MAPE, RMSE统计
    store.get_schedule_efficiency(days)  # 完成率、延迟率
    store.get_system_performance(hours) # 响应时间、成功率
    store.get_trend("forecast", days)    # 趋势数据

步骤3: 构建仪表盘
    dashboard = {
        "forecast_accuracy": {
            "avg_mape": float,
            "avg_rmse": float,
            "count": int,
            "trend": [...]
        },
        "schedule_efficiency": {...},
        "system_performance": {...}
    }

步骤4: 返回响应
    ApiResponse.success(data=dashboard, message="获取成功")
```

---

*文档版本: 2.0 - 完整方法链记录*
