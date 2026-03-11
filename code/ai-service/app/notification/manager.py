"""
消息队列通知模块 - 可插拔架构

支持多种通知渠道：
- Email: 邮件通知
- SMS: 短信通知
- Webhook: Webhook 回调
- Redis/消息队列: 异步消息
- 日志: 记录日志

特性：
- 可插拔：支持添加新的通知渠道
- 异步：支持异步发送
- 批量：支持批量通知
- 模板：支持消息模板
"""
from typing import List, Dict, Any, Optional, Callable
from dataclasses import dataclass, field
from enum import Enum
from abc import ABC, abstractmethod
import asyncio
import json
import logging
from datetime import datetime
from queue import Queue
import threading

logger = logging.getLogger(__name__)


class NotificationType(Enum):
    """通知类型"""
    RISK_WARNING = "risk_warning"         # 风险预警
    INVENTORY_ALERT = "inventory_alert"   # 库存预警
    ORDER_UPDATE = "order_update"          # 订单更新
    MRP_RESULT = "mrp_result"             # MRP计算结果
    SCHEDULE_CHANGE = "schedule_change"   # 排程变更
    SYSTEM_ALERT = "system_alert"         # 系统告警


class Priority(Enum):
    """优先级"""
    LOW = 1
    NORMAL = 2
    HIGH = 3
    URGENT = 4


@dataclass
class Notification:
    """通知消息"""
    type: NotificationType
    title: str
    content: str
    recipients: List[str]  # 接收者列表
    priority: Priority = Priority.NORMAL
    metadata: Dict[str, Any] = field(default_factory=dict)
    template: Optional[str] = None
    created_at: datetime = field(default_factory=datetime.now)
    
    def to_dict(self) -> Dict:
        return {
            "type": self.type.value,
            "title": self.title,
            "content": self.content,
            "recipients": self.recipients,
            "priority": self.priority.value,
            "metadata": self.metadata,
            "created_at": self.created_at.isoformat()
        }


class NotificationChannel(ABC):
    """通知渠道基类"""
    
    @abstractmethod
    async def send(self, notification: Notification) -> bool:
        """发送通知"""
        pass
    
    @abstractmethod
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        """批量发送"""
        pass
    
    @abstractmethod
    def is_available(self) -> bool:
        """检查是否可用"""
        pass
    
    @property
    @abstractmethod
    def name(self) -> str:
        """渠道名称"""
        pass


class EmailChannel(NotificationChannel):
    """邮件通知渠道"""
    
    def __init__(self, smtp_host: str = "", smtp_port: int = 587,
                 username: str = "", password: str = "",
                 use_tls: bool = True, from_email: str = ""):
        self.smtp_host = smtp_host
        self.smtp_port = smtp_port
        self.username = username
        self.password = password
        self.use_tls = use_tls
        self.from_email = from_email or username
    
    @property
    def name(self) -> str:
        return "email"
    
    def is_available(self) -> bool:
        return bool(self.smtp_host and self.username)
    
    async def send(self, notification: Notification) -> bool:
        """发送邮件"""
        if not self.is_available():
            logger.warning("Email channel not configured")
            return False
        
        try:
            # 实际发送邮件
            logger.info(f"📧 Sending email to {notification.recipients}: {notification.title}")
            # TODO: 实现真正的邮件发送
            # import smtplib
            # from email.mime.text import MIMEText
            # ...
            return True
        except Exception as e:
            logger.error(f"Failed to send email: {e}")
            return False
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        """批量发送"""
        results = []
        for notif in notifications:
            results.append(await self.send(notif))
        return results


class SMSChannel(NotificationChannel):
    """短信通知渠道"""
    
    def __init__(self, api_key: str = "", api_secret: str = "",
                 endpoint: str = ""):
        self.api_key = api_key
        self.api_secret = api_secret
        self.endpoint = endpoint
    
    @property
    def name(self) -> str:
        return "sms"
    
    def is_available(self) -> bool:
        return bool(self.api_key and self.endpoint)
    
    async def send(self, notification: Notification) -> bool:
        """发送短信"""
        if not self.is_available():
            logger.warning("SMS channel not configured")
            return False
        
        try:
            logger.info(f"📱 Sending SMS to {notification.recipients}: {notification.title}")
            # TODO: 实现真正的短信发送
            return True
        except Exception as e:
            logger.error(f"Failed to send SMS: {e}")
            return False
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        results = []
        for notif in notifications:
            results.append(await self.send(notif))
        return results


