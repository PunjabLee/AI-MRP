"""
插单影响分析引擎 - 全面深化版

支持多维度影响分析：
1. 订单影响分析
2. 产能影响分析
3. 物料影响分析
4. 成本影响分析
5. 交期影响分析
6. 库存影响分析
7. 风险评估

特性：
- 全面影响评估
- 多方案对比
- 智能建议
- 一键执行
"""
from typing import List, Dict, Any, Optional, Tuple
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from enum import Enum
import numpy as np


class OrderImpactDimension(Enum):
    """订单影响维度"""
    SCHEDULE = "schedule"         # 排程影响
    CAPACITY = "capacity"         # 产能影响
    MATERIAL = "material"         # 物料影响
    COST = "cost"                # 成本影响
    LEADTIME = "leadtime"        # 交期影响
    INVENTORY = "inventory"       # 库存影响
    QUALITY = "quality"          # 质量影响
    RISK = "risk"                # 风险影响


class RiskLevel(Enum):
    """风险等级"""
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


@dataclass
class NewOrder:
    """新订单"""
    order_no: str
    item_code: str
    quantity: float
    priority: int = 1  # 1=最高
    required_date: Optional[datetime] = None
    unit_price: float = 0
    unit_cost: float = 0
    process_type: str = ""
    work_center: str = ""


@dataclass
class ExistingPlan:
    """现有计划"""
    plan_no: str
    order_no: str
    item_code: str
    quantity: float
    start_date: datetime
    end_date: datetime
    work_center: str
    priority: int = 5
    status: str = "planned"


@dataclass
class Resource:
    """资源/设备"""
    id: str
    name: str
    capacity_hours: float  # 每日可用小时
    hourly_cost: float
    available: bool = True


@dataclass
class MaterialRequirement:
    """物料需求"""
    item_code: str
    quantity_required: float
    current_inventory: float
    on_order_qty: float = 0
    safety_stock: float = 0
    lead_time_days: int = 7


@dataclass
class DimensionImpact:
    """单维度影响"""
    dimension: OrderImpactDimension
    impact_score: float  # 0-100
    description: str
    affected_items: List[str] = field(default_factory=list)
    financial_impact: float = 0  # 金额影响
    time_impact_days: float = 0  # 时间影响（天）


@dataclass
class OrderImpactResult:
    """订单影响分析结果"""
    new_order_no: str
    is_feasible: bool
    dimensions: List[DimensionImpact]
    total_impact_score: float
    risk_level: str
    affected_orders: List[Dict] = field(default_factory=list)
    capacity_analysis: Dict[str, Any] = field(default_factory=dict)
    material_analysis: Dict[str, Any] = field(default_factory=dict)
    cost_analysis: Dict[str, Any] = field(default_factory=dict)
    timeline_analysis: Dict[str, Any] = field(default_factory=dict)
    recommendations: List[str] = field(default_factory=list)
    alternative_solutions: List[Dict] = field(default_factory=list)


