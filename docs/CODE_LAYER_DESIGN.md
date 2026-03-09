# AI MRP 代码分层设计

> **版本**：1.1  
> **日期**：2026-03-09  
> **说明**：基于 DDD + 模块化架构的代码分层设计 v1.1（增强 DDD 建模）

---

## 一、整体目录结构

```
AI-MRP/
├── code/
│   ├── backend/                    # Java 后端
│   │   ├── aimrp-core/            # 核心 domain 模块
│   │   ├── aimrp-demand/          # 需求管理模块
│   │   ├── aimrp-forecast/        # 预测模块
│   │   ├── aimrp-bom/            # BOM 模块
│   │   ├── aimrp-inventory/      # 库存模块
│   │   ├── aimrp-mrp/            # MRP 计算模块
│   │   ├── aimrp-purchase/       # 采购模块
│   │   ├── aimrp-production/     # 生产模块
│   │   ├── aimrp-risk/           # 风险预警模块
│   │   ├── aimrp-sandbox/        # 沙箱模块
│   │   ├── aimrp-whatif/         # What-if 模块
│   │   ├── aimrp-conversation/   # 对话服务模块
│   │   ├── aimrp-api/            # API 网关入口
│   │   └── aimrp-common/         # 公共模块
│   │
│   ├── ai-service/                # Python AI 微服务
│   │   ├── app/                  # FastAPI 应用
│   │   ├── agents/               # Agent 编排
│   │   ├── llm/                  # LLM 调用
│   │   ├── or_solver/            # OR 求解器
│   │   ├── predictor/            # 预测引擎
│   │   ├── knowledge/            # RAG 知识库
│   │   └── services/             # 业务服务
│   │
│   └── frontend/                  # React 前端
│       ├── apps/
│       │   └── admin/            # 管理后台
│       ├── packages/
│       │   ├── ui/               # UI 组件库
│       │   ├── hooks/            # 公共 Hooks
│       │   ├── services/         # API 调用
│       │   └── utils/            # 工具函数
│       └── tools/                # 构建工具
```

---

## 附录 A：架构决策记录（ADR）

### A.1 技术选型说明

| 技术 | 状态 | 说明 |
|------|------|------|
| **状态机** | 🔶 备选 | 当前业务使用简单的状态字段管理，如需复杂状态流转（如图审批流）时可引入 Spring Statemachine |
| **事件驱动架构** | 🔶 备选 | 当前模块间通过直接调用通信，如需解耦和异步处理（如订单创建后触发库存检查）时可引入 Spring Event + MQ |

### A.2 备选技术引入原则

> **原则**：不要过度设计。当前 MVP 阶段以快速交付为目标，复杂技术引入需满足以下条件：
> 1. 业务复杂度确实需要（如审批流、多步骤流程）
> 2. 性能瓶颈确实存在（如高并发异步处理）
> 3. 团队有能力维护

**引入触发条件**：
- 状态机：当业务状态 > 5 种，或状态流转规则复杂时
- 事件驱动：当模块间需要异步通信，或需要事务最终一致性时

---

## 二、Java 后端分层架构

### 2.1 模块分层结构（单模块示例）

```
aimrp-demand/                          # 需求管理模块
├── src/main/java/com/aimrp/demand/
│   ├── domain/                       # 领域层
│   │   ├── entity/                   # 实体
│   │   │   ├── SalesOrder.java
│   │   │   ├── SalesOrderLine.java
│   │   │   ├── DemandPool.java
│   │   │   └── DemandSource.java
│   │   ├── valueobject/              # 值对象
│   │   │   ├── OrderPriority.java
│   │   │   └── DemandStatus.java
│   │   ├── repository/               # 仓储接口
│   │   │   ├── SalesOrderRepository.java
│   │   │   └── DemandPoolRepository.java
│   │   └── service/                  # 领域服务
│   │       └── DemandMergeService.java
│   │
│   ├── application/                  # 应用层
│   │   ├── dto/                      # 数据传输对象
│   │   │   ├── SalesOrderDTO.java
│   │   │   ├── CreateOrderRequest.java
│   │   │   └── DemandPoolVO.java
│   │   ├── command/                  # 命令
│   │   │   ├── CreateOrderCommand.java
│   │   │   └── UpdateOrderCommand.java
│   │   ├── query/                    # 查询
│   │   │   ├── OrderListQuery.java
│   │   │   └── DemandPoolQuery.java
│   │   ├── service/                  # 应用服务
│   │   │   ├── SalesOrderApplicationService.java
│   │   │   └── DemandPoolApplicationService.java
│   │   └── listener/                 # 事件监听
│   │       └── OrderCreatedListener.java
│   │
│   ├── infrastructure/                # 基础设施层
│   │   ├── persistence/              # 持久化
│   │   │   ├── mapper/               # MyBatis Mapper
│   │   │   │   ├── SalesOrderMapper.java
│   │   │   │   └── SalesOrderMapper.xml
│   │   │   └── repository/           # 仓储实现
│   │   │       └── SalesOrderRepositoryImpl.java
│   │   ├── mq/                        # 消息队列
│   │   │   └── OrderEventPublisher.java
│   │   └── config/                    # 配置
│   │       └── DemandModuleConfig.java
│   │
│   ├── api/                          # 接口层
│   │   ├── controller/               # REST 控制器
│   │   │   ├── SalesOrderController.java
│   │   │   └── DemandPoolController.java
│   │   ├── dto/                      # API 返回对象
│   │   │   └── ApiResponse.java
│   │   └── assembler/                # 对象转换
│   │       └── OrderAssembler.java
│   │
│   └── DemandApplication.java        # 启动类
```

