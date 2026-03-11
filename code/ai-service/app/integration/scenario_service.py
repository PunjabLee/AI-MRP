"""
What-if 场景持久化服务

提供：
1. 场景保存/加载
2. 场景版本管理
3. 场景对比
4. 场景分类/标签
"""
import os
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


class ScenarioStatus(str, Enum):
    """场景状态"""
    DRAFT = "draft"           # 草稿
    ACTIVE = "active"         # 活动中
    COMPARED = "compared"     # 已对比
    ARCHIVED = "archived"     # 已归档


class ScenarioCategory(str, Enum):
    """场景分类"""
    DEMAND = "demand"           # 需求场景
    SUPPLY = "supply"           # 供应场景
    CAPACITY = "capacity"       # 产能场景
    COST = "cost"               # 成本场景
    CUSTOM = "custom"            # 自定义


@dataclass
class WhatIfScenarioData:
    """What-if 场景数据"""
    name: str
    description: str
    category: str
    baseline_params: Dict[str, Any]  # 基线参数
    changed_params: Dict[str, Any]    # 变化的参数
    orders: List[Dict] = field(default_factory=list)
    resources: List[Dict] = field(default_factory=list)
    results: Optional[Dict] = None
    impact_analysis: Optional[Dict] = None


@dataclass
class ScenarioMetadata:
    """场景元数据"""
    scenario_id: str
    name: str
    description: str
    category: str
    status: str
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
    created_by: str = "system"
    tags: List[str] = field(default_factory=list)
    parent_scenario_id: Optional[str] = None  # 派生场景的父场景
    version: str = "v1.0"
    file_path: str = ""


