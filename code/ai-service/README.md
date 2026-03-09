# AI MRP Python Service

> AI 微服务 - FastAPI + LangChain + OR-Tools

## 环境要求

- Python 3.11+
- PostgreSQL 15+
- Redis 7.0+

## 安装依赖

```bash
pip install -r requirements.txt
```

## 运行

```bash
# 开发环境
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 生产环境
gunicorn app.main:app -w 4 -k uvicorn.workers.UvicornWorker
```

## 配置

见 `config/settings.py`