---

### 2.2 分层职责说明

| 层级 | 职责 | 依赖 |
|------|------|------|
| **domain** | 业务实体、值对象、领域事件、领域服务 | 无（核心） |
| **application** | 命令/查询处理、事务编排、事件发布 | domain |
| **infrastructure** | 持久化、MQ、第三方服务调用 | domain |
| **api** | HTTP 入口、参数校验、响应封装 | application |

---

### 2.3 核心模块清单

| 模块 | 模块名 | 功能 |
|------|--------|------|
| 公共模块 | aimrp-common | 通用工具、异常、响应 |
| 核心域 | aimrp-core | 公共域模型、事件总线 |
| 需求管理 | aimrp-demand | 销售订单、需求池 |
| 预测 | aimrp-forecast | 需求预测 |
| BOM | aimrp-bom | BOM 管理、BOM 展开 |
| 库存 | aimrp-inventory | 库存、库存事务 |
| MRP | aimrp-mrp | MRP 计算引擎 |
| 采购 | aimrp-purchase | 采购订单 |
| 生产 | aimrp-production | 生产工单、报工 |
| 风险 | aimrp-risk | 风险预警 |
| 沙箱 | aimrp-sandbox | 沙箱会话 |
| What-if | aimrp-whatif | 场景模拟 |
| 对话 | aimrp-conversation | 对话式交互 |
| API | aimrp-api | 网关入口 |

---

## 三、DDD 领域建模增强

### 2.4 对象类型定义

| 类型 | 缩写 | 说明 | 示例 |
|------|------|------|------|
| **DO** | Domain Object | 领域实体，有唯一标识 | `SalesOrder` |
| **VO** | Value Object | 值对象，无标识，不可变 | `Money`, `Address` |
| **BO** | Business Object | 业务对象，封装业务规则 | `MrpResult` |
| **DTO** | Data Transfer Object | 数据传输对象 | `SalesOrderDTO` |
| **PO** | Persistent Object | 持久化对象，与表一对一 | `SalesOrderPO` |
| **VO(API)** | View Object | API 返回视图对象 | `OrderListVO` |
| **CO** | Command Object | 命令对象 | `CreateOrderCommand` |
| **QO** | Query Object | 查询对象 | `OrderQuery` |

### 2.5 领域对象分层结构

