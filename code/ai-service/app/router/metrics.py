"""
Metrics Router - 监控指标 API

提供：
- 预测准确率统计
- 排程效率统计
- 系统性能统计
- 趋势分析
"""
from fastapi import APIRouter, Query
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any

from app.integration.metrics_service import (
    MetricsStore,
    get_metrics_store,
    ForecastAccuracyMetric,
    ScheduleEfficiencyMetric,
    SystemPerformanceMetric,
    record_forecast_accuracy,
    record_schedule_efficiency,
    record_system_performance
)
from app.utils.response import ApiResponse

router = APIRouter()


# ========== Request Models ==========

class RecordForecastAccuracyRequest(BaseModel):
    """记录预测准确率请求"""
    item_code: str
    forecast_date: str
    actual_qty: float
    predicted_qty: float
    confidence_level: float = 0.95


class RecordScheduleEfficiencyRequest(BaseModel):
    """记录排程效率请求"""
    schedule_id: str
    total_orders: int
    completed_orders: int
    delayed_orders: int
    total_planned_hours: float
    actual_hours: float
    resource_utilization: Dict[str, float]


class RecordSystemPerformanceRequest(BaseModel):
    """记录系统性能请求"""
    endpoint: str
    method: str
    response_time_ms: float
    status_code: int
    error: Optional[str] = None


# ========== API Endpoints ==========

# ---------- 预测准确率 ----------

@router.post("/forecast/record")
async def record_forecast_metric(request: RecordForecastAccuracyRequest):
    """记录预测准确率"""
    try:
        record_forecast_accuracy(
            item_code=request.item_code,
            forecast_date=request.forecast_date,
            actual_qty=request.actual_qty,
            predicted_qty=request.predicted_qty,
            confidence_level=request.confidence_level
        )
        return ApiResponse.response_success(message="记录成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"记录失败: {str(e)}")


@router.get("/forecast/stats")
async def get_forecast_stats(
    item_code: Optional[str] = None,
    days: int = 30
):
    """获取预测准确率统计"""
    try:
        store = get_metrics_store()
        stats = store.get_forecast_accuracy(item_code, days)
        return ApiResponse.response_success(data=stats, message="获取成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"获取失败: {str(e)}")


# ---------- 排程效率 ----------

@router.post("/schedule/record")
async def record_schedule_metric(request: RecordScheduleEfficiencyRequest):
    """记录排程效率"""
    try:
        record_schedule_efficiency(
            schedule_id=request.schedule_id,
            total_orders=request.total_orders,
            completed_orders=request.completed_orders,
            delayed_orders=request.delayed_orders,
            total_planned_hours=request.total_planned_hours,
            actual_hours=request.actual_hours,
            resource_utilization=request.resource_utilization
        )
        return ApiResponse.response_success(message="记录成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"记录失败: {str(e)}")


@router.get("/schedule/stats")
async def get_schedule_stats(days: int = 30):
    """获取排程效率统计"""
    try:
        store = get_metrics_store()
        stats = store.get_schedule_efficiency(days)
        return ApiResponse.response_success(data=stats, message="获取成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"获取失败: {str(e)}")


# ---------- 系统性能 ----------

@router.post("/system/record")
async def record_system_metric(request: RecordSystemPerformanceRequest):
    """记录系统性能"""
    try:
        record_system_performance(
            endpoint=request.endpoint,
            method=request.method,
            response_time_ms=request.response_time_ms,
            status_code=request.status_code,
            error=request.error
        )
        return ApiResponse.response_success(message="记录成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"记录失败: {str(e)}")


@router.get("/system/stats")
async def get_system_stats(hours: int = 24):
    """获取系统性能统计"""
    try:
        store = get_metrics_store()
        stats = store.get_system_performance(hours)
        return ApiResponse.response_success(data=stats, message="获取成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"获取失败: {str(e)}")


# ---------- 趋势分析 ----------

@router.get("/trend/{metric_type}")
async def get_metric_trend(metric_type: str, days: int = 7):
    """获取指标趋势
    
    metric_type: forecast, schedule, system
    """
    try:
        store = get_metrics_store()
        trend = store.get_trend(metric_type, days)
        return ApiResponse.response_success(data=trend, message="获取成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"获取失败: {str(e)}")


# ---------- 汇总仪表盘 ----------

@router.get("/dashboard")
async def get_dashboard(days: int = 7):
    """获取监控仪表盘"""
    try:
        store = get_metrics_store()
        
        # 获取各项统计
        forecast_stats = store.get_forecast_accuracy(days=days)
        schedule_stats = store.get_schedule_efficiency(days=days)
        system_stats = store.get_system_performance(hours=days*24)
        
        # 获取趋势
        forecast_trend = store.get_trend("forecast", days)
        schedule_trend = store.get_trend("schedule", days)
        
        dashboard = {
            "forecast_accuracy": {
                "avg_mape": forecast_stats.get("avg_mape", 0),
                "avg_rmse": forecast_stats.get("avg_rmse", 0),
                "count": forecast_stats.get("count", 0),
                "trend": forecast_trend.get("trend", [])[-7:]
            },
            "schedule_efficiency": {
                "completion_rate": schedule_stats.get("avg_completion_rate", 0),
                "delay_rate": schedule_stats.get("avg_delay_rate", 0),
                "utilization": schedule_stats.get("avg_utilization", 0),
                "trend": schedule_trend.get("trend", [])[-7:]
            },
            "system_performance": {
                "success_rate": system_stats.get("success_rate", 0),
                "avg_response_time_ms": system_stats.get("avg_response_time_ms", 0),
                "p95_response_time_ms": system_stats.get("p95_response_time_ms", 0),
                "request_count": system_stats.get("count", 0)
            },
            "period_days": days
        }
        
        return ApiResponse.response_success(data=dashboard, message="获取成功")
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"获取失败: {str(e)}")


# ---------- 健康检查 ----------

@router.get("/health")
async def metrics_health():
    """指标服务健康检查"""
    try:
        store = get_metrics_store()
        return ApiResponse.response_success(
            data={
                "status": "healthy",
                "forecast_count": len(store._metrics.get("forecast", [])),
                "schedule_count": len(store._metrics.get("schedule", [])),
                "system_count": len(store._metrics.get("system", []))
            },
            message="健康"
        )
    except Exception as e:
        return ApiResponse.response_bad_request(message=f"异常: {str(e)}")
