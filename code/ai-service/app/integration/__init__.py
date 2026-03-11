"""
AI集成模块

提供Java-Python数据交换协议和结果持久化
"""
from app.integration.protocol import (
    AIRequest,
    AIResponse,
    RequestType,
    CallbackConfig,
    create_success_response,
    create_error_response,
    create_progress_response,
    request_to_dict,
    response_to_dict,
    parse_request,
    parse_response,
    validate_signature,
    generate_signature
)

from app.integration.persistence import (
    AIResult,
    ResultStatus,
    ResultStore,
    InMemoryResultStore,
    CallbackManager,
    AIIntegrationService,
    get_integration_service,
    set_integration_service
)

__all__ = [
    # Protocol
    "AIRequest",
    "AIResponse", 
    "RequestType",
    "CallbackConfig",
    "create_success_response",
    "create_error_response",
    "create_progress_response",
    "request_to_dict",
    "response_to_dict",
    "parse_request",
    "parse_response",
    "validate_signature",
    "generate_signature",
    
    # Persistence
    "AIResult",
    "ResultStatus",
    "ResultStore",
    "InMemoryResultStore",
    "CallbackManager",
    "AIIntegrationService",
    "get_integration_service",
    "set_integration_service"
]
