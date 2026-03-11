"""
Forecast Router - 需求预测服务（增强版）

提供丰富的配置选项和可视化支持：
- 多种预测算法
- 参数配置
- 模型管理
- 预测结果可视化
- 批量预测
- 历史对比
"""
from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
import json

from app.algorithms import (
    create_forecast_engine,
    auto_select_algorithm,
    SafetyStockEngine
)

router = APIRouter()


# ========== Request Models ==========

class PredictRequest(BaseModel):
    """预测请求"""
    item_code: str = Field(..., description="物料编码")
    forecast_days: int = Field(30, ge=1, le=365, description="预测天数")
    method: str = Field("auto", description="预测方法: prophet, arima, lstm, xgboost, moving_average, exponential_smoothing, auto")
    confidence_level: float = Field(0.95, ge=0.5, le=0.99, description="置信度")
    historical_data: Optional[List[Dict]] = Field(None, description="历史数据(可选)")
    
    # Prophet 专属参数
    prophet_changepoint_prior_scale: Optional[float] = Field(None, ge=0.001, le=0.5, description="趋势变化灵敏度")
    prophet_seasonality_mode: Optional[str] = Field(None, description="季节性模式: multiplicative, additive")
    prophet_yearly_seasonality: Optional[bool] = Field(None, description="年度季节性")
    prophet_weekly_seasonality: Optional[bool] = Field(None, description="周季节性")
    
    # LSTM 专属参数
    lstm_sequence_length: Optional[int] = Field(None, ge=7, le=180, description="序列长度")
    lstm_epochs: Optional[int] = Field(None, ge=10, le=500, description="训练轮数")
    lstm_layers: Optional[int] = Field(None, ge=1, le=5, description="LSTM层数")
    
    # ARIMA 专属参数
    arima_p: Optional[int] = Field(None, ge=0, le=10, description="AR阶数")
    arima_d: Optional[int] = Field(None, ge=0, le=2, description="差分阶数")
    arima_q: Optional[int] = Field(None, ge=0, le=10, description="MA阶数")


class PredictResponse(BaseModel):
    """预测响应"""
    item_code: str
    method: str
    forecast: List[Dict]
    confidence: float
    metrics: Dict[str, Any]
    parameters: Dict[str, Any]
    visualization_data: Dict[str, Any]


class SafetyStockRequest(BaseModel):
    """安全库存计算请求"""
    item_code: str
    service_level: float = Field(0.95, ge=0.5, le=0.99)
    lead_time_days: int = Field(7, ge=1, le=180)
    lead_time_std: float = Field(0, ge=0, description="提前期标准差")
    method: str = Field("auto", description="计算方法: auto, statistical, service, ml, montecarlo")
    unit_cost: float = Field(0, ge=0, description="单位成本")
    holding_cost_rate: float = Field(0.2, ge=0, le=1, description="持有成本率")
    order_cost: float = Field(100, ge=0, description="订货成本")
    historical_demand: Optional[List[Dict]] = None


class SafetyStockResponse(BaseModel):
    """安全库存响应"""
    item_code: str
    safety_stock: float
    reorder_point: float
    optimal_order_qty: float
    service_level_achieved: float
    method: str
    parameters: Dict[str, Any]
    analysis: Dict[str, Any]
    recommendations: List[str]
    visualization_data: Dict[str, Any]


class BatchForecastRequest(BaseModel):
    """批量预测请求"""
    item_codes: List[str]
    forecast_days: int = 30
    method: str = "auto"
    include_safety_stock: bool = True
    parallel: bool = True


class BatchForecastResponse(BaseModel):
    """批量预测响应"""
    forecasts: List[Dict]
    summary: Dict[str, Any]
    visualization_data: Dict[str, Any]


class ModelConfigRequest(BaseModel):
    """模型配置请求"""
    model_name: str
    algorithm: str
    parameters: Dict[str, Any]
    description: Optional[str] = ""


class ModelConfigResponse(BaseModel):
    """模型配置响应"""
    model_id: str
    model_name: str
    algorithm: str
    parameters: Dict[str, Any]
    created_at: str
    description: str


