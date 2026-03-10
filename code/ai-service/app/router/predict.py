"""
Predict Router - 需求预测服务
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
import random

router = APIRouter()


class PredictRequest(BaseModel):
    """预测请求"""
    item_code: str
    forecast_days: int = 30
    method: str = "prophet"  # prophet, arima, moving_average
    confidence_level: float = 0.95


class PredictResponse(BaseModel):
    """预测响应"""
    item_code: str
    forecast_date: str
    forecast_qty: float
    lower_bound: float
    upper_bound: float
    confidence: float
    method: str


class SafetyStockRequest(BaseModel):
    """安全库存计算请求"""
    item_code: str
    service_level: float = 0.95  # 服务水平
    lead_time_days: int = 7  # 采购提前期
    demand_std_multiplier: float = 1.65  # 95%服务水平对应1.65


class SafetyStockResponse(BaseModel):
    """安全库存响应"""
    item_code: str
    safety_stock: float
    reorder_point: float
    optimal_order_qty: float
    service_level: float
    lead_time_days: int
    demand_avg: float
    demand_std: float


@router.post("/demand", response_model=List[PredictResponse])
async def predict_demand(request: PredictRequest):
    """
    需求预测接口
    
    支持多种预测算法：
    - prophet: Facebook Prophet（需安装prophet）
    - arima: ARIMA时间序列
    - moving_average: 移动平均（默认备选）
    """
    item_code = request.item_code
    forecast_days = request.forecast_days
    method = request.method
    
    results = []
    
    # TODO: 接入真实历史数据
    # 从数据库查询历史销售数据
    # historical_data = query_historical_sales(item_code)
    
    # 模拟历史数据（实际应从数据库获取）
    historical_data = _generate_mock_historical_data(item_code)
    
    # 根据方法调用对应算法
    if method == "prophet":
        forecast_results = _prophet_forecast(item_code, historical_data, forecast_days)
    elif method == "arima":
        forecast_results = _arima_forecast(item_code, historical_data, forecast_days)
    else:  # moving_average
        forecast_results = _moving_average_forecast(item_code, historical_data, forecast_days)
    
    for result in forecast_results:
        results.append(PredictResponse(
            item_code=result["item_code"],
            forecast_date=result["date"].strftime("%Y-%m-%d"),
            forecast_qty=round(result["qty"], 2),
            lower_bound=round(result["lower"], 2),
            upper_bound=round(result["upper"], 2),
            confidence=request.confidence_level,
            method=method
        ))
    
    return results


@router.post("/safety-stock", response_model=SafetyStockResponse)
async def calculate_safety_stock(request: SafetyStockRequest):
    """
    安全库存计算接口
    
    使用公式：SS = Z × σ × √LT
    其中：
    - Z: 服务水平系数（95%=1.65, 99%=2.33）
    - σ: 需求标准差
    - LT: 采购提前期
    """
    item_code = request.item_code
    
    # TODO: 接入真实历史数据计算
    # 从数据库查询历史需求数据
    # historical_demand = query_historical_demand(item_code)
    
    # 模拟数据（实际应从数据库获取）
    demand_avg = _get_demand_average(item_code)
    demand_std = _get_demand_std(item_code)
    
    # 安全库存 = Z × σ × √LT
    z_value = _get_z_value(request.service_level)
    safety_stock = z_value * demand_std * (request.lead_time_days ** 0.5)
    
    # 再订货点 = 平均需求 × 提前期 + 安全库存
    reorder_point = demand_avg * request.lead_time_days + safety_stock
    
    # 经济订货量（EOQ）= √(2 × 年需求 × 订货成本 / 单位持有成本)
    # TODO: 接入真实成本数据
    annual_demand = demand_avg * 365
    order_cost = 100  # 订货成本（应从数据库获取）
    holding_cost_ratio = 0.2  # 持有成本率（应从数据库获取）
    unit_cost = 50  # 单位成本（应从数据库获取）
    
    eoq = ((2 * annual_demand * order_cost) / (unit_cost * holding_cost_ratio)) ** 0.5
    
    return SafetyStockResponse(
        item_code=item_code,
        safety_stock=round(safety_stock, 2),
        reorder_point=round(reorder_point, 2),
        optimal_order_qty=round(eoq, 2),
        service_level=request.service_level,
        lead_time_days=request.lead_time_days,
        demand_avg=round(demand_avg, 2),
        demand_std=round(demand_std, 2)
    )


@router.get("/methods")
async def get_forecast_methods():
    """获取支持的预测方法列表"""
    return {
        "methods": [
            {
                "name": "prophet",
                "description": "Facebook Prophet 时间序列预测",
                "pros": "支持季节性、节假日效应",
                "cons": "需要较多数据，计算较慢"
            },
            {
                "name": "arima",
                "description": "ARIMA 差分自回归移动平均",
                "pros": "经典时间序列方法",
                "cons": "需要平稳时间序列"
            },
            {
                "name": "moving_average",
                "description": "简单移动平均",
                "pros": "简单快速",
                "cons": "无法处理趋势和季节性"
            }
        ],
        "default": "prophet"
    }


# ========== 内部算法实现 ==========

def _generate_mock_historical_data(item_code: str) -> List[Dict]:
    """生成模拟历史数据（实际应从数据库查询）"""
    data = []
    base_qty = random.randint(50, 200)
    
    for i in range(90):  # 90天历史数据
        date = datetime.now() - timedelta(days=90-i)
        # 添加一些随机性和周末效应
        qty = base_qty + random.randint(-30, 30)
        if date.weekday() >= 5:  # 周末
            qty = int(qty * 0.6)
        data.append({
            "date": date,
            "qty": qty
        })
    
    return data


def _prophet_forecast(item_code: str, historical_data: List[Dict], 
                      forecast_days: int) -> List[Dict]:
    """
    Prophet 预测算法
    
    TODO: 实际实现需要安装 prophet 库
    # from prophet import Prophet
    # model = Prophet()
    # model.fit(historical_data)
    # forecast = model.predict(future_days)
    """
    # 简化实现：使用指数平滑作为备选
    return _exponential_smoothing_forecast(item_code, historical_data, forecast_days)


def _arima_forecast(item_code: str, historical_data: List[Dict],
                    forecast_days: int) -> List[Dict]:
    """
    ARIMA 预测算法
    
    TODO: 实际实现需要 statsmodels 库
    # from statsmodels.tsa.arima.model import ARIMA
    # model = ARIMA(data, order=(5,1,0))
    # model.fit()
    # forecast = model.forecast(steps=forecast_days)
    """
    # 简化实现
    return _exponential_smoothing_forecast(item_code, historical_data, forecast_days)


def _moving_average_forecast(item_code: str, historical_data: List[Dict],
                               forecast_days: int) -> List[Dict]:
    """移动平均预测"""
    # 取最近7天平均值
    recent_data = historical_data[-7:]
    avg_qty = sum(d["qty"] for d in recent_data) / len(recent_data)
    
    # 标准差
    variance = sum((d["qty"] - avg_qty) ** 2 for d in recent_data) / len(recent_data)
    std_qty = variance ** 0.5
    
    results = []
    for i in range(1, forecast_days + 1):
        date = datetime.now() + timedelta(days=i)
        results.append({
            "item_code": item_code,
            "date": date,
            "qty": avg_qty,
            "lower": avg_qty - 1.96 * std_qty,  # 95%置信区间
            "upper": avg_qty + 1.96 * std_qty
        })
    
    return results


def _exponential_smoothing_forecast(item_code: str, historical_data: List[Dict],
                                      forecast_days: int) -> List[Dict]:
    """指数平滑预测（简单实现）"""
    if not historical_data:
        return []
    
    # 初始值
    alpha = 0.3
    level = historical_data[0]["qty"]
    
    # 计算平滑值
    for d in historical_data:
        level = alpha * d["qty"] + (1 - alpha) * level
    
    # 计算趋势
    trend = 0
    if len(historical_data) > 1:
        trend = (historical_data[-1]["qty"] - historical_data[0]["qty"]) / len(historical_data)
    
    # 标准差
    avg = sum(d["qty"] for d in historical_data) / len(historical_data)
    std = (sum((d["qty"] - avg) ** 2 for d in historical_data) / len(historical_data)) ** 0.5
    
    results = []
    for i in range(1, forecast_days + 1):
        date = datetime.now() + timedelta(days=i)
        qty = level + trend * i
        # 置信区间随预测天数增加而增大
        margin = 1.96 * std * (1 + i * 0.02)
        
        results.append({
            "item_code": item_code,
            "date": date,
            "qty": max(0, qty),
            "lower": max(0, qty - margin),
            "upper": qty + margin
        })
    
    return results


def _get_demand_average(item_code: str) -> float:
    """获取平均需求（TODO: 从数据库查询）"""
    return random.uniform(80, 150)


def _get_demand_std(item_code: str) -> float:
    """获取需求标准差（TODO: 从数据库查询）"""
    return random.uniform(15, 40)


def _get_z_value(service_level: float) -> float:
    """根据服务水平获取Z值"""
    z_values = {
        0.90: 1.28,
        0.95: 1.65,
        0.97: 1.88,
        0.99: 2.33,
        0.999: 3.09
    }
    return z_values.get(service_level, 1.65)
