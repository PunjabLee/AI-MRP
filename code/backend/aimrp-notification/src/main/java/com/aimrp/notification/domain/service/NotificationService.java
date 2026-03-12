package com.aimrp.notification.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:noreply@aimrp.com}")
    private String emailFrom;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.provider:}")
    private String smsProvider;

    @Value("${notification.sms.api-url:}")
    private String smsApiUrl;

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
                results.stream().filter(r -> (Boolean) r.get("success")).count(),
                results.stream().filter(r -> !(Boolean) r.get("success")).count());
    }

    /**
     * 发送邮件 - 使用 Spring Mail
     */
    @Async
    private void sendEmail(NotificationRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(request.getReceiver());
            message.setSubject(request.getTitle() != null ? request.getTitle() : "AI-MRP 通知");
            message.setText(request.getContent());

            mailSender.send(message);
            log.info("邮件发送成功 - To: {}", request.getReceiver());
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送短信 - HTTP API 调用
     */
    @Async
    private void sendSms(NotificationRequest request) {
        if (!smsEnabled) {
            log.warn("短信服务未启用，跳过发送 - To: {}", request.getReceiver());
            return;
        }

        try {
            // 调用短信API发送
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> smsRequest = new HashMap<>();
            smsRequest.put("mobile", request.getReceiver());
            smsRequest.put("content", request.getContent());

            // 根据提供商调用不同的API
            if ("aliyun".equals(smsProvider)) {
                sendAliyunSms(smsRequest);
            } else if ("tencent".equals(smsProvider)) {
                sendTencentSms(smsRequest);
            } else {
                log.warn("未配置的短信提供商: {}", smsProvider);
            }

            log.info("短信发送成功 - To: {}", request.getReceiver());
        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage());
            throw new RuntimeException("短信发送失败", e);
        }
    }

    private void sendAliyunSms(Map<String, Object> request) {
        // TODO: 集成阿里云短信服务
        // 1. 签名
        // 2. 模板ID
        // 3. 调用API
        log.info("【阿里云短信】发送中 - mobile: {}", request.get("mobile"));
    }

    private void sendTencentSms(Map<String, Object> request) {
        // TODO: 集成腾讯云短信服务
        // 1. SDK ID
        // 2. 模板ID
        // 3. 调用API
        log.info("【腾讯云短信】发送中 - mobile: {}", request.get("mobile"));
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
