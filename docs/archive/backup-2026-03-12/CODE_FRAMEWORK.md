# AI MRP 代码框架设计

> **版本**：1.1  
> **日期**：2026-03-09  
> **理念**：DDD 领域驱动设计  
> **说明**：MVP + Pro 部分已完成，共 19 个后端模块

---

## 一、项目整体结构

```
AI-MRP/
├── ai-mrp-java/                    # Java 后端（Spring Boot）
│   ├── ai-mrp-api/                 # API 层（Controller）
│   ├── ai-mrp-application/         # 应用层（Application Service）
│   ├── ai-mrp-domain/              # 领域层（Domain Model）
│   │   ├── domain/                 # 领域实体
│   │   │   ├── demand/             # 需求领域
│   │   │   ├── bom/                # BOM 领域
│   │   │   ├── inventory/          # 库存领域
│   │   │   ├── mrp/                # MRP 领域
│   │   │   └── ...
│   │   ├── repository/             # 仓储接口
│   │   ├── service/                # 领域服务
│   │   └── event/                  # 领域事件
│   ├── ai-mrp-infrastructure/      # 基础设施层
│   │   ├── persistence/            # 持久化实现
│   │   ├── mq/                     # 消息队列
│   │   └── external/                # 外部集成
│   └── ai-mrp-common/              # 公共模块
│
├── ai-mrp-python/                  # Python AI 服务
│   ├── app/
│   │   ├── api/                    # FastAPI 路由
│   │   ├── services/               # AI 服务
│   │   │   ├── llm/                # LLM 对话
│   │   │   ├── or_solver/         # OR 求解
│   │   │   ├── predictor/          # 预测
│   │   │   └── agent/              # Agent 编排
│   │   └── models/                 # 数据模型
│   ├── ortools/                    # OR-Tools 求解器
│   ├── prophet/                     # 时序预测
│   └── requirements.txt
│
├── ai-mrp-web/                     # React 前端
│   ├── src/
│   │   ├── pages/                  # 页面
│   │   ├── components/              # 组件
│   │   ├── services/                # API 调用
│   │   ├── stores/                  # Zustand 状态
│   │   ├── hooks/                   # 自定义 Hooks
│   │   └── utils/                   # 工具
│   └── package.json
│
└── docs/                           # 文档
```

---

## 二、Java 后端 DDD 架构

### 2.1 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│                  (Controller / VO / DTO)                    │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                        │
│                  (Application Service / DTO)                │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│            (Entity / Value Object / Domain Service)         │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                       │
│           (Repository / MyBatis / MQ / External)           │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 领域模型设计

#### 2.2.1 需求领域（Demand）

```
ai-mrp-domain/
└── demand/
    ├── entity/
    │   ├── SalesOrder.java         # 销售订单（聚合根）
    │   ├── OrderLine.java          # 订单明细
    │   └── DemandForecast.java     # 需求预测
    ├── valueobject/
    │   ├── OrderNo.java            # 订单号（值对象）
    │   ├── Priority.java           # 优先级
    │   └── DueDate.java            # 交货期
    ├── repository/
    │   └── SalesOrderRepository.java  # 仓储接口
    ├── service/
    │   └── DemandMergeService.java    # 领域服务：需求合并
    └── event/
        └── OrderCreatedEvent.java     # 领域事件
```

#### 2.2.2 BOM 领域

```
ai-mrp-domain/
└── bom/
    ├── entity/
    │   ├── BOM.java                 # BOM 主数据（聚合根）
    │   └── BOMLine.java            # BOM 明细
    ├── valueobject/
    │   ├── ItemCode.java           # 物料编码
    │   ├── Quantity.java           # 数量
    │   └── LossRate.java           # 损耗率
    ├── repository/
    │   └── BOMRepository.java
    └── service/
        └── BOMExpandService.java   # BOM 展开服务
```

#### 2.2.3 库存领域

```
ai-mrp-domain/
└── inventory/
    ├── entity/
    │   ├── InventoryItem.java      # 库存项（聚合根）
    │   └── InventoryTransaction.java  # 库存事务
    ├── valueobject/
    │   ├── WarehouseCode.java      # 仓库编码
    │   └── Quantity.java           # 数量
    ├── repository/
    │   └── InventoryRepository.java
    └── service/
        └── InventoryAllocateService.java  # 库存分配服务
```

