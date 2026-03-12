# Java 后端技术架构与分层规范 Review 报告

> **日期**：2026-03-09  
> **版本**：1.0

---

## 一、现状分析

### 1.1 设计文档

| 文档 | 版本 | 说明 |
|------|------|------|
| CODE_LAYER_DESIGN.md | v1.1 | 代码分层设计 |
| CODE_FRAMEWORK.md | v1.1 | 代码框架 |
| TECHNICAL_ARCHITECTURE.md | v1.1 | 技术架构 |

### 1.2 设计 vs 实现差距

#### 设计要求的分层结构

```
├── domain/
│   ├── entity/           ✅ 存在
│   ├── valueobject/       ❌ 缺失
│   ├── repository/        ❌ 缺失
│   └── service/           ❌ 缺失
├── application/
│   ├── dto/              ❌ 缺失
│   ├── command/           ❌ 缺失
│   ├── query/            ❌ 缺失
│   ├── service/          ❌ 缺失
│   └── listener/         ❌ 缺失
├── infrastructure/
│   ├── persistence/
│   │   ├── mapper/       ⚠️ 部分存在
│   │   └── repository/   ❌ 缺失
│   ├── mq/               ❌ 缺失
│   └── config/            ❌ 缺失
├── api/
│   ├── controller/       ✅ 存在
│   ├── dto/              ⚠️ 部分存在
│   └── assembler/         ❌ 缺失
```

#### 实际实现的分层结构

| 模块 | domain/entity | domain/service | application | api/controller | infrastructure/mapper |
|------|---------------|----------------|-------------|-----------------|------------------------|
| demand | ✅ | ❌ | ❌ | ✅ | ✅ |
| item | ✅ | ❌ | ❌ | ✅ | ✅ |
| supplier | ✅ | ❌ | ❌ | ✅ | ✅ |
| bom | ✅ | ❌ | ❌ | ✅ | ❌ |
| inventory | ✅ | ❌ | ❌ | ✅ | ❌ |
| purchase | ✅ | ❌ | ❌ | ✅ | ❌ |
| production | ✅ | ✅ | ❌ | ✅ | ❌ |
| mrp | ✅ | ✅ | ⚠️ | ✅ | ❌ |
| forecast | ✅ | ✅ | ⚠️ | ✅ | ❌ |
| risk | ✅ | ✅ | ⚠️ | ✅ | ❌ |
| whatif | ✅ | ✅ | ⚠️ | ✅ | ❌ |

---

## 二、编码规范现状

### 2.1 命名规范（当前）

| 类型 | 规范 | 示例 |
|------|------|------|
| Entity | 业务名称 + Entity 或直接业务名 | SalesOrder, Item |
| Controller | 业务名称 + Controller | SalesOrderController |
| Mapper | 表名 + Mapper | SalesOrderMapper |

### 2.2 缺失的规范

| 规范 | 设计要求 | 当前状态 |
|------|----------|----------|
| Service 命名 | XxxService | 无 |
| DTO 命名 | XxxDTO, XxxRequest | 无 |
| Command 命名 | CreateXxxCommand | 无 |
| Query 命名 | XxxQuery | 无 |
| Repository 命名 | XxxRepository | 无 |
| ValueObject 命名 | XxxVO | 部分 |

---

## 三、需要补充的规范

### 3.1 分层结构规范

| 层级 | 必须 | 说明 |
|------|------|------|
| domain/entity | ✅ | 实体定义 |
| domain/valueobject | ⚠️ 建议 | 值对象 |
| domain/repository | ⚠️ 建议 | 仓储接口 |
| domain/service | ⚠️ 建议 | 领域服务 |
| application/dto | ⚠️ 建议 | 数据传输对象 |
| application/service | ⚠️ 建议 | 应用服务 |
| infrastructure/mapper | ✅ | 数据访问 |
| api/controller | ✅ | 接口定义 |

### 3.2 包结构规范

| 包名 | 含义 | 示例 |
|------|------|------|
| domain.entity | 实体 | SalesOrder.java |
| domain.valueobject | 值对象 | Money.java |
| domain.repository | 仓储接口 | OrderRepository.java |
| domain.service | 领域服务 | PricingService.java |
| application.dto | 数据传输 | OrderDTO.java |
| application.command | 命令对象 | CreateOrderCommand.java |
| application.query | 查询对象 | OrderQuery.java |
| application.service | 应用服务 | OrderAppService.java |
| infrastructure.mapper | MyBatis Mapper | OrderMapper.java |
| infrastructure.repository | 仓储实现 | OrderRepositoryImpl.java |
| api.controller | REST接口 | OrderController.java |
| api.assembler | 对象转换 | OrderAssembler.java |

---

## 四、待完善任务清单

### 4.1 P0 - 必须完善

| # | 任务 | 模块 | 说明 |
|---|------|------|------|
| 1 | 添加 domain/service 层 | bom/inventory/purchase | 业务逻辑封装 |
| 2 | 添加 application 层 | 所有基础模块 | 应用服务 |
| 3 | 补充 Mapper | bom/inventory/purchase/production | 数据访问 |

### 4.2 P1 - 应该完善

| # | 任务 | 说明 |
|---|------|------|
| 4 | 添加 domain/valueobject | 值对象定义 |
| 5 | 添加 domain/repository | 仓储接口 |
| 6 | 添加 application/dto | 数据传输对象 |
| 7 | 添加 api/assembler | 对象转换 |

### 4.3 P2 - 可以完善

| # | 任务 | 说明 |
|---|------|------|
| 8 | 添加命名规范文档 | 编码规范 |
| 9 | 添加注释规范 | 注释要求 |
| 10 | 添加异常处理规范 | 异常定义 |

---

## 五、规范建议

### 5.1 Entity 规范

```java
/**
 * 销售订单
 */
@Data
@Entity
@TableName("t_sales_order")
public class SalesOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 订单编号 */
    private String orderNo;
    
    /** 客户编码 */
    private String customerCode;
    
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### 5.2 Service 规范

```java
/**
 * 订单领域服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 创建订单
     */
    public Order createOrder(CreateOrderCommand command) {
        // 业务逻辑
        Order order = new Order();
        // ...
        return orderRepository.save(order);
    }
}
```

### 5.3 Controller 规范

```java
/**
 * 销售订单接口
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class SalesOrderController {
    
    private final SalesOrderService salesOrderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public ApiResponse<Order> create(@RequestBody @Valid CreateOrderRequest request) {
        // ...
    }
}
```

---

## 六、结论

| 维度 | 完成度 |
|------|---------|
| 分层结构 | 40% |
| 编码规范 | 30% |
| 文档完整性 | 50% |

**核心差距**：
1. 缺少 domain/service 层（大部分模块）
2. 缺少 application 层
3. 缺少统一编码规范文档

---

*Review 完成*
