"""
Inventory Prediction Router - 库存预测服务
"""
from fastapi import APIRouter
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime, timedelta

router = APIRouter()


class InventoryPredictRequest(BaseModel):
    """库存预测请求"""
    item_code: str
    warehouse_code: str
    forecast_days: int = 30  # 预测天数


class InventoryPredictResponse(BaseModel):
    """库存预测响应"""
    item_code: str
    warehouse_code: str
    current_qty: float  # 当前库存
    predicted_min: List[dict]  # 预测最低库存
    predicted_max: List[dict]  # 预测最高库存
    risk_dates: List[dict]  # 风险日期
    replenishment_suggestion: dict  # 补货建议


@router.post("/predict", response_model=InventoryPredictResponse)
async def predict_inventory(request: InventoryPredictRequest):
    """
    库存预测
    
    预测未来库存水平，识别缺货风险
    """
    item_code = request.item_code
    warehouse_code = request.warehouse_code
    forecast_days = request.forecast_days
    
    # TODO: 从数据库查询当前库存和历史出入库数据
    # current = query_current_inventory(item_code, warehouse_code)
    # history = query_transaction_history(item_code, warehouse_code, days=90)
    
    # 模拟预测结果
    current_qty = 500
    
    # 预测趋势
    predicted_min = _predict_trend(current_qty, forecast_days, "min")
    predicted_max = _predict_trend(current_qty, forecast_days, "max")
    
    # 识别风险日期
    risk_dates = _identify_risk_dates(predicted_min, current_qty)
    
    # 补货建议
    suggestion = _generate_replenishment_suggestion(current_qty, predicted_min)
    
    return InventoryPredictResponse(
        item_code=item_code,
        warehouse_code=warehouse_code,
        current_qty=current_qty,
        predicted_min=predicted_min,
        predicted_max=predicted_max,
        risk_dates=risk_dates,
        replenishment_suggestion=suggestion
    )


@router.get("/safety-stock-recommendation")
async def recommend_safety_stock(item_code: str, warehouse_code: str = None):
    """
    安全库存推荐
    
    基于需求波动和供应不确定性推荐最优安全库存
    """
    # TODO: 从数据库查询历史数据
    
    # 简化计算
    avg_demand = 100  # 日均需求
    demand_std = 30  # 需求标准差
    lead_time = 7  # 提前期
    lead_time_std = 2  # 提前期波动
    
    # 安全库存公式: SS = Z * √(LT * σ² + D² * σ²)
    # 简化: SS = Z * σ * √LT
    service_level_95 = 1.65
    safety_stock = service_level_95 * demand_std * (lead_time ** 0.5)
    
    # 再订货点: ROP = D * LT + SS
    reorder_point = avg_demand * lead_time + safety_stock
    
    return {
        "item_code": item_code,
        "warehouse_code": warehouse_code,
        "avg_daily_demand": avg_demand,
        "demand_std": demand_std,
        "lead_time_days": lead_time,
        "lead_time_std": lead_time_std,
        "recommended_safety_stock": round(safety_stock, 2),
        "reorder_point": round(reorder_point, 2),
        "service_level": 0.95,
        "calculation_method": "continuous_review"
    }


@router.get("/reorder-alert")
async def get_reorder_alerts(warehouse_code: str = None):
    """
    获取需要补货的物料列表
    """
    # TODO: 从数据库查询
    
    alerts = [
        {
            "item_code": "A001",
            "warehouse_code": "WH01",
            "current_qty": 50,
            "safety_stock": 100,
            "reorder_point": 200,
            "urgency": "HIGH",
            "suggested_qty": 300
        },
        {
            "item_code": "B002",
            "warehouse_code": "WH01", 
            "current_qty": 80,
            "safety_stock": 50,
            "reorder_point": 150,
            "urgency": "MEDIUM",
            "suggested_qty": 200
        }
    ]
    
    return {
        "alerts": alerts,
        "total_count": len(alerts)
    }


# 内部方法

def _predict_trend(current_qty: float, days: int, type: str) -> List[dict]:
    """预测库存趋势"""
    results = []
    
    # 简化：假设每天消耗10-20个
    import random
    for i in range(1, days + 1):
        date = datetime.now() + timedelta(days=i)
        if type == "min":
            qty = current_qty - i * random.randint(10, 20)
        else:
            qty = current_qty - i * random.randint(5, 15)
        
        results.append({
            "date": date.strftime("%Y-%m-%d"),
            "qty": max(0, qty)
        })
    
    return results


def _identify_risk_dates(predicted_min: List[dict], current_qty: float) -> List[dict]:
    """识别缺货风险日期"""
    risk_dates = []
    safety_stock = 50  # 安全库存
    
    for item in predicted_min:
        if item["qty"] < safety_stock:
            risk_dates.append({
                "date": item["date"],
                "predicted_qty": item["qty"],
                "shortage": safety_stock - item["qty"]
            })
    
    return risk_dates


def _generate_replenishment_suggestion(current_qty: float, predicted_min: List[dict]) -> dict:
    """生成补货建议"""
    min_qty = min(item["qty"] for item in predicted_min)
    
    if min_qty < 0:
        return {
            "action": "URGENT_ORDER",
            "suggested_qty": abs(min_qty) + 200,
            "priority": "HIGH"
        }
    elif min_qty < 50:
        return {
            "action": "PLANNED_ORDER",
            "suggested_qty": 100,
            "priority": "MEDIUM"
        }
    else:
        return {
            "action": "NO_ACTION",
            "suggested_qty": 0,
            "priority": "LOW"
        }
