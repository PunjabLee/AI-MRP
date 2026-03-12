# Enterprise 微服务架构规划

> **日期**：2026-03-09
> **版本**：1.2
> **阶段**：Enterprise
> **更新**：2026-03-12 - Phase 2 进行中

---

## 一、微服务架构演进背景

### 1.1 当前状态 (Phase 2 进行中)

| 组件 | 状态 | 说明 |
|------|------|------|
| Nacos 依赖 | ✅ 已引入 | spring-cloud-starter-alibaba-nacos |
| Gateway 模块 | ✅ 已创建 | aimrp-gateway (8080端口) |
| 路由配置 | ✅ 已配置 | 10个服务的路由规则 |
| 双模式支持 | ✅ 已实现 | standalone / microservice |
| Feign 客户端 | ✅ 已引入 | 跨模块调用 |
| 独立服务 Application | 🔄 进行中 | demand/inventory/mrp/purchase/production |

```
┌─────────────────────────────────────────┐
│           aimrp-gateway                 │
│        Spring Cloud Gateway             │
│              端口: 8080                 │
└─────────────────┬───────────────────────┘
                  │
        ┌─────────┼─────────┬──────────┐
        ▼         ▼         ▼          ▼
   ┌────────┐┌────────┐┌────────┐┌────────┐
   │ demand ││inventory││  mrp   ││purchase│
   │ :8081  ││ :8082  ││ :8083  ││ :8084  │
   └────────┘└────────┘└────────┘└────────┘

   ┌────────┐
   │production│
   │ :8085  │
   └────────┘
```

### 1.2 目标架构

### 1.3 当前架构

```
┌─────────────────────────────────────────┐
│              aimrp (单体)                │
├─────────────────────────────────────────┤
│  demand | bom | inventory | mrp | ...   │
│     (Maven 模块，同 JVM 调用)            │
└─────────────────────────────────────────┘
```

### 1.4 目标架构

```
┌────────────────────────────────────────────────────────────┐
│                      API Gateway                            │
│                    (Spring Cloud Gateway)                   │
└────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│  Nacos 注册   │   │  Nacos 配置   │   │   SkyWalking  │
│   中心         │   │    中心       │   │    链路追踪   │
└───────────────┘   └───────────────┘   └───────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────┐
│                     服务层 (Spring Boot)                    │
├──────────┬──────────┬──────────┬──────────┬─────────────┤
│ demand   │   bom   │inventory │   mrp    │  production │
│ service  │ service │ service  │ service  │   service   │
├──────────┴──────────┴──────────┴──────────┴─────────────┤
│                     公共组件 (auth, config)                │
└────────────────────────────────────────────────────────────┘
        │
        ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│   PostgreSQL  │   │     Redis    │   │     MQ       │
│   (数据存储)   │   │   (缓存)      │   │  (消息队列)   │
└───────────────┘   └───────────────┘   └───────────────┘
```

---

## 二、微服务技术栈

### 2.1 核心组件

| 组件 | 技术 | 用途 |
|------|------|------|
| **注册/配置中心** | Nacos | 服务发现 + 配置管理 |
| **网关** | Spring Cloud Gateway | 路由 + 鉴权 + 限流 |
| **链路追踪** | SkyWalking / Jaeger | 分布式追踪 |
| **熔断降级** | Sentinel / Resilience4j | 流量控制 |
| **负载均衡** | Ribbon / Spring Cloud LoadBalancer | 客户端负载 |
| **认证授权** | Spring Security + OAuth2 | 统一鉴权 |
| **日志** | ELK (Elasticsearch + Logstash + Kibana) | 日志分析 |
| **监控** | Prometheus + Grafana | 指标监控 |

### 2.2 服务拆分粒度

