"""
性能优化工具

提供：
1. 内存缓存 (LRU)
2. 并行处理 (ThreadPoolExecutor, ProcessPoolExecutor)
3. 批量处理优化
4. 异步任务队列
"""
import time
import hashlib
import json
import threading
from typing import Any, Callable, Optional, List, Dict, Tuple
from functools import wraps
from dataclasses import dataclass
from datetime import datetime, timedelta
from enum import Enum
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor, as_completed
import asyncio
import logging

logger = logging.getLogger(__name__)


class CacheStrategy(str, Enum):
    """缓存策略"""
    LRU = "lru"           # 最近最少使用
    LFU = "lfu"           # 最不经常使用
    FIFO = "fifo"         # 先进先出
    TTL = "ttl"           # 基于时间


@dataclass
class CacheEntry:
    """缓存条目"""
    key: str
    value: Any
    created_at: datetime
    accessed_at: datetime
    access_count: int = 0
    ttl_seconds: Optional[int] = None
    
    def is_expired(self) -> bool:
        if self.ttl_seconds is None:
            return False
        return datetime.now() - self.created_at > timedelta(seconds=self.ttl_seconds)


class MemoryCache:
    """内存缓存 (LRU + TTL)"""
    
    def __init__(self, max_size: int = 1000, default_ttl: int = 3600):
        """
        初始化缓存
        
        Args:
            max_size: 最大缓存条目数
            default_ttl: 默认过期时间(秒)
        """
        self._cache: Dict[str, CacheEntry] = {}
        self._max_size = max_size
        self._default_ttl = default_ttl
        self._lock = threading.RLock()
        self._hits = 0
        self._misses = 0
    
    def get(self, key: str) -> Optional[Any]:
        """获取缓存"""
        with self._lock:
            if key not in self._cache:
                self._misses += 1
                return None
            
            entry = self._cache[key]
            
            # 检查过期
            if entry.is_expired():
                del self._cache[key]
                self._misses += 1
                return None
            
            # 更新访问信息
            entry.accessed_at = datetime.now()
            entry.access_count += 1
            self._hits += 1
            
            return entry.value
    
    def set(self, key: str, value: Any, ttl: Optional[int] = None):
        """设置缓存"""
        with self._lock:
            # 容量满时淘汰
            if len(self._cache) >= self._max_size and key not in self._cache:
                self._evict_lru()
            
            self._cache[key] = CacheEntry(
                key=key,
                value=value,
                created_at=datetime.now(),
                accessed_at=datetime.now(),
                ttl_seconds=ttl or self._default_ttl
            )
    
    def delete(self, key: str) -> bool:
        """删除缓存"""
        with self._lock:
            if key in self._cache:
                del self._cache[key]
                return True
            return False
    
    def clear(self):
        """清空缓存"""
        with self._lock:
            self._cache.clear()
            self._hits = 0
            self._misses = 0
    
    def _evict_lru(self):
        """淘汰最少使用的条目"""
        if not self._cache:
            return
        
        # 找出最久未访问的
        lru_key = min(
            self._cache.keys(),
            key=lambda k: self._cache[k].accessed_at
        )
        del self._cache[lru_key]
    
    def get_stats(self) -> Dict:
        """获取缓存统计"""
        with self._lock:
            total = self._hits + self._misses
            hit_rate = (self._hits / total * 100) if total > 0 else 0
            
            return {
                "size": len(self._cache),
                "max_size": self._max_size,
                "hits": self._hits,
                "misses": self._misses,
                "hit_rate": round(hit_rate, 2)
            }


# ========== 缓存装饰器 ==========