```
aimrp-demand/
├── domain/
│   ├── entity/                      # DO - 领域实体（聚合根 + 实体）
│   │   ├── SalesOrder.java         # 聚合根
│   │   ├── SalesOrderLine.java     # 实体（聚合内）
│   │   └── DemandPool.java         # 聚合根
│   │
│   ├── valueobject/                 # VO - 值对象
│   │   ├── OrderPriority.java
│   │   ├── OrderStatus.java
│   │   ├── Money.java              # 货币值对象
│   │   ├── Quantity.java          # 数量值对象
│   │   └── Address.java
│   │
│   ├── aggregate/                   # 聚合根定义
│   │   ├── SalesOrderAggregate.java
│   │   └── DemandPoolAggregate.java
│   │
│   ├── repository/                  # 仓储接口
│   │   ├── SalesOrderRepository.java
│   │   └── DemandPoolRepository.java
│   │
│   ├── service/                     # 领域服务
│   │   ├── DemandMergeService.java
│   │   └── OrderValidationService.java
│   │
│   └── event/                       # 领域事件
│       ├── OrderCreatedEvent.java
│       ├── OrderConfirmedEvent.java
│       └── DemandChangedEvent.java
│
├── application/
│   ├── command/                     # CO - 命令对象
│   │   ├── CreateOrderCommand.java
│   │   ├── ConfirmOrderCommand.java
│   │   └── CancelOrderCommand.java
│   │
│   ├── query/                       # QO - 查询对象
│   │   ├── OrderListQuery.java
│   │   └── OrderDetailQuery.java
│   │
│   ├── dto/                         # DTO - 数据传输
│   │   ├── SalesOrderDTO.java
│   │   └── SalesOrderLineDTO.java
│   │
│   ├── service/                     # 应用服务
│   │   └── SalesOrderApplicationService.java
│   │
│   └── handler/                     # 命令/查询处理器
│       ├── CreateOrderHandler.java
│       └── OrderQueryHandler.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── po/                     # PO - 持久化对象
│   │   │   ├── SalesOrderPO.java
│   │   │   └── SalesOrderLinePO.java
│   │   │
│   │   ├── mapper/                  # MyBatis Mapper
│   │   │   ├── SalesOrderMapper.java
│   │   │   └── SalesOrderMapper.xml
│   │   │
│   │   └── repository/             # 仓储实现
│   │       └── SalesOrderRepositoryImpl.java
│   │
│   ├── cache/                       # Redis 缓存
│   │   ├── config/                  # 缓存配置
│   │   │   └── CacheConfig.java
│   │   ├── strategy/               # 缓存策略
│   │   │   ├── CausalCacheStrategy.java
│   │   │   └── TtlCacheStrategy.java
│   │   └── handler/                # 缓存处理
│   │       └── OrderCacheHandler.java
│   │
│   ├── mq/                          # 消息队列
│   │   ├── config/                  # MQ 配置
│   │   │   └── RabbitMQConfig.java
│   │   ├── producer/               # 生产者
│   │   │   └── OrderEventProducer.java
│   │   ├── consumer/               # 消费者
│   │   │   └── OrderEventConsumer.java
│   │   └── message/                # 消息定义
│   │       ├── OrderCreatedMessage.java
│   │       └── MrpTriggerMessage.java
│   │
│   └── external/                    # 外部服务
│       ├── feign/                   # Feign 客户端
│       │   └── AIFeignClient.java
│       └── config/                  # 配置
│
└── api/
    ├── controller/
    │   ├── command/                 # 命令式 Controller
    │   │   └── SalesOrderCommandController.java
    │   └── query/                  # 查询式 Controller
    │       └── SalesOrderQueryController.java
    │
    ├── dto/                         # VO(API) - API 返回视图
    │   ├── OrderListVO.java
    │   └── OrderDetailVO.java
    │
    ├── assembler/                   # 对象转换
    │   └── OrderAssembler.java
    │
    └── factory/                     # DTO 工厂
        └── OrderDTOFactory.java
```

### 2.6 聚合根定义示例

```java
/**
 * 销售订单聚合根
 */
@Aggregate
public class SalesOrder {
    
    @AggregateId
    private Long id;
    private String orderNo;
    private String customerCode;
    private List<SalesOrderLine> lines;
    private OrderStatus status;
    private Money totalAmount;
    
    // 聚合工厂方法
    public static SalesOrder create(CreateOrderCommand cmd) {
        // 业务校验
        Assert.notNull(cmd.getCustomerCode(), "客户不能为空");
        
        SalesOrder order = new SalesOrder();
        order.id = IdGenerator.generate();
        order.orderNo = generateOrderNo();
        order.customerCode = cmd.getCustomerCode();
        order.lines = cmd.getLines().stream()
            .map(SalesOrderLine::create)
            .collect(Collectors.toList());
        order.status = OrderStatus.PENDING;
        order.totalAmount = order.calculateTotal();
        
        // 发布领域事件
        order.addDomainEvent(new OrderCreatedEvent(order));
        
        return order;
    }
    
    // 领域行为
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException("只有待确认订单可确认");
        }
        this.status = OrderStatus.CONFIRMED;
        this.addDomainEvent(new OrderConfirmedEvent(this));
    }
    
    public void updateQuantity(Long lineId, BigDecimal newQty) {
        // 聚合内一致性保证
    }
}
```

### 2.7 值对象示例

```java
/**
 * 货币值对象 - 不可变
 */
@ValueObject
public class Money {
    private final BigDecimal amount;
    private final String currency;
    
    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money add(Money other) {
        Assert.equals(this.currency, other.currency, "货币必须一致");
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }
}

/**
 * 数量值对象
 */
@ValueObject
public class Quantity {
    private final BigDecimal value;
    private final String unit;
    
    public Quantity(BigDecimal value, String unit) {
        this.value = value;
        this.unit = unit;
    }
}
```

---

## 四、Redis 缓存策略

### 4.1 缓存层次设计

