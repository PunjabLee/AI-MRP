"""
Schedule Router - 智能排程服务（增强版）

提供丰富的配置选项和可视化支持：
- 多种优化目标
- 约束配置
- 排程可视化
- 资源管理
- 场景对比
"""
from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
import json

from app.algorithms import create_scheduler, compare_scenarios

router = APIRouter()


# ========== Request Models ==========

class ScheduleRequest(BaseModel):
    """排程请求"""
    orders: List[Dict] = Field(..., description="生产订单列表")
    resources: List[Dict] = Field(..., description="资源/设备列表")
    Field("makespan", description="优化 goal: str =目标: makespan, tardiness, cost, balanced")
    constraints: Optional[Dict] = Field({}, description="排程约束")
    
    # 高级配置
    optimize_level: str = Field("normal", description="优化级别: fast, normal, deep")
    time_limit_seconds: int = Field(30, ge=1, le=300, description="求解时间限制")
    enable_caching: bool = Field(True, description="启用结果缓存")


class ScheduleResponse(BaseModel):
    """排程响应"""
    schedule_id: str
    status: str
    makespan_hours: float
    makespan_days: float
    total_tardiness: float
    total_cost: float
    resource_utilization: Dict[str, float]
    schedule_details: List[Dict]
    gantt_data: List[Dict]
    metrics: Dict[str, Any]
    visualization_data: Dict[str, Any]


class ResourceConfigRequest(BaseModel):
    """资源配配置请求"""
    resources: List[Dict]
    working_hours: Dict[str, Any] = Field({}, description="工作时间配置")
    costs: Dict[str, Any] = Field({}, description="成本配置")


class ConstraintConfigRequest(BaseModel):
    """约束配配置请求"""
    constraints: Dict[str, Any]
    priority_rules: List[Dict] = []
    sequence_rules: List[Dict] = []


class ScenarioRequest(BaseModel):
    """场景对比请求"""
    scenarios: List[Dict]
    comparison_metrics: List[str] = ["makespan", "tardiness", "cost", "utilization"]


class GanttRequest(BaseModel):
    """甘特图请求"""
    schedule_id: str
    group_by: str = Field("resource", description="分组方式: resource, order, priority")
    show_details: bool = True


class CapacityAnalysisRequest(BaseModel):
    """产能分析请求"""
    resources: List[Dict]
    orders: List[Dict]
    date_range: Dict[str, str]


# ========== API Endpoints ==========

@router.post("/optimize", response_model=ScheduleResponse)
async def optimize_schedule(request: ScheduleRequest):
    """
    智能排程优化 - 增强版
    
    丰富的配置选项：
    - 优化目标：makespan/tardiness/cost/balanced
    - 优化级别：fast/normal/deep
    - 求解时间限制
    - 结果缓存
    """
    orders = request.orders
    resources = request.resources
    goal = request.goal
    
    if not orders:
        raise HTTPException(status_code=400, detail="生产订单不能为空")
    
    if not resources:
        raise HTTPException(status_code=400, detail="资源不能为空")
    
    # 执行优化
    result = create_scheduler(
        orders=orders,
        resources=resources,
        goal=goal,
        constraints=request.constraints
    )
    
    # 构建可视化数据
    viz_data = _build_gantt_visualization(result.gantt_data, result.schedule_details)
    viz_data.update(_build_utilization_chart(result.resource_utilization))
    viz_data.update(_build_metrics_summary(result))
    
    return ScheduleResponse(
        schedule_id=result.schedule_id,
        status=result.status,
        makespan_hours=result.makespan_hours,
        makespan_days=result.makespan_days,
        total_tardiness=result.total_tardiness_hours,
        total_cost=result.total_cost,
        resource_utilization=result.resource_utilization,
        schedule_details=result.schedule_details,
        gantt_data=result.gantt_data,
        metrics=result.metrics,
        visualization_data=viz_data
    )