class WebhookChannel(NotificationChannel):
    """Webhook 通知渠道"""
    
    def __init__(self, webhook_url: str = "", headers: Dict[str, str] = None,
                 timeout: int = 10):
        self.webhook_url = webhook_url
        self.headers = headers or {}
        self.timeout = timeout
    
    @property
    def name(self) -> str:
        return "webhook"
    
    def is_available(self) -> bool:
        return bool(self.webhook_url)
    
    async def send(self, notification: Notification) -> bool:
        """发送 Webhook"""
        if not self.is_available():
            logger.warning("Webhook not configured")
            return False
        
        try:
            import httpx
            payload = notification.to_dict()
            
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    self.webhook_url,
                    json=payload,
                    headers=self.headers
                )
                return response.status_code == 200
        except Exception as e:
            logger.error(f"Failed to send webhook: {e}")
            return False
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        results = []
        for notif in notifications:
            results.append(await self.send(notif))
        return results


class RedisChannel(NotificationChannel):
    """Redis 消息队列渠道"""
    
    def __init__(self, redis_url: str = "redis://localhost:6379/0",
                 queue_name: str = "notifications"):
        self.redis_url = redis_url
        self.queue_name = queue_name
        self._client = None
    
    @property
    def name(self) -> str:
        return "redis"
    
    def is_available(self) -> bool:
        try:
            import redis
            r = redis.from_url(self.redis_url)
            r.ping()
            return True
        except:
            return False
    
    async def send(self, notification: Notification) -> bool:
        """发送到 Redis 队列"""
        try:
            import redis
            r = redis.from_url(self.redis_url)
            r.rpush(self.queue_name, json.dumps(notification.to_dict()))
            return True
        except Exception as e:
            logger.error(f"Failed to send to Redis: {e}")
            return False
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        try:
            import redis
            r = redis.from_url(self.redis_url)
            pipe = r.pipeline()
            for notif in notifications:
                pipe.rpush(self.queue_name, json.dumps(notif.to_dict()))
            pipe.execute()
            return [True] * len(notifications)
        except Exception as e:
            logger.error(f"Failed to batch send to Redis: {e}")
            return [False] * len(notifications)


class LogChannel(NotificationChannel):
    """日志通知渠道（默认）"""
    
    def __init__(self, level: str = "INFO"):
        self.level = getattr(logging, level.upper(), logging.INFO)
    
    @property
    def name(self) -> str:
        return "log"
    
    def is_available(self) -> bool:
        return True
    
    async def send(self, notification: Notification) -> bool:
        """记录日志"""
        log_msg = f"[{notification.type.value}] {notification.title}: {notification.content}"
        
        if notification.priority == Priority.URGENT:
            logger.critical(log_msg)
        elif notification.priority == Priority.HIGH:
            logger.error(log_msg)
        elif notification.priority == Priority.NORMAL:
            logger.warning(log_msg)
        else:
            logger.info(log_msg)
        
        return True
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        results = []
        for notif in notifications:
            results.append(await self.send(notif))
        return results


class RabbitMQChannel(NotificationChannel):
    """RabbitMQ 消息队列渠道"""
    
    def __init__(self, host: str = "localhost", port: int = 5672,
                 username: str = "guest", password: str = "guest",
                 virtual_host: str = "/", exchange: str = "ai_mrp_notifications",
                 routing_key: str = "notification"):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.virtual_host = virtual_host
        self.exchange = exchange
        self.routing_key = routing_key
        self._connection = None
        self._channel = None
    
    @property
    def name(self) -> str:
        return "rabbitmq"
    
    def is_available(self) -> bool:
        try:
            import pika
            credentials = pika.PlainCredentials(self.username, self.password)
            params = pika.ConnectionParameters(
                host=self.host,
                port=self.port,
                virtual_host=self.virtual_host,
                credentials=credentials,
                connection_attempts=1,
                socket_timeout=5
            )
            connection = pika.BlockingConnection(params)
            connection.close()
            return True
        except:
            return False
    
    def _get_connection(self):
        """获取连接"""
        import pika
        if self._connection is None or self._connection.is_closed:
            credentials = pika.PlainCredentials(self.username, self.password)
            params = pika.ConnectionParameters(
                host=self.host,
                port=self.port,
                virtual_host=self.virtual_host,
                credentials=credentials
            )
            self._connection = pika.BlockingConnection(params)
        return self._connection
    
    async def send(self, notification: Notification) -> bool:
        """发送到 RabbitMQ"""
        try:
            import pika
            import json
            
            connection = self._get_connection()
            channel = connection.channel()
            
            # 声明交换机
            channel.exchange_declare(
                exchange=self.exchange,
                exchange_type='topic',
                durable=True
            )
            
            # 发送消息
            message = json.dumps(notification.to_dict(), ensure_ascii=False)
            
            channel.basic_publish(
                exchange=self.exchange,
                routing_key=self.routing_key,
                body=message,
                properties=pika.BasicProperties(
                    delivery_mode=2,  # 持久化
                    content_type='application/json'
                )
            )
            
            logger.info(f"📨 Sent to RabbitMQ: {notification.title}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to send to RabbitMQ: {e}")
            # 关闭连接以便重试
            self._connection = None
            return False
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        """批量发送"""
        results = []
        for notif in notifications:
            results.append(await self.send(notif))
        return results
    
    def close(self):
        """关闭连接"""
        if self._connection and not self._connection.is_closed:
            self._connection.close()