| 缓存类型 | 过期策略 | 场景 | 示例 |
|----------|----------|------|------|
| **本地缓存** | TTL 5min | 热点数据 | 用户菜单、字典 |
| **分布式缓存** | TTL 30min | 业务数据 | 订单详情、物料 |
| **因果缓存** | 事件驱动 | 关联数据 | 订单 → 客户 |
| **延迟缓存** | 写后 TTL | 更新频繁 | 库存数量 |

### 4.2 缓存 Key 规范

```
# 格式: {前缀}:{业务}:{维度}:{ID}

# 示例
aimrp:item:code:A001          # 物料详情
aimrp:order:id:12345          # 订单详情
aimrp:inventory:wh:WH01:item:A001  # 仓库库存
aimrp:user:session:abc123     # 用户会话
```

### 6.3 缓存实现

```java
@Component
public class ItemCacheService {
    
    @Cacheable(value = "item", key = "#itemCode")
    public ItemDTO getItem(String itemCode) {
        return itemRepository.findByCode(itemCode)
            .map(ItemAssembler::toDTO)
            .orElse(null);
    }
    
    @CachePut(value = "item", key = "#result.itemCode")
    public ItemDTO saveItem(ItemDTO item) {
        // ...
    }
    
    @CacheEvict(value = "item", key = "#itemCode")
    public void deleteItem(String itemCode) {
        // ...
    }
    
    // 因果缓存 - 清除客户关联的订单缓存
    @Cascade(evict = {
        @CascadeKey("aimrp:order:customer:{customerCode}")
    })
    public void updateCustomerCode(String oldCode, String newCode) {
        // ...
    }
}
```

---

## 五、MyBatis-Plus 集成

### 5.1 MP 在 DDD 分层中的定位

| DDD 层 | MP 组件 | 说明 |
|--------|---------|------|
| **domain** | 无 | 纯 Java，不依赖 MP |
| **infrastructure** | `BaseMapper<T>` | 持久化接口 |
| **infrastructure** | `IService<T>` | 通用服务（可选） |
| **application** | `QueryWrapper` | 动态查询条件 |
| **application** | `Page<T>` | 分页查询 |

### 5.2 Infrastructure 层 MP 集成示例

```java
// ============== Infrastructure 层 ==============

/**
 * PO - 继承 MP 的 Model
 */
@TableName("t_sales_order")
public class SalesOrderPO extends Model<SalesOrderPO> {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_no")
    private String orderNo;
    
    @TableField("customer_code")
    private String customerCode;
    
    @TableField(exist = false)
    private List<SalesOrderLinePO> lines;  // 非数据库字段
}

/**
 * Mapper - 继承 BaseMapper
 */
public interface SalesOrderMapper extends BaseMapper<SalesOrderPO> {
    List<SalesOrderPO> selectByCustomerCode(@Param("customerCode") String customerCode);
}

/**
 * Repository 实现
 */
@Repository
public class SalesOrderRepositoryImpl implements SalesOrderRepository {
    
    @Autowired
    private SalesOrderMapper salesOrderMapper;
    
    @Override
    public Optional<SalesOrder> findById(Long id) {
        SalesOrderPO po = salesOrderMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }
    
    @Override
    public Page<SalesOrder> findByCondition(OrderQuery query) {
        QueryWrapper<SalesOrderPO> wrapper = new QueryWrapper<>();
        if (query.getCustomerCode() != null) {
            wrapper.eq("customer_code", query.getCustomerCode());
        }
        
        IPage<SalesOrderPO> page = salesOrderMapper.selectPage(
            new Page<>(query.getPageNum(), query.getPageSize()),
            wrapper
        );
        return page.convert(this::toDomain);
    }
}
```

### 5.3 MP 常用注解

| 注解 | 用在 | 说明 |
|------|------|------|
| `@TableName` | 类 | 表名映射 |
| `@TableId` | 字段 | 主键映射 |
| `@TableField` | 字段 | 字段映射 |
| `@TableLogic` | 字段 | 逻辑删除 |
| `@Version` | 字段 | 乐观锁 |

---

## 六、消息队列设计

| 消息类型 | Topic/Queue | 说明 | 场景 |
|----------|-------------|------|------|
| **领域事件** | aimrp.event.* | 领域内事件传播 | 订单创建、状态变更 |
| **MRP 触发** | aimrp.mrp.trigger | MRP 计算触发 | 需求变更触发重算 |
| **库存同步** | aimrp.inventory.sync | 库存变动同步 | 多仓库同步 |
| **风险预警** | aimrp.risk.alert | 风险消息推送 | 预警通知 |
| **AI 任务** | aimrp.ai.task | AI 任务队列 | 预测、排程任务 |

