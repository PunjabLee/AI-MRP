# AI MRP 运维手册

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、监控

### 1.1 监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| CPU 使用率 | 服务器 CPU 使用情况 | > 80% |
| 内存使用率 | 内存使用情况 | > 85% |
| 磁盘使用率 | 磁盘空间使用 | > 90% |
| 请求延迟 | API 响应时间 | > 2s |
| 错误率 | 请求错误比例 | > 1% |
| QPS | 每秒请求数 | > 1000 |

### 1.2 应用指标

```yaml
# Spring Boot Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

```bash
# 查看健康状态
curl http://localhost:8080/actuator/health

# 查看指标
curl http://localhost:8080/actuator/metrics

# 查看特定指标
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### 1.3 日志收集

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  paths:
    - /var/log/aimrp/*.log
  fields:
    service: aimrp
    environment: production

output.logstash:
  hosts: ["logstash:5044"]
```

---

## 二、告警

### 2.1 告警规则

| 告警名称 | 条件 | 级别 | 通知方式 |
|----------|------|------|----------|
| CPU 高 | > 80% 持续 5min | 警告 | 邮件/钉钉 |
| 内存高 | > 85% 持续 5min | 警告 | 邮件/钉钉 |
| 服务宕机 | 进程不存在 | 严重 | 电话/短信 |
| API 错误率 | > 1% 持续 1min | 警告 | 邮件 |
| 数据库连接 | 连接池耗尽 | 严重 | 电话 |

### 2.2 告警配置

```yaml
# Prometheus 告警规则
groups:
- name: aimrp
  rules:
  - alert: HighCPUUsage
    expr: rate(process_cpu_seconds_total{service="aimrp"}[5m]) > 0.8
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "CPU 使用率过高"
      description: "{{ $labels.instance }} CPU 使用率 {{ $value | humanizePercentage }}"

  - alert: ServiceDown
    expr: up{service="aimrp"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "服务宕机"
      description: "{{ $labels.instance }} 服务已停止"
```

---

## 三、日志管理

### 3.1 日志级别

| 级别 | 使用场景 |
|------|----------|
| DEBUG | 调试信息，生产关闭 |
| INFO | 正常业务流程 |
| WARN | 可恢复异常 |
| ERROR | 业务错误 |
| FATAL | 系统崩溃 |

### 3.2 日志查询

```bash
# 查看最近 100 行日志
tail -n 100 /var/log/aimrp/application.log

# 实时查看日志
tail -f /var/log/aimrp/application.log

# 搜索错误日志
grep "ERROR" /var/log/aimrp/application.log | tail -n 50

# 按时间范围搜索
sed -n '/2024-03-11 10:00:00/,/2024-03-11 11:00:00/p' /var/log/aimrp/application.log
```

### 3.3 日志归档

```bash
# 每天 0 点压缩日志
0 0 * * * find /var/log/aimrp -name "*.log" -mtime +7 -exec gzip {} \;

# 删除 30 天前的日志
0 1 * * * find /var/log/aimrp -name "*.log.gz" -mtime +30 -delete
```

---

## 四、性能优化

### 4.1 JVM 优化

```bash
# JVM 参数
JAVA_OPTS="
  -Xms2g -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/aimrp/heapdump.hprof
  -Xloggc:/var/log/aimrp/gc.log
  -XX:+PrintGCDetails
  -XX:+PrintGCDateStamps
"
```

### 4.2 数据库优化

```sql
-- 创建索引
CREATE INDEX idx_forecast_item_date ON t_demand_forecast(item_code, forecast_date);

-- 分析查询
EXPLAIN ANALYZE SELECT * FROM t_demand_forecast WHERE item_code = 'A001';

-- 查看慢查询
SELECT query, calls, mean_time, total_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

### 4.3 缓存优化

```java
// 使用缓存
@Cacheable(value = "forecast", key = "#itemCode")
public ForecastResult getForecast(String itemCode) {
    return repository.findByItemCode(itemCode);
}

// 缓存配置
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

---

## 五、安全

### 5.1 访问控制

```yaml
# Spring Security 配置
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com
```

### 5.2 敏感数据

```bash
# 敏感环境变量
DB_PASSWORD=xxx  # 不能明文存储
LLM_API_KEY=xxx  # 使用密钥管理

# 使用 Docker Secret
echo "password" | docker secret create db_password -
```

### 5.3 审计日志

```java
// 记录操作日志
@Auditable
public void updateOrder(Order order) {
    // 业务逻辑
}
```

---

## 六、备份

### 6.1 数据库备份

```bash
# 每日备份脚本
#!/bin/bash
DATE=$(date +%Y%m%d)
pg_dump -U aimrp -d aimrp | gzip > /backup/aimrp_${DATE}.sql.gz

# 保留 30 天
find /backup -name "aimrp_*.sql.gz" -mtime +30 -delete
```

### 6.2 文件备份

```bash
# 备份上传文件
rsync -avz /data/uploads /backup/uploads_$(date +%Y%m%d)/
```

---

## 七、扩容

### 7.1 水平扩容

```bash
# 增加副本数
kubectl scale deployment aimrp-backend --replicas=5 -n aimrp

# 自动扩容
kubectl autoscale deployment aimrp-backend --cpu-percent=80 --min=3 --max=10 -n aimrp
```

### 7.2 垂直扩容

```yaml
# 修改资源限制
resources:
  requests:
    memory: "4Gi"
    cpu: "2000m"
  limits:
    memory: "8Gi"
    cpu: "4000m"
```

---

## 八、巡检

### 8.1 每日巡检

```bash
# 1. 检查服务状态
docker ps
kubectl get pods -n aimrp

# 2. 检查资源使用
docker stats

# 3. 检查磁盘空间
df -h

# 4. 检查日志错误
grep -i error /var/log/aimrp/application.log | tail -n 20

# 5. 检查数据库连接
curl http://localhost:8080/actuator/health
```

### 8.2 每周巡检

```bash
# 1. 数据库性能分析
EXPLAIN ANALYZE SELECT ...

# 2. 清理过期数据
DELETE FROM t_log WHERE created_at < NOW() - INTERVAL '90 days';

# 3. 备份验证
pg_restore --list /backup/aimrp_latest.dump
```

---

*文档版本: 1.0*
