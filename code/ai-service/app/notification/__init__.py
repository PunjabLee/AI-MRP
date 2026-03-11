"""
通知模块 - 可插拔消息队列通知机制

导出：
- NotificationManager: 通知管理器
- RiskAlertManager: 风险预警管理器
- Notification: 通知消息
- NotificationType: 通知类型
- Priority: 优先级
- 各种渠道: Email, SMS, Webhook, Redis, Log
"""
from app.notification.manager import (
    NotificationManager,
    RiskAlertManager,
    Notification,
    NotificationType,
    Priority,
    NotificationChannel,
    EmailChannel,
    SMSChannel,
    WebhookChannel,
    RedisChannel,
    LogChannel,
    create_notification_manager,
    get_notification_manager,
    get_risk_alert_manager
)

__all__ = [
    "NotificationManager",
    "RiskAlertManager",
    "Notification",
    "NotificationType",
    "Priority",
    "NotificationChannel",
    "EmailChannel",
    "SMSChannel",
    "WebhookChannel",
    "RedisChannel",
    "LogChannel",
    "create_notification_manager",
    "get_notification_manager",
    "get_risk_alert_manager"
]
