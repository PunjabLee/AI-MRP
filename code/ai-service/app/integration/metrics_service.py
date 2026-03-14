"""
监控指标服务

提供：
1. 预测准确率追踪 (MAPE, MAE, RMSE, Bias)
2. 排程效率指标 (完成率, 延迟率, 资源利用率)
3. 系统性能指标 (响应时间, 吞吐量)
4. 指标存储与查询
5. 趋势分析
"""
import os
import json
import time
import uuid
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
from dataclasses import dataclass, field, asdict
from enum import Enum
import threading
import logging

logger = logging.getLogger(__name__)


class MetricType(str, Enum):
    """指标类型"""
    FORECAST_ACCURACY = "forecast_accuracy"   # 预测准确率
    SCHEDULE_EFFICIENCY = "schedule_efficiency"  # 排程效率
    SYSTEM_PERFORMANCE = "system_performance"  # 系统性能
    BUSINESS_KPI = "business_kpi"            # 业务KPI


class ForecastAccuracyMetric:
    """预测准确率指标"""
    
    def __init__(self,
                 item_code: str,
                 forecast_date: str,
                 actual_qty: float,
                 predicted_qty: float,
                 confidence_level: float = 0.95):
        self.metric_id = f"fc_{uuid.uuid4().hex[:8]}"
        self.item_code = item_code
        self.forecast_date = forecast_date
        self.actual_qty = actual_qty
        self.predicted_qty = predicted_qty
        self.confidence_level = confidence_level
        self.created_at = datetime.now()
        
        # 计算衍生指标
        self.error = actual_qty - predicted_qty
        self.abs_error = abs(self.error)
        self.pct_error = (self.error / actual_qty * 100) if actual_qty != 0 else 0
        self.abs_pct_error = abs(self.pct_error)
        
        # MAPE (Mean Absolute Percentage Error)
        self.mape = self.abs_pct_error
        
        # RMSE (Root Mean Square Error)
        self.rmse = self.abs_error  # 单点rmse = abs_error
    
    def to_dict(self) -> Dict:
        return {
            "metric_id": self.metric_id,
            "metric_type": MetricType.FORECAST_ACCURACY.value,
            "item_code": self.item_code,
            "forecast_date": self.forecast_date,
            "actual_qty": self.actual_qty,
            "predicted_qty": self.predicted_qty,
            "confidence_level": self.confidence_level,
            "error": round(self.error, 2),
            "abs_error": round(self.abs_error, 2),
            "pct_error": round(self.pct_error, 2),
            "abs_pct_error": round(self.abs_pct_error, 2),
            "mape": round(self.mape, 2),
            "rmse": round(self.rmse, 2),
            "created_at": self.created_at.isoformat()
        }


class ScheduleEfficiencyMetric:
    """排程效率指标"""
    
    def __init__(self,
                 schedule_id: str,
                 total_orders: int,
                 completed_orders: int,
                 delayed_orders: int,
                 total_planned_hours: float,
                 actual_hours: float,
                 resource_utilization: Dict[str, float]):
        self.metric_id = f"se_{uuid.uuid4().hex[:8]}"
        self.schedule_id = schedule_id
        self.total_orders = total_orders
        self.completed_orders = completed_orders
        self.delayed_orders = delayed_orders
        self.total_planned_hours = total_planned_hours
        self.actual_hours = actual_hours
        self.resource_utilization = resource_utilization
        self.created_at = datetime.now()
        
        # 计算衍生指标
        self.completion_rate = (completed_orders / total_orders * 100) if total_orders > 0 else 0
        self.delay_rate = (delayed_orders / total_orders * 100) if total_orders > 0 else 0
        self.avg_utilization = sum(resource_utilization.values()) / len(resource_utilization) if resource_utilization else 0
        self.schedule_accuracy = 100 - self.delay_rate  # 简化计算
    
    def to_dict(self) -> Dict:
        return {
            "metric_id": self.metric_id,
            "metric_type": MetricType.SCHEDULE_EFFICIENCY.value,
            "schedule_id": self.schedule_id,
            "total_orders": self.total_orders,
            "completed_orders": self.completed_orders,
            "delayed_orders": self.delayed_orders,
            "completion_rate": round(self.completion_rate, 2),
            "delay_rate": round(self.delay_rate, 2),
            "total_planned_hours": round(self.total_planned_hours, 2),
            "actual_hours": round(self.actual_hours, 2),
            "resource_utilization": self.resource_utilization,
            "avg_utilization": round(self.avg_utilization, 2),
            "schedule_accuracy": round(self.schedule_accuracy, 2),
            "created_at": self.created_at.isoformat()
        }


