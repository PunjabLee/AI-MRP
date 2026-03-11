"""
模型持久化服务

提供：
1. 模型保存/加载 (pickle/joblib)
2. 模型元数据管理
3. 模型版本控制
4. 自动清理过期模型
"""
import os
import pickle
import joblib
import json
import uuid
import hashlib
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
from dataclasses import dataclass, field, asdict
from enum import Enum
import shutil
import logging

logger = logging.getLogger(__name__)


class ModelType(str, Enum):
    """模型类型"""
    PROPHET = "prophet"
    ARIMA = "arima"
    LSTM = "lstm"
    XGBOOST = "xgboost"
    MOVING_AVERAGE = "moving_average"
    EXPONENTIAL_SMOOTHING = "exponential_smoothing"
    SCHEDULER = "scheduler"
    SAFETY_STOCK = "safety_stock"


class ModelStatus(str, Enum):
    """模型状态"""
    TRAINING = "training"
    READY = "ready"
    FAILED = "failed"
    DEPRECATED = "deprecated"


@dataclass
class ModelMetadata:
    """模型元数据"""
    model_id: str
    model_type: str
    model_name: str
    version: str
    status: str
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
    trained_at: Optional[datetime] = None
    item_code: Optional[str] = None  # 关联的物料编码
    description: str = ""
    parameters: Dict[str, Any] = field(default_factory=dict)
    metrics: Dict[str, Any] = field(default_factory=dict)
    file_path: str = ""
    file_size: int = 0  # bytes
    checksum: str = ""  # 文件校验和