| 服务 | 模块 | 独立部署 | 说明 |
|------|------|----------|------|
| aimrp-demand | demand | ✅ | 需求管理 |
| aimrp-bom | bom | ✅ | BOM管理 |
| aimrp-inventory | inventory | ✅ | 库存管理 |
| aimrp-mrp | mrp | ✅ | MRP计算（计算密集） |
| aimrp-production | production | ✅ | 生产管理 |
| aimrp-purchase | purchase | ✅ | 采购管理 |
| aimrp-forecast | forecast | ✅ | AI预测 |
| aimrp-risk | risk | ✅ | 风险监控 |
| aimrp-whatif | whatif | ✅ | What-if模拟 |
| aimrp-conversation | conversation | ✅ | 对话服务 |
| aimrp-system | system | ✅ | 系统管理 |

---

## 三、Nacos 架构设计

### 3.1 服务注册

```yaml
# application.yml (各服务)
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos.aimrp.com:8848
        namespace: production
        group: AIMRP_GROUP
      config:
        server-addr: nacos.aimrp.com:8848
        namespace: production
        group: AIMRP_GROUP
        file-extension: yml
```

### 3.2 配置管理

| 配置项 | 说明 |
|--------|------|
| datasource.yml | 数据库连接池 |
| redis.yml | Redis 配置 |
| mq.yml | 消息队列 |
| swagger.yml | API 文档 |
| actuator.yml | 健康检查 |

### 3.3 配置共享

```yaml
# shared-data.yml (共享配置)
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  redis:
    database: 0
  jpa:
    show-sql: false
```

---

## 四、Gateway 架构设计

### 4.1 路由规则

| 路径 | 服务 | 说明 |
|------|------|------|
| /api/demand/** | aimrp-demand | 需求管理 |
| /api/bom/** | aimrp-bom | BOM管理 |
| /api/inventory/** | aimrp-inventory | 库存管理 |
| /api/mrp/** | aimrp-mrp | MRP计算 |
| /api/production/** | aimrp-production | 生产管理 |
| /api/purchase/** | aimrp-purchase | 采购管理 |
| /api/forecast/** | aimrp-forecast | AI预测 |
| /api/risk/** | aimrp-risk | 风险监控 |
| /api/whatif/** | aimrp-whatif | What-if模拟 |
| /api/system/** | aimrp-system | 系统管理 |

### 4.2 核心功能

```java
@Configuration
public class GatewayConfig {
    
    /**
     * 全局过滤器：鉴权
     */
    @Bean
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            // 验证 Token
            // 检查权限
            return chain.filter(exchange);
        };
    }
    
    /**
     * 全局过滤器：限流
     */
    @Bean
    public GlobalFilter rateLimitFilter() {
        return new SentinelRateLimitFilter();
    }
}
```

---

## 五、SkyWalking 链路追踪

### 5.1 接入配置

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-trace</artifactId>
    <version>${skywalking.version}</version>
</dependency>
```

### 5.2 追踪要点

| 追踪点 | 说明 |
|--------|------|
| Gateway → Service | HTTP 调用 |
| Service → Service | Feign 调用 |
| Service → DB | SQL 执行 |
| Service → MQ | 消息发送/消费 |
| Service → Redis | 缓存操作 |

### 5.3 关键指标

| 指标 | 告警阈值 |
|------|----------|
| 响应时间 P99 | > 3s |
| 错误率 | > 1% |
| QPS | > 10000 |
| 链路深度 | > 10 |

---

## 六、Feign 服务调用改造

### 6.1 当前 vs 未来

| 阶段 | 调用方式 | 示例 |
|------|----------|------|
| 当前 | Maven 依赖 | `@Autowired MrpService mrpService` |
| 未来 | Feign | `@Autowired MrpClient mrpClient` |

### 6.2 改造示例

```java
// 当前 (Maven)
@Service
@RequiredArgsConstructor
public class BomApplicationService {
    private final MrpDomainService mrpService; // 直接注入
}

// 未来 (Feign)
@FeignClient(name = "aimrp-mrp", path = "/api/mrp")
public interface MrpClient {
    @PostMapping("/calculate")
    MrpResult calculate(@RequestBody MrpRequest request);
}

@Service
@RequiredArgsConstructor
public class BomApplicationService {
    private final MrpClient mrpClient; // 远程调用
}
```

### 6.3 改造计划