#### 2.2.4 MRP 领域

```
ai-mrp-domain/
└── mrp/
    ├── entity/
    │   ├── MRPParam.java           # MRP 计算参数
    │   ├── MRPResult.java         # MRP 计算结果
    │   ├── NetRequirement.java    # 净需求
    │   └── PlanOrder.java         # 计划订单
    ├── valueobject/
    │   ├── MRPPolicy.java         # MRP 策略
    │   └── LeadTime.java          # 提前期
    ├── repository/
    │   ├── MRPRepository.java
    │   └── PlanOrderRepository.java
    ├── service/
    │   ├── MRPCalculateService.java    # MRP 计算服务
    │   └── PlanOrderService.java       # 计划订单服务
    └── event/
        └── MRPCompletedEvent.java      # MRP 完成事件
```

---

## 三、代码规范

### 3.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| **包名** | 小写，层级用 `.` 分隔 | `com.aimrp.domain.demand.entity` |
| **类名** | 大驼峰 | `MR `SalesOrder`,PCalculateService` |
| **接口名** | 大驼峰 + `I` 前缀或 `Repository/Service` 后缀 | `ISalesOrderService`, `SalesOrderRepository` |
| **方法名** | 小驼峰 | `calculate()`, `findByCode()` |
| **常量** | 全大写下划线 | `ORDER_STATUS_PENDING` |
| **枚举** | 大驼峰 | `OrderStatusEnum` |

### 3.2 代码注释规范

```java
/**
 * 销售订单聚合根
 * 
 * 负责：订单创建、状态变更、订单明细管理
 *
 * @author AI MRP Team
 * @since 1.0
 */
public class SalesOrder {
    
    /**
     * 订单号，唯一标识
     */
    private OrderNo orderNo;
    
    /**
     * 客户编码
     * 非空，订单创建时必填
     */
    @NotBlank(message = "客户编码不能为空")
    private String customerCode;
    
    /**
     * 创建销售订单
     * 
     * 业务规则：
     * 1. 订单号自动生成
     * 2. 优先级默认 5（1-10，1 最高）
     * 3. 状态默认为 PENDING
     *
     * @param customerCode 客户编码
     * @param lines 订单明细
     * @return 创建的订单
     */
    public static SalesOrder create(String customerCode, List<OrderLine> lines) {
        // 业务校验
        Assert.notEmpty(lines, "订单明细不能为空");
        
        // 构建订单
        SalesOrder order = new SalesOrder();
        order.orderNo = OrderNo.generate();
        order.customerCode = customerCode;
        order.lines = lines;
        order.status = OrderStatus.PENDING;
        order.priority = Priority.DEFAULT;
        order.createdAt = LocalDateTime.now();
        
        // 返回
        return order;
    }
    
    /**
     * 确认订单
     * 
     * 状态流转：PENDING -> CONFIRMED
     * 
     * @throws IllegalStateException 当前状态不允许确认
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("只有待确认状态的订单才能确认");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

### 3.3 DDD 注释标记

| 标记 | 说明 | 示例 |
|------|------|------|
| `@Aggregate` | 聚合根 | 标记 SalesOrder 为聚合根 |
| `@Entity` | 实体 | 实体类 |
| `@ValueObject` | 值对象 | 不可变对象 |
| `@DomainService` | 领域服务 | 复杂业务逻辑 |
| `@Repository` | 仓储 | 数据访问接口 |
| `@DomainEvent` | 领域事件 | 业务事件 |

---

## 四、Python AI 服务结构

### 4.1 目录结构

```
ai-mrp-python/
├── app/
│   ├── __init__.py
│   ├── main.py                      # FastAPI 入口
│   ├── api/
│   │   ├── __init__.py
│   │   ├── chat.py                  # 对话 API
│   │   ├── schedule.py              # 排程 API
│   │   └── predict.py               # 预测 API
│   ├── services/
│   │   ├── __init__.py
│   │   ├── llm/
│   │   │   ├── __init__.py
│   │   │   ├── chat_service.py      # 对话服务
│   │   │   ├── prompt_manager.py    # Prompt 管理
│   │   │   └── intent_classifier.py # 意图识别
│   │   ├── or_solver/
│   │   │   ├── __init__.py
│   │   │   ├── scheduler.py         # 排程求解器
│   │   │   └── models.py            # 数学模型
│   │   ├── predictor/
│   │   │   ├── __init__.py
│   │   │   ├── demand_predictor.py  # 需求预测
│   │   │   └── inventory_predictor.py # 库存预测
│   │   └── agent/
│   │       ├── __init__.py
│   │       ├── orchestrator.py      # Agent 编排
│   │       └── tasks/               # 子任务
│   └── models/
│       ├── __init__.py
│       ├── request.py               # 请求模型
│       └── response.py              # 响应模型
│
├── ortools/                         # OR-Tools 求解器
│   ├── __init__.py
│   ├── jobshop_solver.py           # 车间调度
│   └── lot_sizing.py               # 批量优化
│
├── prophet/                         # 预测模型
│   ├── __init__.py
│   └── demand_model.py
│
├── requirements.txt
├── Dockerfile
└── README.md
```

### 4.2 Python 代码规范

```python
"""
AI MRP 对话服务模块

提供基于 LangChain 的智能对话能力

主要功能：
- 意图识别
- 实体提取
- 对话上下文管理

Author: AI MRP Team
Version: 1.0
"""

