"""
AI MRP Python Service - Main Entry
"""
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config.settings import get_settings
from app.router import chat, predict, schedule
from app.utils.response import ApiResponse
from app.integration import gateway as integration_gateway

settings = get_settings()

# 创建 FastAPI 应用
app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    debug=settings.debug,
    description="AI MRP 智能物料需求计划系统 - AI 微服务"
)

# CORS 中间件
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 全局异常处理
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """全局异常处理器"""
    return ApiResponse.server_error(
        message="服务器内部错误",
        error={"path": str(request.url), "error": str(exc)[:200]}
    )


@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    """值错误处理器"""
    return ApiResponse.validation_error(
        message="参数验证错误",
        error={"error": str(exc)}
    )


@app.get("/")
async def root():
    """根路由"""
    return ApiResponse.success(
        data={
            "name": settings.app_name,
            "version": settings.app_version,
            "status": "running"
        },
        message="服务运行中"
    )


@app.get("/health")
async def health():
    """健康检查"""
    return ApiResponse.success(data={"status": "healthy"}, message="健康")


# 注册路由
app.include_router(chat.router, prefix="/chat", tags=["Chat"])
app.include_router(predict.router, prefix="/predict", tags=["Predict"])
app.include_router(schedule.router, prefix="/schedule", tags=["Schedule"])
app.include_router(integration_gateway.router, prefix="/integration", tags=["Integration"])


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug
    )
