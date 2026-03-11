"""
Model Router - 模型管理 API

提供模型的CRUD操作：
- 保存模型
- 加载模型
- 列出模型
- 删除模型
- 获取模型元数据
"""
from fastapi import APIRouter, HTTPException, UploadFile, File
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime
import io

from app.integration.model_service import (
    ModelStore,
    ModelType,
    ModelStatus,
    get_model_store
)
from app.utils.response import ApiResponse

router = APIRouter()


# ========== Request Models ==========

class SaveModelRequest(BaseModel):
    """保存模型请求"""
    model_name: str = Field(..., description="模型名称")
    model_type: str = Field(..., description="模型类型: prophet, arima, lstm, xgboost, scheduler")
    item_code: Optional[str] = Field(None, description="关联物料编码")
    version: str = Field("v1.0", description="版本号")
    description: str = Field("", description="描述")
    parameters: Dict[str, Any] = Field(default_factory=dict, description="模型参数")
    model_data: str = Field(..., description="Base64编码的模型数据")


class ModelMetadataResponse(BaseModel):
    """模型元数据响应"""
    model_id: str
    model_type: str
    model_name: str
    version: str
    status: str
    created_at: str
    updated_at: str
    trained_at: Optional[str]
    item_code: Optional[str]
    description: str
    parameters: Dict[str, Any]
    metrics: Dict[str, Any]
    file_size: int
    checksum: str


class ListModelsQuery(BaseModel):
    """查询参数"""
    model_type: Optional[str] = None
    item_code: Optional[str] = None
    status: Optional[str] = None
    limit: int = 100


# ========== API Endpoints ==========

