# AI MRP 部署手册

> **版本**: 1.1
> **日期**: 2026-03-12
> **更新**: 新增微服务部署支持

---

## 一、环境要求

### 1.1 硬件要求

| 环境 | CPU | 内存 | 磁盘 | 说明 |
|------|-----|------|------|------|
| 开发 | 4核 | 8GB | 50GB | 本地运行 |
| 测试 | 8核 | 16GB | 100GB | |
| 生产 | 16核+ | 32GB+ | 200GB+ | 高可用部署 |

### 1.2 软件要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java 17 LTS |
| Node.js | 18+ | 前端构建 |
| Python | 3.10+ | AI 服务 |
| PostgreSQL | 14+ | 主数据库 |
| Redis | 7+ | 缓存/消息队列 |
| Docker | 24+ | 容器化 |
| Nginx | 1.20+ | 反向代理 |

---

## 二、Docker 部署

### 2.1 镜像构建

```bash
# 1. 构建 Java 后端镜像
cd code/backend
docker build -t aimrp-backend:latest .

# 2. 构建 Python AI 服务镜像
cd code/ai-service
docker build -t aimrp-ai-service:latest .

# 3. 构建前端镜像
cd code/frontend/aimrp-admin
docker build -t aimrp-frontend:latest .
```

### 2.2 Docker Compose 部署

```yaml
# docker-compose.yml
version: '3.8'

services:
  # PostgreSQL 数据库
  postgres:
    image: postgres:14
    container_name: aimrp-postgres
    environment:
      POSTGRES_DB: aimrp
      POSTGRES_USER: aimrp
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - aimrp-network

  # Redis
  redis:
    image: redis:7-alpine
    container_name: aimrp-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - aimrp-network

  # Java 后端
  backend:
    image: aimrp-backend:latest
    container_name: aimrp-backend
    environment:
      SPRING_PROFILES_ACTIVE: ${PROFILE}
      DATABASE_URL: jdbc:postgresql://postgres:5432/aimrp
      DATABASE_USERNAME: aimrp
      DATABASE_PASSWORD: ${DB_PASSWORD}
      REDIS_URL: redis://:${REDIS_PASSWORD}@redis:6379/0
    depends_on:
      - postgres
      - redis
    ports:
      - "8080:8080"
    networks:
      - aimrp-network

  # Python AI 服务
  ai-service:
    image: aimrp-ai-service:latest
    container_name: aimrp-ai-service
    environment:
      PYTHON_ENV: production
      DATABASE_URL: postgresql://aimrp:${DB_PASSWORD}@postgres:5432/aimrp
      REDIS_URL: redis://:${REDIS_PASSWORD}@redis:6379/0
    depends_on:
      - postgres
      - redis
    ports:
      - "8000:8000"
    networks:
      - aimrp-network

  # 前端
  frontend:
    image: aimrp-frontend:latest
    container_name: aimrp-frontend
    ports:
      - "80:80"
    networks:
      - aimrp-network

volumes:
  postgres_data:
  redis_data:

networks:
  aimrp-network:
    driver: bridge
```

### 2.3 启动服务

```bash
# 1. 复制环境变量模板
cp .env.example .env
# 编辑 .env 文件配置密码

# 2. 启动所有服务
docker-compose up -d

# 3. 查看日志
docker-compose logs -f

# 4. 查看服务状态
docker-compose ps

# 5. 停止服务
docker-compose down
```

---

## 三、K8s 部署

### 3.1 部署配置

```yaml
# k8s/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aimrp-backend
  namespace: aimrp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: aimrp-backend
  template:
    metadata:
      labels:
        app: aimrp-backend
    spec:
      containers:
      - name: backend
        image: aimrp-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: aimrp-secrets
              key: database-url
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: aimrp-backend
  namespace: aimrp
spec:
  selector:
    app: aimrp-backend
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### 3.2 部署命令

```bash
# 1. 创建命名空间
kubectl create namespace aimrp

