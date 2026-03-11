"""
What-if 模拟引擎 - 全面深化版

支持多维度影响分析：
1. 需求变化影响
2. 产能变化影响
3. 物料供应影响
4. 成本变化影响
5. 交期变化影响
6. 供应商风险影响
7. 库存策略影响

特性：
- 敏感性分析
- 多场景对比
- 智能建议
- 蒙特卡洛模拟
"""
from typing import List, Dict, Any, Optional, Tuple
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from enum import Enum
import numpy as np
import json


class ImpactDimension(Enum):
    """影响维度"""
    DEMAND = "demand"           # 需求变化
    CAPACITY = "capacity"       # 产能变化
    MATERIAL = "material"       # 物料供应
    COST = "cost"              # 成本变化
    LEADTIME = "leadtime"      # 交期变化
    SUPPLIER = "supplier"      # 供应商风险
    INVENTORY = "inventory"     # 库存策略
    SCHEDULE = "schedule"      # 排程影响


class ScenarioType(Enum):
    """场景类型"""
    DEMAND_INCREASE = "demand_increase"       # 需求增加
    DEMAND_DECREASE = "demand_decrease"       # 需求减少
    SUPPLY_DISRUPTION = "supply_disruption"  # 供应中断
    PRICE_CHANGE = "price_change"             # 价格变化
    LEADTIME_CHANGE = "leadtime_change"       # 交期变化
    CAPACITY_CHANGE = "capacity_change"       # 产能变化
    CUSTOM = "custom"                         # 自定义


@dataclass
class WhatIfScenario:
    """What-if 场景"""
    name: str
    description: str
    scenario_type: ScenarioType
    parameters: Dict[str, Any]  # 变化的参数
    baseline_parameters: Dict[str, Any]  # 基线参数
    priority: int = 1


@dataclass
class ImpactAnalysis:
    """单维度影响分析"""
    dimension: ImpactDimension
    baseline_value: float
    new_value: float
    change_rate: float  # 变化率
    impact_score: float  # 影响程度 (0-100)
    description: str
    affected_items: List[str] = field(default_factory=list)


@dataclass
class WhatIfResult:
    """What-if 模拟结果"""
    scenario_name: str
    scenario_type: str
    baseline_metrics: Dict[str, float]
    simulated_metrics: Dict[str, float]
    changes: Dict[str, float]
    impacts: List[ImpactAnalysis]
    risk_level: str  # LOW, MEDIUM, HIGH, CRITICAL
    recommendations: List[str]
    sensitivity_analysis: Dict[str, Any] = field(default_factory=dict)
    comparison_with_baseline: Dict[str, Any] = field(default_factory=dict)


@dataclass
class ComparisonResult:
    """场景对比结果"""
    scenarios: List[WhatIfResult]
    best_for_cost: Optional[WhatIfResult] = None
    best_for_speed: Optional[WhatIfResult] = None
    best_for_risk: Optional[WhatIfResult] = None
    tradeoff_analysis: Dict[str, Any] = field(default_factory=dict)


