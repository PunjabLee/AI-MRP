# AI MRP 开发环境配置指南

> **版本**：1.0  
> **日期**：2026-03-09  
> **目标**：快速搭建本地开发环境

---

## 一、环境要求

### 1.1 必需软件

| 软件 | 版本 | 说明 |
|------|------|------|
| **JDK** | 17+ | Java 17 或更高版本 |
| **Maven** | 3.8+ | 项目构建工具 |
| **Python** | 3.11+ | AI 服务运行环境 |
| **Node.js** | 18+ | 前端开发环境 |
| **pnpm** | 8+ | 前端包管理器 |
| **PostgreSQL** | 15+ | 主数据库 |
| **Redis** | 7.0+ | 缓存数据库 |
| **Git** | 2.30+ | 版本管理 |

### 1.2 可选软件

| 软件 | 说明 |
|------|------|
| **Docker** | 容器化部署 |
| **pgAdmin** | PostgreSQL 管理工具 |
| **Redis Desktop Manager** | Redis 可视化工具 |
| **IntelliJ IDEA** | Java 开发 IDE |
| **VS Code** | Python/前端开发 IDE |

---

## 二、环境安装

### 2.1 Java 环境

```bash
# macOS (使用 Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt-get install openjdk-17-jdk

# Windows
# 下载 JDK 17: https://www.oracle.com/java/technologies/downloads/#java17
```

验证安装：
```bash
java -version
mvn -version
```

### 2.2 Python 环境

```bash
# macOS
brew install python@3.11

# Ubuntu
sudo apt-get install python3.11 python3.11-venv python3.11-dev

# 创建虚拟环境
cd AI-MRP/code/ai-service
python3.11 -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 2.3 Node.js 环境

```bash
# macOS
brew install node@18

# 安装 pnpm
npm install -g pnpm

# 验证
node -v
pnpm -v
```

### 2.4 数据库环境

#### PostgreSQL

```bash
# macOS
brew install postgresql@15
brew services start postgresql@15

# Ubuntu
sudo apt-get install postgresql-15

# 创建数据库
psql -U postgres
CREATE DATABASE aimrp;
```

#### Redis

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu
sudo apt-get install redis-server

# 测试连接
redis-cli ping
```

---

## 三、项目配置

### 3.1 克隆项目

```bash
git clone https://github.com/PunjabLee/AI-MRP.git
cd AI-MRP
git checkout develop
```

### 3.2 后端配置

```bash
cd code/backend

# 编译项目（可选，首次下载依赖）
mvn clean compile -DskipTests
```

配置数据库连接（修改 `aimrp-api/src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/aimrp
    username: postgres
    password: your_password
```

初始化数据库：

```bash
# 执行 DDL
psql -U postgres -d aimrp -f ../docs/data-architecture/DATABASE_DDL.sql
psql -U postgres -d aimrp -f ../docs/data-architecture/DATABASE_DDL_SUPPLEMENT.sql
```

### 3.3 AI 服务配置

```bash
cd code/ai-service

# 创建环境变量文件
cp .env.example .env

# 编辑配置
vim .env
```

`.env` 配置示例：

```env
# DeepSeek API（推荐）
DEEPSEEK_API_KEY=your_deepseek_api_key
LLM_PROVIDER=deepseek

# 或使用 OpenAI
# OPENAI_API_KEY=your_openai_api_key
# LLM_PROVIDER=openai

# 数据库
DATABASE_URL=postgresql://postgres:password@localhost:5432/aimrp

# Redis
REDIS_URL=redis://localhost:6379/0
```

### 3.4 前端配置

```bash
cd code/frontend/aimrp-admin

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev
```

---

## 四、启动服务

### 4.1 启动顺序

```
1. PostgreSQL (5432)
2. Redis (6379)
3. 后端 API (8080)
4. AI 服务 (8000)
5. 前端 (5173)
```

### 4.2 启动命令

#### 后端 API

```bash
cd code/backend/aimrp-api
mvn spring-boot:run
# 或打包后运行
mvn clean package -DskipTests
java -jar aimrp-api/target/aimrp-api-1.0.0-SNAPSHOT.jar
```

#### AI 服务

```bash
cd code/ai-service
source venv/bin/activate
uvicorn app.main:app --reload --port 8000
```

#### 前端

```bash
cd code/frontend/aimrp-admin
pnpm dev
```

---

## 五、服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot |
| AI 服务 | 8000 | FastAPI |
| 前端 | 5173 | Vite Dev Server |
| PostgreSQL | 5432 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 六、常见问题

### 6.1 数据库连接失败

- 检查 PostgreSQL 是否启动：`pg_ctl status`
- 检查用户名密码是否正确
- 检查数据库是否存在

### 6.2 AI 服务无法启动

- 检查 Python 版本：`python --version`（需要 3.11+）
- 检查依赖是否安装：`pip list`
- 检查 API Key 是否配置

### 6.3 前端无法启动

- 检查 Node 版本：`node -v`（需要 18+）
- 检查 pnpm 安装：`pnpm -v`
- 删除 node_modules 重新安装：`rm -rf node_modules && pnpm install`

---

## 七、开发工具推荐

### 7.1 IDE 配置

#### IntelliJ IDEA

- 安装 Lombok 插件
- 配置 Maven
- 配置 Python 解释器

#### VS Code

- Extensions:
  - ESLint
  - Prettier
  - Python
  - Tailwind CSS IntelliSense
  - Tabnine

---

## 八、后续步骤

1. 确认环境搭建完成
2. 从 `develop` 分支创建功能分支
3. 开始功能开发
4. 提交代码并创建 Pull Request

---

*开发环境配置完成*