# 2. 创建密钥
kubectl create secret generic aimrp-secrets \
  --from-literal=database-url='jdbc:postgresql://...' \
  --from-literal=redis-url='redis://...' \
  -n aimrp

# 3. 部署后端
kubectl apply -f k8s/backend-deployment.yaml

# 4. 部署 AI 服务
kubectl apply -f k8s/ai-service-deployment.yaml

# 5. 查看部署状态
kubectl get pods -n aimrp
kubectl get svc -n aimrp

# 6. 查看日志
kubectl logs -f deployment/aimrp-backend -n aimrp
```

---

## 四、Nginx 配置

```nginx
# /etc/nginx/conf.d/aimrp.conf

upstream aimrp_backend {
    server localhost:8080;
}

upstream aimrp_ai {
    server localhost:8000;
}

server {
    listen 80;
    server_name ai-mrp.example.com;

    # 前端静态文件
    location / {
        root /var/www/aimrp-admin;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Java API
    location /api/ {
        proxy_pass http://aimrp_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Python AI 服务
    location /ai/ {
        rewrite ^/ai/(.*) /$1 break;
        proxy_pass http://aimrp_ai;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_connect_timeout 300s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

---

## 五、数据库初始化

### 5.1 创建数据库

```sql
-- 创建数据库
CREATE DATABASE aimrp WITH ENCODING 'UTF8';

-- 创建用户
CREATE USER aimrp WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE aimrp TO aimrp;

-- 赋予 schema 权限
GRANT ALL ON SCHEMA public TO aimrp;
```

### 5.2 执行 SQL 脚本

```bash
# Java 后端自动建表 (JPA Hibernate)
# 确保 spring.jpa.hibernate.ddl-auto=update 或 validate

# 或手动执行 SQL
psql -U aimrp -d aimrp -f scripts/init.sql
```

---

## 六、配置清单

### 6.1 环境变量

| 变量 | 开发 | 测试 | 生产 | 说明 |
|------|------|------|------|------|
| `SPRING_PROFILES_ACTIVE` | dev | test | production | Spring Profile |
| `DATABASE_URL` | localhost:5432 | test-db:5432 | prod-db:5432 | 数据库地址 |
| `DATABASE_PASSWORD` | - | - | xxx | 数据库密码 |
| `REDIS_URL` | localhost:6379 | redis:6379 | redis:6379 | Redis地址 |
| `LLM_PROVIDER` | deepseek | deepseek | deepseek | LLM供应商 |
| `LLM_API_KEY` | - | - | sk-xxx | LLM API密钥 |

### 6.2 配置文件

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: aimrp
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false

server:
  port: 8080
  tomcat:
    threads:
      max: 200
      min-spare: 10

logging:
  level:
    root: INFO
    com.aimrp: INFO
  file:
    name: /var/log/aimrp/application.log
```

---

## 七、健康检查

### 7.1 后端健康检查

```bash
# HTTP 检查
curl http://localhost:8080/actuator/health

# 响应
{"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

### 7.2 AI 服务健康检查

```bash
# HTTP 检查
curl http://localhost:8000/health

# 响应
{"status":"healthy","version":"1.0.0"}
```

---

## 八、备份与恢复

### 8.1 数据库备份

```bash
# 备份
pg_dump -U aimrp -d aimrp -F c -b -v -f aimrp_backup.dump

# 压缩备份
pg_dump -U aimrp -d aimrp | gzip > aimrp_$(date +%Y%m%d).sql.gz
```

### 8.2 恢复

```bash
# 恢复
pg_restore -U aimrp -d aimrp -c aimrp_backup.dump

# 从压缩文件恢复
gunzip -c aimrp_20240311.sql.gz | psql -U aimrp -d aimrp
```

---

## 九、微服务部署

### 9.1 架构概述

AI MRP 支持两种运行模式：
- **单体模式 (standalone)**: 默认模式，所有模块在同一个 JVM 中运行
- **微服务模式 (microservice)**: 注册到 Nacos，支持服务发现和负载均衡

```
┌─────────────────────────────────────────┐
│           aimrp-gateway                 │
│        Spring Cloud Gateway             │
│              端口: 8080                 │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│              aimrp-api                   │
│         (支持双模式运行)                  │
│  - standalone: 单体模式 (默认)           │
│  - microservice: 微服务模式           │
└─────────────────────────────────────────┘
```

### 9.2 Nacos 部署

```bash
# 1. 拉取 Nacos 镜像
docker pull nacos/nacos-server:v2.2.3

# 2. 启动 Nacos (单机模式)
docker run -d \
  --name nacos \
  -p 8848:8848 \
  -p 9848:9848 \
  -e MODE=standalone \
  -e SPRING_DATASOURCE_PLATFORM=postgres \
  -e MYSQL_SERVICE_HOST=postgres \
  -e MYSQL_SERVICE_PORT=5432 \
  -e MYSQL_SERVICE_DB_NAME=nacos_config \
  -e MYSQL_SERVICE_USER=nacos \
  -e MYSQL_SERVICE_PASSWORD=nacos \
  -v nacos_logs:/home/nacos/logs \
  nacos/nacos-server:v2.2.3

# 3. 访问 Nacos 控制台
# http://localhost:8848/nacos
# 默认账号: nacos / nacos
```

### 9.3 Gateway 部署

```bash
# 1. 构建 Gateway 镜像
cd code/backend
docker build -t aimrp-gateway:latest -f aimrp-gateway/Dockerfile .

# 2. 启动 Gateway
docker run -d \
  --name aimrp-gateway \
  -p 8080:8080 \
  -e SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848 \
  -e SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=nacos:8848 \
  aimrp-gateway:latest

# 3. 路由规则
# /api/demand/**    -> aimrp-demand
# /api/bom/**      -> aimrp-bom
# /api/inventory/**-> aimrp-inventory
# /api/mrp/**      -> aimrp-mrp
# /api/production/** -> aimrp-production
# /api/purchase/** -> aimrp-purchase
```

### 9.4 双模式配置

```yaml
# 单体模式 (默认)
# java -jar aimrp-api.jar
spring:
  profiles:
    active: standalone
  cloud:
    discovery:
      enabled: false

# 微服务模式
# java -jar aimrp-api.jar --spring.profiles.active=microservice
spring:
  profiles:
    active: microservice
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: nacos.8848
        namespace: production
        group: AIMRP_GROUP
```

### 9.5 微服务 Docker Compose

```yaml
# docker-compose-microservice.yml
version: '3.8'

services:
  # Nacos
  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: aimrp-nacos
    environment:
      MODE: standalone
    ports:
      - "8848:8848"
      - "9848:9848"
    networks:
      - aimrp-network

  # Gateway
  gateway:
    image: aimrp-gateway:latest
    container_name: aimrp-gateway
    ports:
      - "8080:8080"
    depends_on:
      - nacos
    networks:
      - aimrp-network

  # API 服务 (微服务模式)
  api:
    image: aimrp-api:latest
    container_name: aimrp-api
    environment:
      SPRING_PROFILES_ACTIVE: microservice
      SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR: nacos:8848
    depends_on:
      - nacos
    networks:
      - aimrp-network

networks:
  aimrp-network:
    driver: bridge
```

### 9.6 服务注册配置

在 Nacos 控制台创建配置 `application-${spring.profiles.active}.yml`：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: ${NAMESPACE:production}
        group: AIMRP_GROUP
      config:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: ${NAMESPACE:production}
        group: AIMRP_GROUP
        file-extension: yml
        shared-configs:
          - data-id: datasource.yml
            group: AIMRP_GROUP
            refresh: true
```

---

## 十、运维命令

```bash
# 重启服务
docker-compose restart backend

# 查看资源使用
docker stats

# 进入容器
docker exec -it aimrp-backend bash

# 查看日志
docker logs -f --tail 100 aimrp-backend

# 滚动更新 (K8s)
kubectl rollout restart deployment/aimrp-backend -n aimrp

# 回滚 (K8s)
kubectl rollout undo deployment/aimrp-backend -n aimrp
```

---

*文档版本: 1.1*