def cached(ttl: int = 3600, key_func: Optional[Callable] = None):
    """
    缓存装饰器
    
    Args:
        ttl: 过期时间(秒)
        key_func: 自定义key生成函数
    """
    _cache = MemoryCache(default_ttl=ttl)
    
    def decorator(func: Callable):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 生成缓存key
            if key_func:
                cache_key = key_func(*args, **kwargs)
            else:
                # 默认: 函数名 + 参数hash
                key_data = f"{func.__name__}:{str(args)}:{str(kwargs)}"
                cache_key = hashlib.md5(key_data.encode()).hexdigest()
            
            # 尝试获取缓存
            cached_value = _cache.get(cache_key)
            if cached_value is not None:
                logger.debug(f"Cache hit: {func.__name__}")
                return cached_value
            
            # 执行函数
            result = func(*args, **kwargs)
            
            # 存入缓存
            _cache.set(cache_key, result, ttl)
            logger.debug(f"Cache miss: {func.__name__}")
            
            return result
        
        # 暴露缓存对象
        wrapper.cache = _cache
        return wrapper
    
    return decorator


# ========== 并行处理 ==========

class ParallelProcessor:
    """并行处理器"""
    
    def __init__(self, max_workers: int = 4, use_processes: bool = False):
        """
        初始化并行处理器
        
        Args:
            max_workers: 最大工作线程/进程数
            use_processes: True使用进程, False使用线程
        """
        self.max_workers = max_workers
        self.use_processes = use_processes
    
    def map(self, func: Callable, items: List[Any], 
            timeout: Optional[float] = None) -> List[Any]:
        """
        并行映射
        
        Args:
            func: 处理函数
            items: 输入列表
            timeout: 超时时间(秒)
        
        Returns:
            结果列表
        """
        if not items:
            return []
        
        # 小任务不需要并行
        if len(items) <= 2 or self.max_workers == 1:
            return [func(item) for item in items]
        
        executor_class = ProcessPoolExecutor if self.use_processes else ThreadPoolExecutor
        
        results = [None] * len(items)
        
        with executor_class(max_workers=self.max_workers) as executor:
            future_to_idx = {
                executor.submit(func, item): idx 
                for idx, item in enumerate(items)
            }
            
            for future in as_completed(future_to_idx, timeout=timeout):
                idx = future_to_idx[future]
                try:
                    results[idx] = future.result()
                except Exception as e:
                    logger.error(f"Task {idx} failed: {e}")
                    results[idx] = None
        
        return results
    
    def batch_map(self, func: Callable, items: List[Any], 
                  batch_size: int = 10) -> List[Any]:
        """
        批量并行映射
        
        将 items 分批处理，每批内部并行
        """
        if not items:
            return []
        
        # 分批
        batches = [items[i:i+batch_size] for i in range(0, len(items), batch_size)]
        
        batch_results = []
        for batch in batches:
            batch_results.extend(self.map(func, batch))
        
        return batch_results


# ========== 异步任务队列 ==========

class AsyncTaskQueue:
    """异步任务队列"""
    
    def __init__(self, max_workers: int = 4):
        self._queue: asyncio.Queue = asyncio.Queue()
        self._workers: List[asyncio.Task] = []
        self._max_workers = max_workers
        self._running = False
    
    async def _worker(self, worker_id: int):
        """工作协程"""
        while self._running:
            try:
                # 获取任务，超时则继续
                task_func, args, kwargs = await asyncio.wait_for(
                    self._queue.get(), timeout=1.0
                )
                
                try:
                    await task_func(*args, **kwargs)
                except Exception as e:
                    logger.error(f"Task failed in worker {worker_id}: {e}")
                finally:
                    self._queue.task_done()
                    
            except asyncio.TimeoutError:
                continue
            except Exception as e:
                logger.error(f"Worker {worker_id} error: {e}")
    
    async def start(self):
        """启动工作协程"""
        if self._running:
            return
        
        self._running = True
        self._workers = [
            asyncio.create_task(self._worker(i))
            for i in range(self._max_workers)
        ]
        logger.info(f"AsyncTaskQueue started with {self._max_workers} workers")
    
    async def stop(self):
        """停止工作协程"""
        self._running = False
        await asyncio.gather(*self._workers, return_exceptions=True)
        self._workers.clear()
        logger.info("AsyncTaskQueue stopped")
    
    async def submit(self, task_func: Callable, *args, **kwargs):
        """提交任务"""
        await self._queue.put((task_func, args, kwargs))
    
    async def wait(self):
        """等待所有任务完成"""
        await self._queue.join()


