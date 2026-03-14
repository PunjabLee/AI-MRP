# AI MRP 代码规范

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、命名规范

### 1.1 包命名

```java
// Java
package com.aimrp.forecast.domain.service;
package com.aimrp.production.domain.entity;
```

```python
# Python
from app.algorithms import forecast
from app.router import predict
```

### 1.2 类/接口命名

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Controller | `XxxController` | `ForecastController` |
| Service | `XxxService` | `DemandForecastService` |
| Repository | `XxxRepository` | `DemandForecastRepository` |
| Entity | `XxxEntity` | `SalesOrderEntity` |
| DTO | `XxxDTO` | `ForecastRequestDTO` |
| VO | `XxxVO` | `ForecastResultVO` |
| Enum | `XxxEnum` | `ForecastMethodEnum` |

### 1.3 方法命名

```java
// Java
public List<ForecastResult> forecast(...) { }
public void saveForecast(ForecastResult result) { }
public Optional<ForecastResult> findById(Long id) { }
public Page<ForecastResult> findAll(Pageable pageable) { }
```

```python
# Python
def forecast_demand(request: PredictRequest) -> ApiResponse:
def create_forecast_engine(method: str, **kwargs) -> ForecastEngine:
async def send_notification(notification: Notification) -> bool:
```

### 1.4 变量命名

```java
// Java
private String itemCode;           // 驼峰命名
private List<ForecastResult> results;  // 复数形式
private Map<String, Object> params;    // Map用泛型
private static final int MAX_SIZE = 100;  // 常量全大写下划线
```

```python
# Python
item_code = "A001"              # snake_case
results = []                    # 复数形式
max_size = 100                  # 常量大写
is_active = True               # is/has/can 前缀
```

---

## 二、代码格式

### 2.1 缩进与空格

```java
// Java
public void method() {
    if (condition) {
        doSomething();
    } else {
        doOther();
    }
}

// 运算符前后空格
int result = a + b * c;
for (int i = 0; i < 10; i++) { }

// 逗号后空格
method(param1, param2, param3);
```

```python
# Python
def method():
    if condition:
        do_something()
    else:
        do_other()

# 运算符前后空格
result = a + b * c
for i in range(10):

# 逗号后空格
method(param1, param2, param3)
```

### 2.2 行长度限制

| 语言 | 最大行长度 |
|------|----------|
| Java | 120 字符 |
| Python | 120 字符 |
| TypeScript | 100 字符 |

### 2.3 空行规范

```java
// Java - 类内方法间空一行
public class ForecastController {
    
    private final ForecastService service;
    
    @PostMapping("/forecast")
    public void forecast() {
        // 方法体
    }
    
    @GetMapping("/history")
    public List<?> getHistory() {
        // 方法体
    }
}
```

---

## 三、注释规范

### 3.1 类注释

```java
/**
 * 需求预测服务
 * 
 * 负责处理需求预测的核心业务逻辑，支持多种预测算法
 *
 * @author developer
 * @since 2024-01-01
 */
public class DemandForecastService { }
```

### 3.2 方法注释

```java
/**
 * 执行需求预测
 *
 * @param itemCode 物料编码
 * @param historicalData 历史数据
 * @param forecastDays 预测天数
 * @param method 预测方法
 * @return 预测结果列表
 * @throws IllegalArgumentException 参数无效时抛出
 */
public List<ForecastResult> forecast(...) { }
```

### 3.3 行内注释

```java
// 初始化预测引擎
engine = createForecastEngine(method);

// 计算加权平均 (权重: 7,6,5,4,3,2,1)
BigDecimal weightedSum = calculateWeightedSum(data, windowSize);
```

---

## 四、异常处理

### 4.1 业务异常

```java
// Java - 自定义异常
public class BusinessException extends RuntimeException {
    private final int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

// 使用
if (itemCode == null) {
    throw new BusinessException(400, "itemCode不能为空");
}
```

```python
# Python - 自定义异常
class BusinessException(Exception):
    def __init__(self, code: int, message: str):
        self.code = code
        self.message = message
        super().__init__(message)

# 使用
if not item_code:
    raise BusinessException(400, "item_code不能为空")
```

### 4.2 异常捕获

```java
// Java
try {
    result = service.forecast(request);
} catch (BusinessException e) {
    log.warn("业务异常: {}", e.getMessage());
    return ApiResponse.error(e.getCode(), e.getMessage());
} catch (Exception e) {
    log.error("系统异常", e);
    return ApiResponse.error(500, "系统内部错误");
}
```

