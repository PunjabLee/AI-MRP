"""
AI集成网关

提供统一的API入口，处理来自Java后端的请求
- 请求验证
- 结果存储
- 回调触发
"""
from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from datetime import datetime
import time
import logging

from app.integration.protocol import (
    AIRequest,
    AIResponse,
    RequestType,
    create_success_response,
    create_error_response,
    parse_request
)
from app.integration.persistence import (
    AIIntegrationService,
    AIResult,
    ResultStatus,
    get_integration_service
)

logger = logging.getLogger(__name__)
router = APIRouter()


class GatewayRequest(BaseModel):
    """网关请求 (兼容旧格式)"""
    type: str = Field(..., description="请求类型")
    data: Dict[str, Any] = Field(default_factory=dict, description="请求数据")
    callback_url: Optional[str] = Field(default=None, description="回调URL")
    request_id: Optional[str] = Field(default=None, description="请求ID")


class GatewayResponse(BaseModel):
    """网关响应"""
    request_id: str
    code: int
    message: str
    success: bool
    data: Optional[Dict[str, Any]]
    error: Optional[Dict[str, Any]]
    processing_time_ms: int
    status: Optional[str] = None


# 全局服务
_integration = get_integration_service()


# ========== 统一入口 ==========

@router.post("/invoke", response_model=GatewayResponse)
async def invoke(request: GatewayRequest):
    """
    统一调用入口
    
    支持所有AI服务功能
    """
    start_time = time.time()
    request_id = request.request_id or f"req_{datetime.now().strftime('%Y%m%d%H%M%S')}"
    
    try:
        # 解析请求类型
        try:
            req_type = RequestType(request.type)
        except ValueError:
            return GatewayResponse(
                request_id=request_id,
                code=400,
                message=f"Unknown request type: {request.type}",
                success=False,
                data=None,
                error={"type": "INVALID_REQUEST_TYPE"},
                processing_time_ms=int((time.time() - start_time) * 1000)
            )
        
        # 创建待处理结果
        result = _integration.create_pending_result(
            request_id=request_id,
            request_type=request.type,
            callback_url=request.callback_url
        )
        
        # 处理请求
        data = await _process_request(req_type, request.data)
        
        # 完成结果
        processing_time = int((time.time() - start_time) * 1000)
        _integration.complete_result(
            request_id=request_id,
            data=data,
            processing_time_ms=processing_time
        )
        
        return GatewayResponse(
            request_id=request_id,
            code=200,
            message="success",
            success=True,
            data=data,
            error=None,
            processing_time_ms=processing_time,
            status="completed"
        )
        
    except Exception as e:
        logger.error(f"Request {request_id} failed: {e}")
        processing_time = int((time.time() - start_time) * 1000)
        
        # 记录失败
        _integration.fail_result(
            request_id=request_id,
            error={"type": type(e).__name__, "message": str(e)},
            processing_time_ms=processing_time
        )
        
        return GatewayResponse(
            request_id=request_id,
            code=500,
            message="Internal Server Error",
            success=False,
            data=None,
            error={"type": type(e).__name__, "message": str(e)[:200]},
            processing_time_ms=processing_time,
            status="failed"
        )


@router.post("/invoke/async", response_model=GatewayResponse)
async def invoke_async(request: GatewayRequest):
    """
    异步调用入口
    
    立即返回request_id，通过回调或轮询获取结果
    """
    start_time = time.time()
    request_id = request.request_id or f"req_{datetime.now().strftime('%Y%m%d%H%M%S')}"
    
    try:
        # 解析请求类型
        try:
            req_type = RequestType(request.type)
        except ValueError:
            return GatewayResponse(
                request_id=request_id,
                code=400,
                message=f"Unknown request type: {request.type}",
                success=False,
                data=None,
                error={"type": "INVALID_REQUEST_TYPE"},
                processing_time_ms=int((time.time() - start_time) * 1000)
            )
        
        # 创建待处理结果
        result = _integration.create_pending_result(
            request_id=request_id,
            request_type=request.type,
            callback_url=request.callback_url
        )
        
        # 更新为处理中
        _integration.update_result(
            request_id,
            status=ResultStatus.PROCESSING
        )
        
        # 异步处理
        # TODO: 放入后台任务队列
        try:
            data = await _process_request(req_type, request.data)
            _integration.complete_result(
                request_id=request_id,
                data=data,
                processing_time_ms=int((time.time() - start_time) * 1000)
            )
        except Exception as e:
            _integration.fail_result(
                request_id=request_id,
                error={"type": type(e).__name__, "message": str(e)},
                processing_time_ms=int((time.time() - start_time) * 1000)
            )
        
        return GatewayResponse(
            request_id=request_id,
            code=202,
            message="Accepted - processing",
            success=True,
            data={"status": "processing"},
            error=None,
            processing_time_ms=int((time.time() - start_time) * 1000),
            status="processing"
        )
        
    except Exception as e:
        return GatewayResponse(
            request_id=request_id,
            code=500,
            message="Internal Server Error",
            success=False,
            data=None,
            error={"type": type(e).__name__, "message": str(e)[:200]},
            processing_time_ms=int((time.time() - start_time) * 1000),
            status="failed"
        )


# ========== 结果查询 ==========