### 6.2 消息格式

```java
/**
 * 领域事件消息
 */
@Message
public class DomainEventMessage {
    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private Object payload;
    private Instant timestamp;
    private Map<String, String> headers;
}

/**
 * MRP 触发消息
 */
@Message
public class MrpTriggerMessage {
    private String triggerType;  // DEMAND_CHANGE, INVENTORY_CHANGE, MANUAL
    private List<String> itemCodes;
    private String sourceId;
    private Instant triggerTime;
}
```

### 6.3 消息生产者/消费者

```java
/**
 * 订单事件生产者
 */
@Component
public class OrderEventProducer {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreated(SalesOrder order) {
        OrderCreatedMessage message = OrderCreatedMessage.builder()
            .orderId(order.getId())
            .orderNo(order.getOrderNo())
            .customerCode(order.getCustomerCode())
            .totalAmount(order.getTotalAmount())
            .build();
        
        rabbitTemplate.convertAndSend(
            "aimrp.event.order",
            "created",
            message
        );
    }
}

/**
 * MRP 触发消费者
 */
@Component
public class MrpTriggerConsumer {
    
    @RabbitListener(queues = "aimrp.mrp.trigger")
    public void handleMrpTrigger(MrpTriggerMessage message) {
        log.info("收到 MRP 触发消息: {}", message);
        mrpApplicationService.runMrp(message);
    }
}
```

---

## 七、AI 服务插件化设计

### 5.1 设计目标

| 目标 | 说明 |
|------|------|
| **松耦合** | AI 服务独立，可插拔 |
| **多供应商** | 支持 OpenAI/DeepSeek/Ollama |
| **零侵入** | 切换供应商无需修改业务代码 |
| **SPI 扩展** | 通过 SPI 发现新实现 |

### 5.2 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    AI Service Client (插件化)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │            aimrp-ai-client (Starter)                     │  │
│  │  ┌─────────────────────────────────────────────────────┐│  │
│  │  │              AI Service Interface                   ││  │
│  │  │  ChatResponse chat(ChatRequest request);           ││  │
│  │  │  PredictResponse predict(PredictRequest request);  ││  │
│  │  │  ScheduleResponse schedule(ScheduleParam param);  ││  │
│  │  └─────────────────────────────────────────────────────┘│  │
│  │                           │                               │  │
│  │  ┌────────────────────────┼────────────────────────┐      │  │
│  │  ▼                        ▼                        ▼      │  │
│  │ ┌──────────┐       ┌──────────┐           ┌──────────┐  │  │
│  │ │ OpenAI  │       │ DeepSeek │           │ Ollama  │  │  │
│  │ │ Provider │       │ Provider │           │ Provider │  │  │
│  │ └──────────┘       └──────────┘           └──────────┘  │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.3 核心接口定义

```java
/**
 * AI 服务接口（核心抽象）
 */
@AIService(provider = "default")
public interface AIAgentService {
    
    /**
     * 对话交互
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * 需求预测
     */
    PredictResponse predict(PredictRequest request);
    
    /**
     * 生产排程优化
     */
    ScheduleResponse optimizeSchedule(ScheduleParam param);
    
    /**
     * 安全库存推荐
     */
    SafetyStockResponse recommendSafetyStock(SafetyStockParam param);
}

/**
 * AI 服务提供者注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AIProvider {
    String value();  // 提供者名称: openai, deepseek, ollama
}
```

### 5.4 多供应商实现

```java
/**
 * DeepSeek 实现（默认）
 */
@AIProvider("deepseek")
public class DeepSeekAgentService implements AIAgentService {
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        // 调用 DeepSeek API
    }
}

/**
 * OpenAI 实现
 */
@AIProvider("openai")
public class OpenAIAgentService implements AIAgentService {
    // 调用 OpenAI API
}

/**
 * Ollama 本地部署实现
 */
@AIProvider("ollama")
public class OllamaAgentService implements AIAgentService {
    // 调用 Ollama 本地 API
}
```

### 5.5 自动装配与策略选择

```java
/**
 * AI 服务工厂（策略模式 + 条件装配）
 */
@Configuration
public class AIServiceAutoConfiguration {
    
    @Autowired
    private Map<String, AIAgentService> agentServices;  // 自动注入所有实现
    
    @Value("${aimrp.ai.provider:deepseek}")
    private String activeProvider;
    
    @Bean
    @Primary
    public AIAgentService activeAgentService() {
        String beanName = activeProvider + "AgentService";
        AIAgentService service = agentServices.get(beanName);
        if (service == null) {
            throw new IllegalStateException("未找到 AI 服务提供者: " + activeProvider);
        }
        return service;
    }
}
```