class KafkaChannel(NotificationChannel):
    """Kafka 消息队列渠道"""
    
    def __init__(self, bootstrap_servers: str = "localhost:9092",
                 topic: str = "ai-mrp-notifications",
                 producer_config: Dict = None):
        self.bootstrap_servers = bootstrap_servers
        self.topic = topic
        self.producer_config = producer_config or {}
        self._producer = None
    
    @property
    def name(self) -> str:
        return "kafka"
    
    def is_available(self) -> bool:
        try:
            from kafka import KafkaProducer
            from kafka.admin import KafkaAdminClient
            admin = KafkaAdminClient(
                bootstrap_servers=self.bootstrap_servers,
                request_timeout_ms=5000
            )
            admin.close()
            return True
        except:
            return False
    
    def _get_producer(self):
        """获取生产者"""
        from kafka import KafkaProducer
        if self._producer is None:
            self._producer = KafkaProducer(
                bootstrap_servers=self.bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                **self.producer_config
            )
        return self._producer
    
    async def send(self, notification: Notification) -> bool:
        """发送到 Kafka"""
        try:
            producer = self._get_producer()
            producer.send(self.topic, value=notification.to_dict())
            producer.flush()
            logger.info(f"📨 Sent to Kafka: {notification.title}")
            return True
        except Exception as e:
            logger.error(f"Failed to send to Kafka: {e}")
            self._producer = None
            return False
    
    async def batch_send(self, notifications: List[Notification]) -> List[bool]:
        """批量发送"""
        results = []
        for notif in notifications:
            results.append(await self.send(notif))
        return results


class NotificationManager:
    """通知管理器"""
    
    def __init__(self):
        self.channels: Dict[str, NotificationChannel] = {}
        self.enabled_channels: List[str] = ["log"]  # 默认启用日志
        self._queue: Queue = Queue()
        self._worker_thread: Optional[threading.Thread] = None
        self._running = False
    
    def register_channel(self, name: str, channel: NotificationChannel):
        """注册通知渠道"""
        self.channels[name] = channel
        logger.info(f"Registered notification channel: {name}")
    
    def enable_channel(self, name: str):
        """启用渠道"""
        if name in self.channels:
            self.enabled_channels.append(name)
    
    def disable_channel(self, name: str):
        """禁用渠道"""
        if name in self.enabled_channels:
            self.enabled_channels.remove(name)
    
    def send(self, notification: Notification, channel: Optional[str] = None) -> bool:
        """发送通知（同步）"""
        if channel:
            # 发送到指定渠道
            if channel in self.channels:
                return asyncio.run(self.channels[channel].send(notification))
            return False
        
        # 发送到所有启用的渠道
        success = False
        for ch in self.enabled_channels:
            if ch in self.channels:
                result = asyncio.run(self.channels[ch].send(notification))
                success = success or result
        
        return success
    
    def send_async(self, notification: Notification, callback: Optional[Callable] = None):
        """异步发送通知"""
        self._queue.put((notification, callback))
        if not self._running:
            self._start_worker()
    
    def send_batch(self, notifications: List[Notification], channel: Optional[str] = None) -> List[bool]:
        """批量发送"""
        if channel:
            if channel in self.channels:
                return asyncio.run(self.channels[channel].batch_send(notifications))
            return [False] * len(notifications)
        
        # 发送到所有启用的渠道
        results = []
        for ch in self.enabled_channels:
            if ch in self.channels:
                batch_results = asyncio.run(self.channels[ch].batch_send(notifications))
                results.extend(batch_results)
        
        return results
    
    def _start_worker(self):
        """启动工作线程"""
        self._running = True
        self._worker_thread = threading.Thread(target=self._worker, daemon=True)
        self._worker_thread.start()
    
    def _worker(self):
        """工作线程"""
        while self._running:
            try:
                item = self._queue.get(timeout=1)
                notification, callback = item
                
                success = self.send(notification)
                
                if callback:
                    callback(success)
                
                self._queue.task_done()
            except:
                continue
    
    def stop(self):
        """停止工作线程"""
        self._running = False
        if self._worker_thread:
            self._worker_thread.join(timeout=5)


