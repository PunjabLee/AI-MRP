"""
安全库存 AI 推荐引擎

基于历史数据和机器学习的安全库存优化推荐：
- 统计法：经典安全库存公式
- 服务水平法：基于目标服务水平的计算
- 机器学习法：基于需求预测的动态安全库存
- 蒙特卡洛模拟：基于随机模拟的优化
"""
from typing import List, Dict, Any, Optional, Tuple
from datetime import datetime, timedelta
from dataclasses import dataclass
import numpy as np
import pandas as pd
import warnings

warnings.filterwarnings('ignore')


@dataclass
class SafetyStockResult:
    """安全库存计算结果"""
    item_code: str
    safety_stock: float
    reorder_point: float
    optimal_order_qty: float
    service_level_achieved: float
    method: str
    parameters: Dict[str, Any]
    analysis: Dict[str, Any]
    recommendations: List[str]


class SafetyStockEngine:
    """安全库存引擎"""
    
    def __init__(self):
        self.historical_demand = None
        self.lead_time_data = None
        self.item_params = {}
    
    def calculate(
        self,
        item_code: str,
        historical_demand: List[Dict],
        lead_time_days: int = 7,
        lead_time_std: float = 0,
        service_level: float = 0.95,
        method: str = "auto",
        unit_cost: float = 0,
        holding_cost_rate: float = 0.2,
        order_cost: float = 100,
        annual_demand: float = 0
    ) -> SafetyStockResult:
        """
        计算安全库存
        
        Args:
            item_code: 物料编码
            historical_demand: 历史需求数据
            lead_time_days: 采购提前期（天）
            lead_time_std: 提前期标准差
            service_level: 目标服务水平
            method: 计算方法 (auto, statistical, service, ml, montecarlo)
            unit_cost: 单位成本
            holding_cost_rate: 持有成本率
            order_cost: 订货成本
            annual_demand: 年需求
        
        Returns:
            SafetyStockResult
        """
        
        if not historical_demand:
            return self._empty_result(item_code)
        
        # 转换数据
        demand_values = np.array([d["qty"] for d in historical_demand])
        
        # 根据方法计算
        if method == "auto":
            method = self._select_best_method(demand_values)
        
        if method == "statistical":
            result = self._statistical_method(
                item_code, demand_values, lead_time_days, lead_time_std, service_level
            )
        elif method == "service":
            result = self._service_level_method(
                item_code, demand_values, lead_time_days, lead_time_std, service_level
            )
        elif method == "ml":
            result = self._ml_method(
                item_code, demand_values, lead_time_days, lead_time_std, service_level
            )
        elif method == "montecarlo":
            result = self._montecarlo_method(
                item_code, demand_values, lead_time_days, lead_time_std, service_level
            )
        else:
            result = self._statistical_method(
                item_code, demand_values, lead_time_days, lead_time_std, service_level
            )
        
        # 计算再订货点和经济订货量
        avg_demand = np.mean(demand_values)
        
        result.reorder_point = avg_demand * lead_time_days + result.safety_stock
        
        # EOQ 计算
        if annual_demand > 0 and unit_cost > 0 and holding_cost_rate > 0:
            eoq = np.sqrt(2 * annual_demand * order_cost / (unit_cost * holding_cost_rate))
            result.optimal_order_qty = round(eoq, 2)
        
        # 生成建议
        result.recommendations = self._generate_recommendations(
            result, demand_values, lead_time_days, service_level
        )
        
        return result
    
    def _statistical_method(
        self,
        item_code: str,
        demand_values: np.ndarray,
        lead_time_days: int,
        lead_time_std: float,
        service_level: float
    ) -> SafetyStockResult:
        """
        统计法：经典安全库存公式
        
        SS = Z × σ × √LT
        
        其中：
        - Z: 服务水平系数
        - σ: 需求标准差
        - LT: 提前期
        """
        avg_demand = np.mean(demand_values)
        demand_std = np.std(demand_values)
        
        # Z 值
        z = self._get_z_value(service_level)
        
        # 安全库存
        safety_stock = z * demand_std * np.sqrt(lead_time_days)
        
        # 考虑提前期波动
        if lead_time_std > 0:
            # 需求和提前期都波动
            safety_stock = z * np.sqrt(
                lead_time_days * demand_std**2 + avg_demand**2 * lead_time_std**2
            )
        
        result = SafetyStockResult(
            item_code=item_code,
            safety_stock=round(safety_stock, 2),
            reorder_point=0,
            optimal_order_qty=0,
            service_level_achieved=service_level,
            method="statistical",
            parameters={
                "z_value": z,
                "demand_avg": round(avg_demand, 2),
                "demand_std": round(demand_std, 2),
                "lead_time_days": lead_time_days,
                "lead_time_std": lead_time_std
            },
            analysis={
                "formula": "SS = Z × σ × √LT",
                "max_daily_demand": round(np.max(demand_values), 2),
                "min_daily_demand": round(np.min(demand_values), 2),
                "demand_cv": round(demand_std / avg_demand, 4) if avg_demand > 0 else 0
            },
            recommendations=[]
        )
        
        return result
    
    def _service_level_method(
        self,
        item_code: str,
        demand_values: np.ndarray,
        lead_time_days: int,
        lead_time_std: float,
        target_service_level: float
    ) -> SafetyStockResult:
        """
        服务水平法：基于周期服务水平和填充率
        
        计算满足目标服务水平所需的安全库存
        """
        avg_demand = np.mean(demand_values)
        
        # 分析历史缺货情况
        daily_demand = demand_values
        sorted_demand = np.sort(daily_demand)[::-1]
        
        # 计算不同安全库存下的服务水平
        service_levels = []
        ss_range = range(0, int(np.max(daily_demand)) + 10, 5)
        
        for ss in ss_range:
            # 模拟服务水平
            fulfilled = np.sum(np.minimum(daily_demand, ss + avg_demand * lead_time_days))
            total = np.sum(daily_demand)
            level = fulfilled / total if total > 0 else 1.0
            service_levels.append((ss, level))
        
        # 找到满足目标服务水平的安全库存
        achieved_ss = 0
        achieved_level = 0
        for ss, level in service_levels:
            if level >= target_service_level:
                achieved_ss = ss
                achieved_level = level
                break
        
        if achieved_ss == 0:
            achieved_ss = int(np.max(daily_demand))
            achieved_level = 1.0
        
        demand_std = np.std(demand_values)
        
        result = SafetyStockResult(
            item_code=item_code,
            safety_stock=achieved_ss,
            reorder_point=0,
            optimal_order_qty=0,
            service_level_achieved=round(achieved_level * 100, 2),
            method="service_level",
            parameters={
                "target_service_level": target_service_level,
                "demand_avg": round(avg_demand, 2),
                "demand_std": round(demand_std, 2),
                "lead_time_days": lead_time_days
            },
            analysis={
                "formula": "基于周期服务水平迭代计算",
                "achieved_fill_rate": round(achieved_level * 100, 2),
                "simulation_points": len(service_levels)
            },
            recommendations=[]
        )
        
        return result
    
    def _ml_method(
        self,
        item_code: str,
        demand_values: np.ndarray,
        lead_time_days: int,
        lead_time_std: float,
        service_level: float
    ) -> SafetyStockResult:
        """
        机器学习法：基于需求预测的动态安全库存
        
        使用预测误差来动态调整安全库存
        """
        n = len(demand_values)
        
        # 计算移动统计
        window = min(14, n)
        rolling_std = []
        rolling_mean = []
        
        for i in range(window, n):
            window_data = demand_values[i-window:i]
            rolling_mean.append(np.mean(window_data))
            rolling_std.append(np.std(window_data))
        
        # 趋势分析
        x = np.arange(len(rolling_mean))
        if len(x) > 1:
            trend_coef = np.polyfit(x, rolling_mean, 1)[0]
            trend = "increasing" if trend_coef > 0.5 else "decreasing" if trend_coef < -0.5 else "stable"
        else:
            trend = "stable"
        
        # 波动性分析
        avg_std = np.mean(rolling_std) if rolling_std else np.std(demand_values)
        cv = avg_std / np.mean(demand_values) if np.mean(demand_values) > 0 else 0
        
        # 季节性检测
        if n >= 28:
            weekly_pattern = []
            for day in range(7):
                indices = [i for i in range(n) if (datetime.now() - timedelta(days=n-i)).weekday() == day]
                if indices:
                    weekly_pattern.append(np.mean([demand_values[i] for i in indices]))
            seasonality = max(weekly_pattern) / min(weekly_pattern) if min(weekly_pattern) > 0 else 1.0
        else:
            seasonality = 1.0
        
        # 动态安全库存
        base_ss = self._get_z_value(service_level) * avg_std * np.sqrt(lead_time_days)
        
        # 根据趋势调整
        if trend == "increasing":
            trend_factor = 1.2
        elif trend == "decreasing":
            trend_factor = 0.8
        else:
            trend_factor = 1.0
        
        # 根据波动性调整
        volatility_factor = 1 + cv
        
        # 根据季节性调整
        seasonality_factor = seasonality
        
        dynamic_ss = base_ss * trend_factor * volatility_factor * seasonality_factor
        
        result = SafetyStockResult(
            item_code=item_code,
            safety_stock=round(dynamic_ss, 2),
            reorder_point=0,
            optimal_order_qty=0,
            service_level_achieved=service_level * 100,
            method="ml",
            parameters={
                "demand_avg": round(np.mean(demand_values), 2),
                "demand_std": round(avg_std, 2),
                "lead_time_days": lead_time_days,
                "trend": trend,
                "seasonality": round(seasonality, 2),
                "cv": round(cv, 4)
            },
            analysis={
                "formula": "SS = Z × σ_d × √LT × 趋势因子 × 波动因子 × 季节因子",
                "trend_factor": trend_factor,
                "volatility_factor": round(volatility_factor, 2),
                "seasonality_factor": round(seasonality_factor, 2),
                "prediction": f"需求{trend}，波动率{cv:.2%}"
            },
            recommendations=[]
        )
        
        return result
    
    def _montecarlo_method(
        self,
        item_code: str,
        demand_values: np.ndarray,
        lead_time_days: int,
        lead_time_std: float,
        service_level: float
    ) -> SafetyStockResult:
        """
        蒙特卡洛模拟法
        
        通过随机模拟来确定最优安全库存
        """
        n_simulations = 10000
        avg_demand = np.mean(demand_values)
        demand_std = np.std(demand_values)
        
        # 模拟
        simulations = []
        
        for _ in range(n_simulations):
            # 随机生成提前期
            if lead_time_std > 0:
                sim_lt = max(1, np.random.normal(lead_time_days, lead_time_std))
            else:
                sim_lt = lead_time_days
            
            # 随机生成需求
            sim_demand = max(0, np.random.normal(avg_demand, demand_std))
            
            # 计算缺货量
            simulations.append({
                "lead_time": sim_lt,
                "demand": sim_demand,
                "expected_demand": avg_demand * sim_lt
            })
        
        # 寻找满足服务水平的最小安全库存
        target_fill_rate = service_level
        best_ss = 0
        best_fill_rate = 0
        
        ss_candidates = range(0, int(avg_demand * lead_time_days * 2) + 10, 5)
        
        for ss in ss_candidates:
            # 计算填充率
            fulfilled = sum(
                min(sim["demand"] * sim["lead_time"], ss + sim["expected_demand"])
                for sim in simulations
            )
            total_demand = sum(sim["demand"] * sim["lead_time"] for sim in simulations)
            
            fill_rate = fulfilled / total_demand if total_demand > 0 else 1.0
            
            if fill_rate >= target_fill_rate:
                best_ss = ss
                best_fill_rate = fill_rate
                break
        
        if best_ss == 0:
            best_ss = int(avg_demand * lead_time_days)
            best_fill_rate = 1.0
        
        result = SafetyStockResult(
            item_code=item_code,
            safety_stock=best_ss,
            reorder_point=0,
            optimal_order_qty=0,
            service_level_achieved=round(best_fill_rate * 100, 2),
            method="montecarlo",
            parameters={
                "n_simulations": n_simulations,
                "demand_avg": round(avg_demand, 2),
                "demand_std": round(demand_std, 2),
                "lead_time_days": lead_time_days,
                "lead_time_std": lead_time_std,
                "target_fill_rate": service_level
            },
            analysis={
                "formula": "蒙特卡洛模拟优化",
                "simulation_range": f"0-{int(avg_demand * lead_time_days * 2)}",
                "achieved_fill_rate": round(best_fill_rate * 100, 2),
                "confidence": "95%"
            },
            recommendations=[]
        )
        
        return result
    
    def _select_best_method(self, demand_values: np.ndarray) -> str:
        """
        自动选择最佳计算方法
        
        基于数据特征：
        - 数据量少：statistical
        - 数据量中等：service
        - 数据量多且有趋势：ml
        """
        n = len(demand_values)
        cv = np.std(demand_values) / np.mean(demand_values) if np.mean(demand_values) > 0 else 0
        
        if n < 30:
            return "statistical"
        elif n < 100:
            return "service"
        elif cv > 0.5:  # 高波动
            return "montecarlo"
        else:
            return "ml"
    
    def _get_z_value(self, service_level: float) -> float:
        """根据服务水平获取Z值"""
        z_map = {
            0.80: 0.84,
            0.85: 1.04,
            0.90: 1.28,
            0.95: 1.65,
            0.97: 1.88,
            0.98: 2.05,
            0.99: 2.33,
            0.995: 2.58,
            0.999: 3.09
        }
        return z_map.get(service_level, 1.65)
    
    def _generate_recommendations(
        self,
        result: SafetyStockResult,
        demand_values: np.ndarray,
        lead_time_days: int,
        service_level: float
    ) -> List[str]:
        """生成优化建议"""
        recommendations = []
        
        avg_demand = np.mean(demand_values)
        
        # 安全库存水平评估
        if result.safety_stock < avg_demand * lead_time_days * 0.1:
            recommendations.append("⚠️ 安全库存偏低，建议增加以提高服务水平")
        elif result.safety_stock > avg_demand * lead_time_days * 0.5:
            recommendations.append("⚠️ 安全库存偏高，可能造成库存积压")
        
        # 需求波动建议
        cv = np.std(demand_values) / avg_demand if avg_demand > 0 else 0
        if cv > 0.5:
            recommendations.append("📊 需求波动较大，建议使用ML方法动态调整安全库存")
        
        # 提前期建议
        if lead_time_days > 14:
            recommendations.append("🚚 采购提前期较长，建议与供应商协商缩短交期")
        
        # 服务水平建议
        if service_level < 0.95:
            recommendations.append(f"💡 当前服务水平 {service_level*100}%，建议提升至95%以减少缺货")
        
        if not recommendations:
            recommendations.append("✅ 安全库存设置合理")
        
        return recommendations
    
    def _empty_result(self, item_code: str) -> SafetyStockResult:
        """空结果"""
        return SafetyStockResult(
            item_code=item_code,
            safety_stock=0,
            reorder_point=0,
            optimal_order_qty=0,
            service_level_achieved=0,
            method="none",
            parameters={},
            analysis={},
            recommendations=["无历史数据，无法计算安全库存"]
        )


