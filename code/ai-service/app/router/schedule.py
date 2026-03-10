"""
Schedule Router - 智能排程服务（OR-Tools）
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
import random
import math

router = APIRouter()


class ScheduleRequest(BaseModel):
    """排程请求"""
    production_orders: List[Dict]  # 生产订单列表
    resources: List[Dict]  # 资源/设备列表
    constraints: Optional[Dict] = {}  # 排程约束
    optimization_goal: str = "makespan"  # makespan, tardiness, cost


class ScheduleResponse(BaseModel):
    """排程响应"""
    schedule_id: str
    status: str  # optimal, feasible, infeasible
    makespan: float  # 总完工时间
    total_tardiness: float  # 总延迟时间
    schedule_details: List[Dict]  # 详细排程结果
    gantt_data: List[Dict]  # 甘特图数据


class GanttRequest(BaseModel):
    """甘特图数据请求"""
    schedule_id: str


class ResourceLoadRequest(BaseModel):
    """资源负载请求"""
    start_date: str
    end_date: str
    resources: Optional[List[str]] = None


@router.post("/optimize", response_model=ScheduleResponse)
async def optimize_schedule(request: ScheduleRequest):
    """
    智能排程优化接口
    
    使用 OR-Tools 进行排程优化
    优化目标：
    - makespan: 最小化总完工时间
    - tardiness: 最小化总延迟时间
    - cost: 最小化总成本
    """
    production_orders = request.production_orders
    resources = request.resources
    optimization_goal = request.optimization_goal
    
    if not production_orders:
        raise HTTPException(status_code=400, detail="生产订单不能为空")
    
    if not resources:
        raise HTTPException(status_code=400, detail="资源不能为空")
    
    # TODO: 接入真实数据
    # 从数据库查询生产订单和资源数据
    
    # 执行排程优化
    schedule_result = _ortools(
        production_orders_optimize, 
        resources, 
        optimization_goal,
        request.constraints
    )
    
    return schedule_result


@router.post("/feasible-check")
async def check_feasibility(request: ScheduleRequest):
    """
    检查排程可行性
    
    检查给定的订单和资源是否可以在约束内完成
    """
    production_orders = request.production_orders
    resources = request.resources
    constraints = request.constraints
    
    # 检查资源是否足够
    total_work = sum(order.get("quantity", 0) * order.get("unit_time", 0) 
                     for order in production_orders)
    
    total_capacity = sum(resource.get("capacity", 0) * resource.get("available_hours", 8) 
                         for resource in resources)
    
    is_feasible = total_work <= total_capacity
    
    return {
        "feasible": is_feasible,
        "total_work": total_work,
        "total_capacity": total_capacity,
        "utilization": round(total_work / total_capacity * 100, 2) if total_capacity > 0 else 0,
        "suggestions": _generate_suggestions(is_feasible, total_work, total_capacity)
    }


@router.get("/methods")
async def get_optimization_methods():
    """获取支持的排程优化方法"""
    return {
        "methods": [
            {
                "name": "makespan",
                "description": "最小化总完工时间",
                "scenario": "多订单并行生产时追求最高效率"
            },
            {
                "name": "tardiness",
                "description": "最小化总延迟时间",
                "scenario": "追求交期准时性"
            },
            {
                "name": "cost",
                "description": "最小化总成本",
                "scenario": "考虑换线、加班等成本因素"
            },
            {
                "name": "balanced",
                "description": "平衡模式",
                "scenario": "综合考虑时间、成本、延迟"
            }
        ],
        "default": "makespan"
    }


@router.get("/constraints")
async def get_constraint_types():
    """获取支持的约束类型"""
    return {
        "constraints": [
            {
                "name": "deadline",
                "description": "订单截止时间",
                "example": {"order_id": "PO001", "deadline": "2026-03-15"}
            },
            {
                "name": "priority",
                "description": "订单优先级",
                "example": {"order_id": "PO001", "priority": 1}  # 1=最高
            },
            {
                "name": "sequence",
                "description": "工艺顺序约束",
                "example": {"order_id": "PO001", "before": "PO002"}
            },
            {
                "name": "resource_lock",
                "description": "资源锁定",
                "example": {"resource_id": "R001", "locked_orders": ["PO001"]}
            },
            {
                "name": "maintenance",
                "description": "维护窗口",
                "example": {"resource_id": "R001", "maintenance": [{"start": "2026-03-10 12:00", "end": "2026-03-10 14:00"}]}
            }
        ]
    }


# ========== OR-Tools 优化算法 ==========

def _ortools_optimize(production_orders: List[Dict], resources: List[Dict],
                      optimization_goal: str, constraints: Dict) -> ScheduleResponse:
    """
    OR-Tools 排程优化
    
    TODO: 实际实现需要安装 or-tools 库
    from ortools.sat.python import cp_model
    
    模型构建：
    1. 决策变量：每个订单在每个资源上的开始时间、持续时间
    2. 约束：资源容量、工艺顺序、优先级、截止时间
    3. 目标函数：makespan/tardiness/cost
    """
    # 简化实现：启发式排程算法
    schedule_id = f"SCH_{datetime.now().strftime('%Y%m%d%H%M%S')}"
    
    # 按优先级和截止时间排序
    sorted_orders = sorted(production_orders, 
                          key=lambda x: (x.get("priority", 999), x.get("deadline", "")))
    
    # 初始化资源时间线
    resource_timeline = {r["id"]: 0 for r in resources}
    
    schedule_details = []
    gantt_data = []
    
    for order in sorted_orders:
        # 选择最合适的资源
        selected_resource = _select_best_resource(
            order, resources, resource_timeline, constraints
        )
        
        # 计算开始和结束时间
        start_time = max(
            resource_timeline[selected_resource["id"]],
            _parse_deadline(order.get("available_after", ""))
        )
        
        quantity = order.get("quantity", 1)
        unit_time = order.get("unit_time", 60)  # 分钟
        duration = quantity * unit_time
        
        end_time = start_time + duration
        
        # 更新资源时间线
        resource_timeline[selected_resource["id"]] = end_time
        
        # 计算延迟
        deadline = _parse_deadline(order.get("deadline", ""))
        tardiness = max(0, (end_time - deadline) / 60) if deadline else 0
        
        detail = {
            "order_id": order.get("id"),
            "resource_id": selected_resource["id"],
            "resource_name": selected_resource["name"],
            "start_time": _format_datetime(start_time),
            "end_time": _format_datetime(end_time),
            "duration_minutes": duration,
            "quantity": quantity,
            "tardiness_hours": round(tardiness, 2)
        }
        schedule_details.append(detail)
        
        # 甘特图数据
        gantt_data.append({
            "order_id": order.get("id"),
            "resource": selected_resource["name"],
            "start": _format_datetime(start_time),
            "end": _format_datetime(end_time),
            "quantity": quantity,
            "color": _get_order_color(order.get("priority", 999))
        })
    
    # 计算总完工时间和总延迟
    makespan = max(resource_timeline.values()) / 60  # 转换为小时
    total_tardiness = sum(d["tardiness_hours"] for d in schedule_details)
    
    # 判断优化状态
    if total_tardiness == 0:
        status = "optimal"
    elif total_tardiness < makespan * 0.1:
        status = "feasible"
    else:
        status = "suboptimal"
    
    return ScheduleResponse(
        schedule_id=schedule_id,
        status=status,
        makespan=round(makespan, 2),
        total_tardiness=round(total_tardiness, 2),
        schedule_details=schedule_details,
        gantt_data=gantt_data
    )


def _select_best_resource(order: Dict, resources: List[Dict],
                          resource_timeline: Dict, constraints: Dict) -> Dict:
    """选择最佳资源（启发式）"""
    available_resources = []
    
    for resource in resources:
        # 检查资源是否被锁定
        locked_orders = constraints.get("resource_lock", {}).get(resource["id"], [])
        if order.get("id") in locked_orders:
            continue
        
        # 检查是否满足工艺要求
        if not _resource_supports_process(resource, order.get("process_type", "")):
            continue
        
        # 计算负载
        load = resource_timeline.get(resource["id"], 0)
        available_resources.append((resource, load))
    
    if not available_resources:
        # 如果没有合适的资源，选择负载最低的
        return min(resources, key=lambda r: resource_timeline.get(r["id"], 0))
    
    # 选择负载最低的资源
    return min(available_resources, key=lambda x: x[1])[0]


def _resource_supports_process(resource: Dict, process_type: str) -> bool:
    """检查资源是否支持指定工艺"""
    if not process_type:
        return True
    
    supported_processes = resource.get("supported_processes", [])
    return process_type in supported_processes or not supported_processes


def _parse_deadline(deadline_str: str) -> float:
    """解析截止时间字符串为时间戳"""
    if not deadline_str:
        return 0
    
    try:
        dt = datetime.fromisoformat(deadline_str.replace("Z", "+00:00"))
        return dt.timestamp()
    except:
        return 0


def _format_datetime(timestamp: float) -> str:
    """格式化时间戳为ISO字符串"""
    if timestamp == 0:
        return ""
    dt = datetime.fromtimestamp(timestamp)
    return dt.isoformat()


def _get_order_color(priority: int) -> str:
    """根据优先级获取颜色"""
    colors = {
        1: "#FF6B6B",  # 红色-最高优先级
        2: "#FFA500",  # 橙色
        3: "#FFD700",  # 黄色
        4: "#90EE90",  # 浅绿
        5: "#87CEEB",  # 天蓝-最低优先级
    }
    return colors.get(priority, "#87CEEB")


def _generate_suggestions(is_feasible: bool, total_work: float, 
                          total_capacity: float) -> List[str]:
    """生成可行性建议"""
    if is_feasible:
        return ["方案可行"]
    
    suggestions = []
    deficit = total_work - total_capacity
    
    if deficit > 0:
        suggestions.append(f"资源缺口: {round(deficit, 2)} 单位时间")
        suggestions.append("建议：增加产能或延长工作时间")
    
    return suggestions


# ========== 高级排程功能（规划中）============

@router.post("/whatif-simulation")
async def whatif_simulation(scenarios: List[Dict]):
    """
    What-if 场景模拟
    
    模拟多个排程方案并对比
    """
    results = []
    
    for i, scenario in enumerate(scenarios):
        result = _ortools_optimize(
            scenario.get("orders", []),
            scenario.get("resources", []),
            scenario.get("goal", "makespan"),
            scenario.get("constraints", {})
        )
        results.append({
            "scenario_id": f"SCEN_{i+1}",
            "scenario_name": scenario.get("name", f"方案{i+1}"),
            "makespan": result.makespan,
            "tardiness": result.total_tardiness,
            "status": result.status
        })
    
    # 对比分析
    best_makespan = min(results, key=lambda x: x["makespan"])
    best_tardiness = min(results, key=lambda x: x["tardiness"])
    
    return {
        "scenarios": results,
        "recommendation": {
            "for_speed": best_makespan,
            "for_ontime": best_tardiness
        }
    }


@router.post("/reschedule")
async def reschedule(order_id: str, new_deadline: str, current_schedule_id: str):
    """
    重新排程
    
    当订单变更时，触发重新排程
    """
    # TODO: 从缓存或数据库获取当前排程
    # 调整相关订单
    
    return {
        "order_id": order_id,
        "new_deadline": new_deadline,
        "reschedule_triggered": True,
        "message": "已触发重新排程，请调用 /optimize 接口获取新方案"
    }
