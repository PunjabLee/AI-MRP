"""
Scenario Router - What-if 场景管理 API

提供场景的CRUD操作：
- 保存场景
- 加载场景
- 列出场景
- 删除场景
- 对比场景
- 克隆场景
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any

from app.integration.scenario_service import (
    ScenarioStore,
    ScenarioStatus,
    ScenarioCategory,
    WhatIfScenarioData,
    get_scenario_store
)
from app.utils.response import ApiResponse

router = APIRouter()


# ========== Request Models ==========

class SaveScenarioRequest(BaseModel):
    """保存场景请求"""
    name: str = Field(..., description="场景名称")
    description: str = Field("", description="场景描述")
    category: str = Field("custom", description="场景分类: demand, supply, capacity, cost, custom")
    baseline_params: Dict[str, Any] = Field(default_factory=dict, description="基线参数")
    changed_params: Dict[str, Any] = Field(default_factory=dict, description="变化的参数")
    orders: List[Dict] = Field(default_factory=list, description="订单列表")
    resources: List[Dict] = Field(default_factory=list, description="资源列表")
    results: Optional[Dict] = Field(None, description="场景结果")
    impact_analysis: Optional[Dict] = Field(None, description="影响分析结果")
    tags: List[str] = Field(default_factory=list, description="标签")
    created_by: str = Field("system", description="创建者")


class UpdateScenarioRequest(BaseModel):
    """更新场景请求"""
    scenario_id: str = Field(..., description="场景ID")
    name: Optional[str] = Field(None, description="场景名称")
    description: Optional[str] = Field(None, description="场景描述")
    changed_params: Optional[Dict[str, Any]] = Field(None, description="变化的参数")
    results: Optional[Dict] = Field(None, description="场景结果")
    impact_analysis: Optional[Dict] = Field(None, description="影响分析结果")
    tags: Optional[List[str]] = Field(None, description="标签")
    status: Optional[str] = Field(None, description="状态")


class CompareScenariosRequest(BaseModel):
    """对比场景请求"""
    scenario_ids: List[str] = Field(..., description="场景ID列表")


class CloneScenarioRequest(BaseModel):
    """克隆场景请求"""
    source_scenario_id: str = Field(..., description="源场景ID")
    new_name: str = Field(..., description="新场景名称")
    new_description: str = Field("", description="新场景描述")


# ========== API Endpoints ==========

@router.post("/save")
async def save_scenario(request: SaveScenarioRequest):
    """
    保存场景
    
    创建新的 What-if 场景
    """
    try:
        # 构建场景数据
        scenario = WhatIfScenarioData(
            name=request.name,
            description=request.description,
            category=request.category,
            baseline_params=request.baseline_params,
            changed_params=request.changed_params,
            orders=request.orders,
            resources=request.resources,
            results=request.results,
            impact_analysis=request.impact_analysis
        )
        
        # 保存场景
        store = get_scenario_store()
        scenario_id = store.save_scenario(
            scenario=scenario,
            tags=request.tags,
            created_by=request.created_by
        )
        
        return ApiResponse.success(
            data={"scenario_id": scenario_id},
            message="场景保存成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"保存场景失败: {str(e)}")


@router.post("/load/{scenario_id}")
async def load_scenario(scenario_id: str):
    """
    加载场景
    
    获取场景的完整数据
    """
    try:
        store = get_scenario_store()
        scenario = store.load_scenario(scenario_id)
        
        if scenario is None:
            return ApiResponse.bad_request(message=f"场景不存在: {scenario_id}")
        
        metadata = store.get_metadata(scenario_id)
        
        return ApiResponse.success(
            data={
                "scenario_id": scenario_id,
                "name": metadata.name,
                "description": metadata.description,
                "category": scenario.category,
                "baseline_params": scenario.baseline_params,
                "changed_params": scenario.changed_params,
                "orders": scenario.orders,
                "resources": scenario.resources,
                "results": scenario.results,
                "impact_analysis": scenario.impact_analysis,
                "version": metadata.version,
                "created_at": metadata.created_at.isoformat(),
                "updated_at": metadata.updated_at.isoformat(),
                "created_by": metadata.created_by,
                "tags": metadata.tags,
                "status": metadata.status
            },
            message="场景加载成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"加载场景失败: {str(e)}")


@router.get("/list")
async def list_scenarios(
    category: Optional[str] = None,
    status: Optional[str] = None,
    tags: Optional[str] = None,
    created_by: Optional[str] = None,
    limit: int = 100
):
    """
    列出场景
    
    支持按分类、状态、标签、创建者过滤
    """
    try:
        store = get_scenario_store()
        
        # 转换过滤条件
        cat = ScenarioCategory(category) if category else None
        sta = ScenarioStatus(status) if status else None
        tag_list = tags.split(",") if tags else None
        
        scenarios = store.list_scenarios(
            category=cat,
            status=sta,
            tags=tag_list,
            created_by=created_by,
            limit=limit
        )
        
        # 转换为响应格式
        result = []
        for s in scenarios:
            result.append({
                "scenario_id": s.scenario_id,
                "name": s.name,
                "description": s.description,
                "category": s.category,
                "status": s.status,
                "created_at": s.created_at.isoformat(),
                "updated_at": s.updated_at.isoformat(),
                "created_by": s.created_by,
                "tags": s.tags,
                "version": s.version
            })
        
        return ApiResponse.success(
            data={"scenarios": result, "total": len(result)},
            message="获取成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"获取场景列表失败: {str(e)}")


@router.put("/update")
async def update_scenario(request: UpdateScenarioRequest):
    """
    更新场景
    """
    try:
        store = get_scenario_store()
        
        # 先加载现有场景
        existing = store.load_scenario(request.scenario_id)
        if existing is None:
            return ApiResponse.bad_request(message=f"场景不存在: {request.scenario_id}")
        
        # 更新字段
        if request.name is not None:
            existing.name = request.name
        if request.description is not None:
            existing.description = request.description
        if request.changed_params is not None:
            existing.changed_params = request.changed_params
        if request.results is not None:
            existing.results = request.results
        if request.impact_analysis is not None:
            existing.impact_analysis = request.impact_analysis
        
        # 转换状态
        status = None
        if request.status:
            status = ScenarioStatus(request.status)
        
        # 保存更新
        success = store.update_scenario(
            scenario_id=request.scenario_id,
            scenario=existing,
            status=status,
            tags=request.tags
        )
        
        if success:
            return ApiResponse.success(message="场景更新成功")
        else:
            return ApiResponse.bad_request(message="更新失败")
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"更新场景失败: {str(e)}")


@router.delete("/{scenario_id}")
async def delete_scenario(scenario_id: str):
    """
    删除场景
    """
    try:
        store = get_scenario_store()
        success = store.delete_scenario(scenario_id)
        
        if success:
            return ApiResponse.success(message="场景删除成功")
        else:
            return ApiResponse.bad_request(message=f"场景不存在: {scenario_id}")
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"删除场景失败: {str(e)}")


@router.post("/compare")
async def compare_scenarios(request: CompareScenariosRequest):
    """
    对比场景
    
    对比多个 What-if 场景的结果
    """
    try:
        store = get_scenario_store()
        comparison = store.compare_scenarios(request.scenario_ids)
        
        if comparison is None:
            return ApiResponse.bad_request(message="场景对比失败")
        
        return ApiResponse.success(
            data=comparison,
            message="对比成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"对比失败: {str(e)}")


@router.post("/clone")
async def clone_scenario(request: CloneScenarioRequest):
    """
    克隆场景
    
    基于现有场景创建新场景
    """
    try:
        store = get_scenario_store()
        new_id = store.clone_scenario(
            scenario_id=request.source_scenario_id,
            new_name=request.new_name,
            new_description=request.new_description
        )
        
        if new_id is None:
            return ApiResponse.bad_request(message=f"源场景不存在: {request.source_scenario_id}")
        
        return ApiResponse.success(
            data={"new_scenario_id": new_id},
            message="场景克隆成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"克隆失败: {str(e)}")


@router.post("/{scenario_id}/archive")
async def archive_scenario(scenario_id: str):
    """
    归档场景
    """
    try:
        store = get_scenario_store()
        success = store.archive_scenario(scenario_id)
        
        if success:
            return ApiResponse.success(message="场景已归档")
        else:
            return ApiResponse.bad_request(message=f"场景不存在: {scenario_id}")
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"归档失败: {str(e)}")


@router.get("/stats")
async def get_scenario_stats():
    """
    获取场景统计
    """
    try:
        store = get_scenario_store()
        stats = store.get_scenario_stats()
        
        return ApiResponse.success(data=stats, message="获取成功")
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"获取统计失败: {str(e)}")
