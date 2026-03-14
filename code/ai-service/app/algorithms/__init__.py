"""
AI MRP 算法引擎模块

导出：
- forecast: 预测算法 (Prophet/LSTM/ARIMA/XGBoost)
- scheduler: 排程优化 (OR-Tools)
- safety_stock: 安全库存计算
- llm: LLM 集成
- whatif: What-if 模拟
- impact_analysis: 插单影响分析
"""
from app.algorithms.forecast import (
    ForecastEngine,
    ProphetForecast,
    ARIMAForecast,
    LSTMForecast,
    XGBoostForecast,
    MovingAverageForecast,
    ExponentialSmoothingForecast,
    create_forecast_engine,
    auto_select_algorithm
)

from app.algorithms.scheduler import (
    ORToolsScheduler,
    ScheduleResult,
    ProductionOrder,
    Resource,
    OptimizationGoal,
    create_scheduler,
    compare_scenarios
)

from app.algorithms.safety_stock import (
    SafetyStockEngine,
    SafetyStockResult,
    calculate_safety_stock_batch,
    generate_replenishment_plan
)

from app.algorithms.llm import (
    LLMClient,
    LLMConfig,
    LLMProvider,
    Message,
    ToolDefinition,
    create_llm_client,
    get_mrp_tools
)

from app.algorithms.whatif import (
    WhatIfSimulator,
    WhatIfScenario,
    WhatIfResult,
    ComparisonResult,
    ImpactDimension,
    ScenarioType,
    create_whatif_scenario,
    run_whatif_simulation
)

from app.algorithms.impact_analysis import (
    ImpactAnalysisEngine,
    OrderImpactResult,
    NewOrder,
    ExistingPlan,
    Resource,
    MaterialRequirement,
    DimensionImpact,
    OrderImpactDimension,
    RiskLevel,
    analyze_insert_order_impact
)

__all__ = [
    # Forecast
    "ForecastEngine",
    "ProphetForecast",
    "ARIMAForecast",
    "LSTMForecast",
    "XGBoostForecast",
    "MovingAverageForecast",
    "ExponentialSmoothingForecast",
    "create_forecast_engine",
    "auto_select_algorithm",
    
    # Scheduler
    "ORToolsScheduler",
    "ScheduleResult",
    "ProductionOrder",
    "Resource",
    "OptimizationGoal",
    "create_scheduler",
    "compare_scenarios",
    
    # Safety Stock
    "SafetyStockEngine",
    "SafetyStockResult",
    "calculate_safety_stock_batch",
    "generate_replenishment_plan",
    
    # LLM
    "LLMClient",
    "LLMConfig",
    "LLMProvider",
    "Message",
    "ToolDefinition",
    "create_llm_client",
    "get_mrp_tools",
    
    # What-if
    "WhatIfSimulator",
    "WhatIfScenario",
    "WhatIfResult",
    "ComparisonResult",
    "ImpactDimension",
    "ScenarioType",
    "create_whatif_scenario",
    "run_whatif_simulation",
    
    # Impact Analysis
    "ImpactAnalysisEngine",
    "OrderImpactResult",
    "NewOrder",
    "ExistingPlan",
    "MaterialRequirement",
    "DimensionImpact",
    "OrderImpactDimension",
    "RiskLevel",
    "analyze_insert_order_impact"
]