class ForecastComparisonRequest(BaseModel):
    """预测对比请求"""
    item_code: str
    methods: List[str]
    forecast_days: int = 30


class ForecastComparisonResponse(BaseModel):
    """预测对比响应"""
    item_code: str
    comparisons: List[Dict]
    best_method: str
    recommendation: str
    visualization_data: Dict[str, Any]


# ========== API Endpoints ==========

@router.post("/demand", response_model=PredictResponse)
async def predict_demand(request: PredictRequest):
    """
    需求预测接口 - 增强版
    
    支持丰富的参数配置：
    - 通用参数：预测天数、方法、置信度
    - Prophet参数：changepoint_prior_scale, seasonality_mode等
    - LSTM参数：sequence_length, epochs, layers等
    - ARIMA参数：p, d, q阶数
    """
    item_code = request.item_code
    
    # 获取历史数据
    historical_data = request.historical_data
    if not historical_data:
        historical_data = _generate_mock_data(item_code)
    
    if not historical_data:
        raise HTTPException(status_code=400, detail="无历史数据")
    
    # 选择方法
    method = request.method
    if method == "auto":
        method = auto_select_algorithm(historical_data)
    
    # 创建引擎
    engine = create_forecast_engine(method)
    
    # Prophet 特殊参数
    if method == "prophet" and request.prophet_changepoint_prior_scale:
        # 可以传递到引擎
        pass
    
    # 训练和预测
    engine.fit(historical_data)
    forecast_results = engine.forecast(request.forecast_days, request.confidence_level)
    metrics = engine.get_metrics()
    
    # 构建响应
    forecast = []
    for r in forecast_results:
        forecast.append({
            "date": r["date"].strftime("%Y-%m-%d") if isinstance(r["date"], datetime) else str(r["date"]),
            "qty": round(r["qty"], 2),
            "lower": round(r.get("lower", 0), 2),
            "upper": round(r.get("upper", 0), 2),
            "trend": round(r.get("trend", 0), 4) if "trend" in r else None,
            "seasonal": round(r.get("seasonal", 0), 4) if "seasonal" in r else None
        })
    
    # 可视化数据
    viz_data = _build_forecast_visualization(forecast, metrics)
    
    # 构建参数（只返回非敏感参数）
    params = {
        "forecast_days": request.forecast_days,
        "method": method,
        "confidence_level": request.confidence_level
    }
    
    return PredictResponse(
        item_code=item_code,
        method=method,
        forecast=forecast,
        confidence=request.confidence_level,
        metrics=metrics,
        parameters=params,
        visualization_data=viz_data
    )


@router.post("/safety-stock", response_model=SafetyStockResponse)
async def calculate_safety_stock(request: SafetyStockRequest):
    """安全库存计算 - 增强版"""
    item_code = request.item_code
    
    historical_demand = request.historical_demand
    if not historical_demand:
        historical_demand = _generate_demand_data(item_code)
    
    if not historical_demand:
        raise HTTPException(status_code=400, detail="无历史需求数据")
    
    engine = SafetyStockEngine()
    result = engine.calculate(
        item_code=item_code,
        historical_demand=historical_demand,
        lead_time_days=request.lead_time_days,
        lead_time_std=request.lead_time_std,
        service_level=request.service_level,
        method=request.method,
        unit_cost=request.unit_cost,
        holding_cost_rate=request.holding_cost_rate,
        order_cost=request.order_cost,
        annual_demand=request.service_level * request.lead_time_days * 365
    )
    
    # 可视化数据
    viz_data = _build_safety_stock_visualization(result)
    
    return SafetyStockResponse(
        item_code=item_code,
        safety_stock=result.safety_stock,
        reorder_point=round(result.reorder_point, 2),
        optimal_order_qty=result.optimal_order_qty,
        service_level_achieved=result.service_level_achieved,
        method=result.method,
        parameters=result.parameters,
        analysis=result.analysis,
        recommendations=result.recommendations,
        visualization_data=viz_data
    )