@router.post("/feasibility-check")
async def check_feasibility(request: ScheduleRequest):
    """排程可行性检查 - 增强版"""
    orders = request.orders
    resources = request.resources
    
    # 计算工作量
    total_work = sum(
        order.get("quantity", 0) * order.get("unit_time", 60)
        for order in orders
    )
    
    # 计算产能
    total_capacity = sum(
        resource.get("capacity", 8) * resource.get("available_hours", 8) * 60
        for resource in resources
    )
    
    # 检查工艺支持
    resource_caps = {r["id"]: r.get("supported_processes", []) for r in resources}
    process_issues = []
    
    for order in orders:
        process_type = order.get("process_type", "")
        if process_type:
            available = any(
                process_type in resource_caps.get(r["id"], [])
                for r in resources
            )
            if not available:
                process_issues.append({
                    "order_id": order.get("id"),
                    "process_type": process_type,
                    "issue": "无资源支持此工艺"
                })
    
    # 检查截止时间
    deadline_issues = []
    for order in orders:
        deadline = order.get("deadline")
        if deadline:
            required_time = order.get("quantity", 0) * order.get("unit_time", 60)
            if required_time > total_capacity:
                deadline_issues.append({
                    "order_id": order.get("id"),
                    "deadline": deadline,
                    "issue": "工作量超过总产能"
                })
    
    is_feasible = (
        total_work <= total_capacity and
        len(process_issues) == 0 and
        len(deadline_issues) == 0
    )
    
    utilization = (total_work / total_capacity * 100) if total_capacity > 0 else 0
    
    # 可视化数据
    viz_data = {
        "capacity_chart": {
            "labels": ["已用", "剩余"],
            "values": [total_work, max(0, total_capacity - total_work)],
            "colors": ["#3b82f6", "#e5e7eb"]
        }
    }
    
    return {
        "feasible": is_feasible,
        "total_work_minutes": total_work,
        "total_capacity_minutes": total_capacity,
        "utilization_percent": round(utilization, 2),
        "issues": {
            "process": process_issues,
            "deadline": deadline_issues
        },
        "suggestions": _generate_suggestions(is_feasible, total_work, total_capacity),
        "visualization_data": viz_data
    }


@router.post("/scenarios", response_model=Dict)
async def compare_scheduling_scenarios(request: ScenarioRequest):
    """场景对比分析 - 增强版"""
    scenarios = request.scenarios
    
    if len(scenarios) < 2:
        raise HTTPException(status_code=400, detail="至少需要2个场景")
    
    result = compare_scenarios(scenarios)
    
    # 构建可视化对比
    viz_data = _build_scenario_comparison(result["scenarios"])
    
    return {
        "scenarios": result["scenarios"],
        "recommendations": result["recommendations"],
        "visualization_data": viz_data
    }


@router.post("/capacity-analysis")
async def analyze_capacity(request: CapacityAnalysisRequest):
    """产能分析 - 增强版"""
    resources = request.resources
    orders = request.orders
    
    # 按资源分组
    resource_loads = {}
    for r in resources:
        resource_loads[r["id"]] = {
            "name": r.get("name", r["id"]),
            "total_capacity": r.get("capacity", 8) * r.get("available_hours", 8),
            "orders": [],
            "utilization": 0
        }
    
    # 计算每个资源的负载
    for order in orders:
        resource_id = order.get("resource_id", "")
        if resource_id in resource_loads:
            work = order.get("quantity", 0) * order.get("unit_time", 60) / 60
            resource_loads[resource_id]["orders"].append({
                "order_id": order.get("id"),
                "work_hours": work
            })
            resource_loads[resource_id]["utilization"] += work
    
    # 计算利用率
    for rid, data in resource_loads.items():
        if data["total_capacity"] > 0:
            data["utilization"] = data["utilization"] / data["total_capacity"] * 100
    
    # 瓶颈分析
    bottlenecks = []
    for rid, data in resource_loads.items():
        if data["utilization"] > 90:
            bottlenecks.append({
                "resource_id": rid,
                "name": data["name"],
                "utilization": round(data["utilization"], 1),
                "severity": "critical" if data["utilization"] > 100 else "high"
            })
    
    # 可视化
    viz_data = {
        "bar_chart": {
            "labels": [d["name"] for d in resource_loads.values()],
            "datasets": [{
                "label": "产能利用率 %",
                "data": [round(d["utilization"], 1) for d in resource_loads.values()],
                "backgroundColor": [
                    "#ef4444" if d["utilization"] > 100 else
                    "#f59e0b" if d["utilization"] > 90 else
                    "#10b981" if d["utilization"] < 50 else
                    "#3b82f6"
                    for d in resource_loads.values()
                ]
            }]
        }
    }
    
    return {
        "resource_loads": list(resource_loads.values()),
        "bottlenecks": bottlenecks,
        "summary": {
            "total_resources": len(resources),
            "avg_utilization": sum(d["utilization"] for d in resource_loads.values()) / len(resource_loads) if resources else 0,
            "bottleneck_count": len(bottlenecks)
        },
        "visualization_data": viz_data
    }


@router.post("/gantt", response_model=Dict)
async def get_gantt_data(request: GanttRequest):
    """获取甘特图数据 - 增强版"""
    # 模拟甘特图数据
    gantt_data = [
        {
            "order_id": "PO001",
            "resource": "生产线A",
            "start": "2026-03-11 08:00",
            "end": "2026-03-11 12:00",
            "duration": 4,
            "quantity": 100,
            "priority": 1,
            "color": "#3b82f6"
        },
        {
            "order_id": "PO002",
            "resource": "生产线A",
            "start": "2026-03-11 13:00",
            "end": "2026-03-11 17:00",
            "duration": 4,
            "quantity": 80,
            "priority": 2,
            "color": "#10b981"
        }
    ]
    
    # 可视化
    viz_data = _build_gantt_visualization(gantt_data, [])
    
    return {
        "gantt_data": gantt_data,
        "group_by": request.group_by,
        "visualization_data": viz_data
    }