class WhatIfSimulator:
    """What-if 模拟引擎"""
    
    def __init__(self):
        self.baseline_data: Dict[str, Any] = {}
        self.current_scenario: Optional[WhatIfScenario] = None
    
    def set_baseline(self, baseline_data: Dict[str, Any]):
        """设置基线数据"""
        self.baseline_data = baseline_data
    
    def simulate(
        self,
        scenario: WhatIfScenario,
        include_sensitivity: bool = True
    ) -> WhatIfResult:
        """
        执行 What-if 模拟
        
        Args:
            scenario: What-if 场景
            include_sensitivity: 是否包含敏感性分析
        
        Returns:
            模拟结果
        """
        self.current_scenario = scenario
        
        # 1. 计算各维度影响
        impacts = self._calculate_impacts(scenario)
        
        # 2. 计算指标变化
        simulated_metrics = self._calculate_metrics(scenario, impacts)
        
        # 3. 计算变化
        changes = self._calculate_changes(simulated_metrics)
        
        # 4. 风险评估
        risk_level = self._assess_risk(impacts, changes)
        
        # 5. 生成建议
        recommendations = self._generate_recommendations(scenario, impacts, changes)
        
        # 6. 敏感性分析
        sensitivity_analysis = {}
        if include_sensitivity:
            sensitivity_analysis = self._sensitivity_analysis(scenario)
        
        # 7. 基线对比
        comparison = self._compare_with_baseline(simulated_metrics)
        
        return WhatIfResult(
            scenario_name=scenario.name,
            scenario_type=scenario.scenario_type.value,
            baseline_metrics=self.baseline_data.get("metrics", {}),
            simulated_metrics=simulated_metrics,
            changes=changes,
            impacts=impacts,
            risk_level=risk_level,
            recommendations=recommendations,
            sensitivity_analysis=sensitivity_analysis,
            comparison_with_baseline=comparison
        )
    
    def _calculate_impacts(self, scenario: WhatIfScenario) -> List[ImpactAnalysis]:
        """计算各维度影响"""
        impacts = []
        params = scenario.parameters
        baseline = scenario.baseline_parameters
        
        # 1. 需求变化影响
        if "demand_change_rate" in params:
            old_demand = baseline.get("monthly_demand", 1000)
            new_demand = old_demand * (1 + params["demand_change_rate"])
            impact = self._analyze_demand_impact(
                old_demand, new_demand, params
            )
            impacts.append(impact)
        
        # 2. 产能变化影响
        if "capacity_change_rate" in params:
            old_capacity = baseline.get("monthly_capacity", 1500)
            new_capacity = old_capacity * (1 + params["capacity_change_rate"])
            impact = self._analyze_capacity_impact(
                old_capacity, new_capacity, params
            )
            impacts.append(impact)
        
        # 3. 成本变化影响
        if "cost_change_rate" in params:
            old_cost = baseline.get("unit_cost", 50)
            new_cost = old_cost * (1 + params["cost_change_rate"])
            impact = self._analyze_cost_impact(
                old_cost, new_cost, params
            )
            impacts.append(impact)
        
        # 4. 交期变化影响
        if "leadtime_change_rate" in params:
            old_lt = baseline.get("lead_time_days", 7)
            new_lt = old_lt * (1 + params["leadtime_change_rate"])
            impact = self._analyze_leadtime_impact(
                old_lt, new_lt, params
            )
            impacts.append(impact)
        
        # 5. 物料供应影响
        if "material_availability" in params:
            old_avail = baseline.get("material_availability", 1.0)
            new_avail = params["material_availability"]
            impact = self._analyze_material_impact(
                old_avail, new_avail, params
            )
            impacts.append(impact)
        
        # 6. 供应商风险影响
        if "supplier_risk_rate" in params:
            old_risk = baseline.get("supplier_reliability", 0.95)
            new_risk = 1 - params["supplier_risk_rate"]
            impact = self._analyze_supplier_impact(
                old_risk, new_risk, params
            )
            impacts.append(impact)
        
        # 7. 库存策略影响
        if "safety_stock_change_rate" in params:
            old_ss = baseline.get("safety_stock", 100)
            new_ss = old_ss * (1 + params["safety_stock_change_rate"])
            impact = self._analyze_inventory_impact(
                old_ss, new_ss, params
            )
            impacts.append(impact)
        
        return impacts
    
    def _analyze_demand_impact(
        self, old_demand: float, new_demand: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_demand - old_demand) / old_demand
        impact_score = min(100, abs(change_rate) * 100)
        
        affected_items = []
        if new_demand > old_demand:
            affected_items = ["产能利用率", "物料需求", "交期"]
        else:
            affected_items = ["库存水平", "生产计划"]
        
        return ImpactAnalysis(
            dimension=ImpactDimension.DEMAND,
            baseline_value=old_demand,
            new_value=new_demand,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"需求变化 {change_rate:+.1%}",
            affected_items=affected_items
        )
    
    def _analyze_capacity_impact(
        self, old_capacity: float, new_capacity: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_capacity - old_capacity) / old_capacity
        impact_score = min(100, abs(change_rate) * 80)
        
        return ImpactAnalysis(
            dimension=ImpactDimension.CAPACITY,
            baseline_value=old_capacity,
            new_value=new_capacity,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"产能变化 {change_rate:+.1%}",
            affected_items=["订单完成率", "设备利用率", "加班成本"]
        )
    
    def _analyze_cost_impact(
        self, old_cost: float, new_cost: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_cost - old_cost) / old_cost
        impact_score = min(100, abs(change_rate) * 150)  # 成本影响较大
        
        baseline_demand = self.baseline_data.get("metrics", {}).get("monthly_demand", 1000)
        old_total = old_cost * baseline_demand
        new_total = new_cost * baseline_demand
        
        return ImpactAnalysis(
            dimension=ImpactDimension.COST,
            baseline_value=old_total,
            new_value=new_total,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"总成本变化 ¥{new_total - old_total:,.0f}",
            affected_items=["利润", "定价", "客户满意度"]
        )
    
    def _analyze_leadtime_impact(
        self, old_lt: float, new_lt: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_lt - old_lt) / old_lt
        impact_score = min(100, abs(change_rate) * 80)
        
        return ImpactAnalysis(
            dimension=ImpactDimension.LEADTIME,
            baseline_value=old_lt,
            new_value=new_lt,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"交期变化 {new_lt - old_lt:+.0f}天",
            affected_items=["安全库存", "缺货风险", "供应商绩效"]
        )
    
    def _analyze_material_impact(
        self, old_avail: float, new_avail: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_avail - old_avail) / old_avail if old_avail > 0 else 0
        impact_score = min(100, (old_avail - new_avail) * 100)
        
        return ImpactAnalysis(
            dimension=ImpactDimension.MATERIAL,
            baseline_value=old_avail * 100,
            new_value=new_avail * 100,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"物料可用性 {new_avail * 100:.0f}%",
            affected_items=["生产计划", "采购计划", "缺料风险"]
        )
    
    def _analyze_supplier_impact(
        self, old_reliability: float, new_reliability: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_reliability - old_reliability) / old_reliability
        impact_score = min(100, (old_reliability - new_reliability) * 200)
        
        return ImpactAnalysis(
            dimension=ImpactDimension.SUPPLIER,
            baseline_value=old_reliability * 100,
            new_value=new_reliability * 100,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"供应商可靠性 {new_reliability * 100:.0f}%",
            affected_items=["交期", "质量", "备选供应商"]
        )
    
    def _analyze_inventory_impact(
        self, old_ss: float, new_ss: float, params: Dict
    ) -> ImpactAnalysis:
        change_rate = (new_ss - old_ss) / old_ss
        impact_score = min(100, abs(change_rate) * 50)
        
        holding_cost_rate = self.baseline_data.get("holding_cost_rate", 0.2)
        old_holding = old_ss * holding_cost_rate
        new_holding = new_ss * holding_cost_rate
        
        return ImpactAnalysis(
            dimension=ImpactDimension.INVENTORY,
            baseline_value=old_holding,
            new_value=new_holding,
            change_rate=change_rate,
            impact_score=impact_score,
            description=f"库存持有成本变化 ¥{new_holding - old_holding:,.0f}/月",
            affected_items=["现金流", "仓储空间", "呆滞风险"]
        )
    
    def _calculate_metrics(
        self,
        scenario: WhatIfScenario,
        impacts: List[ImpactAnalysis]
    ) -> Dict[str, float]:
        """计算模拟后的指标"""
        baseline = self.baseline_data.get("metrics", {})
        
        metrics = {}
        
        # 基础指标
        metrics["monthly_demand"] = baseline.get("monthly_demand", 1000)
        metrics["monthly_capacity"] = baseline.get("monthly_capacity", 1500)
        metrics["unit_cost"] = baseline.get("unit_cost", 50)
        metrics["lead_time_days"] = baseline.get("lead_time_days", 7)
        
        # 应用影响
        for impact in impacts:
            if impact.dimension == ImpactDimension.DEMAND:
                metrics["monthly_demand"] = impact.new_value
            elif impact.dimension == ImpactDimension.CAPACITY:
                metrics["monthly_capacity"] = impact.new_value
            elif impact.dimension == ImpactDimension.COST:
                metrics["unit_cost"] = impact.new_value / metrics["monthly_demand"]
            elif impact.dimension == ImpactDimension.LEADTIME:
                metrics["lead_time_days"] = impact.new_value
        
        # 衍生指标
        demand = metrics["monthly_demand"]
        capacity = metrics["monthly_capacity"]
        unit_cost = metrics["unit_cost"]
        
        metrics["capacity_utilization"] = min(150, demand / capacity * 100)
        metrics["total_cost"] = demand * unit_cost
        metrics["profit_margin"] = baseline.get("profit_margin", 0.2)
        metrics["profit"] = demand * unit_cost * metrics["profit_margin"]
        metrics["on_time_rate"] = min(100, (1 - (demand - capacity) / capacity) * 100) if demand > capacity else 100
        metrics["stockout_risk"] = max(0, min(100, (demand - capacity) / capacity * 100)) if demand > capacity else 0
        
        # 库存相关
        safety_stock = self.baseline_data.get("safety_stock", 100)
        for impact in impacts:
            if impact.dimension == ImpactDimension.INVENTORY:
                safety_stock = impact.new_value / self.baseline_data.get("holding_cost_rate", 0.2)
        
        metrics["safety_stock"] = safety_stock
        metrics["average_inventory"] = demand * metrics["lead_time_days"] / 30 + safety_stock
        metrics["inventory_turnover"] = demand / metrics["average_inventory"] if metrics["average_inventory"] > 0 else 0
        
        return metrics
    
    def _calculate_changes(self, simulated: Dict[str, float]) -> Dict[str, float]:
        """计算变化"""
        baseline = self.baseline_data.get("metrics", {})
        changes = {}
        
        for key in simulated:
            old_val = baseline.get(key, 0)
            new_val = simulated[key]
            if old_val > 0:
                changes[key] = (new_val - old_val) / old_val
            else:
                changes[key] = new_val - old_val
        
        return changes
    
    def _assess_risk(
        self,
        impacts: List[ImpactAnalysis],
        changes: Dict[str, float]
    ) -> str:
        """评估风险等级"""
        total_impact = sum(i.impact_score for i in impacts)
        avg_impact = total_impact / len(impacts) if impacts else 0
        
        # 特殊风险检查
        for change in changes:
            if "stockout_risk" in change and changes[change] > 0.3:
                return "CRITICAL"
            if "capacity_utilization" in change and changes[change] > 1.2:
                return "HIGH"
        
        if avg_impact >= 60:
            return "HIGH"
        elif avg_impact >= 30:
            return "MEDIUM"
        else:
            return "LOW"
    
    def _generate_recommendations(
        self,
        scenario: WhatIfScenario,
        impacts: List[ImpactAnalysis],
        changes: Dict[str, float]
    ) -> List[str]:
        """生成建议"""
        recommendations = []
        
        for impact in impacts:
            if impact.dimension == ImpactDimension.DEMAND:
                if impact.change_rate > 0.2:
                    recommendations.append("📈 需求增长超过20%，建议提前备货或增加产能")
                elif impact.change_rate < -0.2:
                    recommendations.append("📉 需求下降，建议调整生产计划或开发新市场")
            
            elif impact.dimension == ImpactDimension.CAPACITY:
                if impact.change_rate < -0.2:
                    recommendations.append("⚠️ 产能下降超过20%，建议启用备用生产线或外包")
            
            elif impact.dimension == ImpactDimension.COST:
                if impact.change_rate > 0.1:
                    recommendations.append("💰 成本上升超过10%，建议寻找替代供应商或优化工艺")
            
            elif impact.dimension == ImpactDimension.LEADTIME:
                if impact.change_rate > 0.3:
                    recommendations.append("⏰ 交期延长超过30%，建议增加安全库存")
            
            elif impact.dimension == ImpactDimension.MATERIAL:
                if impact.new_value < 0.8:
                    recommendations.append("🔧 物料可用性低于80%，建议寻找替代物料或备选供应商")
            
            elif impact.dimension == ImpactDimension.SUPPLIER:
                if impact.new_value < 0.9:
                    recommendations.append("🏭 供应商可靠性低于90%，建议评估备选供应商")
        
        # 基于风险等级的建议
        risk = self._assess_risk(impacts, changes)
        if risk == "CRITICAL":
            recommendations.append("🚨 高风险场景，建议立即制定应急预案")
        elif risk == "HIGH":
            recommendations.append("⚠️ 中高风险，建议48小时内制定应对方案")
        
        if not recommendations:
            recommendations.append("✅ 当前场景风险可控，维持现有策略")
        
        return recommendations
    
    def _sensitivity_analysis(self, scenario: WhatIfScenario) -> Dict[str, Any]:
        """敏感性分析"""
        sensitivity = {}
        params = scenario.parameters
        
        # 对每个参数进行±10%, ±20%变化分析
        for param_name, param_value in params.items():
            if isinstance(param_value, (int, float)):
                variations = {}
                for rate in [-0.2, -0.1, 0.1, 0.2]:
                    test_value = param_value * (1 + rate)
                    test_params = params.copy()
                    test_params[param_name] = test_value
                    
                    test_scenario = WhatIfScenario(
                        name=f" sensitivity_{param_name}_{rate}",
                        description="",
                        scenario_type=scenario.scenario_type,
                        parameters=test_params,
                        baseline_parameters=scenario.baseline_parameters
                    )
                    
                    test_impacts = self._calculate_impacts(test_scenario)
                    total_impact = sum(i.impact_score for i in test_impacts)
                    variations[f"{rate:+.0%}"] = total_impact
                
                sensitivity[param_name] = variations
        
        return sensitivity
    
    def _compare_with_baseline(self, simulated: Dict[str, float]) -> Dict[str, Any]:
        """与基线对比"""
        baseline = self.baseline_data.get("metrics", {})
        
        comparison = {}
        for key in simulated:
            old_val = baseline.get(key, 0)
            new_val = simulated[key]
            
            if old_val > 0:
                diff_pct = (new_val - old_val) / old_val * 100
                status = "↑" if diff_pct > 0 else "↓" if diff_pct < 0 else "→"
                comparison[key] = {
                    "baseline": old_val,
                    "simulated": new_val,
                    "diff": diff_pct,
                    "status": status
                }
        
        return comparison
    
    def compare_scenarios(
        self,
        scenarios: List[WhatIfScenario]
    ) -> ComparisonResult:
        """对比多个场景"""
        results = []
        
        for scenario in scenarios:
            result = self.simulate(scenario)
            results.append(result)
        
        # 找出各维度最优
        best_cost = min(results, key=lambda r: r.simulated_metrics.get("total_cost", float('inf')))
        best_speed = max(results, key=lambda r: r.simulated_metrics.get("on_time_rate", 0))
        best_risk = min(results, key=lambda r: {"LOW": 0, "MEDIUM": 1, "HIGH": 2, "CRITICAL": 3}.get(r.risk_level, 4))
        
        # 权衡分析
        tradeoff = {
            "cost_vs_speed": "高速方案通常成本较高" if best_speed != best_cost else "速度和成本可兼顾",
            "risk_vs_cost": "低风险方案通常成本较高" if best_risk != best_cost else "风险和成本可兼顾"
        }
        
        return ComparisonResult(
            scenarios=results,
            best_for_cost=best_cost,
            best_for_speed=best_speed,
            best_for_risk=best_risk,
            tradeoff_analysis=tradeoff
        )