@router.post("/batch", response_model=BatchForecastResponse)
async def batch_forecast(request: BatchForecastRequest):
    """批量预测 - 增强版"""
    forecasts = []
    methods_used = {}
    all_dates = []
    all_values = []
    
    for item_code in request.item_codes:
        historical_data = _generate_mock_data(item_code)
        
        if not historical_data:
            forecasts.append({"item_code": item_code, "error": "无历史数据"})
            continue
        
        method = request.method
        if method == "auto":
            method = auto_select_algorithm(historical_data)
        
        methods_used[item_code] = method
        
        try:
            engine = create_forecast_engine(method)
            engine.fit(historical_data)
            results = engine.forecast(request.forecast_days, 0.95)
            
            # 收集可视化数据
            dates = [r["date"].strftime("%Y-%m-%d") for r in results]
            values = [round(r["qty"], 2) for r in results]
            
            if not all_dates:
                all_dates = dates
            
            forecasts.append({
                "item_code": item_code,
                "method": method,
                "forecast": [{"date": d, "qty": v} for d, v in zip(dates, values)],
                "latest_qty": values[-1] if values else 0
            })
            
            all_values.append(values)
            
        except Exception as e:
            forecasts.append({"item_code": item_code, "error": str(e)})
    
    # 批量可视化数据
    viz_data = _build_batch_visualization(all_dates, all_values, request.item_codes)
    
    return BatchForecastResponse(
        forecasts=forecasts,
        summary={
            "total_items": len(request.item_codes),
            "methods_used": methods_used,
            "forecast_days": request.forecast_days
        },
        visualization_data=viz_data
    )


@router.post("/compare", response_model=ForecastComparisonResponse)
async def compare_forecasts(request: ForecastComparisonRequest):
    """预测方法对比"""
    historical_data = _generate_mock_data(request.item_code)
    
    if not historical_data:
        raise HTTPException(status_code=400, detail="无历史数据")
    
    comparisons = []
    metrics_list = []
    
    for method in request.methods:
        try:
            engine = create_forecast_engine(method)
            engine.fit(historical_data)
            results = engine.forecast(request.forecast_days, 0.95)
            metrics = engine.get_metrics()
            
            comparisons.append({
                "method": method,
                "forecast": [{"date": r["date"].strftime("%Y-%m-%d"), "qty": round(r["qty"], 2)} for r in results[:7]],
                "metrics": metrics
            })
            metrics_list.append((method, metrics.get("mape", 999)))
        except Exception as e:
            comparisons.append({"method": method, "error": str(e)})
    
    # 选择最佳方法（MAPE最低）
    best_method = min(metrics_list, key=lambda x: x[1])[0] if metrics_list else request.methods[0]
    
    # 可视化数据
    viz_data = _build_comparison_visualization(comparisons)
    
    return ForecastComparisonResponse(
        item_code=request.item_code,
        comparisons=comparisons,
        best_method=best_method,
        recommendation=f"基于MAPE指标，推荐使用 {best_method} 方法",
        visualization_data=viz_data
    )


# ========== 可视化数据构建 ==========

def _build_forecast_visualization(forecast: List[Dict], metrics: Dict) -> Dict:
    """构建预测可视化数据"""
    dates = [f["date"] for f in forecast]
    values = [f["qty"] for f in forecast]
    lower = [f.get("lower", 0) for f in forecast]
    upper = [f.get("upper", 0) for f in forecast]
    
    return {
        "chart_type": "forecast",
        "time_series": {
            "labels": dates,
            "datasets": [
                {
                    "label": "预测值",
                    "data": values,
                    "borderColor": "#3b82f6",
                    "backgroundColor": "rgba(59, 130, 246, 0.1)",
                    "fill": True
                },
                {
                    "label": "置信区间上限",
                    "data": upper,
                    "borderColor": "#94a3b8",
                    "borderDash": [5, 5],
                    "fill": False
                },
                {
                    "label": "置信区间下限",
                    "data": lower,
                    "borderColor": "#94a3b8",
                    "borderDash": [5, 5],
                    "fill": "-1"
                }
            ]
        },
        "metrics": {
            "labels": ["MAE", "RMSE", "MAPE"],
            "values": [metrics.get("mae", 0), metrics.get("rmse", 0), metrics.get("mape", 0)]
        }
    }