# ========== 批量计算 =========-

def calculate_safety_stock_batch(
    items: List[Dict],
    historical_data: Dict[str, List[Dict]],
    default_lead_time: int = 7,
    default_service_level: float = 0.95
) -> List[SafetyStockResult]:
    """
    批量计算安全库存
    
    Args:
        items: 物料列表，每个包含 item_code, lead_time_days, service_level
        historical_data: 历史需求数据，{item_code: [demand records]}
        default_lead_time: 默认提前期
        default_service_level: 默认服务水平
    
    Returns:
        安全库存结果列表
    """
    engine = SafetyStockEngine()
    results = []
    
    for item in items:
        item_code = item.get("item_code")
        history = historical_data.get(item_code, [])
        
        result = engine.calculate(
            item_code=item_code,
            historical_demand=history,
            lead_time_days=item.get("lead_time_days", default_lead_time),
            lead_time_std=item.get("lead_time_std", 0),
            service_level=item.get("service_level", default_service_level),
            method=item.get("method", "auto"),
            unit_cost=item.get("unit_cost", 0),
            holding_cost_rate=item.get("holding_cost_rate", 0.2),
            order_cost=item.get("order_cost", 100),
            annual_demand=item.get("annual_demand", 0)
        )
        
        results.append(result)
    
    return results