# ========== 便捷函数 =========-

def create_whatif_scenario(
    name: str,
    scenario_type: str,
    parameters: Dict[str, Any],
    baseline: Dict[str, Any]
) -> WhatIfScenario:
    """创建 What-if 场景"""
    return WhatIfScenario(
        name=name,
        description="",
        scenario_type=ScenarioType(scenario_type),
        parameters=parameters,
        baseline_parameters=baseline
    )


def run_whatif_simulation(
    baseline: Dict[str, Any],
    scenarios: List[Dict]
) -> Dict:
    """
    运行 What-if 模拟
    
    Args:
        baseline: 基线数据，包含 metrics 字段
        scenarios: 场景列表，每个包含 name, type, parameters
    
    Returns:
        模拟结果
    """
    simulator = WhatIfSimulator()
    simulator.set_baseline(baseline)
    
    # 创建场景对象
    whatif_scenarios = []
    for s in scenarios:
        whatif_scenarios.append(create_whatif_scenario(
            name=s["name"],
            scenario_type=s["type"],
            parameters=s["parameters"],
            baseline=baseline.get("metrics", {})
        ))
    
    # 对比场景
    comparison = simulator.compare_scenarios(whatif_scenarios)
    
    return {
        "scenarios": [
            {
                "name": r.scenario_name,
                "type": r.scenario_type,
                "risk_level": r.risk_level,
                "metrics": r.simulated_metrics,
                "changes": r.changes,
                "recommendations": r.recommendations
            }
            for r in comparison.scenarios
        ],
        "best_recommendations": {
            "cost": comparison.best_for_cost.scenario_name if comparison.best_for_cost else None,
            "speed": comparison.best_for_speed.scenario_name if comparison.best_for_speed else None,
            "risk": comparison.best_for_risk.scenario_name if comparison.best_for_risk else None
        },
        "tradeoff_analysis": comparison.tradeoff_analysis
    }