@router.post("/save")
async def save_model(request: SaveModelRequest):
    """
    保存模型
    
    接收Base64编码的模型数据并保存
    """
    import base64
    import pickle
    import joblib
    
    try:
        # 解码模型数据
        model_bytes = base64.b64decode(request.model_data)
        
        # 反序列化模型
        try:
            import numpy as np
            model = pickle.loads(model_bytes)
        except:
            model = joblib.load(io.BytesIO(model_bytes))
        
        # 保存模型
        store = get_model_store()
        model_id = store.save_model(
            model=model,
            model_name=request.model_name,
            model_type=ModelType(request.model_type),
            item_code=request.item_code,
            parameters=request.parameters,
            description=request.description,
            version=request.version
        )
        
        return ApiResponse.success(
            data={"model_id": model_id},
            message="模型保存成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"保存模型失败: {str(e)}")


@router.post("/load/{model_id}")
async def load_model(model_id: str):
    """
    加载模型
    
    返回Base64编码的模型数据
    """
    import base64
    import pickle
    import joblib
    import io
    
    try:
        store = get_model_store()
        model = store.load_model(model_id)
        
        if model is None:
            return ApiResponse.bad_request(message=f"模型不存在: {model_id}")
        
        # 序列化模型
        try:
            buffer = io.BytesIO()
            joblib.dump(model, buffer)
            model_bytes = buffer.getvalue()
        except:
            model_bytes = pickle.dumps(model)
        
        # 编码返回
        model_data = base64.b64encode(model_bytes).decode('utf-8')
        
        # 获取元数据
        metadata = store.get_metadata(model_id)
        
        return ApiResponse.success(
            data={
                "model_id": model_id,
                "model_data": model_data,
                "metadata": {
                    "model_type": metadata.model_type,
                    "model_name": metadata.model_name,
                    "version": metadata.version,
                    "parameters": metadata.parameters,
                    "trained_at": metadata.trained_at.isoformat() if metadata.trained_at else None
                }
            },
            message="模型加载成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"加载模型失败: {str(e)}")


@router.get("/list")
async def list_models(
    model_type: Optional[str] = None,
    item_code: Optional[str] = None,
    status: Optional[str] = None,
    limit: int = 100
):
    """
    列出模型
    
    支持按类型、物料编码、状态过滤
    """
    try:
        store = get_model_store()
        
        # 转换过滤条件
        mt = ModelType(model_type) if model_type else None
        ms = ModelStatus(status) if status else None
        
        models = store.list_models(
            model_type=mt,
            item_code=item_code,
            status=ms,
            limit=limit
        )
        
        # 转换为响应格式
        result = []
        for m in models:
            result.append({
                "model_id": m.model_id,
                "model_type": m.model_type,
                "model_name": m.model_name,
                "version": m.version,
                "status": m.status,
                "created_at": m.created_at.isoformat(),
                "updated_at": m.updated_at.isoformat(),
                "trained_at": m.trained_at.isoformat() if m.trained_at else None,
                "item_code": m.item_code,
                "description": m.description,
                "file_size": m.file_size,
                "metrics": m.metrics
            })
        
        return ApiResponse.success(
            data={"models": result, "total": len(result)},
            message="获取成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"获取模型列表失败: {str(e)}")


@router.get("/metadata/{model_id}")
async def get_model_metadata(model_id: str):
    """
    获取模型元数据
    """
    try:
        store = get_model_store()
        metadata = store.get_metadata(model_id)
        
        if metadata is None:
            return ApiResponse.bad_request(message=f"模型不存在: {model_id}")
        
        return ApiResponse.success(
            data={
                "model_id": metadata.model_id,
                "model_type": metadata.model_type,
                "model_name": metadata.model_name,
                "version": metadata.version,
                "status": metadata.status,
                "created_at": metadata.created_at.isoformat(),
                "updated_at": metadata.updated_at.isoformat(),
                "trained_at": metadata.trained_at.isoformat() if metadata.trained_at else None,
                "item_code": metadata.item_code,
                "description": metadata.description,
                "parameters": metadata.parameters,
                "metrics": metadata.metrics,
                "file_size": metadata.file_size,
                "checksum": metadata.checksum
            },
            message="获取成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"获取元数据失败: {str(e)}")


@router.delete("/{model_id}")
async def delete_model(model_id: str):
    """
    删除模型
    """
    try:
        store = get_model_store()
        success = store.delete_model(model_id)
        
        if success:
            return ApiResponse.success(message="模型删除成功")
        else:
            return ApiResponse.bad_request(message=f"模型不存在: {model_id}")
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"删除模型失败: {str(e)}")


@router.post("/cleanup")
async def cleanup_old_models(days: int = 30):
    """
    清理过期模型
    
    删除指定天数之前的模型
    """
    try:
        store = get_model_store()
        deleted_count = store.cleanup_old_models(days)
        
        return ApiResponse.success(
            data={"deleted_count": deleted_count},
            message=f"已清理 {deleted_count} 个过期模型"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"清理失败: {str(e)}")


@router.get("/stats")
async def get_storage_stats():
    """
    获取存储统计
    """
    try:
        store = get_model_store()
        stats = store.get_storage_stats()
        
        return ApiResponse.success(data=stats, message="获取成功")
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"获取统计失败: {str(e)}")


# ========== 便捷方法 ==========

@router.post("/predict-with-model/{model_id}")
async def predict_with_saved_model(model_id: str, request: Dict):
    """
    使用已保存的模型进行预测
    
    这是一个便捷端点，结合模型加载和预测
    """
    from app.algorithms import create_forecast_engine
    
    try:
        store = get_model_store()
        model = store.load_model(model_id)
        
        if model is None:
            return ApiResponse.bad_request(message=f"模型不存在: {model_id}")
        
        metadata = store.get_metadata(model_id)
        
        # 检查是否是预测模型
        if metadata.model_type not in [m.value for m in ModelType]:
            return ApiResponse.bad_request(message="不是有效的预测模型类型")
        
        # 这里可以添加预测逻辑
        # 实际使用时，模型应该已经训练好并包含预测方法
        
        return ApiResponse.success(
            data={
                "model_id": model_id,
                "model_type": metadata.model_type,
                "model_name": metadata.model_name,
                "message": "模型加载成功，请使用 /predict 接口进行预测"
            },
            message="成功"
        )
    
    except Exception as e:
        return ApiResponse.bad_request(message=f"操作失败: {str(e)}")