def generate_replenishment_plan(
    safety_stocks: List[SafetyStockResult],
    current_inventory: Dict[str, float],
    pending_orders: Dict[str, float]
) -> Dict:
    """
    生成补货计划
    
    Args:
        safety_stocks: 安全库存计算结果
        current_inventory: 当前库存 {item_code: qty}
        pending_orders: 在途订单 {item_code: qty}
    
    Returns:
        补货计划
    """
    recommendations = []
    
    for ss in safety_stocks:
        item = ss.item_code
        current = current_inventory.get(item, 0)
        on_way = pending_orders.get(item, 0)
        available = current + on_way
        
        if available < ss.safety_stock:
            # 缺货风险
            gap = ss.safety_stock - available
            urgency = "high" if available < ss.safety_stock * 0.5 else "medium"
            
            recommendations.append({
                "item_code": item,
                "current_inventory": current,
                "pending_orders": on_way,
                "available": available,
                "safety_stock": ss.safety_stock,
                "gap": round(gap, 2),
                "recommended_order_qty": round(max(gap, ss.optimal_order_qty), 2),
                "urgency": urgency,
                "reason": "低于安全库存"
            })
        elif available < ss.reorder_point:
            # 达到再订货点
            recommendations.append({
                "item_code": item,
                "current_inventory": current,
                "pending_orders": on_way,
                "available": available,
                "reorder_point": ss.reorder_point,
                "recommended_order_qty": round(ss.optimal_order_qty, 2),
                "urgency": "low",
                "reason": "达到再订货点"
            })
    
    return {
        "recommendations": recommendations,
        "summary": {
            "total_items": len(safety_stocks),
            "urgent_replenishment": len([r for r in recommendations if r["urgency"] == "high"]),
            "medium_replenishment": len([r for r in recommendations if r["urgency"] == "medium"]),
            "low_replenishment": len([r for r in recommendations if r["urgency"] == "low"])
        }
    }