class ModelStore:
    """模型存储管理"""
    
    def __init__(self, storage_path: str = "./models"):
        """
        初始化模型存储
        
        Args:
            storage_path: 模型存储根目录
        """
        self.storage_path = storage_path
        self.metadata_file = os.path.join(storage_path, "metadata.json")
        self._ensure_storage()
        self._metadata: Dict[str, ModelMetadata] = {}
        self._load_metadata()
    
    def _ensure_storage(self):
        """确保存储目录存在"""
        os.makedirs(self.storage_path, exist_ok=True)
        os.makedirs(os.path.join(self.storage_path, "models"), exist_ok=True)
    
    def _load_metadata(self):
        """加载元数据"""
        if os.path.exists(self.metadata_file):
            try:
                with open(self.metadata_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    for k, v in data.items():
                        v['created_at'] = datetime.fromisoformat(v['created_at'])
                        v['updated_at'] = datetime.fromisoformat(v['updated_at'])
                        if v.get('trained_at'):
                            v['trained_at'] = datetime.fromisoformat(v['trained_at'])
                        self._metadata[k] = ModelMetadata(**v)
            except Exception as e:
                logger.warning(f"加载模型元数据失败: {e}")
    
    def _save_metadata(self):
        """保存元数据"""
        try:
            data = {}
            for k, v in self._metadata.items():
                d = asdict(v)
                d['created_at'] = v.created_at.isoformat()
                d['updated_at'] = v.updated_at.isoformat()
                d['trained_at'] = v.trained_at.isoformat() if v.trained_at else None
                data[k] = d
            
            with open(self.metadata_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.error(f"保存模型元数据失败: {e}")
    
    def _calculate_checksum(self, file_path: str) -> str:
        """计算文件校验和"""
        hash_md5 = hashlib.md5()
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                hash_md5.update(chunk)
        return hash_md5.hexdigest()
    
    # ========== 公共 API ==========
    
    def save_model(
        self,
        model: Any,
        model_name: str,
        model_type: ModelType,
        item_code: Optional[str] = None,
        parameters: Optional[Dict] = None,
        metrics: Optional[Dict] = None,
        description: str = "",
        version: str = "v1.0"
    ) -> str:
        """
        保存模型
        
        Args:
            model: 模型对象
            model_name: 模型名称
            model_type: 模型类型
            item_code: 关联的物料编码
            parameters: 模型参数
            metrics: 模型评估指标
            description: 描述
            version: 版本号
        
        Returns:
            model_id
        """
        # 生成唯一ID
        model_id = f"{model_type.value}_{item_code or 'global'}_{uuid.uuid4().hex[:8]}"
        
        # 确定文件路径
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{model_id}_{timestamp}.pkl"
        file_path = os.path.join(self.storage_path, "models", filename)
        
        # 保存模型文件
        try:
            joblib.dump(model, file_path)
        except Exception:
            # 回退到 pickle
            with open(file_path, 'wb') as f:
                pickle.dump(model, f)
        
        # 计算校验和
        checksum = self._calculate_checksum(file_path)
        file_size = os.path.getsize(file_path)
        
        # 创建元数据
        metadata = ModelMetadata(
            model_id=model_id,
            model_type=model_type.value,
            model_name=model_name,
            version=version,
            status=ModelStatus.READY.value,
            trained_at=datetime.now(),
            item_code=item_code,
            description=description,
            parameters=parameters or {},
            metrics=metrics or {},
            file_path=file_path,
            file_size=file_size,
            checksum=checksum
        )
        
        self._metadata[model_id] = metadata
        self._save_metadata()
        
        logger.info(f"模型已保存: {model_id}")
        return model_id
    
    def load_model(self, model_id: str) -> Optional[Any]:
        """
        加载模型
        
        Args:
            model_id: 模型ID
        
        Returns:
            模型对象，如果不存在返回None
        """
        if model_id not in self._metadata:
            logger.warning(f"模型不存在: {model_id}")
            return None
        
        metadata = self._metadata[model_id]
        
        if not os.path.exists(metadata.file_path):
            logger.error(f"模型文件丢失: {metadata.file_path}")
            return None
        
        # 验证校验和
        current_checksum = self._calculate_checksum(metadata.file_path)
        if current_checksum != metadata.checksum:
            logger.error(f"模型文件校验失败: {model_id}")
            return None
        
        # 加载模型
        try:
            model = joblib.load(metadata.file_path)
        except Exception:
            try:
                with open(metadata.file_path, 'rb') as f:
                    model = pickle.load(f)
            except Exception as e:
                logger.error(f"加载模型失败: {e}")
                return None
        
        # 更新访问时间
        metadata.updated_at = datetime.now()
        self._save_metadata()
        
        return model
    
    def get_metadata(self, model_id: str) -> Optional[ModelMetadata]:
        """获取模型元数据"""
        return self._metadata.get(model_id)
    
    def list_models(
        self,
        model_type: Optional[ModelType] = None,
        item_code: Optional[str] = None,
        status: Optional[ModelStatus] = None,
        limit: int = 100
    ) -> List[ModelMetadata]:
        """
        列出模型
        
        Args:
            model_type: 按类型过滤
            item_code: 按物料编码过滤
            status: 按状态过滤
            limit: 返回数量限制
        
        Returns:
            模型元数据列表
        """
        results = list(self._metadata.values())
        
        if model_type:
            results = [m for m in results if m.model_type == model_type.value]
        
        if item_code:
            results = [m for m in results if m.item_code == item_code]
        
        if status:
            results = [m for m in results if m.status == status.value]
        
        # 按更新时间倒序
        results.sort(key=lambda x: x.updated_at, reverse=True)
        
        return results[:limit]
    
    def delete_model(self, model_id: str) -> bool:
        """
        删除模型
        
        Args:
            model_id: 模型ID
        
        Returns:
            是否成功
        """
        if model_id not in self._metadata:
            return False
        
        metadata = self._metadata[model_id]
        
        # 删除文件
        if os.path.exists(metadata.file_path):
            try:
                os.remove(metadata.file_path)
            except Exception as e:
                logger.error(f"删除模型文件失败: {e}")
        
        # 删除元数据
        del self._metadata[model_id]
        self._save_metadata()
        
        logger.info(f"模型已删除: {model_id}")
        return True
    
    def cleanup_old_models(self, days: int = 30) -> int:
        """
        清理过期模型
        
        Args:
            days: 保留天数
        
        Returns:
            删除数量
        """
        cutoff = datetime.now() - timedelta(days=days)
        to_delete = []
        
        for model_id, metadata in self._metadata.items():
            if metadata.updated_at < cutoff:
                to_delete.append(model_id)
        
        for model_id in to_delete:
            self.delete_model(model_id)
        
        return len(to_delete)
    
    def get_storage_stats(self) -> Dict[str, Any]:
        """获取存储统计"""
        total_size = sum(m.file_size for m in self._metadata.values())
        model_counts = {}
        for m in self._metadata.values():
            model_counts[m.model_type] = model_counts.get(m.model_type, 0) + 1
        
        return {
            "total_models": len(self._metadata),
            "total_size_mb": round(total_size / 1024 / 1024, 2),
            "model_counts": model_counts,
            "storage_path": self.storage_path
        }


# ========== 全局实例 ==========

_model_store: Optional[ModelStore] = None


def get_model_store() -> ModelStore:
    """获取全局模型存储"""
    global _model_store
    if _model_store is None:
        _model_store = ModelStore()
    return _model_store


def set_model_store(store: ModelStore):
    """设置全局模型存储"""
    global _model_store
    _model_store = store