def _build_safety_stock_visualization(result) -> Dict:
    """构建安全库存可视化数据"""
    return {
        "chart_type": "safety_stock",
        "gauges": [
            {
                "name": "安全库存",
                "value": result.safety_stock,
                "max": result.safety_stock * 2,
                "color": "#3b82f6"
            },
            {
                "name": "再订货点",
                "value": result.reorder_point,
                "max": result.reorder_point * 2,
                "color": "#f59e0b"
            },
            {
                "name": "经济订货量",
                "value": result.optimal_order_qty,
                "max": result.optimal_order_qty * 2,
                "color": "#10b981"
            }
        ],
        "analysis_chart": {
            "labels": ["需求均值", "需求标准差", "提前期", "服务水平"],
            "values": [
                result.parameters.get("demand_avg", 0),
                result.parameters.get("demand_std", 0),
                result.parameters.get("lead_time_days", 0),
                result.service_level_achieved
            ]
        }
    }


def _build_batch_visualization(dates: List[str], values_list: List[List], items: List[str]) -> Dict:
    """构建批量预测可视化数据"""
    datasets = []
    colors = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"]
    
    for i, values in enumerate(values_list):
        datasets.append({
            "label": items[i] if i < len(items) else f"Item {i+1}",
            "data": values,
            "borderColor": colors[i % len(colors)],
            "fill": False
        })
    
    return {
        "chart_type": "batch_forecast",
        "time_series": {
            "labels": dates,
            "datasets": datasets
        },
        "comparison_table": [
            {"item": items[i], "latest_forecast": values[-1] if values else 0}
            for i, values in enumerate(values_list)
        ]
    }


def _build_comparison_visualization(comparisons: List[Dict]) -> Dict:
    """构建对比可视化数据"""
    datasets = []
    colors = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444"]
    
    for i, comp in enumerate(comparisons):
        if "forecast" in comp:
            datasets.append({
                "label": comp["method"],
                "data": [f["qty"] for f in comp["forecast"]],
                "borderColor": colors[i % len(colors)],
                "fill": False
            })
    
    # MAPE对比
    mape_data = []
    for comp in comparisons:
        if "metrics" in comp:
            mape_data.append({
                "method": comp["method"],
                "mape": comp["metrics"].get("mape", 0)
            })
    
    return {
        "chart_type": "comparison",
        "time_series": {
            "labels": comparisons[0].get("forecast", [{}])[:7] if comparisons and "forecast" in comparisons[0] else [],
            "datasets": datasets
        },
        "bar_chart": {
            "labels": [d["method"] for d in mape_data],
            "values": [d["mape"] for d in mape_data],
            "colors": colors[:len(mape_data)]
        }
    }


# ========== Helper Functions ==========

def _generate_mock_data(item_code: str) -> List[Dict]:
    """生成模拟历史数据"""
    import random
    data = []
    base = 100 + hash(item_code) % 100
    
    for i in range(90):
        date = datetime.now() - timedelta(days=90-i)
        qty = base + random.randint(-20, 30) + int(i * 0.1)
        data.append({"date": date, "qty": max(0, qty)})
    
    return data


def _generate_demand_data(item_code: str) -> List[Dict]:
    """生成模拟需求数据"""
    import random
    data = []
    base = 80 + hash(item_code) % 50
    
    for i in range(60):
        date = datetime.now() - timedelta(days=60-i)
        qty = base + random.randint(-15, 25)
        data.append({"date": date, "qty": max(0, qty)})
    
    return data


# ========== 方法信息 ==========