class SystemPerformanceMetric:
    """系统性能指标"""
    
    def __init__(self,
                 endpoint: str,
                 method: str,
                 response_time_ms: float,
                 status_code: int,
                 error: Optional[str] = None):
        self.metric_id = f"sp_{uuid.uuid4().hex[:8]}"
        self.endpoint = endpoint
        self.method = method
        self.response_time_ms = response_time_ms
        self.status_code = status_code
        self.error = error
        self.created_at = datetime.now()
        
        # 状态
        self.is_success = 200 <= status_code < 300
        self.is_slow = response_time_ms > 1000  # > 1s 视为慢请求
    
    def to_dict(self) -> Dict:
        return {
            "metric_id": self.metric_id,
            "metric_type": MetricType.SYSTEM_PERFORMANCE.value,
            "endpoint": self.endpoint,
            "method": self.method,
            "response_time_ms": round(self.response_time_ms, 2),
            "status_code": self.status_code,
            "is_success": self.is_success,
            "is_slow": self.is_slow,
            "error": self.error,
            "created_at": self.created_at.isoformat()
        }


class MetricsStore:
    """指标存储"""
    
    def __init__(self, storage_path: str = "./metrics"):
        self.storage_path = storage_path
        self._ensure_storage()
        self._metrics: Dict[str, Any] = {
            "forecast": [],
            "schedule": [],
            "system": []
        }
        self._lock = threading.Lock()
        self._load_metrics()
    
    def _ensure_storage(self):
        os.makedirs(self.storage_path, exist_ok=True)
    
    def _load_metrics(self):
        """加载历史指标"""
        for metric_type in ["forecast", "schedule", "system"]:
            file_path = os.path.join(self.storage_path, f"{metric_type}_metrics.json")
            if os.path.exists(file_path):
                try:
                    with open(file_path, 'r') as f:
                        self._metrics[metric_type] = json.load(f)
                except:
                    pass
    
    def _save_metrics(self, metric_type: str):
        """保存指标"""
        file_path = os.path.join(self.storage_path, f"{metric_type}_metrics.json")
        try:
            with open(file_path, 'w') as f:
                json.dump(self._metrics.get(metric_type, []), f, indent=2)
        except Exception as e:
            logger.error(f"Failed to save metrics: {e}")
    
    # ========== 预测准确率 ==========
    
    def record_forecast_accuracy(self, metric: ForecastAccuracyMetric):
        """记录预测准确率"""
        with self._lock:
            self._metrics["forecast"].append(metric.to_dict())
            # 保留最近1000条
            if len(self._metrics["forecast"]) > 1000:
                self._metrics["forecast"] = self._metrics["forecast"][-1000:]
            self._save_metrics("forecast")
    
    def get_forecast_accuracy(self,
                              item_code: Optional[str] = None,
                              days: int = 30) -> Dict:
        """获取预测准确率统计"""
        cutoff = datetime.now() - timedelta(days=days)
        
        with self._lock:
            metrics = [m for m in self._metrics["forecast"]
                      if datetime.fromisoformat(m["created_at"]) > cutoff]
            
            if item_code:
                metrics = [m for m in metrics if m.get("item_code") == item_code]
        
        if not metrics:
            return {"count": 0, "avg_mape": 0, "avg_rmse": 0}
        
        avg_mape = sum(m["mape"] for m in metrics) / len(metrics)
        avg_rmse = sum(m["rmse"] for m in metrics) / len(metrics)
        avg_error = sum(m["error"] for m in metrics) / len(metrics)
        
        return {
            "count": len(metrics),
            "avg_mape": round(avg_mape, 2),
            "avg_rmse": round(avg_rmse, 2),
            "avg_error": round(avg_error, 2),
            "min_mape": round(min(m["mape"] for m in metrics), 2),
            "max_mape": round(max(m["mape"] for m in metrics), 2),
            "by_item": self._group_by_item(metrics)
        }
    
    def _group_by_item(self, metrics: List[Dict]) -> Dict:
        """按物料分组"""
        grouped = {}
        for m in metrics:
            item = m.get("item_code", "unknown")
            if item not in grouped:
                grouped[item] = {"count": 0, "total_mape": 0}
            grouped[item]["count"] += 1
            grouped[item]["total_mape"] += m["mape"]
        
        for item in grouped:
            grouped[item]["avg_mape"] = round(
                grouped[item]["total_mape"] / grouped[item]["count"], 2
            )
            del grouped[item]["total_mape"]
        
        return grouped
    
    # ========== 排程效率 ==========
    
    def record_schedule_efficiency(self, metric: ScheduleEfficiencyMetric):
        """记录排程效率"""
        with self._lock:
            self._metrics["schedule"].append(metric.to_dict())
            if len(self._metrics["schedule"]) > 500:
                self._metrics["schedule"] = self._metrics["schedule"][-500:]
            self._save_metrics("schedule")
    
    def get_schedule_efficiency(self, days: int = 30) -> Dict:
        """获取排程效率统计"""
        cutoff = datetime.now() - timedelta(days=days)
        
        with self._lock:
            metrics = [m for m in self._metrics["schedule"]
                      if datetime.fromisoformat(m["created_at"]) > cutoff]
        
        if not metrics:
            return {"count": 0, "avg_completion_rate": 0, "avg_delay_rate": 0}
        
        avg_completion = sum(m["completion_rate"] for m in metrics) / len(metrics)
        avg_delay = sum(m["delay_rate"] for m in metrics) / len(metrics)
        avg_utilization = sum(m["avg_utilization"] for m in metrics) / len(metrics)
        
        return {
            "count": len(metrics),
            "avg_completion_rate": round(avg_completion, 2),
            "avg_delay_rate": round(avg_delay, 2),
            "avg_utilization": round(avg_utilization, 2),
            "total_orders": sum(m["total_orders"] for m in metrics),
            "completed_orders": sum(m["completed_orders"] for m in metrics),
            "delayed_orders": sum(m["delayed_orders"] for m in metrics)
        }
    
    # ========== 系统性能 ==========
    
    def record_system_performance(self, metric: SystemPerformanceMetric):
        """记录系统性能"""
        with self._lock:
            self._metrics["system"].append(metric.to_dict())
            if len(self._metrics["system"]) > 2000:
                self._metrics["system"] = self._metrics["system"][-2000:]
            self._save_metrics("system")
    
    def get_system_performance(self, hours: int = 24) -> Dict:
        """获取系统性能统计"""
        cutoff = datetime.now() - timedelta(hours=hours)
        
        with self._lock:
            metrics = [m for m in self._metrics["system"]
                      if datetime.fromisoformat(m["created_at"]) > cutoff]
        
        if not metrics:
            return {"count": 0, "avg_response_time": 0}
        
        total_count = len(metrics)
        success_count = sum(1 for m in metrics if m["is_success"])
        slow_count = sum(1 for m in metrics if m["is_slow"])
        
        avg_response = sum(m["response_time_ms"] for m in metrics) / total_count
        p95_response = self._percentile(
            [m["response_time_ms"] for m in metrics], 95
        )
        
        return {
            "count": total_count,
            "success_count": success_count,
            "success_rate": round(success_count / total_count * 100, 2),
            "slow_count": slow_count,
            "slow_rate": round(slow_count / total_count * 100, 2),
            "avg_response_time_ms": round(avg_response, 2),
            "p95_response_time_ms": round(p95_response, 2),
            "by_endpoint": self._group_by_endpoint(metrics)
        }
    
    def _percentile(self, values: List[float], percentile: int) -> float:
        """计算百分位数"""
        if not values:
            return 0
        sorted_values = sorted(values)
        index = int(len(sorted_values) * percentile / 100)
        return sorted_values[min(index, len(sorted_values) - 1)]
    
    def _group_by_endpoint(self, metrics: List[Dict]) -> Dict:
        """按端点分组"""
        grouped = {}
        for m in metrics:
            endpoint = m.get("endpoint", "unknown")
            if endpoint not in grouped:
                grouped[endpoint] = {"count": 0, "total_time": 0}
            grouped[endpoint]["count"] += 1
            grouped[endpoint]["total_time"] += m["response_time_ms"]
        
        for endpoint in grouped:
            grouped[endpoint]["avg_time"] = round(
                grouped[endpoint]["total_time"] / grouped[endpoint]["count"], 2
            )
            del grouped[endpoint]["total_time"]
        
        return grouped
    
    # ========== 趋势分析 ==========
    
    def get_trend(self, metric_type: str, days: int = 7) -> Dict:
        """获取指标趋势"""
        cutoff = datetime.now() - timedelta(days=days)
        
        with self._lock:
            type_map = {
                "forecast": "forecast",
                "schedule": "schedule",
                "system": "system"
            }
            key = type_map.get(metric_type, "system")
            metrics = [m for m in self._metrics[key]
                      if datetime.fromisoformat(m["created_at"]) > cutoff]
        
        # 按天聚合
        daily_data = {}
        for m in metrics:
            date = datetime.fromisoformat(m["created_at"]).strftime("%Y-%m-%d")
            if date not in daily_data:
                daily_data[date] = {"count": 0, "values": []}
            daily_data[date]["count"] += 1
            
            # 提取关键指标值
            if metric_type == "forecast":
                daily_data[date]["values"].append(m.get("mape", 0))
            elif metric_type == "schedule":
                daily_data[date]["values"].append(m.get("completion_rate", 0))
            elif metric_type == "system":
                daily_data[date]["values"].append(m.get("response_time_ms", 0))
        
        # 计算日均值
       趋势 = []
        for date in sorted(daily_data.keys()):
            values = daily_data[date]["values"]
            avg_value = sum(values) / len(values) if values else 0
            趋势.append({
                "date": date,
                "count": daily_data[date]["count"],
                "avg_value": round(avg_value, 2)
            })
        
        return {"trend": 趋势, "metric_type": metric_type}


