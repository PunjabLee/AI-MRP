package com.aimrp.notification.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务
 */
@Slf4j
@Service
public class NotificationService {
    
    /**
     * 发送通知
     */
    public void send(NotificationRequest request) {
        log.info("发送通知 - 类型: {}, 接收人: {}, 内容: {}", 
                request.getType(), request.getReceiver(), request.getContent());
        
        // 根据通知类型选择发送渠道
        switch (request.getChannel()) {
            case "EMAIL":
                sendEmail(request);
                break;
            case "SMS":
                sendSms(request);
                break;
            case "SYSTEM":
                sendSystem(request);
                break;
            default:
                log.warn("未知通知渠道: {}", request.getChannel());
        }
    }
    
    /**
     * 批量发送
     */
    public void sendBatch(List<NotificationRequest> requests) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (NotificationRequest request : requests) {
            try {
                send(request);
                results.add(Map.of("success", true, "receiver", request.getReceiver()));
            } catch (Exception e) {
                log.error("发送通知失败: {}", e.getMessage());
                results.add(Map.of("success", false, "receiver", request.getReceiver(), "error", e.getMessage()));
            }
        }
        
        log.info("批量发送通知完成 - 成功: {}, 失败: {}", 
                results.stream().filter(r -> (Boolean)r.get("success")).count(),
                results.stream().filter(r -> !(Boolean)r.get("success")).count());
    }
    
    /**
     * 发送邮件
     */
    private void sendEmail(NotificationRequest request) {
        // TODO: 集成真实邮件服务
        log.info("【邮件发送】To: {}, Subject: {}", request.getReceiver(), request.getTitle());
    }
    
    /**
     * 发送短信
     */
    private void sendSms(NotificationRequest request) {
        // TODO: 集成真实短信服务
        log.info("【短信发送】To: {}, Content: {}", request.getReceiver(), request.getContent());
    }
    
    /**
     * 站内消息
     */
    private void sendSystem(NotificationRequest request) {
        // 保存到数据库
        log.info("【系统消息】User: {}, Content: {}", request.getReceiver(), request.getContent());
    }
    
    /**
     * 发送风险预警
     */
    public void sendRiskWarning(String riskId, String riskTitle, String level, String receiver) {
        NotificationRequest request = NotificationRequest.builder()
                .type("RISK_WARNING")
                .channel("SYSTEM")
                .receiver(receiver)
                .title("⚠️ 风险预警提醒")
                .content(String.format("风险ID: %s, 标题: %s, 级别: %s", riskId, riskTitle, level))
                .priority("HIGH")
                .build();
        
        send(request);
    }
    
    /**
     * 通知请求
     */
    @lombok.Data
    @lombok.Builder
    public static class NotificationRequest {
        private String type;           // 通知类型
        private String channel;        // 发送渠道: EMAIL/SMS/SYSTEM
        private String receiver;       // 接收人
        private String title;          // 标题
        private String content;        // 内容
        private String priority;       // 优先级: HIGH/MEDIUM/LOW
        private Map<String, Object> extData; // 扩展数据
    }
}