from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field
from langchain import LLMChain
from langchain.prompts import PromptTemplate


class ChatRequest(BaseModel):
    """
    对话请求模型
    
    Attributes:
        message: 用户消息
        session_id: 会话 ID
        context: 额外上下文
    """
    message: str = Field(..., description="用户消息")
    session_id: str = Field(..., description="会话 ID")
    context: Optional[Dict[str, Any]] = Field(default=None, description="额外上下文")


class ChatResponse(BaseModel):
    """
    对话响应模型
    
    Attributes:
        reply: 回复消息
        intent: 识别的意图
        entities: 提取的实体
    """
    reply: str = Field(..., description="回复消息")
    intent: str = Field(..., description="识别的意图")
    entities: Dict[str, Any] = Field(default_factory=dict, description="提取的实体")


class ChatService:
    """
    对话服务类
    
    提供智能对话能力，支持多轮对话和上下文记忆
    
    Example:
        >>> service = ChatService()
        >>> response = service.chat(ChatRequest(
        ...     message="帮我查一下A物料的库存",
        ...     session_id="session_001"
        ... ))
        >>> print(response.reply)
    """
    
    def __init__(self, llm_provider: str = "openai"):
        """
        初始化对话服务
        
        Args:
            llm_provider: LLM 提供商，可选 "openai" / "deepseek"
        """
        self.llm_provider = llm_provider
        self.llm = self._init_llm()
        self.chain = self._init_chain()
    
    def _init_llm(self):
        """初始化 LLM"""
        # TODO: 支持多 provider
        pass
    
    def _init_chain(self) -> LLMChain:
        """初始化对话链"""
        prompt = PromptTemplate.from_template(
            "你是 AI MRP 智能助手，帮助用户管理物料需求计划。\n"
            "用户问题：{question}\n"
            "上下文：{context}\n"
            "请给出专业的回答。"
        )
        return LLMChain(llm=self.llm, prompt=prompt)
    
    def chat(self, request: ChatRequest) -> ChatResponse:
        """
        处理对话请求
        
        Args:
            request: 对话请求
            
        Returns:
            对话响应
        """
        # 1. 意图识别
        intent = self._recognize_intent(request.message)
        
        # 2. 实体提取
        entities = self._extract_entities(request.message)
        
        # 3. 构建上下文
        context = self._build_context(request.session_id, request.context)
        
        # 4. 调用 LLM
        reply = self.chain.run(
            question=request.message,
            context=context
        )
        
        # 5. 返回结果
        return ChatResponse(
            reply=reply,
            intent=intent,
            entities=entities
        )