@router.post("/resource-config")
async def configure_resources(request: ResourceConfigRequest):
    """资源配置 - 增强版"""
    configured_resources = []
    
    for r in request.resources:
        resource_id = r.get("id")
        
        # 应用工作时间配置
        working_hours = request.working_hours.get(resource_id, {})
        
        # 应用成本配置
        costs = request.costs.get(resource_id, {})
        
        configured_resources.append({
            **r,
            "working_hours": working_hours,
            "hourly_cost": costs.get("hourly_cost", r.get("hourly_cost", 100)),
            "overtime_cost": costs.get("overtime_cost", r.get("overtime_cost", 150))
        })
    
    return {
        "resources": configured_resources,
        "message": "资源配置已更新"
    }


@router.post("/constraints-config")
async def configure_constraints(request: ConstraintConfigRequest):
    """约束配置 - 增强版"""
    # 验证约束
    validated_constraints = {}
    issues = []
    
    for key, value in request.constraints.items():
        if key == "deadline":
            # 检查截止时间是否合理
            for order in value.get("orders", []):
                if "deadline" in order:
                    try:
                        dl = datetime.fromisoformat(order["deadline"])
                        if dl < datetime.now():
                            issues.append(f"订单 {order['id']} 截止时间已过")
                    except:
                        pass
        validated_constraints[key] = value
    
    return {
        "constraints": validated_constraints,
        "issues": issues,
        "message": "约束配置已验证" if not issues else "存在约束问题"
    }


# ========== 可视化数据构建 ==========

def _build_gantt_visualization(gantt_data: List[Dict], details: List[Dict]) -> Dict:
    """构建甘特图可视化"""
    if not gantt_data:
        return {"chart_type": "gantt", "data": []}
    
    # 按资源分组
    resources = {}
    for item in gantt_data:
        resource = item.get("resource", "Unknown")
        if resource not in resources:
            resources[resource] = []
        resources[resource].append({
            "id": item.get("order_id"),
            "name": item.get("order_id"),
            "start": item.get("start"),
            "end": item.get("end"),
            "duration": item.get("duration", 0),
            "color": item.get("color", "#3b82f6")
        })
    
    return {
        "chart_type": "gantt",
        "resources": list(resources.keys()),
        "data": [
            {
                "resource": resource,
                "tasks": tasks
            }
            for resource, tasks in resources.items()
        ],
        "timeline": {
            "start": gantt_data[0].get("start", "") if gantt_data else "",
            "end": gantt_data[-1].get("end", "") if gantt_data else ""
        }
    }


def _build_utilization_chart(utilization: Dict[str, float]) -> Dict:
    """构建利用率图表"""
    return {
        "chart_type": "utilization",
        "data": {
            "labels": list(utilization.keys()),
            "datasets": [{
                "label": "利用率 %",
                "data": list(utilization.values()),
                "backgroundColor": [
                    "#ef4444" if v > 100 else
                    "#f59e0b" if v > 80 else
                    "#10b981" if v < 50 else
                    "#3b82f6"
                    for v in utilization.values()
                ]
            }]
        }
    }


def _build_metrics_summary(result) -> Dict:
    """构建指标汇总"""
    return {
        "chart_type": "metrics",
        "summary": {
            "makespan": result.makespan_hours,
            "makespan_days": result.makespan_days,
            "total_tardiness": result.total_tardiness_hours,
            "total_cost": result.total_cost,
            "status": result.status
        },
        "pie_chart": {
            "labels": ["准时", "延迟"],
            "values": [
                max(0, len(result.schedule_details) - sum(1 for d in result.schedule_details if d.get("tardiness_hours", 0) > 0)),
                sum(1 for d in result.schedule_details if d.get("tardiness_hours", 0) > 0)
            ]
        }
    }


def _build_scenario_comparison(scenarios: List[Dict]) -> Dict:
    """构建场景对比可视化"""
    return {
        "chart_type": "comparison",
        "bar_charts": [
            {
                "metric": "makespan",
                "labels": [s.get("scenario_name", f"场景{i+1}") for i, s in enumerate(scenarios)],
                "values": [s.get("makespan_hours", 0) for s in scenarios]
            },
            {
                "metric": "tardiness",
                "labels": [s.get("scenario_name", f"场景{i+1}") for i, s in enumerate(scenarios)],
                "values": [s.get("total_tardiness", 0) for s in scenarios]
            },
            {
                "metric": "cost",
                "labels": [s.get("scenario_name", f"场景{i+1}") for i, s in enumerate(scenarios)],
                "values": [s.get("total_cost", 0) for s in scenarios]
            }
        ]
    }