class ImpactAnalysisEngine:
    """影响分析引擎"""
    
    def __init__(self):
        self.resources: List[Resource] = []
        self.material_requirements: Dict[str, MaterialRequirement] = {}
    
    def set_resources(self, resources: List[Dict]):
        """设置资源"""
        self.resources = [
            Resource(
                id=r["id"],
                name=r["name"],
                capacity_hours=r.get("capacity_hours", 8),
                hourly_cost=r.get("hourly_cost", 100),
                available=r.get("available", True)
            )
            for r in resources
        ]
    
    def set_material_requirements(self, materials: Dict[str, Dict]):
        """设置物料需求"""
        self.material_requirements = {
            code: MaterialRequirement(
                item_code=code,
                quantity_required=data.get("quantity_required", 0),
                current_inventory=data.get("current_inventory", 0),
                on_order_qty=data.get("on_order_qty", 0),
                safety_stock=data.get("safety_stock", 0),
                lead_time_days=data.get("lead_time_days", 7)
            )
            for code, data in materials.items()
        }
    
    def analyze_order_impact(
        self,
        new_order: NewOrder,
        existing_plans: List[ExistingPlan],
        include_alternatives: bool = True
    ) -> OrderImpactResult:
        """
        分析插入新订单的影响
        
        Args:
            new_order: 新订单
            existing_plans: 现有计划
            include_alternatives: 是否包含替代方案
        
        Returns:
            影响分析结果
        """
        dimensions = []
        
        # 1. 排程影响分析
        schedule_impact = self._analyze_schedule_impact(new_order, existing_plans)
        dimensions.append(schedule_impact)
        
        # 2. 产能影响分析
        capacity_impact = self._analyze_capacity_impact(new_order, existing_plans)
        dimensions.append(capacity_impact)
        
        # 3. 物料影响分析
        material_impact = self._analyze_material_impact(new_order)
        dimensions.append(material_impact)
        
        # 4. 成本影响分析
        cost_impact = self._analyze_cost_impact(new_order, existing_plans)
        dimensions.append(cost_impact)
        
        # 5. 交期影响分析
        leadtime_impact = self._analyze_leadtime_impact(new_order, existing_plans)
        dimensions.append(leadtime_impact)
        
        # 6. 库存影响分析
        inventory_impact = self._analyze_inventory_impact(new_order)
        dimensions.append(inventory_impact)
        
        # 7. 风险评估
        risk_impact = self._analyze_risk_impact(dimensions, new_order, existing_plans)
        dimensions.append(risk_impact)
        
        # 计算总分
        total_score = sum(d.impact_score for d in dimensions) / len(dimensions)
        
        # 评估可行性
        is_feasible = (
            capacity_impact.impact_score < 80 and
            material_impact.impact_score < 80 and
            risk_impact.impact_score < 60
        )
        
        # 风险等级
        risk_level = self._calculate_risk_level(dimensions)
        
        # 受影响的订单
        affected_orders = self._get_affected_orders(new_order, existing_plans)
        
        # 生成建议
        recommendations = self._generate_recommendations(
            new_order, dimensions, is_feasible
        )
        
        # 生成替代方案
        alternatives = []
        if include_alternatives:
            alternatives = self._generate_alternatives(
                new_order, dimensions, existing_plans
            )
        
        return OrderImpactResult(
            new_order_no=new_order.order_no,
            is_feasible=is_feasible,
            dimensions=dimensions,
            total_impact_score=total_score,
            risk_level=risk_level,
            affected_orders=affected_orders,
            capacity_analysis=self._format_capacity_analysis(capacity_impact),
            material_analysis=self._format_material_analysis(material_impact),
            cost_analysis=self._format_cost_analysis(cost_impact),
            timeline_analysis=self._format_timeline_analysis(leadtime_impact),
            recommendations=recommendations,
            alternative_solutions=alternatives
        )
    
    def _analyze_schedule_impact(
        self,
        new_order: NewOrder,
        existing_plans: List[ExistingPlan]
    ) -> DimensionImpact:
        """排程影响分析"""
        affected = []
        max_delay = 0 = 0
        

        conflict_count        for plan in existing_plans:
            if plan.work_center == new_order.work_center:
                # 检查时间冲突
                if new_order.required_date and plan.end_date > new_order.required_date:
                    conflict_count += 1
                    delay_days = (plan.end_date - new_order.required_date).days
                    max_delay = max(max_delay, delay_days)
                    affected.append(plan.plan_no)
        
        impact_score = min(100, conflict_count * 20 + max_delay * 5)
        
        return DimensionImpact(
            dimension=OrderImpactDimension.SCHEDULE,
            impact_score=impact_score,
            description=f"与{len(affected)}个计划冲突，最大延期{max_delay}天" if affected else "无冲突",
            affected_items=affected,
            time_impact_days=max_delay
        )
    
    def _analyze_capacity_impact(
        self,
        new_order: NewOrder,
        existing_plans: List[ExistingPlan]
    ) -> DimensionImpact:
        """产能影响分析"""
        # 计算所需产能（小时）
        estimated_hours = new_order.quantity * 1  # 假设1小时/件
        
        # 计算现有产能使用
        target_work_center = new_order.work_center
        used_hours = sum(
            p.quantity for p in existing_plans 
            if p.work_center == target_work_center
        )
        
        # 查找资源
        resource = next((r for r in self.resources if r.name == target_work_center), None)
        if resource:
            available_hours = resource.capacity_hours
            utilization = (used_hours + estimated_hours) / available_hours * 100
        else:
            available_hours = 80  # 默认
            utilization = (used_hours + estimated_hours) / available_hours * 100
        
        # 需要加班？
        overtime_hours = max(0, (used_hours + estimated_hours) - available_hours)
        
        impact_score = min(100, utilization)
        
        return DimensionImpact(
            dimension=OrderImpactDimension.CAPACITY,
            impact_score=impact_score,
            description=f"产能利用率{utilization:.0f}%，需加班{overtime_hours:.0f}小时" if overtime_hours > 0 else f"产能利用率{utilization:.0f}%",
            affected_items=[target_work_center],
            financial_impact=overtime_hours * (resource.hourly_cost * 1.5 if resource else 150)
        )
    
    def _analyze_material_impact(
        self,
        new_order: NewOrder
    ) -> DimensionImpact:
        """物料影响分析"""
        material = self.material_requirements.get(new_order.item_code)
        
        if not material:
            return DimensionImpact(
                dimension=OrderImpactDimension.MATERIAL,
                impact_score=50,
                description="物料信息未知",
                affected_items=[new_order.item_code]
            )
        
        available = material.current_inventory + material.on_order_qty
        needed = new_order.quantity
        shortage = max(0, needed - available)
        
        # 计算缺料影响
        if shortage > 0:
            # 需要紧急采购
            urgency = "high" if shortage > available * 0.5 else "medium"
            impact_score = 70 if urgency == "high" else 50
            description = f"缺料 {shortage} 件，需要紧急采购"
        else:
            impact_score = 10
            description = "物料充足"
        
        return DimensionImpact(
            dimension=OrderImpactDimension.MATERIAL,
            impact_score=impact_score,
            description=description,
            affected_items=[new_order.item_code],
            financial_impact=shortage * material.quantity_required * 0.1  # 紧急采购额外成本
        )
    
    def _analyze_cost_impact(
        self,
        new_order: NewOrder,
        existing_plans: List[ExistingPlan]
    ) -> DimensionImpact:
        """成本影响分析"""
        base_cost = new_order.quantity * new_order.unit_cost
        
        # 加班成本
        schedule_impact = self._analyze_schedule_impact(new_order, existing_plans)
        overtime_cost = schedule_impact.financial_impact
        
        # 急单加急费
        rush_fee = base_cost * 0.1 if new_order.priority <= 2 else 0
        
        # 物料紧急采购成本
        material_impact = self._analyze_material_impact(new_order)
        emergency_purchase_cost = material_impact.financial_impact
        
        total_extra_cost = overtime_cost + rush_fee + emergency_purchase_cost
        cost_increase_rate = total_extra_cost / base_cost if base_cost > 0 else 0
        
        impact_score = min(100, cost_increase_rate * 200)
        
        return DimensionImpact(
            dimension=OrderImpactDimension.COST,
            impact_score=impact_score,
            description=f"额外成本 ¥{total_extra_cost:,.0f} ({cost_increase_rate:.1%})",
            affected_items=["生产成本", "采购成本"],
            financial_impact=total_extra_cost
        )
    
    def _analyze_leadtime_impact(
        self,
        new_order: NewOrder,
        existing_plans: List[ExistingPlan]
    ) -> DimensionImpact:
        """交期影响分析"""
        if not new_order.required_date:
            return DimensionImpact(
                dimension=OrderImpactDimension.LEADTIME,
                impact_score=0,
                description="无指定交期"
            )
        
        # 查找被影响的订单
        affected_deliveries = []
        for plan in existing_plans:
            if plan.end_date > new_order.required_date:
                delay = (plan.end_date - new_order.required_date).days
                affected_deliveries.append({
                    "order": plan.order_no,
                    "original_date": plan.end_date.strftime("%Y-%m-%d"),
                    "new_date": new_order.required_date.strftime("%Y-%m-%d"),
                    "delay_days": delay
                })
        
        max_delay = max((d["delay_days"] for d in affected_deliveries), default=0)
        impact_score = min(100, max_delay * 10)
        
        return DimensionImpact(
            dimension=OrderImpactDimension.LEADTIME,
            impact_score=impact_score,
            description=f"影响{len(affected_deliveries)}个订单，最大延期{max_delay}天",
            affected_items=[d["order"] for d in affected_deliveries],
            time_impact_days=max_delay
        )
    
    def _analyze_inventory_impact(
        self,
        new_order: NewOrder
    ) -> DimensionImpact:
        """库存影响分析"""
        material = self.material_requirements.get(new_order.item_code)
        
        if not material:
            return DimensionImpact(
                dimension=OrderImpactDimension.INVENTORY,
                impact_score=30,
                description="无法评估"
            )
        
        available = material.current_inventory + material.on_order_qty
        after_order = available - new_order.quantity
        
        # 检查是否低于安全库存
        below_safety = after_order < material.safety_stock
        stockout_risk = max(0, material.safety_stock - after_order) / material.safety_stock if material.safety_stock > 0 else 0
        
        if below_safety:
            impact_score = 60 + stockout_risk * 40
            description = f"库存低于安全库存，缺货风险 {stockout_risk:.0%}"
        else:
            impact_score = 10
            description = "库存充足"
        
        return DimensionImpact(
            dimension=OrderImpactDimension.INVENTORY,
            impact_score=impact_score,
            description=description,
            affected_items=[new_order.item_code]
        )
    
    def _analyze_risk_impact(
        self,
        dimensions: List[DimensionImpact],
        new_order: NewOrder,
        existing_plans: List[ExistingPlan]
    ) -> DimensionImpact:
        """风险影响分析"""
        # 基于各维度计算综合风险
        risk_factors = []
        
        for dim in dimensions:
            if dim.dimension == OrderImpactDimension.CAPACITY:
                if dim.impact_score > 80:
                    risk_factors.append("产能严重不足")
                elif dim.impact_score > 50:
                    risk_factors.append("产能紧张")
            
            elif dim.dimension == OrderImpactDimension.MATERIAL:
                if dim.impact_score > 50:
                    risk_factors.append("物料风险")
            
            elif dim.dimension == OrderImpactDimension.LEADTIME:
                if dim.time_impact_days > 5:
                    risk_factors.append("交期风险")
        
        # 优先级风险
        if new_order.priority <= 2:
            risk_factors.append("急单风险")
        
        impact_score = min(100, len(risk_factors) * 25 + 20)
        
        return DimensionImpact(
            dimension=OrderImpactDimension.RISK,
            impact_score=impact_score,
            description="; ".join(risk_factors) if risk_factors else "无明显风险",
            affected_items=[]
        )
    
    def _calculate_risk_level(self, dimensions: List[DimensionImpact]) -> str:
        """计算风险等级"""
        scores = [d.impact_score for d in dimensions]
        avg_score = sum(scores) / len(scores) if scores else 0
        max_score = max(scores) if scores else 0
        
        if max_score >= 80 or avg_score >= 60:
            return "CRITICAL"
        elif max_score >= 60 or avg_score >= 40:
            return "HIGH"
        elif max_score >= 40 or avg_score >= 20:
            return "MEDIUM"
        else:
            return "LOW"
    
    def _get_affected_orders(
        self,
        new_order: NewOrder,
        existing_plans: List[ExistingPlan]
    ) -> List[Dict]:
        """获取受影响的订单"""
        affected = []
        
        for plan in existing_plans:
            if plan.work_center == new_order.work_center:
                if new_order.required_date and plan.end_date > new_order.required_date:
                    affected.append({
                        "order_no": plan.order_no,
                        "original_end_date": plan.end_date.strftime("%Y-%m-%d"),
                        "new_end_date": new_order.required_date.strftime("%Y-%m-%d"),
                        "delay_days": (plan.end_date - new_order.required_date).days
                    })
        
        return affected
    
    def _generate_recommendations(
        self,
        new_order: NewOrder,
        dimensions: List[DimensionImpact],
        is_feasible: bool
    ) -> List[str]:
        """生成建议"""
        recommendations = []
        
        if not is_feasible:
            recommendations.append("⚠️ 当前方案不可行，建议调整订单参数或选择替代方案")
        
        for dim in dimensions:
            if dim.impact_score > 50:
                if dim.dimension == OrderImpactDimension.CAPACITY:
                    if dim.financial_impact > 0:
                        recommendations.append(f"📊 产能影响较大，建议加班生产（额外成本 ¥{dim.financial_impact:,.0f}）")
                    recommendations.append("🏭 考虑外包部分产能")
                
                elif dim.dimension == OrderImpactDimension.MATERIAL:
                    recommendations.append(f"🔧 物料缺口 {dim.description}，建议紧急采购")
                
                elif dim.dimension == OrderImpactDimension.COST:
                    recommendations.append(f"💰 成本增加 {dim.description}，评估是否接受")
                
                elif dim.dimension == OrderImpactDimension.LEADTIME:
                    recommendations.append(f"⏰ {dim.description}，建议与客户沟通延期")
                
                elif dim.dimension == OrderImpactDimension.INVENTORY:
                    recommendations.append(f"📦 {dim.description}，建议调整库存策略")
        
        if not recommendations:
            recommendations.append("✅ 插单影响可控，可以执行")
        
        return recommendations
    
    def _generate_alternatives(
        self,
        new_order: NewOrder,
        dimensions: List[DimensionImpact],
        existing_plans: List[ExistingPlan]
    ) -> List[Dict]:
        """生成替代方案"""
        alternatives = []
        
        # 方案1: 分批交货
        alternatives.append({
            "name": "分批交货",
            "description": "将订单分批交付，缓解产能压力",
            "pros": ["降低产能压力", "减少库存占用"],
            "cons": ["增加物流成本", "客户可能不接受"],
            "impact_reduction": "30%"
        })
        
        # 方案2: 加班生产
        capacity_dim = next((d for d in dimensions if d.dimension == OrderImpactDimension.CAPACITY), None)
        if capacity_dim and capacity_dim.financial_impact > 0:
            alternatives.append({
                "name": "加班生产",
                "description": "通过加班满足交期",
                "pros": ["满足交期", "无需外部协调"],
                "cons": ["增加成本", "员工疲劳"],
                "additional_cost": capacity_dim.financial_impact,
                "impact_reduction": "50%"
            })
        
        # 方案3: 外包
        alternatives.append({
            "name": "部分外包",
            "description": "将部分产能外包给供应商",
            "pros": ["解决产能瓶颈", "保持质量"],
            "cons": ["增加管理复杂度", "可能有泄露风险"],
            "impact_reduction": "70%"
        })
        
        # 方案4: 协商延期
        alternatives.append({
            "name": "协商延期",
            "description": "与客户协商调整交期",
            "pros": ["降低所有风险", "成本最低"],
            "cons": ["可能丢失订单"],
            "impact_reduction": "90%"
        })
        
        return alternatives
    
    def _format_capacity_analysis(self, impact: DimensionImpact) -> Dict:
        return {
            "score": impact.impact_score,
            "description": impact.description,
            "financial_impact": impact.financial_impact
        }
    
    def _format_material_analysis(self, impact: DimensionImpact) -> Dict:
        return {
            "score": impact.impact_score,
            "description": impact.description,
            "affected_items": impact.affected_items
        }
    
    def _format_cost_analysis(self, impact: DimensionImpact) -> Dict:
        return {
            "score": impact.impact_score,
            "description": impact.description,
            "financial_impact": impact.financial_impact
        }
    
    def _format_timeline_analysis(self, impact: DimensionImpact) -> Dict:
        return {
            "score": impact.impact_score,
            "description": impact.description,
            "time_impact_days": impact.time_impact_days,
            "affected_orders": len(impact.affected_items)
        }