### 5.6 SPI 机制扩展

```java
# META-INF/services/com.aimrp.ai.client.AIAgentService
# 通过 SPI 发现更多实现

com.aimrp.ai.client.impl.DeepSeekAgentService
com.aimrp.ai.client.impl.OpenAIAgentService
com.aimrp.ai.client.impl.OllamaAgentService
```

### 5.7 配置文件

```yaml
# application.yml
aimrp:
  ai:
    provider: deepseek  # 切换 AI 供应商
    
    deepseek:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      model: deepseek-chat
      timeout: 30000
    
    openai:
      base-url: https://api.openai.com
      api-key: ${OPENAI_API_KEY}
      model: gpt-4
```

### 5.8 使用方式

```java
// 应用层注入（无感知切换）
@Service
public class ConversationApplicationService {
    
    @Autowired
    private AIAgentService aiAgentService;  // 自动注入当前配置的实现
    
    public ChatResponse chat(String message) {
        ChatRequest request = ChatRequest.builder()
            .message(message)
            .context(getContext())
            .build();
        
        return aiAgentService.chat(request);  // 调用实际实现
    }
}
```

---

## 八、CQRS 命令查询分离

### 5.1 CQRS 架构

```
                    ┌─────────────────┐
                    │     API Layer   │
                    └────────┬────────┘
                             │
           ┌─────────────────┼─────────────────┐
           ▼                                   ▼
┌───────────────────────┐           ┌───────────────────────┐
│      Command Side    │           │      Query Side       │
│   (命令端 - 写)      │           │   (查询端 - 读)       │
├───────────────────────┤           ├───────────────────────┤
│ CommandController    │           │ QueryController       │
│          ↓           │           │          ↓            │
│ CommandHandler       │           │ QueryHandler          │
│          ↓           │           │          ↓            │
│ ApplicationService   │           │ ReadModelProjection  │
│          ↓           │           │          ↓            │
│ Domain Aggregate     │           │ Materialized View     │
│          ↓           │           │          ↓            │
│ Repository (PO)     │           │ Repository (PO)       │
└───────────────────────┘           └───────────────────────┘
           │                                   │
           └─────────────────┬─────────────────┘
                             ▼
                    ┌─────────────────┐
                    │   Event Bus     │
                    │ (事件同步)      │
                    └─────────────────┘
```

### 5.2 读模型投影

```java
/**
 * 订单列表读模型（物化视图同步）
 */
@Entity
@Table(name = "v_order_list")
public class OrderListView {
    
    @Id
    private Long orderId;
    private String orderNo;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDate orderDate;
    private LocalDate dueDate;
    
    // 冗余字段，用于快速查询
    private String statusName;
    private Integer overdueDays;
}

/**
 * 订单列表查询处理器
 */
@Component
public class OrderQueryHandler {
    
    public Page<OrderListVO> query(OrderListQuery query) {
        return orderReadRepository.findByCondition(query);
    }
}
```

---

## 九、模块间依赖关系

### 4.1 目录结构

```
ai-service/
├── app/
│   ├── main.py                      # FastAPI 启动
│   ├── config.py                    # 配置
│   └── router/                      # 路由
│       ├── chat.py                  # 对话路由
│       ├── predict.py               # 预测路由
│       └── schedule.py              # 排程路由
│
├── agents/                          # Agent 编排
│   ├── base.py                      # Agent 基类
│   ├── mrpa_agent.py               # MRP Agent
│   ├── order_agent.py              # 订单分析 Agent
│   ├── schedule_agent.py           # 排程 Agent
│   ├── risk_agent.py               # 风险分析 Agent
│   └── orchestrator.py             # 编排器
│
├── llm/                            # LLM 调用
│   ├── client.py                   # LLM 客户端
│   ├── prompt/                     # Prompt 模板
│   │   ├── chat_prompt.py
│   │   ├── intent_prompt.py
│   │   └── analysis_prompt.py
│   └── handler.py                   # 响应处理
│
├── or_solver/                      # OR 求解器
│   ├── base.py                      # 求解器基类
│   ├── scheduler.py                 # 排程优化
│   ├── lot_sizing.py                # 批量优化
│   └── vrp.py                      # 物流优化
│
├── predictor/                      # 预测引擎
│   ├── base.py                      # 预测器基类
│   ├── demand_forecaster.py         # 需求预测
│   ├── safety_stock.py             # 安全库存推荐
│   └── models/                     # 模型管理
│       ├── prophet_model.py
│       └── xgboost_model.py
│
├── knowledge/                     # RAG 知识库
│   ├── vector_store.py             # 向量存储
│   ├── document_loader.py          # 文档加载
│   └── retrieval.py                # 检索
│
├── services/                      # 业务服务
│   ├── conversation_service.py     # 对话服务
│   ├── prediction_service.py       # 预测服务
│   ├── whatif_service.py           # What-if 服务
│   └── risk_service.py             # 风险服务
│
└── utils/                         # 工具
    ├── logger.py                   # 日志
    ├── http_client.py              # HTTP 客户端
    └── date_utils.py               # 日期工具
```

