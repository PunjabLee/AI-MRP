# AI MRP 故障排查手册

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、常见问题

### 1.1 服务启动失败

#### 问题: Java 后端启动失败

```
现象:
Error: Application run failed
org.springframework.beans.BeanCreationException
```

排查步骤:
```bash
# 1. 查看详细日志
tail -n 500 /var/log/aimrp/application.log | grep -A 10 "BeanCreationException"

# 2. 检查数据库连接
curl http://localhost:8080/actuator/health

# 3. 检查数据库配置
grep -i "database" application.yml

# 4. 测试数据库连接
psql -h localhost -U aimrp -d aimrp -c "SELECT 1;"
```

常见原因:
| 原因 | 解决方案 |
|------|----------|
| 数据库未启动 | 启动 PostgreSQL: `systemctl start postgresql` |
| 数据库连接失败 | 检查 DATABASE_URL 配置 |
| 端口被占用 | 杀掉占用进程: `lsof -i:8080` |

---

#### 问题: Python AI 服务启动失败

```
现象:
uvicorn.error: Exception: No module named 'prophet'
```

排查步骤:
```bash
# 1. 查看错误日志
tail -n 100 /var/log/ai-service/app.log

# 2. 检查依赖
pip list | grep prophet

# 3. 重新安装依赖
pip install -r requirements.txt
```

---

### 1.2 API 请求失败

#### 问题: 预测接口返回 500

```bash
# 1. 查看后端日志
tail -f /var/log/aimrp/application.log | grep "forecast"

# 2. 检查请求参数
curl -X POST http://localhost:8080/api/forecast/forecast \
  -H "Content-Type: application/json" \
  -d '{"itemCode":"A001","forecastDays":30}'

# 3. 检查 Python 服务
curl http://localhost:8000/predict/demand
```

常见错误:
| 错误码 | 原因 | 解决方案 |
|--------|------|----------|
| 400 | 参数错误 | 检查请求参数 |
| 404 | 接口不存在 | 检查 URL |
| 500 | 服务器错误 | 查看日志 |
| 503 | 服务不可用 | 检查依赖服务 |

---

### 1.3 数据库问题

#### 问题: 连接池耗尽

```
现象:
org.hibernate.exception.GenericJDBCException: 
Unable to acquire JDBC Connection
```

排查:
```sql
-- 查看活跃连接
SELECT count(*) FROM pg_stat_activity 
WHERE datname = 'aimrp';

-- 查看最大连接数
SHOW max_connections;
```

解决方案:
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

---

## 二、性能问题

### 2.1 API 响应慢

排查步骤:
```bash
# 1. 检查响应时间
time curl -X POST http://localhost:8080/api/forecast/forecast -d '{}'

# 2. 查看慢查询
tail -n 1000 /var/log/aimrp/application.log | grep -i "slow"

# 3. 检查数据库查询时间
EXPLAIN ANALYZE SELECT * FROM t_forecast WHERE item_code = 'A001';

# 4. 检查服务资源
top
free -h
df -h
```

解决方案:
| 问题 | 解决方案 |
|------|----------|
| 慢查询 | 添加索引: `CREATE INDEX idx_xxx ...` |
| 内存不足 | 增加 JVM 堆内存 |
| CPU 高 | 优化算法或增加资源 |

---

### 2.2 预测算法执行慢

```
现象:
POST /predict/demand 返回超时
```

排查:
```python
# 查看 Python 日志
tail -f /var/log/ai-service/app.log

# 检查算法耗时
import time
start = time.time()
results = engine.forecast(days, confidence)
print(f"耗时: {time.time() - start}s")
```

解决方案:
```python
# 1. 减少预测天数
request.forecast_days = 30  # 原来是 365

# 2. 使用轻量级算法
request.method = "moving_average"  # 原来是 "prophet"

# 3. 调整求解时间
request.time_limit_seconds = 10  # 原来是 30
```

---

## 三、集成问题

### 3.1 Java 调用 Python 失败

```
现象:
feign.FeignException$ServiceUnavailable
```

排查:
```bash
# 1. 检查 Python 服务状态
curl http://localhost:8000/health

# 2. 检查网络连通性
curl -v http://localhost:8000/predict/demand

# 3. 查看 Feign 日志
logging.level.feign=DEBUG
```

解决方案:
```yaml
# application.yml
ai:
  service:
    url: http://localhost:8000
    timeout: 60000
    fallback:
      enabled: true
```