| 阶段 | 任务 | 工作量 |
|------|------|--------|
| 1 | 定义 API 接口 | 1d/模块 |
| 2 | 添加 Feign Client | 1d/模块 |
| 3 | 服务拆分部署 | 2d/模块 |
| 4 | 配置 Nacos | 3d |
| 5 | 配置 Gateway | 2d |
| 6 | 接入 SkyWalking | 2d |

---

## 七、实施路线图

### 7.1 Enterprise 阶段任务

| 序号 | 任务 | 优先级 | 工时 | 依赖 |
|------|------|--------|------|------|
| 1 | 微服务架构设计 | P0 | 3d | - |
| 2 | Nacos 部署与配置 | P0 | 3d | 1 |
| 3 | Gateway 部署与路由 | P0 | 3d | 2 |
| 4 | 服务拆分 - demand | P1 | 2d | 3 |
| 5 | 服务拆分 - bom | P1 | 2d | 3 |
| 6 | 服务拆分 - inventory | P1 | 2d | 3 |
| 7 | 服务拆分 - mrp | P1 | 3d | 3 |
| 8 | 服务拆分 - production | P1 | 2d | 3 |
| 9 | 服务拆分 - purchase | P1 | 2d | 3 |
| 10 | Feign 调用改造 | P1 | 5d | 4-9 |
| 11 | SkyWalking 接入 | P2 | 2d | 4-9 |
| 12 | Sentinel 熔断配置 | P2 | 2d | 10 |
| 13 | ELK 日志接入 | P2 | 3d | 4-9 |
| 14 | 整体集成测试 | P1 | 3d | 以上 |

### 7.2 预估工时

| 阶段 | 任务数 | 总工时 |
|------|--------|--------|
| 架构准备 | 3 | 9d |
| 服务拆分 | 6 | 13d |
| 调用改造 | 1 | 5d |
| 可观测性 | 3 | 7d |
| 测试集成 | 1 | 3d |
| **总计** | **14** | **~37d** |

---

## 八、风险与对策

| 风险 | 影响 | 对策 |
|------|------|------|
| 服务拆分复杂 | 高 | 按优先级逐步拆分 |
| 分布式事务 | 中 | 使用 Seata |
| 调用延迟增加 | 中 | 异步化 + 缓存 |
| 运维成本增加 | 低 | 自动化部署 |

---

## 九、总结

| 维度 | 当前 | Enterprise |
|------|------|------------|
| 架构 | 模块化单体 | 微服务 |
| 调用 | Maven | Feign |
| 部署 | 单体部署 | 独立部署 |
| 治理 | 无 | Nacos + Gateway + SkyWalking |
| 复杂度 | 低 | 中 |

**Enterprise 阶段目标**：在保持业务快速迭代的同时，通过微服务架构提升系统的可扩展性和可维护性。

---

## 十、实施进度

### 10.1 当前进度

| 阶段 | 状态 | 完成时间 |
|------|------|----------|
| Phase 1: 基础设施 | ✅ 已完成 | 2026-03-12 |
| Phase 2: 服务拆分 | 🔄 进行中 | 2026-03-12 |
| Phase 3: 治理能力 | ⏳ 待开始 | - |
| Phase 4: 数据治理 | ⏳ 待开始 | - |

### 10.2 Phase 2 完成项

| 服务 | 端口 | Application类 | Nacos配置 | 状态 |
|------|------|---------------|-----------|------|
| aimrp-demand | 8081 | ✅ | ✅ | ✅ |
| aimrp-inventory | 8082 | ✅ | ✅ | ✅ |
| aimrp-mrp | 8083 | ✅ | ✅ | ✅ |
| aimrp-purchase | 8084 | ✅ | ✅ | ✅ |
| aimrp-production | 8085 | ✅ | ✅ | ✅ |

### 10.3 下一步计划

1. **Phase 2 收尾**
   - 测试各服务独立启动
   - 验证服务注册到 Nacos
   - 配置 Gateway 动态路由

2. **Phase 3 治理能力**
   - Sentinel 熔断降级
   - 限流配置
   - SkyWalking 链路追踪

---

*规划完成 - 2026-03-09*
*更新完成 - 2026-03-12 (Phase 2 进行中)*
