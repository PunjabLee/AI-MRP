# AI MRP OR求解模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

OR（运筹学）求解模块负责在约束条件下找到最优或近优的排程方案。

### 1.2 求解问题

| 问题类型 | 说明 |
|----------|------|
| 生产排程 | 多订单多工序排程 |
| 批量优化 | 最优采购/生产批量 |
| 库存优化 | 安全库存计算 |
| 成本优化 | 最小化总成本 |

---

## 二、算法设计

### 2.1 排程算法

```python
# 启发式算法
class SchedulingSolver:
    
    # 先进先出
    def fifo(self, jobs):
        return sorted(jobs, key=lambda x: x.release_time)
    
    # 最早交期优先
    def edd(self, jobs):
        return sorted(jobs, key=lambda x: x.due_date)
    
    # 最短加工时间
    def spt(self, jobs):
        return sorted(jobs, key=lambda x: x.processing_time)
    
    # 优先级+关键比
    def cr(self, jobs):
        return sorted(jobs, key=lambda x: x.critical_ratio())
```

### 2.2 智能优化

```python
# 遗传算法
class GeneticScheduler:
    def __init__(self, population_size=100, generations=50):
        self.population_size = population_size
        self.generations = generations
    
    def evolve(self, jobs, resources, constraints):
        population = self.init_population(jobs)
        
        for gen in range(self.generations):
            fitness = [self.fitness(chrom) for chrom in population]
            selection = self.select(population, fitness)
            crossover = self.crossover(selection)
            mutation = self.mutate(crossover)
            population = mutation
            
        return self.best(population)
```

---

## 三、约束建模

### 3.1 排程约束

```python
# 约束条件
CONSTRAINTS = {
    "capacity": "产能约束 - 工序不能超过产能",
    "precedence": "先后约束 - 工序有先后顺序",
    "resource": "资源约束 - 同一资源不能同时运行",
    "delivery": "交期约束 - 必须按时完成",
    "setup": "换线约束 - 换线需要时间"
}
```

### 3.2 目标函数

```python
# 多目标优化
def objective(schedule):
    # 最小化总延迟
    tardiness = sum(job.tardiness() for job in schedule.jobs)
    
    # 最小化换线次数
    setup = schedule.setup_count()
    
    # 最大化设备利用率
    utilization = schedule.resource_utilization()
    
    # 加权求和
    return 0.5 * tardiness + 0.3 * setup + 0.2 * (1 - utilization)
```

---

## 四、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/or/schedule` | POST | 生产排程优化 |
| `/api/or/batch-optimize` | POST | 批量优化 |
| `/api/or/safety-stock` | POST | 安全库存优化 |
| `/api/or/simulate` | POST | What-if模拟 |

---

## 五、求解策略

| 策略 | 适用场景 | 求解时间 |
|------|----------|----------|
| 启发式 | 快速响应 | <1秒 |
| 遗传算法 | 复杂排程 | 10-30秒 |
| 整数规划 | 精确求解 | 1-10分钟 |
| 仿真模拟 | What-if分析 | 30秒-5分钟 |

---

## 六、结果输出

```python
# 排程结果
class ScheduleResult:
    schedule_id: str
    jobs: List[ScheduledJob]
    makespan: float           # 总完成时间
    tardiness: float          # 总延迟
    utilization: float       # 设备利用率
    
    # 甘特图数据
    def to_gantt(self):
        return [
            {
                "job": job.id,
                "resource": resource.id,
                "start": job.start_time,
                "end": job.end_time
            }
            for job, resource in self.assignments
        ]
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
