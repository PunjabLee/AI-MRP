"""
Java-Python 数据交换协议

定义统一的请求/响应格式，用于Java后端与Python AI服务的通信

协议版本: 1.0
日期: 2026-03-11
"""
from typing import Any, Dict, List, Optional
from pydantic import BaseModel, Field
from datetime import datetime
from enum import Enum
import uuid


class RequestType(str, Enum):
    """请求类型枚举"""
    # 预测相关
    PREDICT_DEMAND = "PREDICT_DEMAND"           # 需求预测
    PREDICT_SAFETY_STOCK = "PREDICT_SAFETY_STOCK"  # 安全库存计算
    PREDICT_BATCH = "PREDICT_BATCH"             # 批量预测
    PREDICT_COMPARE = "PREDICT_COMPARE"          # 预测方法对比
    
    # 排程相关
    SCHEDULE_OPTIMIZE = "SCHEDULE_OPTIMIZE"     # 排程优化
    SCHEDULE_FEASIBILITY = "SCHEDULE_FEASIBILITY"  # 可行性检查
    SCHEDULE_SCENARIOS = "SCHEDULE_SCENARIOS"   # 场景对比
    SCHEDULE_CAPACITY = "SCHEDULE_CAPACITY"      # 产能分析
    
    # 对话相关
    CHAT_MESSAGE = "CHAT_MESSAGE"              # AI对话
    
    # What-if
    WHATIF_SIMULATE = "WHATIF_SIMULATE"          # What-if模拟
    IMPACT_ANALYZE = "IMPACT_ANALYZE"            # 影响分析


class CallbackConfig(BaseModel):
    """回调配置"""
    url: str = Field(..., description="回调URL")
    method: str = Field(default="POST", description="回调方法")
    token: Optional[str] = Field(default=None, description="认证Token")
    retry: int = Field(default=3, ge=0, le=5, description="重试次数")
    timeout: int = Field(default=30, ge=5, le=60, description="超时秒数")


class AIRequest(BaseModel):
    """
    统一请求格式
    
    Java后端调用Python AI服务时使用此格式
    """
    # 协议基础
    request_id: str = Field(
        default_factory=lambda: f"req_{uuid.uuid4().hex[:12]}",
        description="请求唯一标识"
    )
    timestamp: str = Field(
        default_factory=lambda: datetime.now().isoformat(),
        description="请求时间戳"
    )
    type: RequestType = Field(..., description="请求类型")
    
    # 数据载荷
    data: Dict[str, Any] = Field(default_factory=dict, description="请求数据")
    
    # 回调配置 (可选)
    callback: Optional[CallbackConfig] = Field(default=None, description="异步回调配置")
    
    # 认证信息
    app_id: Optional[str] = Field(default=None, description="应用ID")
    signature: Optional[str] = Field(default=None, description="签名")
    
    # 扩展参数
    timeout: int = Field(default=60, ge=10, le=300, description="请求超时秒数")
    priority: int = Field(default=5, ge=1, le=10, description="优先级")
    metadata: Optional[Dict[str, Any]] = Field(default=None, description="扩展元数据")
    
    class Config:
        use_enum_values = True


class AIResponse(BaseModel):
    """
    统一响应格式
    
    Python AI服务返回给Java后端使用此格式
    """
    # 协议基础
    request_id: str = Field(..., description="对应请求ID")
    timestamp: str = Field(
        default_factory=lambda: datetime.now().isoformat(),
        description="响应时间戳"
    )
    
    # 状态信息
    code: int = Field(default=200, description="状态码")
    message: str = Field(default="success", description="状态消息")
    success: bool = Field(default=True, description="是否成功")
    
    # 数据载荷
    data: Optional[Dict[str, Any]] = Field(default=None, description="响应数据")
    
    # 错误信息
    error: Optional[Dict[str, Any]] = Field(default=None, description="错误详情")
    
    # 性能信息
    processing_time_ms: int = Field(default=0, description="处理耗时(毫秒)")
    
    # 进度信息 (用于长任务)
    progress: Optional[float] = Field(default=None, description="进度 0-1")
    status: Optional[str] = Field(default=None, description="任务状态: pending/processing/completed/failed")
    
    # 扩展
    metadata: Optional[Dict[str, Any]] = Field(default=None, description="扩展元数据")


# ========== 便捷工厂方法 ==========

def create_success_response(
    request_id: str,
    data: Dict[str, Any],
    message: str = "success",
    processing_time_ms: int = 0
) -> AIResponse:
    """创建成功响应"""
    return AIResponse(
        request_id=request_id,
        code=200,
        message=message,
        success=True,
        data=data,
        processing_time_ms=processing_time_ms,
        status="completed"
    )


def create_error_response(
    request_id: str,
    code: int,
    message: str,
    error: Dict[str, Any] = None,
    processing_time_ms: int = 0
) -> AIResponse:
    """创建错误响应"""
    return AIResponse(
        request_id=request_id,
        code=code,
        message=message,
        success=False,
        data=None,
        error=error,
        processing_time_ms=processing_time_ms,
        status="failed"
    )


def create_progress_response(
    request_id: str,
    progress: float,
    message: str = "processing",
    status: str = "processing"
) -> AIResponse:
    """创建进度响应"""
    return AIResponse(
        request_id=request_id,
        code=202,
        message=message,
        success=True,
        progress=progress,
        status=status
    )


# ========== 数据转换 ==========

def request_to_dict(req: AIRequest) -> Dict[str, Any]:
    """将请求转换为字典"""
    return req.model_dump(mode='json')


def response_to_dict(resp: AIResponse) -> Dict[str, Any]:
    """将响应转换为字典"""
    return resp.model_dump(mode='json')


def parse_request(data: Dict[str, Any]) -> AIRequest:
    """解析请求"""
    return AIRequest(**data)


def parse_response(data: Dict[str, Any]) -> AIResponse:
    """解析响应"""
    return AIResponse(**data)


# ========== 验证工具 ==========

def validate_signature(
    request: AIRequest,
    secret: str,
    signature_field: str = "signature"
) -> bool:
    """
    验证请求签名
    
    Args:
        request: 请求对象
        secret: 密钥
        signature_field: 签名字段名
    
    Returns:
        签名是否有效
    """
    import hmac
    import hashlib
    
    # 构建签名字符串
    sign_string = f"{request.request_id}{request.timestamp}{request.type}"
    
    # 计算签名
    expected_signature = hmac.new(
        secret.encode(),
        sign_string.encode(),
        hashlib.sha256
    ).hexdigest()
    
    return hmac.compare_digest(request.signature or "", expected_signature)


def generate_signature(
    request: AIRequest,
    secret: str
) -> str:
    """生成请求签名"""
    import hmac
    import hashlib
    
    sign_string = f"{request.request_id}{request.timestamp}{request.type}"
    
    return hmac.new(
        secret.encode(),
        sign_string.encode(),
        hashlib.sha256
    ).hexdigest()
