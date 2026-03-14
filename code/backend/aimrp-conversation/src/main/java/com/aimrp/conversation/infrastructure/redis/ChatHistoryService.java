package com.aimrp.conversation.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 对话历史服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CHAT_HISTORY_KEY = "chat:history:";
    private static final String CHAT_CONTEXT_KEY = "chat:context:";
    private static final int HISTORY_EXPIRE_DAYS = 30;
    private static final int CONTEXT_EXPIRE_HOURS = 24;
    
    /**
     * 保存对话消息
     */
    public void saveMessage(String sessionId, ChatMessage message) {
        String key = CHAT_HISTORY_KEY + sessionId;
        
        // 获取现有消息列表
        List<ChatMessage> messages = getMessages(sessionId);
        messages.add(message);
        
        // 保存到Redis
        redisTemplate.opsForList().rightPush(key, message);
        
        // 设置过期时间
        redisTemplate.expire(key, HISTORY_EXPIRE_DAYS, TimeUnit.DAYS);
        
        log.debug("保存对话消息 - sessionId: {}, message: {}", sessionId, message.getContent());
    }
    
    /**
     * 获取对话历史
     */
    public List<ChatMessage> getMessages(String sessionId) {
        String key = CHAT_HISTORY_KEY + sessionId;
        
        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        
        if (objects == null || objects.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ChatMessage> messages = new ArrayList<>();
        for (Object obj : objects) {
            if (obj instanceof ChatMessage) {
                messages.add((ChatMessage) obj);
            }
        }
        
        return messages;
    }
    
    /**
     * 获取最近N条消息
     */
    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        String key = CHAT_HISTORY_KEY + sessionId;
        
        List<Object> objects = redisTemplate.opsForList().range(key, -limit, -1);
        
        if (objects == null || objects.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ChatMessage> messages = new ArrayList<>();
        for (Object obj : objects) {
            if (obj instanceof ChatMessage) {
                messages.add((ChatMessage) obj);
            }
        }
        
        return messages;
    }
    
    /**
     * 保存上下文
     */
    public void saveContext(String sessionId, Map<String, Object> context) {
        String key = CHAT_CONTEXT_KEY + sessionId;
        redisTemplate.opsForHash().putAll(key, context);
        redisTemplate.expire(key, CONTEXT_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 获取上下文
     */
    public Map<String, Object> getContext(String sessionId) {
        String key = CHAT_CONTEXT_KEY + sessionId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        
        Map<String, Object> context = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            context.put(entry.getKey().toString(), entry.getValue());
        }
        
        return context;
    }
    
    /**
     * 清除对话历史
     */
    public void clearHistory(String sessionId) {
        String key = CHAT_HISTORY_KEY + sessionId;
        redisTemplate.delete(key);
        
        String contextKey = CHAT_CONTEXT_KEY + sessionId;
        redisTemplate.delete(contextKey);
        
        log.info("清除对话历史 - sessionId: {}", sessionId);
    }
    
    /**
     * 获取所有会话ID
     */
    public Set<String> getAllSessionIds() {
        Set<String> keys = redisTemplate.keys(CHAT_HISTORY_KEY + "*");
        if (keys == null || keys.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> sessionIds = new HashSet<>();
        for (String key : keys) {
            String sessionId = key.replace(CHAT_HISTORY_KEY, "");
            sessionIds.add(sessionId);
        }
        
        return sessionIds;
    }
    
    /**
     * 对话消息
     */
    @Data
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;        // user/assistant/system
        private String content;      // 消息内容
        private String intent;      // 识别的意图
        private Map<String, Object> entities; // 提取的实体
        private LocalDateTime timestamp; // 时间戳
        
        public ChatMessage() {
            this.timestamp = LocalDateTime.now();
        }
    }
}