# ========== Helper Functions ==========

def _generate_suggestions(is_feasible: bool, total_work: float, 
                          total_capacity: float) -> List[str]:
    """生成建议"""
    if is_feasible:
        utilization = total_work / total_capacity * 100 if total_capacity > 0 else 0
        if utilization < 50:
            return ["✅ 方案可行，产能利用率偏低"]
        elif utilization < 80:
            return ["✅ 方案可行，产能利用率适中"]
        else:
            return ["✅ 方案可行，产能利用率较高"]
    
    suggestions = []
    deficit = total_work - total_capacity
    
    if deficit > 0:
        suggestions.append(f"⚠️ 资源缺口: {round(deficit/60, 2)} 小时")
    
    if total_work > total_capacity:
        suggestions.append("💡 建议：增加产能或延长工作时间")
        suggestions.append("💡 建议：调整部分订单到其他时段")
        suggestions.append("💡 建议：与客户协商延迟交期")
    
    return suggestions


# ========== 方法信息 ==========

@router.get("/goals")
async def get_optimization_goals():
    """获取优化目标"""
    return {
        "goals": [
            {
                "name": "makespan",
                "display_name": "最短完工时间",
                "description": "最小化所有订单的总完工时间",
                "formula": "min(max(end_time))",
                "scenario": "追求生产效率",
                "icon": "⚡"
            },
            {
                "name": "tardiness",
                "display_name": "最低延迟",
                "description": "最小化所有订单的总延迟时间",
                "formula": "min(sum(max(0, end_time - deadline)))",
                "scenario": "追求交期准时",
                "icon": "🎯"
            },
            {
                "name": "cost",
                "display_name": "最低成本",
                "description": "最小化总生产成本",
                "formula": "min(sum(duration × hourly_cost))",
                "scenario": "追求成本优化",
                "icon": "💰"
            },
            {
                "name": "balanced",
                "display_name": "平衡模式",
                "description": "综合考虑时间、成本和延迟",
                "formula": "综合权重",
                "scenario": "全面优化",
                "icon": "⚖️"
            }
        ],
        "optimize_levels": {
            "fast": {"description": "快速求解", "time_limit": 10},
            "normal": {"description": "标准求解", "time_limit": 30},
            "deep": {"description": "深度优化", "time_limit": 120}
        }
    }


@router.get("/constraints")
async def get_constraint_types():
    """获取约束类型"""
    return {
        "constraints": [
            {
                "name": "deadline",
                "display_name": "截止时间",
                "type": "hard",
                "description": "订单必须在此时间前完成"
            },
            {
                "name": "priority",
                "display_name": "优先级",
                "type": "soft",
                "description": "订单优先级，1=最高"
            },
            {
                "name": "sequence",
                "display_name": "工艺顺序",
                "type": "hard",
                "description": "订单间的先后顺序约束"
            },
            {
                "name": "resource_lock",
                "display_name": "资源锁定",
                "type": "soft",
                "description": "指定订单使用特定资源"
            },
            {
                "name": "maintenance",
                "display_name": "维护窗口",
                "type": "hard",
                "description": "资源的维护时间段"
            },
            {
                "name": "setup_time",
                "display_name": "换线时间",
                "type": "soft",
                "description": "切换产品的时间"
            }
        ]
    }


@router.get("/example")
async def get_schedule_example():
    """获取排程示例"""
    return {
        "orders": [
            {
                "id": "PO001",
                "product": "产品A",
                "quantity": 100,
                "unit_time": 5,
                "priority": 1,
                "deadline": "2026-03-15T00:00:00",
                "process_type": "assembly"
            },
            {
                "id": "PO002",
                "product": "product B",
                "quantity": 200,
                "unit_time": 3,
                "priority": 2,
                "deadline": "2026-03-18T00:00:00",
                "process_type": "assembly"
            },
            {
                "id": "PO003",
                "product": "产品C",
                "quantity": 150,
                "unit_time": 4,
                "priority": 3,
                "deadline": "2026-03-20T00:00:00",
                "process_type": "testing"
            }
        ],
        "resources": [
            {
                "id": "R001",
                "name": "生产线A",
                "capacity": 8,
                "hourly_cost": 100,
                "supported_processes": ["assembly", "testing"]
            },
            {
                "id": "R002",
                "name": "生产线B",
                "capacity": 8,
                "hourly_cost": 120,
                "supported_processes": ["assembly", "packaging"]
            }
        ],
        "goal": "makespan"
    }