### 4.2 分层职责

| 层级 | 职责 |
|------|------|
| **app/router** | HTTP 入口、参数校验 |
| **agents** | Agent 编排、任务分解 |
| **llm** | LLM 调用、Prompt 管理 |
| **or_solver** | 运筹学求解算法 |
| **predictor** | AI 预测模型 |
| **services** | 业务逻辑编排 |
| **knowledge** | RAG 知识检索 |

---

## 十、前端分层架构

### 6.1 目录结构

```
frontend/
├── apps/
│   └── admin/                      # 管理后台
│       ├── src/
│       │   ├── pages/              # 页面
│       │   │   ├── demand/          # 需求
│       │   │   │   ├── OrderList.tsx
│       │   │   │   └── OrderDetail.tsx
│       │   │   ├── bom/             # BOM
│       │   │   ├── inventory/        # 库存
│       │   │   ├── mrp/             # MRP
│       │   │   ├── production/       # 生产
│       │   │   ├── risk/            # 风险
│       │   │   └── ai/              # AI 对话
│       │   │
│       │   ├── components/         # 业务组件
│       │   │   ├── OrderForm.tsx
│       │   │   ├── BomTree.tsx
│       │   │   └── MrpGantt.tsx
│       │   │
│       │   ├── layouts/             # 布局
│       │   ├── routes/              # 路由
│       │   ├── stores/              # Zustand 状态
│       │   └── App.tsx
│       │
│       └── index.html
│
├── packages/
│   ├── ui/                         # UI 组件库
│   │   ├── Button/
│   │   ├── Table/
│   │   ├── Form/
│   │   └── Chat/
│   │
│   ├── hooks/                      # 公共 Hooks
│   │   ├── useOrder.ts
│   │   ├── useMrp.ts
│   │   └── useChat.ts
│   │
│   ├── services/                   # API 服务
│   │   ├── api.ts                  # Axios 实例
│   │   ├── demand.ts               # 需求 API
│   │   ├── bom.ts                  # BOM API
│   │   ├── inventory.ts            # 库存 API
│   │   ├── mrp.ts                  # MRP API
│   │   └── ai.ts                   # AI API
│   │
│   └── utils/                      # 工具函数
│       ├── formatter.ts
│       └── validator.ts
│
└── tools/
    └── vite.config.ts
```

### 6.2 前端模块划分

| 模块 | 页面 | 功能 |
|------|------|------|
| 需求 | OrderList, OrderDetail | 销售订单 CRUD |
| 预测 | ForecastList, ForecastDetail | 预测管理 |
| BOM | BomTree, BomEdit | BOM 维护/展开 |
| 库存 | InventoryList, StockAnalysis | 库存查询/分析 |
| MRP | MrpRun, SuggestionList | MRP 执行/建议 |
| 采购 | PurchaseList, PurchaseDetail | 采购订单 |
| 生产 | ProductionList, WorkReport | 生产工单/报工 |
| 风险 | RiskDashboard, AlertList | 风险监控 |
| 沙箱 | SandboxSession, Compare | 沙箱模拟 |
| AI 对话 | ChatWindow | 对话式交互 |

---

## 十一、模块间依赖关系(2)

### 8.1 Java 模块依赖图

```
                    ┌──────────────┐
                    │  aimrp-api  │  (入口)
                    └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
   │ aimrp-demand│  │ aimrp-mrp   │  │aimrp-conver-│
   │ aimrp-forecast  │            │  │   sation    │
   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
          │                │                │
          └────────┬───────┴────────┬───────┘
                   ▼                ▼
            ┌─────────────┐  ┌─────────────┐
            │ aimrp-core  │  │ aimrp-common│
            │   (核心域)  │  │   (公共)    │
            └─────────────┘  └─────────────┘
```

### 5.2 服务间调用关系