# ========== 风险预警专用 ==========

class RiskAlertManager:
    """风险预警管理器"""
    
    def __init__(self, notification_manager: NotificationManager):
        self.notification_manager = notification_manager
        self.risk_rules: Dict[str, Dict] = {}
    
    def register_risk_rule(self, rule_id: str, rule: Dict):
        """注册风险规则"""
        self.risk_rules[rule_id] = rule
    
    def check_and_alert(self, risk_data: Dict) -> List[Notification]:
        """检查风险并发送预警"""
        notifications = []
        
        # 库存风险
        if "inventory" in risk_data:
            for item in risk_data["inventory"]:
                if item.get("current_qty", 0) < item.get("safety_stock", 0):
                    notif = Notification(
                        type=NotificationType.INVENTORY_ALERT,
                        title=f"库存预警: {item.get('item_code')}",
                        content=f"当前库存 {item.get('current_qty')} 低于安全库存 {item.get('safety_stock')}",
                        recipients=item.get("notify_to", []),
                        priority=Priority.HIGH if item.get("current_qty", 0) < item.get("safety_stock", 0) * 0.5 else Priority.NORMAL,
                        metadata=item
                    )
                    notifications.append(notif)
        
        # 供应商风险
        if "supplier" in risk_data:
            for supplier in risk_data["supplier"]:
                if supplier.get("delay_risk", False):
                    notif = Notification(
                        type=NotificationType.RISK_WARNING,
                        title=f"供应商风险: {supplier.get('supplier_name')}",
                        content=f"供应商 {supplier.get('supplier_name')} 存在交期延误风险",
                        recipients=supplier.get("notify_to", []),
                        priority=Priority.HIGH,
                        metadata=supplier
                    )
                    notifications.append(notif)
        
        # 需求突变风险
        if "demand" in risk_data:
            for item in risk_data["demand"]:
                change_rate = abs(item.get("change_rate", 0))
                if change_rate > 0.3:  # 变化超过30%
                    notif = Notification(
                        type=NotificationType.RISK_WARNING,
                        title=f"需求突变: {item.get('item_code')}",
                        content=f"需求变化率 {change_rate:.1%}，超出正常范围",
                        recipients=item.get("notify_to", []),
                        priority=Priority.URGENT if change_rate > 0.5 else Priority.HIGH,
                        metadata=item
                    )
                    notifications.append(notif)
        
        # 发送所有预警
        for notif in notifications:
            self.notification_manager.send_async(notif)
        
        return notifications
    
    def create_alert(self, alert_type: str, title: str, content: str,
                    recipients: List[str], priority: Priority = Priority.NORMAL,
                    metadata: Dict = None) -> Notification:
        """创建自定义预警"""
        notif = Notification(
            type=NotificationType.RISK_WARNING,
            title=title,
            content=content,
            recipients=recipients,
            priority=priority,
            metadata=metadata or {}
        )
        
        self.notification_manager.send_async(notif)
        return notif


# ========== 便捷函数 =========-

def create_notification_manager(config: Dict = None) -> NotificationManager:
    """Create notification manager with configurable channels"""
    manager = NotificationManager()
    
    # Default: register log channel
    manager.register_channel("log", LogChannel())
    
    if config:
        # Register email channel
        if config.get("email"):
            manager.register_channel("email", EmailChannel(**config["email"]))
        
        # Register SMS channel
        if config.get("sms"):
            manager.register_channel("sms", SMSChannel(**config["sms"]))
        
        # Register webhook channel
        if config.get("webhook"):
            manager.register_channel("webhook", WebhookChannel(**config["webhook"]))
        
        # Register Redis channel
        if config.get("redis"):
            manager.register_channel("redis", RedisChannel(**config["redis"]))
        
        # Register RabbitMQ channel
        if config.get("rabbitmq"):
            manager.register_channel("rabbitmq", RabbitMQChannel(**config["rabbitmq"]))
        
        # Register Kafka channel
        if config.get("kafka"):
            manager.register_channel("kafka", KafkaChannel(**config["kafka"]))
        
        # Enable specified channels
        if config.get("enabled"):
            manager.enabled_channels = config["enabled"]
    
    return manager


# 全局通知管理器实例
_notification_manager: Optional[NotificationManager] = None

def get_notification_manager() -> NotificationManager:
    """获取全局通知管理器"""
    global _notification_manager
    if _notification_manager is None:
        _notification_manager = create_notification_manager()
    return _notification_manager


def get_risk_alert_manager() -> RiskAlertManager:
    """获取风险预警管理器"""
    return RiskAlertManager(get_notification_manager())