class ScenarioStore:
    """场景存储管理"""
    
    def __init__(self, storage_path: str = "./scenarios"):
        """
        初始化场景存储
        
        Args:
            storage_path: 场景存储根目录
        """
        self.storage_path = storage_path
        self.metadata_file = os.path.join(storage_path, "metadata.json")
        self._ensure_storage()
        self._metadata: Dict[str, ScenarioMetadata] = {}
        self._load_metadata()
    
    def _ensure_storage(self):
        """确保存储目录存在"""
        os.makedirs(self.storage_path, exist_ok=True)
        os.makedirs(os.path.join(self.storage_path, "scenarios"), exist_ok=True)
    
    def _load_metadata(self):
        """加载元数据"""
        if os.path.exists(self.metadata_file):
            try:
                with open(self.metadata_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    for k, v in data.items():
                        v['created_at'] = datetime.fromisoformat(v['created_at'])
                        v['updated_at'] = datetime.fromisoformat(v['updated_at'])
                        self._metadata[k] = ScenarioMetadata(**v)
            except Exception as e:
                logger.warning(f"加载场景元数据失败: {e}")
    
    def _save_metadata(self):
        """保存元数据"""
        try:
            data = {}
            for k, v in self._metadata.items():
                d = asdict(v)
                d['created_at'] = v.created_at.isoformat()
                d['updated_at'] = v.updated_at.isoformat()
                data[k] = d
            
            with open(self.metadata_file, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.error(f"保存场景元数据失败: {e}")
    
    # ========== 公共 API ==========
    
    def save_scenario(
        self,
        scenario: WhatIfScenarioData,
        scenario_id: Optional[str] = None,
        created_by: str = "system",
        tags: Optional[List[str]] = None,
        parent_scenario_id: Optional[str] = None,
        version: str = "v1.0"
    ) -> str:
        """
        保存场景
        
        Args:
            scenario: 场景数据
            scenario_id: 场景ID（可选，默认自动生成）
            created_by: 创建者
            tags: 标签
            parent_scenario_id: 父场景ID（用于派生场景）
            version: 版本号
        
        Returns:
            scenario_id
        """
        # 生成唯一ID
        if scenario_id is None:
            scenario_id = f"scenario_{uuid.uuid4().hex[:12]}"
        
        # 确定文件路径
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{scenario_id}_{timestamp}.json"
        file_path = os.path.join(self.storage_path, "scenarios", filename)
        
        # 准备保存数据
        save_data = {
            "scenario_id": scenario_id,
            "name": scenario.name,
            "description": scenario.description,
            "category": scenario.category,
            "baseline_params": scenario.baseline_params,
            "changed_params": scenario.changed_params,
            "orders": scenario.orders,
            "resources": scenario.resources,
            "results": scenario.results,
            "impact_analysis": scenario.impact_analysis,
            "version": version,
            "created_at": datetime.now().isoformat()
        }
        
        # 保存场景文件
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(save_data, f, ensure_ascii=False, indent=2)
        
        # 创建元数据
        metadata = ScenarioMetadata(
            scenario_id=scenario_id,
            name=scenario.name,
            description=scenario.description,
            category=scenario.category,
            status=ScenarioStatus.ACTIVE.value,
            created_by=created_by,
            tags=tags or [],
            parent_scenario_id=parent_scenario_id,
            version=version,
            file_path=file_path
        )
        
        self._metadata[scenario_id] = metadata
        self._save_metadata()
        
        logger.info(f"场景已保存: {scenario_id}")
        return scenario_id
    
    def load_scenario(self, scenario_id: str) -> Optional[WhatIfScenarioData]:
        """
        加载场景
        
        Args:
            scenario_id: 场景ID
        
        Returns:
            场景数据，如果不存在返回None
        """
        if scenario_id not in self._metadata:
            logger.warning(f"场景不存在: {scenario_id}")
            return None
        
        metadata = self._metadata[scenario_id]
        
        if not os.path.exists(metadata.file_path):
            logger.error(f"场景文件丢失: {metadata.file_path}")
            return None
        
        # 加载场景文件
        with open(metadata.file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # 转换为场景数据
        scenario = WhatIfScenarioData(
            name=data["name"],
            description=data["description"],
            category=data["category"],
            baseline_params=data.get("baseline_params", {}),
            changed_params=data.get("changed_params", {}),
            orders=data.get("orders", []),
            resources=data.get("resources", []),
            results=data.get("results"),
            impact_analysis=data.get("impact_analysis")
        )
        
        # 更新访问时间
        metadata.updated_at = datetime.now()
        self._save_metadata()
        
        return scenario
    
    def get_metadata(self, scenario_id: str) -> Optional[ScenarioMetadata]:
        """获取场景元数据"""
        return self._metadata.get(scenario_id)
    
    def list_scenarios(
        self,
        category: Optional[ScenarioCategory] = None,
        status: Optional[ScenarioStatus] = None,
        tags: Optional[List[str]] = None,
        created_by: Optional[str] = None,
        limit: int = 100
    ) -> List[ScenarioMetadata]:
        """
        列出场景
        
        Args:
            category: 按分类过滤
            status: 按状态过滤
            tags: 按标签过滤
            created_by: 按创建者过滤
            limit: 返回数量限制
        
        Returns:
            场景元数据列表
        """
        results = list(self._metadata.values())
        
        if category:
            results = [s for s in results if s.category == category.value]
        
        if status:
            results = [s for s in results if s.status == status.value]
        
        if tags:
            results = [s for s in results if any(t in s.tags for t in tags)]
        
        if created_by:
            results = [s for s in results if s.created_by == created_by]
        
        # 按更新时间倒序
        results.sort(key=lambda x: x.updated_at, reverse=True)
        
        return results[:limit]
    
    def update_scenario(
        self,
        scenario_id: str,
        scenario: WhatIfScenarioData,
        status: Optional[ScenarioStatus] = None,
        tags: Optional[List[str]] = None
    ) -> bool:
        """
        更新场景
        
        Args:
            scenario_id: 场景ID
            scenario: 新场景数据
            status: 新状态
            tags: 新标签
        
        Returns:
            是否成功
        """
        if scenario_id not in self._metadata:
            return False
        
        # 重新保存场景数据
        self.save_scenario(
            scenario=scenario,
            scenario_id=scenario_id,
            created_by=self._metadata[scenario_id].created_by,
            tags=tags or self._metadata[scenario_id].tags
        )
        
        # 更新状态
        if status:
            self._metadata[scenario_id].status = status.value
        
        self._save_metadata()
        return True
    
    def delete_scenario(self, scenario_id: str) -> bool:
        """
        删除场景
        
        Args:
            scenario_id: 场景ID
        
        Returns:
            是否成功
        """
        if scenario_id not in self._metadata:
            return False
        
        metadata = self._metadata[scenario_id]
        
        # 删除文件
        if os.path.exists(metadata.file_path):
            try:
                os.remove(metadata.file_path)
            except Exception as e:
                logger.error(f"删除场景文件失败: {e}")
        
        # 删除元数据
        del self._metadata[scenario_id]
        self._save_metadata()
        
        logger.info(f"场景已删除: {scenario_id}")
        return True
    
    def archive_scenario(self, scenario_id: str) -> bool:
        """归档场景"""
        if scenario_id not in self._metadata:
            return False
        
        self._metadata[scenario_id].status = ScenarioStatus.ARCHIVED.value
        self._metadata[scenario_id].updated_at = datetime.now()
        self._save_metadata()
        return True
    
    def clone_scenario(
        self,
        scenario_id: str,
        new_name: str,
        new_description: str = ""
    ) -> Optional[str]:
        """
        克隆场景
        
        Args:
            scenario_id: 源场景ID
            new_name: 新场景名称
            new_description: 新场景描述
        
        Returns:
            新场景ID，如果失败返回None
        """
        source = self.load_scenario(scenario_id)
        if source is None:
            return None
        
        # 修改名称和描述
        source.name = new_name
        if new_description:
            source.description = new_description
        
        # 保存为新场景
        new_id = self.save_scenario(
            scenario=source,
            parent_scenario_id=scenario_id
        )
        
        return new_id
    
    def compare_scenarios(
        self,
        scenario_ids: List[str]
    ) -> Optional[Dict[str, Any]]:
        """
        对比多个场景
        
        Args:
            scenario_ids: 场景ID列表
        
        Returns:
            对比结果
        """
        scenarios = []
        for sid in scenario_ids:
            scenario = self.load_scenario(scenario_id=sid)
            metadata = self.get_metadata(sid)
            if scenario and metadata:
                scenarios.append({
                    "scenario_id": sid,
                    "name": metadata.name,
                    "category": metadata.category,
                    "changed_params": scenario.changed_params,
                    "results": scenario.results,
                    "impact_analysis": scenario.impact_analysis,
                    "created_at": metadata.created_at.isoformat()
                })
        
        if not scenarios:
            return None
        
        # 构建对比数据
        comparison = {
            "scenarios": scenarios,
            "count": len(scenarios)
        }
        
        # 标记状态
        for sid in scenario_ids:
            if sid in self._metadata:
                self._metadata[sid].status = ScenarioStatus.COMPARED.value
        
        self._save_metadata()
        
        return comparison
    
    def get_scenario_stats(self) -> Dict[str, Any]:
        """获取场景统计"""
        category_counts = {}
        status_counts = {}
        
        for m in self._metadata.values():
            category_counts[m.category] = category_counts.get(m.category, 0) + 1
            status_counts[m.status] = status_counts.get(m.status, 0) + 1
        
        return {
            "total_scenarios": len(self._metadata),
            "by_category": category_counts,
            "by_status": status_counts,
            "storage_path": self.storage_path
        }


# ========== 全局实例 ==========

_scenario_store: Optional[ScenarioStore] = None


def get_scenario_store() -> ScenarioStore:
    """获取全局场景存储"""
    global _scenario_store
    if _scenario_store is None:
        _scenario_store = ScenarioStore()
    return _scenario_store


def set_scenario_store(store: ScenarioStore):
    """设置全局场景存储"""
    global _scenario_store
    _scenario_store = store