---

### 3.2 回调失败

```
现象:
callback_url 返回 404
```

排查:
```bash
# 1. 检查回调 URL
curl -X POST http://your-callback-url -v

# 2. 查看回调日志
tail -f /var/log/ai-service/app.log | grep callback
```

---

## 四、数据问题

### 4.1 预测结果异常

```
现象:
预测值为负数或极大值
```

排查:
```python
# 检查历史数据
historical_data = [
    {"date": "2024-01-01", "qty": -100},  # 负数
    {"date": "2024-01-02", "qty": 999999999}  # 异常大值
]

# 检查数据来源
print(f"历史数据: {historical_data}")
print(f"数据范围: {min(qtys)} - {max(qtys)}")
```

解决方案:
```python
# 数据预处理
def validate_data(data):
    return [
        {**d, "qty": max(0, d["qty"])}  # 过滤负数
        for d in data
        if 0 <= d["qty"] <= 1000000  # 过滤异常值
    ]
```

---

### 4.2 排程结果不满意

```
现象:
排程结果不是最优解
```

排查:
```bash
# 1. 检查约束设置
echo "约束: $(cat request.json | jq '.constraints')"

# 2. 检查求解时间
echo "求解时间: $((end_time - start_time))s"

# 3. 查看求解状态
echo "求解状态: $(cat result.json | jq '.status')"
```

解决方案:
```python
# 时间
result1. 增加求解 = create_scheduler(
    orders, resources, goal,
    time_limit_seconds=60  # 从 30 增加到 60
)

# 2. 调整优化目标
result = create_scheduler(
    orders, resources,
    goal="balanced"  # 改为平衡模式
)

# 3. 调整约束
constraints = {
    "max_tardiness": 480,  # 允许最多延迟 8 小时
    "min_utilization": 0.6  # 最小利用率 60%
}
```

---

## 五、应急处理

### 5.1 服务宕机

```bash
# 1. 查看服务状态
docker ps
kubectl get pods -n aimrp

# 2. 重启服务
docker-compose restart backend
kubectl rollout restart deployment/aimrp-backend -n aimrp

# 3. 验证服务
curl http://localhost:8080/actuator/health
```

### 5.2 数据库故障

```bash
# 1. 检查数据库状态
pg_isready -h localhost

# 2. 停止应用
kubectl scale deployment aimrp-backend --replicas=0 -n aimrp

# 3. 恢复数据库
pg_restore -U aimrp -d aimrp /backup/aimrp_latest.dump

# 4. 启动应用
kubectl scale deployment aimrp-backend --replicas=3 -n aimrp
```

### 5.3 数据恢复

```bash
# 1. 停止写入
# 防止新数据覆盖

# 2. 备份当前数据
pg_dump -U aimrp -d aimrp > /backup/before_fix_$(date +%Y%m%d).sql

# 3. 恢复数据
psql -U aimrp -d aimrp < /backup/aimrp_20240311.sql

# 4. 验证数据
psql -U aimrp -d aimrp -c "SELECT COUNT(*) FROM t_forecast;"
```

---

## 六、诊断命令

### 6.1 系统诊断

```bash
# CPU 使用
top -bn1 | head -20

# 内存使用
free -h

# 磁盘使用
df -h

# 网络连接
netstat -tuln

# 进程信息
ps aux | grep java
```

### 6.2 Java 诊断

```bash
# JVM 堆内存
jmap -heap <pid>

# 线程 dump
jstack <pid> > thread_dump.txt

# 堆 dump
jmap -dump:format=b,file=heap_dump.hprof <pid>
```

### 6.3 Python 诊断

```python
# 查看内存使用
import tracemalloc
tracemalloc.start()

# 执行代码
result = engine.forecast(days, confidence)

# 打印内存使用
current, peak = tracemalloc.get_traced_memory()
print(f"当前: {current / 1024 / 1024:.2f} MB, 峰值: {peak / 1024 / 1024:.2f} MB")

tracemalloc.stop()
```

---

## 七、联系支持

| 级别 | 问题类型 | 响应时间 | 联系方式 |
|------|----------|----------|----------|
| P1 | 系统宕机 | 15分钟 | 电话 |
| P2 | 功能异常 | 1小时 | 钉钉 |
| P3 | 性能问题 | 4小时 | 邮件 |
| P4 | 咨询建议 | 24小时 | 邮件 |

---

*文档版本: 1.0*
