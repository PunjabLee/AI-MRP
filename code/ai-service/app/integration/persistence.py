"""
预测/排程结果持久化 + 回调机制

提供：
1. 结果存储 (内存/SQL/MongoDB)
2. 回调机制 (同步/异步)
3. 轮询机制 (备选)
"""
from typing import Any, Dict, Optional, Callable
from datetime import datetime, timedelta
from dataclasses import dataclass, field
from enum import Enum
import json
import time
import threading
import uuid
from queue import Queue
import logging

logger = logging.getLogger(__name__)


class ResultStatus(str, Enum):
    """结果状态"""
    PENDING = "pending"       # 待处理
    PROCESSING = "processing" # 处理中
    COMPLETED = "completed"  # 已完成
    FAILED = "failed"       # 失败
    TIMEOUT = "timeout"      # 超时


@dataclass
class AIResult:
    """AI执行结果"""
    request_id: str
    request_type: str
    status: ResultStatus
    data: Optional[Dict[str, Any]] = None
    error: Optional[Dict[str, Any]] = None
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
    completed_at: Optional[datetime] = None
    processing_time_ms: int = 0
    callback_url: Optional[str] = None
    callback_status: Optional[str] = None
    retry_count: int = 0
    metadata: Dict[str, Any] = field(default_factory=dict)


class ResultStore:
    """结果存储基类"""
    
    def save(self, result: AIResult) -> bool:
        raise NotImplementedError
    
    def get(self, request_id: str) -> Optional[AIResult]:
        raise NotImplementedError
    
    def update(self, request_id: str, **kwargs) -> bool:
        raise NotImplementedError
    
    def list(self, status: Optional[ResultStatus] = None, 
             limit: int = 100) -> list:
        raise NotImplementedError
    
    def delete(self, request_id: str) -> bool:
        raise NotImplementedError


class InMemoryResultStore(ResultStore):
    """内存结果存储 (开发/测试用)"""
    
    def __init__(self, max_size: int = 1000):
        self._store: Dict[str, AIResult] = {}
        self._max_size = max_size
        self._lock = threading.Lock()
    
    def save(self, result: AIResult) -> bool:
        with self._lock:
            # 清理过期数据
            if len(self._store) >= self._max_size:
                self._cleanup_oldest(100)
            
            self._store[result.request_id] = result
            return True
    
    def get(self, request_id: str) -> Optional[AIResult]:
        return self._store.get(request_id)
    
    def update(self, request_id: str, **kwargs) -> bool:
        with self._lock:
            if request_id not in self._store:
                return False
            
            result = self._store[request_id]
            for key, value in kwargs.items():
                if hasattr(result, key):
                    setattr(result, key, value)
            result.updated_at = datetime.now()
            return True
    
    def list(self, status: Optional[ResultStatus] = None, 
             limit: int = 100) -> list:
        with self._lock:
            results = list(self._store.values())
            
            if status:
                results = [r for r in results if r.status == status]
            
            # 按更新时间倒序
            results.sort(key=lambda x: x.updated_at, reverse=True)
            return results[:limit]
    
    def delete(self, request_id: str) -> bool:
        with self._lock:
            if request_id in self._store:
                del self._store[request_id]
                return True
            return False
    
    def _cleanup_oldest(self, count: int):
        """清理最旧的数据"""
        if not self._store:
            return
        
        sorted_results = sorted(
            self._store.values(),
            key=lambda x: x.updated_at
        )
        
        for result in sorted_results[:count]:
            del self._store[result.request_id]


class CallbackManager:
    """回调管理器"""
    
    def __init__(self):
        self._callbacks: Dict[str, Callable] = {}
        self._queue: Queue = Queue()
        self._worker_thread: Optional[threading.Thread] = None
        self._running = False
    
    def register_callback(self, request_type: str, callback: Callable):
        """注册回调函数"""
        self._callbacks[request_type] = callback
        logger.info(f"Registered callback for {request_type}")
    
    def trigger_callback(self, result: AIResult):
        """触发回调"""
        if not result.callback_url:
            return
        
        # 添加到队列
        self._queue.put(result)
        
        # 启动工作线程
        if not self._running:
            self._start_worker()
    
    def _start_worker(self):
        """启动回调工作线程"""
        self._running = True
        self._worker_thread = threading.Thread(target=self._worker, daemon=True)
        self._worker_thread.start()
    
    def _worker(self):
        """回调工作线程"""
        while self._running:
            try:
                result = self._queue.get(timeout=1)
                self._execute_callback(result)
                self._queue.task_done()
            except:
                continue
    
    def _execute_callback(self, result: AIResult):
        """执行回调"""
        import httpx
        
        if not result.callback_url:
            return
        
        try:
            payload = {
                "request_id": result.request_id,
                "status": result.status.value,
                "data": result.data,
                "error": result.error,
                "processing_time_ms": result.processing_time_ms,
                "completed_at": result.completed_at.isoformat() if result.completed_at else None
            }
            
            # 发送回调
            response = httpx.post(
                result.callback_url,
                json=payload,
                timeout=10
            )
            
            if response.status_code == 200:
                logger.info(f"Callback success for {result.request_id}")
            else:
                logger.warning(f"Callback failed: {response.status_code}")
                
        except Exception as e:
            logger.error(f"Callback error: {e}")
    
    def stop(self):
        """停止回调工作线程"""
        self._running = False
        if self._worker_thread:
            self._worker_thread.join(timeout=5)