```
┌─────────────────────────────────────────────────────────────────┐
│                        Java Backend                              │
│                                                                 │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    │
│  │ Demand  │───▶│   MRP   │───▶│ Purchase│───▶│Production│   │
│  │ Service │    │ Engine  │    │ Service │    │ Service │   │
│  └────┬────┘    └────┬────┘    └─────────┘    └─────────┘    │
│       │               │                                          │
│       │               ▼                                          │
│       │        ┌─────────────┐                                   │
│       │        │   Sandbox   │                                   │
│       │        │   Service   │                                   │
│       │        └──────┬──────┘                                   │
│       │               │                                          │
│       └───────────────┼─────────────────────────────────────────┐ │
│                       ▼                                           │
│              ┌────────────────┐                                  │
│              │  AI Feign Client│◀──── Python AI Service          │
│              └────────────────┘                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/JSON
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Python AI Service                             │
│                                                                 │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    │
│  │  Chat   │    │ Predict │    │OR Solver│    │  RAG    │    │
│  │ Router  │    │ Router  │    │ Router  │    │ Router  │    │
│  └────┬────┘    └────┬────┘    └────┬────┘    └─────────┘    │
│       │               │               │                           │
│       ▼               ▼               ▼                           │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐                      │
│  │  Agent  │    │Forecaster│    │Scheduler│                      │
│  │Orchestr.│    │          │    │         │                      │
│  └────┬────┘    └────┬────┘    └────┬────┘                      │
│       │               │               │                           │
│       └───────────────┼───────────────┘                           │
│                       ▼                                            │
│              ┌────────────────┐                                   │
│              │   LLM / OR-Tools│                                   │
│              │   Prophet       │                                   │
│              └────────────────┘                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 十二、代码规范

### 6.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| **Java 类** | PascalCase | `SalesOrderService` |
| **Java 方法** | camelCase | `createOrder()` |
| **Java 常量** | UPPER_SNAKE | `ORDER_STATUS_PENDING` |
| **数据库表** | snake_case | `t_sales_order` |
| **数据库字段** | snake_case | `order_no` |
| **前端组件** | PascalCase | `OrderList.tsx` |
| **前端方法** | camelCase | `handleSubmit()` |

### 6.2 统一 API 返回类型

所有 Controller 必须使用统一响应结构 `ApiResponse<T>`：

```java
// 响应类定义 (com.aimrp.common.result.ApiResponse)
public class ApiResponse<T> implements Serializable {
    private int code;          // 状态码
    private String msg;       // 消息
    private T data;          // 数据
    private long timestamp;   // 时间戳
    
    // 成功响应
    public static <T> ApiResponse<T> ok()
    public static <T> ApiResponse<T> ok(T data)
    
    // 失败响应
    public static <T> ApiResponse<T> fail()
    public static <T> ApiResponse<T> fail(String msg)
}
```

**使用示例**：

```java
@RestController
@RequestMapping("/api/items")
public class ItemController {
    
    @GetMapping
    public R<Page<Item>> list(...) {
        return R.ok(itemMapper.selectPage(page, wrapper));
    }
    
    @GetMapping("/{id}")
    public R<Item> getById(@PathVariable Long id) {
        return R.ok(itemMapper.selectById(id));
    }
    
    @PostMapping
    public R<Item> create(@RequestBody Item item) {
        itemMapper.insert(item);
        return R.ok(item);
    }
    
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return R.ok();
    }
}
```

**状态码规范**：

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 6.3 分层调用规则

```
API Layer ──▶ Application Layer ──▶ Domain Layer ──▶ Infrastructure
   │                │                    │
   │                │                    └──► Repository
   │                │                         ├── MyBatis
   │                │                         ├── Redis
   │                │                         └── MQ
   │                │
   │                └──► Domain Service
   │                              └──► Domain Event
   │
   └──► DTOAssembler ──▶ Response
```

### 6.3 事务边界

| 场景 | 事务范围 |
|------|----------|
| 单表 CRUD | 应用服务层 |
| 多表操作 | 应用服务层 |
| 跨模块调用 | Saga 模式 |
| 异步操作 | 最终一致性 |

---

## 十三、开发工作流

### 6.1 新增模块流程

```
1. 创建模块目录
   └── aimrp-xxx/

2. 定义 Domain 实体
   └── domain/entity/*.java

3. 定义 Repository 接口
   └── domain/repository/*.java

4. 实现 Infrastructure
   └── infrastructure/persistence/*

5. 开发 Application Service
   └── application/service/*

6. 开发 API Controller
   └── api/controller/*

7. 编写单元测试
   └── test/*
```

### 6.2 代码提交规范

```
feat: 新功能
fix: Bug 修复
docs: 文档更新
refactor: 代码重构
test: 测试相关
chore: 构建/工具
```

---

## 十四、版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2026-03-09 | 初始版本 |
| 1.1 | 2026-03-09 | 新增：MyBatis-Plus 集成设计 + AI 服务插件化设计 |

---

*代码分层设计完成*
