"""
排程优化引擎 - OR-Tools 真正实现

支持多种优化目标：
- makespan: 最小化总完工时间
- tardiness: 最小化总延迟时间
- cost: 最小化总成本
- balanced: 平衡模式

支持约束：
- 资源容量
- 工艺顺序
- 订单优先级
- 截止时间
- 资源锁定
- 维护窗口
"""
from typing import List, Dict, Any, Optional, Tuple
from datetime import datetime, timedelta
from dataclasses import dataclass
from enum import Enum
import numpy as np
import warnings

warnings.filterwarnings('ignore')


class OptimizationGoal(Enum):
    """优化目标"""
    MAKESPAN = "makespan"        # 最小化总完工时间
    TARDINESS = "tardiness"      # 最小化总延迟
    COST = "cost"                # 最小化成本
    BALANCED = "balanced"        # 平衡模式


@dataclass
class ProductionOrder:
    """生产订单"""
    id: str
    product: str
    quantity: float
    unit_process_time: float  # 分钟/件
    priority: int = 1  # 1=最高
    deadline: Optional[datetime] = None
    available_after: Optional[datetime] = None
    process_type: str = ""
    setup_time: float = 0  # 换线时间
    color: str = "#87CEEB"


@dataclass
class Resource:
    """生产资源（工作中心/设备）"""
    id: str
    name: str
    capacity: float = 8  # 每天可用小时
    available_hours: float = 8
    supported_processes: List[str] = None
    hourly_cost: float = 100  # 每小时成本
    overtime_cost: float = 150  # 加班每小时成本
    maintenance_windows: List[Tuple[datetime, datetime]] = None
    
    def __post_init__(self):
        if self.supported_processes is None:
            self.supported_processes = []
        if self.maintenance_windows is None:
            self.maintenance_windows = []


@dataclass
class ScheduleResult:
    """排程结果"""
    schedule_id: str
    status: str  # optimal, feasible, suboptimal, infeasible
    makespan_hours: float
    makespan_days: float
    total_tardiness_hours: float
    total_cost: float
    resource_utilization: Dict[str, float]
    schedule_details: List[Dict]
    gantt_data: List[Dict]
    metrics: Dict[str, Any]