# ========== 全局实例 ==========

_metrics_store: Optional[MetricsStore] = None


def get_metrics_store() -> MetricsStore:
    """获取全局指标存储"""
    global _metrics_store
    if _metrics_store is None:
        _metrics_store = MetricsStore()
    return _metrics_store


# ========== 便捷记录函数 ==========

def record_forecast_accuracy(item_code: str, forecast_date: str,
                            actual_qty: float, predicted_qty: float,
                            confidence_level: float = 0.95):
    """记录预测准确率"""
    metric = ForecastAccuracyMetric(
        item_code=item_code,
        forecast_date=forecast_date,
        actual_qty=actual_qty,
        predicted_qty=predicted_qty,
        confidence_level=confidence_level
    )
    get_metrics_store().record_forecast_accuracy(metric)


def record_schedule_efficiency(schedule_id: str, total_orders: int,
                               completed_orders: int, delayed_orders: int,
                               total_planned_hours: float, actual_hours: float,
                               resource_utilization: Dict[str, float]):
    """记录排程效率"""
    metric = ScheduleEfficiencyMetric(
        schedule_id=schedule_id,
        total_orders=total_orders,
        completed_orders=completed_orders,
        delayed_orders=delayed_orders,
        total_planned_hours=total_planned_hours,
        actual_hours=actual_hours,
        resource_utilization=resource_utilization
    )
    get_metrics_store().record_schedule_efficiency(metric)


def record_system_performance(endpoint: str, method: str,
                              response_time_ms: float, status_code: int,
                              error: Optional[str] = None):
    """记录系统性能"""
    metric = SystemPerformanceMetric(
        endpoint=endpoint,
        method=method,
        response_time_ms=response_time_ms,
        status_code=status_code,
        error=error
    )
    get_metrics_store().record_system_performance(metric)