@router.get("/result/{request_id}", response_model=GatewayResponse)
async def get_result(request_id: str):
    """查询结果"""
    start_time = time.time()
    
    result = _integration.get_result(request_id)
    
    if not result:
        return GatewayResponse(
            request_id=request_id,
            code=404,
            message="Result not found",
            success=False,
            data=None,
            error={"type": "NOT_FOUND"},
            processing_time_ms=int((time.time() - start_time) * 1000)
        )
    
    return GatewayResponse(
        request_id=request_id,
        code=200,
        message="success",
        success=result.status == ResultStatus.COMPLETED,
        data=result.data,
        error=result.error,
        processing_time_ms=result.processing_time_ms,
        status=result.status.value
    )


@router.get("/result/{request_id}/wait")
async def wait_result(request_id: str, timeout: int = 60):
    """等待结果 (轮询)"""
    start_time = time.time()
    
    result = _integration.get_result_with_wait(
        request_id=request_id,
        timeout=timeout
    )
    
    if not result:
        return GatewayResponse(
            request_id=request_id,
            code=404,
            message="Result not found",
            success=False,
            data=None,
            error={"type": "NOT_FOUND"},
            processing_time_ms=int((time.time() - start_time) * 1000)
        )
    
    if result.status == ResultStatus.TIMEOUT:
        return GatewayResponse(
            request_id=request_id,
            code=408,
            message="Request timeout",
            success=False,
            data=None,
            error={"type": "TIMEOUT"},
            processing_time_ms=int((time.time() - start_time) * 1000),
            status="timeout"
        )
    
    return GatewayResponse(
        request_id=request_id,
        code=200,
        message="success",
        success=result.status == ResultStatus.COMPLETED,
        data=result.data,
        error=result.error,
        processing_time_ms=result.processing_time_ms,
        status=result.status.value
    )


@router.get("/results")
async def list_results(status: Optional[str] = None, limit: int = 100):
    """列出结果"""
    result_status = ResultStatus(status) if status else None
    results = _integration.list_results(result_status, limit)
    
    return {
        "request_id": "",
        "code": 200,
        "message": "success",
        "success": True,
        "data": {
            "results": [
                {
                    "request_id": r.request_id,
                    "type": r.request_type,
                    "status": r.status.value,
                    "created_at": r.created_at.isoformat(),
                    "completed_at": r.completed_at.isoformat() if r.completed_at else None,
                    "processing_time_ms": r.processing_time_ms
                }
                for r in results
            ],
            "total": len(results)
        },
        "error": None,
        "processing_time_ms": 0
    }


# ========== 内部处理 ==========

async def _process_request(req_type: RequestType, data: Dict[str, Any]) -> Dict[str, Any]:
    """处理请求"""
    
    # 导入各服务
    from app.algorithms import create_forecast_engine, auto_select_algorithm
    from app.algorithms import create_scheduler
    from app.algorithms import SafetyStockEngine
    
    if req_type == RequestType.PREDICT_DEMAND:
        # 需求预测
        item_code = data.get("item_code")
        forecast_days = data.get("forecast_days", 30)
        method = data.get("method", "auto")
        
        # TODO: 从数据库获取真实历史数据
        historical_data = _generate_mock_data(item_code)
        
        if method == "auto":
            method = auto_select_algorithm(historical_data)
        
        engine = create_forecast_engine(method)
        engine.fit(historical_data)
        results = engine.forecast(forecast_days, 0.95)
        
        return {
            "item_code": item_code,
            "method": method,
            "forecast": [
                {
                    "date": r["date"].strftime("%Y-%m-%d") if hasattr(r["date"], "strftime") else str(r["date"]),
                    "qty": round(r["qty"], 2)
                }
                for r in results
            ]
        }
    
    elif req_type == RequestType.PREDICT_SAFETY_STOCK:
        # 安全库存计算
        item_code = data.get("item_code")
        lead_time = data.get("lead_time_days", 7)
        service_level = data.get("service_level", 0.95)
        
        historical_data = _generate_demand_data(item_code)
        
        engine = SafetyStockEngine()
        result = engine.calculate(
            item_code=item_code,
            historical_demand=historical_data,
            lead_time_days=lead_time,
            service_level=service_level
        )
        
        return {
            "item_code": item_code,
            "safety_stock": result.safety_stock,
            "reorder_point": round(result.reorder_point, 2),
            "optimal_order_qty": result.optimal_order_qty,
            "method": result.method
        }
    
    elif req_type == RequestType.SCHEDULE_OPTIMIZE:
        # 排程优化
        orders = data.get("orders", [])
        resources = data.get("resources", [])
        goal = data.get("goal", "makespan")
        
        result = create_scheduler(orders, resources, goal)
        
        return {
            "schedule_id": result.schedule_id,
            "status": result.status,
            "makespan_hours": result.makespan_hours,
            "makespan_days": result.makespan_days,
            "total_cost": result.total_cost,
            "gantt_data": result.gantt_data
        }
    
    else:
        raise ValueError(f"Unsupported request type: {req_type}")


def _generate_mock_data(item_code: str):
    """生成模拟预测数据"""
    import random
    from datetime import datetime, timedelta
    
    base = 100 + hash(item_code) % 100
    data = []
    
    for i in range(90):
        date = datetime.now() - timedelta(days=90-i)
        qty = base + random.randint(-20, 30) + int(i * 0.1)
        data.append({"date": date, "qty": max(0, qty)})
    
    return data


def _generate_demand_data(item_code: str):
    """生成模拟需求数据"""
    import random
    from datetime import datetime, timedelta
    
    base = 80 + hash(item_code) % 50
    data = []
    
    for i in range(60):
        date = datetime.now() - timedelta(days=60-i)
        qty = base + random.randint(-15, 25)
        data.append({"date": date, "qty": max(0, qty)})
    
    return data