```

---

## 五、前端目录结构

```
ai-mrp-web/
├── src/
│   ├── api/                        # API 定义
│   │   ├── demand.ts               # 需求 API
│   │   ├── bom.ts                  # BOM API
│   │   ├── inventory.ts            # 库存 API
│   │   └── mrp.ts                  # MRP API
│   │
│   ├── pages/                      # 页面
│   │   ├── Layout.tsx              # 布局
│   │   ├── Dashboard/              # 首页
│   │   ├── Demand/                 # 需求管理
│   │   │   ├── OrderList.tsx       # 订单列表
│   │   │   └── OrderCreate.tsx     # 创建订单
│   │   ├── BOM/                    # BOM 管理
│   │   ├── Inventory/              # 库存管理
│   │   ├── MRP/                    # MRP 计算
│   │   └── Chat/                   # AI 对话
│   │
│   ├── components/                 # 公共组件
│   │   ├── common/                 # 通用组件
│   │   │   ├── DataTable.tsx      # 数据表格
│   │   │   └── SearchBar.tsx      # 搜索栏
│   │   └── mrp/                   # MRP 组件
│   │       ├── MRPResult.tsx      # MRP 结果
│   │       └── GanttChart.tsx     # 甘特图
│   │
│   ├── stores/                     # Zustand 状态
│   │   ├── useOrderStore.ts       # 订单状态
│   │   ├── useInventoryStore.ts   # 库存状态
│   │   └── useChatStore.ts        # 对话状态
│   │
│   ├── hooks/                      # 自定义 Hooks
│   │   ├── useMRP.ts              # MRP 计算
│   │   └── useChat.ts             # 对话
│   │
│   └── utils/                      # 工具
│       ├── request.ts             # Axios 封装
│       └── format.ts               # 格式化
│
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

## 六、代码注释示例

### 6.1 Controller 层

```java
/**
 * 需求管理控制器
 * 
 * 提供订单、预测等需求的 RESTful API
 *
 * @author AI MRP Team
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/demand")
@RequiredArgsConstructor
public class DemandController {
    
    private final DemandApplicationService applicationService;
    
    /**
     * 创建销售订单
     *
     * @param request 创建请求
     * @return 订单信息
     */
    @PostMapping("/orders")
    public R<OrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderVO order = applicationService.createOrder(request);
        return R.ok(order);
    }
    
    /**
     * 查询订单列表
     *
     * @param query 查询条件
     * @return 订单列表
     */
    @GetMapping("/orders")
    public R<Page<OrderVO>> listOrders(OrderQuery query) {
        Page<OrderVO> result = applicationService.listOrders(query);
        return R.ok(result);
    }
}
```

### 6.2 Application Service 层

```java
/**
 * 需求应用服务
 * 
 * 负责需求领域业务流程编排，事务管理
 *
 * @author AI MRP Team
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DemandApplicationService {
    
    private final SalesOrderRepository orderRepository;
    private final MRPApplicationService mrpService;
    
    /**
     * 创建销售订单
     *
     * 业务流程：
     * 1. 校验请求参数
     * 2. 创建领域对象
     * 3. 保存到仓储
     * 4. 发布领域事件
     *
     * @param request 创建请求
     * @return 订单视图对象
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderRequest request) {
        // 1. 参数校验
        validateRequest(request);
        
        // 2. 创建领域对象
        List<OrderLine> lines = request.getLines().stream()
            .map(this::toOrderLine)
            .collect(Collectors.toList());
        
        SalesOrder order = SalesOrder.create(
            request.getCustomerCode(),
            lines
        );
        
        // 3. 保存
        orderRepository.save(order);
        
        // 4. 发布事件
        applicationEventPublisher.publishEvent(
            new OrderCreatedEvent(order.getId(), order.getOrderNo())
        );
        
        log.info("创建销售订单成功，订单号：{}", order.getOrderNo());
        
        // 5. 返回
        return OrderVO.from(order);
    }
}
```

### 6.3 Repository 接口

```java
/**
 * 销售订单仓储接口
 * 
 * 定义订单的持久化操作，由基础设施层实现
 *
 * @author AI MRP Team
 * @since 1.0
 */
public interface SalesOrderRepository {
    
    /**
     * 根据 ID 查询订单
     *
     * @param id 订单 ID
     * @return 订单（不存在返回 null）
     */
    SalesOrder findById(Long id);
    
    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    SalesOrder findByOrderNo(String orderNo);
    
    /**
     * 保存订单
     *
     * @param order 订单
     */
    void save(SalesOrder order);
    
    /**
     * 分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<SalesOrder> findByPage(OrderQuery query);
}
```

---

*代码框架设计完成*