# ========== 主服务类 ==========

class AIIntegrationService:
    """AI集成服务 - 统一入口"""
    
    def __init__(self, store: Optional[ResultStore] = None):
        # 默认使用内存存储
        self._store = store or InMemoryResultStore()
        self._callback_manager = CallbackManager()
    
    # ---------- 结果管理 ----------
    
    def save_result(self, result: AIResult) -> bool:
        """保存结果"""
        return self._store.save(result)
    
    def get_result(self, request_id: str) -> Optional[AIResult]:
        """获取结果"""
        return self._store.get(request_id)
    
    def update_result(self, request_id: str, **kwargs) -> bool:
        """更新结果"""
        return self._store.update(request_id, **kwargs)
    
    def list_results(self, status: Optional[ResultStatus] = None, 
                    limit: int = 100) -> list:
        """列出结果"""
        return self._store.list(status, limit)
    
    def delete_result(self, request_id: str) -> bool:
        """删除结果"""
        return self._store.delete(request_id)
    
    # ---------- 回调管理 ----------
    
    def register_callback(self, request_type: str, callback: Callable):
        """注册回调"""
        self._callback_manager.register_callback(request_type, callback)
    
    def trigger_callback(self, result: AIResult):
        """触发回调"""
        self._callback_manager.trigger_callback(result)
    
    # ---------- 便捷方法 ----------
    
    def create_pending_result(
        self,
        request_id: str,
        request_type: str,
        callback_url: str = None
    ) -> AIResult:
        """创建待处理结果"""
        result = AIResult(
            request_id=request_id,
            request_type=request_type,
            status=ResultStatus.PENDING,
            callback_url=callback_url
        )
        self.save_result(result)
        return result
    
    def complete_result(
        self,
        request_id: str,
        data: Dict[str, Any],
        processing_time_ms: int = 0
    ) -> bool:
        """标记结果完成"""
        result = self.update_result(
            request_id,
            status=ResultStatus.COMPLETED,
            data=data,
            completed_at=datetime.now(),
            processing_time_ms=processing_time_ms,
            callback_status="pending"
        )
        
        if result:
            # 触发回调
            stored_result = self.get_result(request_id)
            if stored_result:
                self.trigger_callback(stored_result)
        
        return result
    
    def fail_result(
        self,
        request_id: str,
        error: Dict[str, Any],
        processing_time_ms: int = 0
    ) -> bool:
        """标记结果失败"""
        result = self.update_result(
            request_id,
            status=ResultStatus.FAILED,
            error=error,
            completed_at=datetime.now(),
            processing_time_ms=processing_time_ms,
            callback_status="pending"
        )
        
        if result:
            stored_result = self.get_result(request_id)
            if stored_result:
                self.trigger_callback(stored_result)
        
        return result
    
    def get_result_with_wait(
        self,
        request_id: str,
        timeout: int = 60,
        poll_interval: float = 0.5
    ) -> Optional[AIResult]:
        """等待结果 (轮询)"""
        start_time = time.time()
        
        while time.time() - start_time < timeout:
            result = self.get_result(request_id)
            
            if result and result.status in [ResultStatus.COMPLETED, ResultStatus.FAILED]:
                return result
            
            time.sleep(poll_interval)
        
        # 超时，更新状态
        self.update_result(request_id, status=ResultStatus.TIMEOUT)
        return self.get_result(request_id)


# ========== 全局实例 ==========

# 全局集成服务
_integration_service: Optional[AIIntegrationService] = None


def get_integration_service() -> AIIntegrationService:
    """获取全局集成服务"""
    global _integration_service
    if _integration_service is None:
        _integration_service = AIIntegrationService()
    return _integration_service


def set_integration_service(service: AIIntegrationService):
    """设置全局集成服务"""
    global _integration_service
    _integration_service = service