# ========== 便捷函数 =========-

def analyze_insert_order_impact(
    new_order_data: Dict,
    existing_plans_data: List[Dict],
    resources_data: List[Dict],
    materials_data: Dict[str, Dict]
) -> Dict:
    """
    便捷函数：分析插单影响
    
    Args:
        new_order_data: 新订单数据
        existing_plans_data: 现有计划列表
        resources_data: 资源列表
        materials_data: 物料数据
    
    Returns:
        影响分析结果
    """
    engine = ImpactAnalysisEngine()
    engine.set_resources(resources_data)
    engine.set_material_requirements(materials_data)
    
    # 创建新订单对象
    new_order = NewOrder(
        order_no=new_order_data["order_no"],
        item_code=new_order_data["item_code"],
        quantity=new_order_data["quantity"],
        priority=new_order_data.get("priority", 5),
        required_date=datetime.fromisoformat(new_order_data["required_date"]) if new_order_data.get("required_date") else None,
        unit_price=new_order_data.get("unit_price", 0),
        unit_cost=new_order_data.get("unit_cost", 0),
        work_center=new_order_data.get("work_center", "")
    )
    
    # 创建现有计划对象
    existing_plans = []
    for plan in existing_plans_data:
        existing_plans.append(ExistingPlan(
            plan_no=plan["plan_no"],
            order_no=plan["order_no"],
            item_code=plan["item_code"],
            quantity=plan["quantity"],
            start_date=datetime.fromisoformat(plan["start_date"]),
            end_date=datetime.fromisoformat(plan["end_date"]),
            work_center=plan["work_center"],
            priority=plan.get("priority", 5),
            status=plan.get("status", "planned")
        ))
    
    # 分析
    result = engine.analyze_order_impact(new_order, existing_plans)
    
    # 转换为字典
    return {
        "new_order_no": result.new_order_no,
        "is_feasible": result.is_feasible,
        "total_impact_score": result.total_impact_score,
        "risk_level": result.risk_level,
        "affected_orders": result.affected_orders,
        "capacity_analysis": result.capacity_analysis,
        "material_analysis": result.material_analysis,
        "cost_analysis": result.cost_analysis,
        "timeline_analysis": result.timeline_analysis,
        "recommendations": result.recommendations,
        "alternative_solutions": result.alternative_solutions
    }