```python
# Python
try:
    result = service.forecast(request)
except BusinessException as e:
    logger.warning(f"业务异常: {e.message}")
    return ApiResponse.error(code=e.code, message=e.message)
except Exception as e:
    logger.error("系统异常", exc_info=True)
    return ApiResponse.error(code=500, message="系统内部错误")
```

---

## 五、日志规范

### 5.1 日志级别

| 级别 | 使用场景 |
|------|----------|
| DEBUG | 详细调试信息，生产线关闭 |
| INFO | 正常业务流程日志 |
| WARN | 可恢复的异常，非致命错误 |
| ERROR | 致命错误，需要关注 |

### 5.2 日志格式

```java
// Java
log.info("【Service】开始预测 - itemCode: {}, method: {}", itemCode, method);
log.warn("历史数据为空，使用默认数据");
log.error("预测失败: {}", e.getMessage(), e);
```

```python
# Python
logger.info(f"【Service】开始预测 - item_code: {item_code}, method: {method}")
logger.warning("历史数据为空，使用默认数据")
logger.error(f"预测失败: {e}", exc_info=True)
```

---

## 六、数据库操作

### 6.1 JPA 规范

```java
// Repository 接口
@Repository
public interface ForecastRepository extends JpaRepository<ForecastEntity, Long> {
    
    // 方法命名规范
    List<ForecastEntity> findByItemCode(String itemCode);
    List<ForecastEntity> findByItemCodeAndForecastDateBetween(
        String itemCode, LocalDate startDate, LocalDate endDate);
    long countByItemCode(String itemCode);
    void deleteByForecastDateBefore(LocalDate date);
    
    // 自定义查询
    @Query("SELECT f FROM ForecastEntity f WHERE f.itemCode = :itemCode ORDER BY f.forecastDate DESC")
    List<ForecastEntity> findLatestForecast(@Param("itemCode") String itemCode);
}
```

### 6.2 事务规范

```java
// 只读事务 (查询)
@Transactional(readOnly = true)
public List<ForecastResult> forecast(...) { }

// 读写事务 (增删改)
@Transactional
public void saveForecast(ForecastResult result) {
    repository.save(result);
}

// 事务传播
@Transactional(propagation = Propagation.REQUIRED)
public void methodWithTransaction() { }
```

---

## 七、API 设计

### 7.1 RESTful 规范

| 操作 | 方法 | URL | 示例 |
|------|------|-----|------|
| 查询 | GET | /api/xxx/{id} | GET /api/forecast/1 |
| 列表 | GET | /api/xxx | GET /api/forecast?page=1 |
| 创建 | POST | /api/xxx | POST /api/forecast |
| 更新 | PUT | /api/xxx/{id} | PUT /api/forecast/1 |
| 删除 | DELETE | /api/xxx/{id} | DELETE /api/forecast/1 |

### 7.2 响应格式

```json
// 成功
{
  "code": 200,
  "message": "success",
  "success": true,
  "data": { ... }
}

// 失败
{
  "code": 400,
  "message": "参数错误: itemCode不能为空",
  "success": false,
  "error": {
    "type": "IllegalArgumentException"
  }
}
```

---

## 八、安全规范

### 8.1 敏感数据

```java
// 不要在日志中记录敏感信息
log.info("用户登录: username={}", username);  // OK
log.info("密码: {}", password);              // 禁止

// 密码加密存储
@Encrypt
private String password;
```

### 8.2 SQL 注入

```java
// 使用参数化查询 (JPA 自动防护)
repository.findByItemCode(itemCode);  // OK

// 禁止字符串拼接
@Query("SELECT * FROM t_forecast WHERE item_code = '" + itemCode + "'")  // 禁止
```

---

## 九、版本管理

### 9.1 变更日志

```markdown
## [1.0.0] - 2024-01-01

### 新增
- 需求预测功能
- 安全库存计算

### 修改
- 优化预测算法性能

### 修复
- 修复历史数据为空时的空指针异常
```

### 9.2 版本号规范

```
主版本.次版本.修订号
1.0.0 - 初始版本
1.1.0 - 新功能
1.1.1 - Bug修复
2.0.0 - 破坏性变更
```

---

*文档版本: 1.0*