# ========== 批量处理优化 ==========

class BatchProcessor:
    """批量处理器 - 智能合并小请求"""
    
    def __init__(self, max_batch_size: int = 100, max_wait_ms: int = 100):
        """
        初始化批量处理器
        
        Args:
            max_batch_size: 最大批量大小
            max_wait_ms: 最大等待时间(毫秒)
        """
        self.max_batch_size = max_batch_size
        self.max_wait_ms = max_wait_ms
        self._lock = threading.Lock()
        self._pending: List[Tuple[Any, Callable, float]] = []
        self._results: Dict[int, Any] = {}
        self._result_ready = threading.Event()
    
    def add(self, item: Any, callback: Callable) -> int:
        """
        添加处理项
        
        Returns:
            请求ID
        """
        request_id = id(item)
        
        with self._lock:
            self._pending.append((request_id, callback, time.time()))
        
        return request_id
    
    def process_batch(self, processor: Callable) -> Dict[int, Any]:
        """
        处理批量
        
        Args:
            processor: 批量处理函数，接收列表返回列表
        
        Returns:
            {request_id: result}
        """
        with self._lock:
            if not self._pending:
                return {}
            
            # 获取待处理项
            pending = self._pending[:self.max_batch_size]
            self._pending = self._pending[self.max_batch_size:]
        
        # 提取请求ID和回调
        request_ids = [p[0] for p in pending]
        callbacks = {p[0]: p[1] for p in pending}
        
        # 批量处理
        items = [p[2] for p in pending]  # 注意：这里需要修改add的逻辑
        
        try:
            results = processor(items)
            
            # 构建结果映射
            return {
                request_ids[i]: callbacks[request_ids[i]](results[i])
                for i in range(len(results))
            }
        except Exception as e:
            logger.error(f"Batch processing failed: {e}")
            return {}


# ========== 速率限制 ==========

class RateLimiter:
    """速率限制器"""
    
    def __init__(self, max_calls: int, period_seconds: float):
        """
        初始化限流器
        
        Args:
            max_calls: 时间周期内最大调用次数
            period_seconds: 时间周期(秒)
        """
        self.max_calls = max_calls
        self.period = period_seconds
        self._calls: List[float] = []
        self._lock = threading.Lock()
    
    def acquire(self, blocking: bool = True, timeout: float = None) -> bool:
        """
        获取令牌
        
        Args:
            blocking: 是否阻塞
            timeout: 超时时间
        
        Returns:
            是否获取成功
        """
        start_time = time.time()
        
        while True:
            with self._lock:
                now = time.time()
                
                # 清理过期调用记录
                self._calls = [t for t in self._calls if now - t < self.period]
                
                if len(self._calls) < self.max_calls:
                    # 可以执行
                    self._calls.append(now)
                    return True
            
            if not blocking:
                return False
            
            if timeout and (time.time() - start_time) >= timeout:
                return False
            
            # 等待后重试
            time.sleep(0.01)
    
    def reset(self):
        """重置限流器"""
        with self._lock:
            self._calls.clear()


# ========== 全局实例 ==========

_global_cache: Optional[MemoryCache] = None
_parallel_processor: Optional[ParallelProcessor] = None


def get_cache() -> MemoryCache:
    """获取全局缓存"""
    global _global_cache
    if _global_cache is None:
        _global_cache = MemoryCache()
    return _global_cache


def get_parallel_processor(max_workers: int = 4) -> ParallelProcessor:
    """获取全局并行处理器"""
    global _parallel_processor
    if _parallel_processor is None:
        _parallel_processor = ParallelProcessor(max_workers=max_workers)
    return _parallel_processor