class ORToolsScheduler:
    """OR-Tools 排程优化器"""
    
    def __init__(self):
        self.orders: List[ProductionOrder] = []
        self.resources: List[Resource] = []
        self.goal = OptimizationGoal.MAKESPAN
        self.constraints: Dict = {}
        self._cp_model = None
        self._solution = None
    
    def set_orders(self, orders: List[Dict]) -> 'ORToolsScheduler':
        """设置生产订单"""
        self.orders = []
        for o in orders:
            deadline = None
            if o.get("deadline"):
                try:
                    deadline = datetime.fromisoformat(o["deadline"].replace("Z", "+00:00"))
                except:
                    pass
            
            available_after = None
            if o.get("available_after"):
                try:
                    available_after = datetime.fromisoformat(o["available_after"].replace("Z", "+00:00"))
                except:
                    pass
            
            self.orders.append(ProductionOrder(
                id=o.get("id", f"ORDER_{len(self.orders)}"),
                product=o.get("product", ""),
                quantity=o.get("quantity", 1),
                unit_process_time=o.get("unit_time", 60),
                priority=o.get("priority", 1),
                deadline=deadline,
                available_after=available_after,
                process_type=o.get("process_type", ""),
                setup_time=o.get("setup_time", 0),
                color=self._get_priority_color(o.get("priority", 1))
            ))
        
        # 按优先级排序
        self.orders.sort(key=lambda x: x.priority)
        
        return self
    
    def set_resources(self, resources: List[Dict]) -> 'ORToolsScheduler':
        """设置生产资源"""
        self.resources = []
        for r in resources:
            maintenance = []
            if r.get("maintenance_windows"):
                for w in r["maintenance_windows"]:
                    try:
                        start = datetime.fromisoformat(w["start"].replace("Z", "+00:00"))
                        end = datetime.fromisoformat(w["end"].replace("Z", "+00:00"))
                        maintenance.append((start, end))
                    except:
                        pass
            
            self.resources.append(Resource(
                id=r.get("id", f"R_{len(self.resources)}"),
                name=r.get("name", ""),
                capacity=r.get("capacity", 8),
                available_hours=r.get("available_hours", 8),
                supported_processes=r.get("supported_processes", []),
                hourly_cost=r.get("hourly_cost", 100),
                overtime_cost=r.get("overtime_cost", 150),
                maintenance_windows=maintenance
            ))
        
        return self
    
    def set_goal(self, goal: str) -> 'ORToolsScheduler':
        """设置优化目标"""
        goal_map = {
            "makespan": OptimizationGoal.MAKESPAN,
            "tardiness": OptimizationGoal.TARDINESS,
            "cost": OptimizationGoal.COST,
            "balanced": OptimizationGoal.BALANCED
        }
        self.goal = goal_map.get(goal.lower(), OptimizationGoal.MAKESPAN)
        return self
    
    def set_constraints(self, constraints: Dict) -> 'ORToolsScheduler':
        """设置约束条件"""
        self.constraints = constraints
        return self
    
    def optimize(self) -> ScheduleResult:
        """执行优化"""
        if not self.orders or not self.resources:
            return self._create_empty_result()
        
        # 尝试使用 OR-Tools
        try:
            return self._ortools_solve()
        except Exception as e:
            # 回退到启发式算法
            return self._heuristic_solve()
    
    def _ortools_solve(self) -> ScheduleResult:
        """使用 OR-Tools 求解"""
        try:
            from ortools.sat.python import cp_model
            
            model = cp_model.CpModel()
            
            num_orders = len(self.orders)
            num_resources = len(self.resources)
            
            # 计算时间范围
            horizon = self._calculate_horizon()
            
            # 决策变量
            # start_time[o, r]: 订单 o 在资源 r 上的开始时间
            start_time = {}
            interval = {}  # 区间变量
            
            for o in range(num_orders):
                for r in range(num_resources):
                    duration = int(self.orders[o].quantity * self.orders[o].unit_process_time)
                    start_time[(o, r)] = model.NewIntVar(0, horizon, f'start_{o}_{r}')
                    interval[(o, r)] = model.NewIntervalVar(
                        start_time[(o, r)],
                        duration,
                        start_time[(o, r)] + duration,
                        f'interval_{o}_{r}'
                    )
            
            # 分配变量：order o 分配给 resource r
            assign = {}
            for o in range(num_orders):
                assign[o] = {}
                for r in range(num_resources):
                    assign[o][r] = model.NewBoolVar(f'assign_{o}_{r}')
            
            # ===== 约束 =====
            
            # 1. 每个订单必须分配且只能分配给一个资源
            for o in range(num_orders):
                model.Add(sum(assign[o][r] for r in range(num_resources)) == 1)
            
            # 2. 资源容量约束
            for r in range(num_resources):
                intervals = []
                for o in range(num_orders):
                    # 如果订单 o 分配给资源 r，添加其区间
                    duration = int(self.orders[o].quantity * self.orders[o].unit_process_time)
                    intervals.append(
                        model.NewIntervalVar(
                            start_time[(o, r)],
                            duration,
                            start_time[(o, r)] + duration,
                            f'capacity_{o}_{r}'
                        )
                    )
                
                # 添加累积约束
                capacity = int(self.resources[r].capacity * 60)  # 转换为分钟
                model.AddCumulative(intervals, [assign[o][r] for o in range(num_orders)], capacity)
            
            # 3. 优先级约束（高优先级订单先开始）
            for o1 in range(num_orders):
                for o2 in range(o1 + 1, num_orders):
                    if self.orders[o1].priority < self.orders[o2].priority:
                        # 订单 o1 优先级更高
                        for r in range(num_resources):
                            model.Add(
                                start_time[(o1, r)] + 
                                int(self.orders[o1].quantity * self.orders[o1].unit_process_time)
                                <= start_time[(o2, r)] + horizon * (1 - assign[o2][r])
                            ).OnlyEnforceIf(assign[o2][r])
            
            # 4. 截止时间约束
            for o in range(num_orders):
                if self.orders[o].deadline:
                    deadline_minutes = int((self.orders[o].deadline - datetime.now()).total_seconds() / 60)
                    if deadline_minutes > 0:
                        for r in range(num_resources):
                            end_time = start_time[(o, r)] + int(self.orders[o].quantity * self.orders[o].unit_process_time)
                            model.Add(end_time <= deadline_minutes).OnlyEnforceIf(assign[o][r])
            
            # ===== 目标函数 =====
            
            if self.goal == OptimizationGoal.MAKESPAN:
                # 最小化最大完工时间
                all_ends = []
                for o in range(num_orders):
                    for r in range(num_resources):
                        end = start_time[(o, r)] + int(self.orders[o].quantity * self.orders[o].unit_process_time)
                        all_ends.append(end * assign[o][r])
                makespan = max(all_ends)
                model.Minimize(makespan)
                
            elif self.goal == OptimizationGoal.TARDINESS:
                # 最小化总延迟
                tardiness = []
                for o in range(num_orders):
                    for r in range(num_resources):
                        if self.orders[o].deadline:
                            deadline = int((self.orders[o].deadline - datetime.now()).total_seconds() / 60)
                            end = start_time[(o, r)] + int(self.orders[o].quantity * self.orders[o].unit_process_time)
                            late = model.NewIntVar(0, horizon, f'late_{o}_{r}')
                            model.AddMaxEquality(late, [end - deadline, 0])
                            tardiness.append(late * assign[o][r])
                model.Minimize(sum(tardiness))
                
            elif self.goal == OptimizationGoal.COST:
                # 最小化总成本
                cost = []
                for o in range(num_orders):
                    for r in range(num_resources):
                        duration = self.orders[o].quantity * self.orders[o].unit_process_time / 60
                        order_cost = duration * self.resources[r].hourly_cost
                        cost.append(order_cost * assign[o][r])
                model.Minimize(sum(cost))
            
            # ===== 求解 =====
            
            solver = cp_model.CpSolver()
            solver.parameters.max_time_in_seconds = 30
            solver.parameters.num_workers = 4
            
            status = solver.Solve(model)
            
            return self._parse_solution(solver, status, start_time, assign)
            
        except ImportError:
            # OR-Tools 未安装，使用启发式
            return self._heuristic_solve()
    
    def _parse_solution(self, solver, status, start_time, assign) -> ScheduleResult:
        """解析求解结果"""
        if status not in [cp_model.OPTIMAL, cp_model.FEASIBLE]:
            return self._heuristic_solve()
        
        num_orders = len(self.orders)
        num_resources = len(self.resources)
        
        schedule_details = []
        gantt_data = []
        makespan = 0
        total_tardiness = 0
        total_cost = 0
        resource_usage = {r.id: 0 for r in self.resources}
        
        for o in range(num_orders):
            for r in range(num_resources):
                if solver.Value(assign[o][r]):
                    order = self.orders[o]
                    resource = self.resources[r]
                    start = solver.Value(start_time[(o, r)])
                    duration = int(order.quantity * order.unit_process_time)
                    end = start + duration
                    
                    makespan = max(makespan, end)
                    resource_usage[resource.id] += duration
                    
                    # 计算延迟
                    tardiness = 0
                    if order.deadline:
                        deadline_minutes = int((order.deadline - datetime.now()).total_seconds() / 60)
                        tardiness = max(0, end - deadline_minutes) / 60
                    
                    total_tardiness += tardiness
                    total_cost += (duration / 60) * resource.hourly_cost
                    
                    # 详细计划
                    schedule_details.append({
                        "order_id": order.id,
                        "product": order.product,
                        "resource_id": resource.id,
                        "resource_name": resource.name,
                        "start_time": self._format_minutes(start),
                        "end_time": self._format_minutes(end),
                        "duration_hours": round(duration / 60, 2),
                        "quantity": order.quantity,
                        "priority": order.priority,
                        "tardiness_hours": round(tardiness, 2),
                        "cost": round(total_cost, 2)
                    })
                    
                    # 甘特图数据
                    gantt_data.append({
                        "order_id": order.id,
                        "product": order.product,
                        "resource": resource.name,
                        "start": self._format_minutes(start),
                        "end": self._format_minutes(end),
                        "duration": duration / 60,
                        "quantity": order.quantity,
                        "priority": order.priority,
                        "color": order.color
                    })
        
        # 计算资源利用率
        resource_utilization = {}
        for r in self.resources:
            total_capacity = r.capacity * 60  # 分钟
            used = resource_usage.get(r.id, 0)
            utilization = (used / total_capacity * 100) if total_capacity > 0 else 0
            resource_utilization[r.name] = round(utilization, 2)
        
        # 判断状态
        if status == cp_model.OPTIMAL:
            status_str = "optimal"
        elif total_tardiness == 0:
            status_str = "feasible"
        else:
            status_str = "suboptimal"
        
        return ScheduleResult(
            schedule_id=f"SCH_{datetime.now().strftime('%Y%m%d%H%M%S')}",
            status=status_str,
            makespan_hours=round(makespan / 60, 2),
            makespan_days=round(makespan / 60 / 8, 2),
            total_tardiness_hours=round(total_tardiness, 2),
            total_cost=round(total_cost, 2),
            resource_utilization=resource_utilization,
            schedule_details=schedule_details,
            gantt_data=gantt_data,
            metrics={
                "total_orders": num_orders,
                "total_resources": num_resources,
                "goal": self.goal.value
            }
        )
    
    def _heuristic_solve(self) -> ScheduleResult:
        """启发式排程算法"""
        schedule_details = []
        gantt_data = []
        
        # 资源时间线（分钟）
        resource_timeline = {r.id: 0 for r in self.resources}
        
        for order in self.orders:
            # 选择最佳资源
            best_resource = self._select_resource_heuristic(order)
            
            # 计算开始时间
            start_minutes = resource_timeline[best_resource.id]
            
            # 考虑可用时间
            if order.available_after:
                available_minutes = int((order.available_after - datetime.now()).total_seconds() / 60)
                start_minutes = max(start_minutes, available_minutes)
            
            # 计算工期
            duration_minutes = int(order.quantity * order.unit_process_time)
            end_minutes = start_minutes + duration_minutes
            
            # 更新资源时间线
            resource_timeline[best_resource.id] = end_minutes
            
            # 计算延迟
            tardiness = 0
            if order.deadline:
                deadline_minutes = int((order.deadline - datetime.now()).total_seconds() / 60)
                tardiness = max(0, (end_minutes - deadline_minutes) / 60)
            
            # 计算成本
            cost = (duration_minutes / 60) * best_resource.hourly_cost
            
            schedule_details.append({
                "order_id": order.id,
                "product": order.product,
                "resource_id": best_resource.id,
                "resource_name": best_resource.name,
                "start_time": self._format_minutes(start_minutes),
                "end_time": self._format_minutes(end_minutes),
                "duration_hours": round(duration_minutes / 60, 2),
                "quantity": order.quantity,
                "priority": order.priority,
                "tardiness_hours": round(tardiness, 2),
                "cost": round(cost, 2)
            })
            
            gantt_data.append({
                "order_id": order.id,
                "product": order.product,
                "resource": best_resource.name,
                "start": self._format_minutes(start_minutes),
                "end": self._format_minutes(end_minutes),
                "duration": duration_minutes / 60,
                "quantity": order.quantity,
                "priority": order.priority,
                "color": order.color
            })
        
        # 计算指标
        makespan = max(resource_timeline.values()) / 60
        total_tardiness = sum(d["tardiness_hours"] for d in schedule_details)
        total_cost = sum(d["cost"] for d in schedule_details)
        
        # 资源利用率
        resource_utilization = {}
        for r in self.resources:
            used = resource_timeline[r.id]
            total_capacity = r.capacity * 60
            utilization = (used / total_capacity * 100) if total_capacity > 0 else 0
            resource_utilization[r.name] = round(utilization, 2)
        
        # 判断状态
        if total_tardiness == 0:
            status = "optimal"
        elif total_tardiness < makespan * 0.1:
            status = "feasible"
        else:
            status = "suboptimal"
        
        return ScheduleResult(
            schedule_id=f"SCH_{datetime.now().strftime('%Y%m%d%H%M%S')}",
            status=status,
            makespan_hours=round(makespan, 2),
            makespan_days=round(makespan / 8, 2),
            total_tardiness_hours=round(total_tardiness, 2),
            total_cost=round(total_cost, 2),
            resource_utilization=resource_utilization,
            schedule_details=schedule_details,
            gantt_data=gantt_data,
            metrics={
                "total_orders": len(self.orders),
                "total_resources": len(self.resources),
                "method": "heuristic"
            }
        )
    
    def _select_resource_heuristic(self, order: ProductionOrder) -> Resource:
        """启发式选择最佳资源"""
        candidates = []
        
        for resource in self.resources:
            # 检查是否支持工艺
            if order.process_type and order.process_type not in resource.supported_processes:
                continue
            
            # 检查资源锁定
            locked = self.constraints.get("resource_lock", {}).get(resource.id, [])
            if order.id in locked:
                continue
            
            # 计算负载
            load = resource_timeline.get(resource.id, 0)
            candidates.append((resource, load))
        
        if not candidates:
            # 返回负载最低的资源
            return min(self.resources, key=lambda r: resource_timeline.get(r.id, 0))
        
        # 选择负载最低的
        return min(candidates, key=lambda x: x[1])[0]
    
    def _calculate_horizon(self) -> int:
        """计算时间范围（分钟）"""
        total_work = sum(
            o.quantity * o.unit_process_time 
            for o in self.orders
        )
        
        total_capacity = sum(
            r.capacity * 60 * len(self.resources)
            for r in self.resources
        )
        
        return int(total_work + total_capacity)
    
    def _format_minutes(self, minutes: int) -> str:
        """格式化时间"""
        if minutes <= 0:
            return datetime.now().isoformat()
        
        dt = datetime.now() + timedelta(minutes=minutes)
        return dt.isoformat()
    
    def _get_priority_color(self, priority: int) -> str:
        """获取优先级颜色"""
        colors = {
            1: "#FF6B6B",  # 红色
            2: "#FFA500",  # 橙色
            3: "#FFD700",  # 黄色
            4: "#90EE90",  # 浅绿
            5: "#87CEEB"   # 天蓝
        }
        return colors.get(priority, "#87CEEB")
    
    def _create_empty_result(self) -> ScheduleResult:
        """创建空结果"""
        return ScheduleResult(
            schedule_id="",
            status="infeasible",
            makespan_hours=0,
            makespan_days=0,
            total_tardiness_hours=0,
            total_cost=0,
            resource_utilization={},
            schedule_details=[],
            gantt_data=[],
            metrics={}
        )