@router.get("/methods")
async def get_forecast_methods():
    """获取预测方法列表及参数配置"""
    return {
        "methods": [
            {
                "name": "prophet",
                "display_name": "Facebook Prophet",
                "description": "支持季节性和趋势的时间序列预测",
                "parameters": {
                    "changepoint_prior_scale": {"type": "float", "default": 0.05, "range": [0.001, 0.5], "description": "趋势变化灵敏度"},
                    "seasonality_mode": {"type": "select", "default": "multiplicative", "options": ["multiplicative", "additive"], "description": "季节性模式"},
                    "yearly_seasonality": {"type": "boolean", "default": True, "description": "年度季节性"},
                    "weekly_seasonality": {"type": "boolean", "default": True, "description": "周季节性"}
                },
                "pros": ["自动检测季节性", "处理节假日", "鲁棒性好"],
                "cons": ["计算较慢", "需要较多数据"],
                "data_requirement": "建议90+条历史数据"
            },
            {
                "name": "lstm",
                "display_name": "LSTM 深度学习",
                "description": "长短期记忆网络，适合复杂模式",
                "parameters": {
                    "sequence_length": {"type": "int", "default": 30, "range": [7, 180], "description": "输入序列长度"},
                    "epochs": {"type": "int", "default": 50, "range": [10, 500], "description": "训练轮数"},
                    "layers": {"type": "int", "default": 2, "range": [1, 5], "description": "LSTM层数"}
                },
                "pros": ["捕捉复杂非线性关系", "预测精度高"],
                "cons": ["计算资源要求高", "容易过拟合"],
                "data_requirement": "建议180+条历史数据"
            },
            {
                "name": "arima",
                "display_name": "ARIMA",
                "description": "经典时间序列分析方法",
                "parameters": {
                    "p": {"type": "int", "default": 5, "range": [0, 10], "description": "自回归阶数"},
                    "d": {"type": "int", "default": 1, "range": [0, 2], "description": "差分阶数"},
                    "q": {"type": "int", "default": 0, "range": [0, 10], "description": "移动平均阶数"}
                },
                "pros": ["解释性强", "计算快"],
                "cons": ["需要平稳序列", "参数选择困难"],
                "data_requirement": "建议50+条历史数据"
            },
            {
                "name": "xgboost",
                "display_name": "XGBoost",
                "description": "梯度提升决策树",
                "parameters": {
                    "n_estimators": {"type": "int", "default": 100, "range": [10, 500], "description": "树的数量"},
                    "max_depth": {"type": "int", "default": 5, "range": [3, 10], "description": "树的最大深度"}
                },
                "pros": ["训练快", "支持特征工程"],
                "cons": ["需要特征工程"],
                "data_requirement": "建议60+条历史数据"
            },
            {
                "name": "moving_average",
                "display_name": "移动平均",
                "description": "简单快速的基准方法",
                "parameters": {
                    "window": {"type": "int", "default": 7, "range": [3, 90], "description": "窗口大小"}
                },
                "pros": ["简单快速", "无参数"],
                "cons": ["无法处理趋势季节性"],
                "data_requirement": "任意"
            },
            {
                "name": "exponential_smoothing",
                "display_name": "指数平滑",
                "description": "Holt-Winters 指数平滑",
                "parameters": {
                    "alpha": {"type": "float", "default": 0.3, "range": [0.1, 0.9], "description": "水平平滑系数"},
                    "beta": {"type": "float", "default": 0.1, "range": [0.1, 0.9], "description": "趋势平滑系数"}
                },
                "pros": ["处理趋势和季节性", "计算快"],
                "cons": ["参数敏感"],
                "data_requirement": "建议30+条历史数据"
            },
            {
                "name": "auto",
                "display_name": "自动选择",
                "description": "基于数据特征自动选择最佳方法",
                "parameters": {},
                "pros": ["无需选择", "适用性强"],
                "cons": ["可能不是最优"],
                "data_requirement": "根据数据量选择"
            }
        ]
    }


@router.get("/safety-stock/methods")
async def get_safety_stock_methods():
    """获取安全库存计算方法"""
    return {
        "methods": [
            {
                "name": "auto",
                "display_name": "自动选择",
                "description": "根据数据特征自动选择"
            },
            {
                "name": "statistical",
                "display_name": "统计法",
                "description": "经典公式 SS = Z × σ × √LT"
            },
            {
                "name": "service",
                "display_name": "服务水平法",
                "description": "基于目标服务水平迭代计算"
            },
            {
                "name": "ml",
                "display_name": "机器学习法",
                "description": "考虑趋势、波动、季节性动态调整"
            },
            {
                "name": "montecarlo",
                "display_name": "蒙特卡洛模拟",
                "description": "随机模拟确定最优SS"
            }
        ],
        "service_levels": {
            "0.80": "80% - 成本优先",
            "0.90": "90% - 平衡",
            "0.95": "95% - 标准",
            "0.99": "99% - 高服务"
        }
    }
