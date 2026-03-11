"""
统一API响应结构

提供标准的API响应格式，包含：
- 状态码和消息
- 成功/失败标识
- 数据载荷
- 错误详情
- 时间戳和请求ID
- 元数据（分页等）
"""
from typing import Any, Optional, Dict, List, Type, TypeVar
from pydantic import BaseModel, Field
from datetime import datetime
import uuid
import traceback

# 响应码定义
class ResponseCode:
    """响应状态码"""
    SUCCESS = 200
    CREATED = 201
    ACCEPTED = 202
    NO_CONTENT = 204
    
    # 客户端错误
    BAD_REQUEST = 400
    UNAUTHORIZED = 401
    FORBIDDEN = 403
    NOT_FOUND = 404
    METHOD_NOT_ALLOWED = 405
    CONFLICT = 409
    UNPROCESSABLE_ENTITY = 422
    TOO_MANY_REQUESTS = 429
    
    # 服务器错误
    INTERNAL_ERROR = 500
    NOT_IMPLEMENTED = 501
    BAD_GATEWAY = 502
    SERVICE_UNAVAILABLE = 503


class ApiResponse(BaseModel):
    """
    统一API响应结构
    
    所有API接口统一使用此响应格式
    """
    # 状态码
    code: int = Field(default=200, description="HTTP状态码")
    
    # 状态消息
    message: str = Field(default="success", description="状态描述")
    
    # 是否成功
    success: bool = Field(default=True, description="业务成功标识")
    
    # 响应数据
    data: Any = Field(default=None, description="响应数据载荷")
    
    # 错误详情
    error: Optional[Dict] = Field(default=None, description="错误详情")
    
    # 时间戳
    timestamp: str = Field(default="", description="响应时间戳")
    
    # 请求ID（用于链路追踪）
    request_id: str = Field(default="", description="请求唯一标识")
    
    # 元数据（分页、总数等）
    metadata: Optional[Dict] = Field(default=None, description="元数据")
    
    def __init__(self, **data):
        if 'timestamp' not in data or not data.get('timestamp'):
            data['timestamp'] = datetime.now().isoformat()
        if 'request_id' not in data or not data.get('request_id'):
            data['request_id'] = str(uuid.uuid4())[:8]
        super().__init__(**data)
    
    @classmethod
    def success(cls, data: Any = None, message: str = "success", 
               metadata: Dict = None, code: int = 200) -> 'ApiResponse':
        """
        成功响应
        
        Args:
            data: 响应数据
            message: 成功消息
            metadata: 元数据（如分页信息）
            code: HTTP状态码
        
        Returns:
            ApiResponse实例
        """
        return cls(
            code=code,
            message=message,
            success=True,
            data=data,
            error=None,
            metadata=metadata
        )
    
    @classmethod
    def error(cls, code: int = 500, message: str = "Internal Server Error",
              error: Dict = None, data: Any = None) -> 'ApiResponse':
        """
        错误响应
        
        Args:
            code: HTTP状态码
            message: 错误消息
            error: 错误详情
            data: 部分错误数据（如验证失败的字段）
        
        Returns:
            ApiResponse实例
        """
        return cls(
            code=code,
            message=message,
            success=False,
            data=data,
            error=error
        )
    
    @classmethod
    def bad_request(cls, message: str = "Bad Request", error: Dict = None) -> 'ApiResponse':
        """400 错误响应"""
        return cls.error(code=ResponseCode.BAD_REQUEST, message=message, error=error)
    
    @classmethod
    def unauthorized(cls, message: str = "Unauthorized") -> 'ApiResponse':
        """401 错误响应"""
        return cls.error(code=ResponseCode.UNAUTHORIZED, message=message)
    
    @classmethod
    def forbidden(cls, message: str = "Forbidden") -> 'ApiResponse':
        """403 错误响应"""
        return cls.error(code=ResponseCode.FORBIDDEN, message=message)
    
    @classmethod
    def not_found(cls, message: str = "Resource Not Found") -> 'ApiResponse':
        """404 错误响应"""
        return cls.error(code=ResponseCode.NOT_FOUND, message=message)
    
    @classmethod
    def validation_error(cls, message: str = "Validation Error", error: Dict = None) -> 'ApiResponse':
        """422 验证错误响应"""
        return cls.error(
            code=ResponseCode.UNPROCESSABLE_ENTITY, 
            message=message, 
            error=error
        )
    
    @classmethod
    def server_error(cls, message: str = "Internal Server Error", error: Dict = None) -> 'ApiResponse':
        """500 服务器错误响应"""
        error_detail = error or {}
        if not error_detail.get('stack_trace'):
            error_detail['stack_trace'] = traceback.format_exc()[:500]
        return cls.error(
            code=ResponseCode.INTERNAL_ERROR, 
            message=message, 
            error=error_detail
        )
    
    @classmethod
    def paginated(cls, data: List, total: int, page: int = 1, 
                  page_size: int = 20, message: str = "success") -> 'ApiResponse':
        """
        分页响应
        
        Args:
            data: 当前页数据列表
            total: 总记录数
            page: 当前页码
            page_size: 每页大小
            message: 成功消息
        
        Returns:
            ApiResponse实例
        """
        total_pages = (total + page_size - 1) // page_size
        
        metadata = {
            "pagination": {
                "page": page,
                "page_size": page_size,
                "total": total,
                "total_pages": total_pages,
                "has_next": page < total_pages,
                "has_prev": page > 1
            }
        }
        
        return cls.success(data=data, message=message, metadata=metadata)


class ApiListResponse(BaseModel):
    """
    列表数据响应（用于data字段）
    
    提供更丰富的列表元数据
    """
    items: List[Any] = Field(default_factory=list, description="数据列表")
    total: int = Field(default=0, description="总记录数")
    page: int = Field(default=1, description="当前页")
    page_size: int = Field(default=20, description="每页大小")
    
    @classmethod
    def create(cls, items: List[Any], total: int = None, 
               page: int = 1, page_size: int = 20) -> 'ApiListResponse':
        """创建列表响应"""
        if total is None:
            total = len(items)
        return cls(
            items=items,
            total=total,
            page=page,
            page_size=page_size
        )


# 分页参数模型
class PageParams(BaseModel):
    """分页请求参数"""
    page: int = Field(default=1, ge=1, description="页码")
    page_size: int = Field(default=20, ge=1, le=100, description="每页大小")
    
    @property
    def offset(self) -> int:
        """计算偏移量"""
        return (self.page - 1) * self.page_size
    
    @property
    def limit(self) -> int:
        """返回限制数"""
        return self.page_size


# 响应码映射
CODE_MESSAGE_MAP = {
    200: "success",
    201: "Created",
    204: "No Content",
    400: "Bad Request",
    401: "Unauthorized",
    403: "Forbidden",
    404: "Not Found",
    405: "Method Not Allowed",
    409: "Conflict",
    422: "Unprocessable Entity",
    429: "Too Many Requests",
    500: "Internal Server Error",
    501: "Not Implemented",
    502: "Bad Gateway",
    503: "Service Unavailable"
}


def get_code_message(code: int) -> str:
    """获取状态码对应的默认消息"""
    return CODE_MESSAGE_MAP.get(code, "Unknown Error")