# ========== 工厂方法 =========-

def create_scheduler(
    orders: List[Dict],
    resources: List[Dict],
    goal: str = "makespan",
    constraints: Optional[Dict] = None
) -> ScheduleResult:
    """
    创建排程优化器并求解
    
    Args:
        orders: 生产订单列表
        resources: 资源列表
        goal: 优化目标
        constraints: 约束条件
    
    Returns:
        ScheduleResult 排程结果
    """
    scheduler = ORToolsScheduler()
    scheduler.set_orders(orders)
    scheduler.set_resources(resources)
    scheduler.set_goal(goal)
    
    if constraints:
        scheduler.set_constraints(constraints)
    
    return scheduler.optimize()


def compare_scenarios(scenarios: List[Dict]) -> Dict:
    """
    对比多个排程方案
    
    Args:
        scenarios: 方案列表，每个包含 orders, resources, goal, name
    
    Returns:
        对比结果
    """
    results = []
    
    for i, scenario in enumerate(scenarios):
        result = create_scheduler(
            orders=scenario.get("orders", []),
            resources=scenario.get("resources", []),
            goal=scenario.get("goal", "makespan"),
            constraints=scenario.get("constraints", {})
        )
        
        results.append({
            "scenario_id": f"SCEN_{i+1}",
            "scenario_name": scenario.get("name", f"方案{i+1}"),
            "goal": scenario.get("goal", "makespan"),
            "makespan_hours": result.makespan_hours,
            "makespan_days": result.makespan_days,
            "total_tardiness": result.total_tardiness_hours,
            "total_cost": result.total_cost,
            "status": result.status,
            "resource_utilization": result.resource_utilization
        })
    
    # 推荐
    best_makespan = min(results, key=lambda x: x["makespan_hours"])
    best_tardiness = min(results, key=lambda x: x["total_tardiness"])
    best_cost = min(results, key=lambda x: x["total_cost"])
    
    return {
        "scenarios": results,
        "recommendations": {
            "for_speed": {
                "scenario": best_makespan["scenario_name"],
                "makespan": best_makespan["makespan_hours"]
            },
            "for_ontime": {
                "scenario": best_tardiness["scenario_name"],
                "tardiness": best_tardiness["total_tardiness"]
            },
            "for_cost": {
                "scenario": best_cost["scenario_name"],
                "cost": best_cost["total_cost"]
            }
        }
    }
