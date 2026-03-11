package com.aimrp.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI MRP Python Service Java Client
 * 
 * 用于Java后端调用Python AI服务
 * 
 * 使用示例:
 * 
 * <pre>
 * {@code
 * AIRMPClient client = new AIRMPClient("http://localhost:8000");
 * 
 * // 需求预测
 * Map<String, Object> result = client.predictDemand("ITEM001", 30, "prophet");
 * 
 * // 排程优化
 * Map<String, Object> scheduleResult = client.optimizeSchedule(orders, resources, "makespan");
 * }
 * </pre>
 */
public class AIRMPClient {
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String appId;
    private String appSecret;
    
    public AIRMPClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    public AIRMPClient(String baseUrl, String appId, String appSecret) {
        this(baseUrl);
        this.appId = appId;
        this.appSecret = appSecret;
    }
    
    // ========== 预测服务 ==========
    
    /**
     * 需求预测
     */
    public Map<String, Object> predictDemand(String itemCode, int forecastDays, String method) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("item_code", itemCode);
        data.put("forecast_days", forecastDays);
        data.put("method", method);
        
        return invoke("PREDICT_DEMAND", data);
    }
    
    /**
     * 批量预测
     */
    public Map<String, Object> batchPredict(java.util.List<String> itemCodes, int forecastDays) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("item_codes", itemCodes);
        data.put("forecast_days", forecastDays);
        
        return invoke("PREDICT_BATCH", data);
    }
    
    /**
     * 安全库存计算
     */
    public Map<String, Object> calculateSafetyStock(String itemCode, int leadTimeDays, double serviceLevel) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("item_code", itemCode);
        data.put("lead_time_days", leadTimeDays);
        data.put("service_level", serviceLevel);
        
        return invoke("PREDICT_SAFETY_STOCK", data);
    }
    
    /**
     * 预测方法对比
     */
    public Map<String, Object> compareForecastMethods(String itemCode, java.util.List<String> methods) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("item_code", itemCode);
        data.put("methods", methods);
        
        return invoke("PREDICT_COMPARE", data);
    }
    
    // ========== 排程服务 ==========
    
    /**
     * 排程优化
     */
    public Map<String, Object> optimizeSchedule(
            java.util.List<Map<String, Object>> orders,
            java.util.List<Map<String, Object>> resources,
            String goal) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("orders", orders);
        data.put("resources", resources);
        data.put("goal", goal);
        
        return invoke("SCHEDULE_OPTIMIZE", data);
    }
    
    /**
     * 排程可行性检查
     */
    public Map<String, Object> checkFeasibility(
            java.util.List<Map<String, Object>> orders,
            java.util.List<Map<String, Object>> resources) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("orders", orders);
        data.put("resources", resources);
        
        return invoke("SCHEDULE_FEASIBILITY", data);
    }
    
    /**
     * 产能分析
     */
    public Map<String, Object> analyzeCapacity(
            java.util.List<Map<String, Object>> resources,
            java.util.List<Map<String, Object>> orders) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("resources", resources);
        data.put("orders", orders);
        
        return invoke("SCHEDULE_CAPACITY", data);
    }
    
    /**
     * 场景对比
     */
    public Map<String, Object> compareScenarios(java.util.List<Map<String, Object>> scenarios) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("scenarios", scenarios);
        
        return invoke("SCHEDULE_SCENARIOS", data);
    }
    
    // ========== 对话服务 ==========
    
    /**
     * AI对话
     */
    public Map<String, Object> chat(String message, String sessionId, boolean useLlm) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("session_id", sessionId);
        data.put("use_llm", useLlm);
        
        return invoke("CHAT_MESSAGE", data);
    }
    
    // ========== 核心调用 ==========
    
    /**
     * 统一调用接口
     */
    public Map<String, Object> invoke(String type, Map<String, Object> requestData) throws Exception {
        return invoke(type, requestData, null);
    }
    
    /**
     * 统一调用接口 (带回调)
     */
    public Map<String, Object> invoke(String type, Map<String, Object> requestData, String callbackUrl) throws Exception {
        // 构建请求
        Map<String, Object> request = new HashMap<>();
        request.put("type", type);
        request.put("data", requestData);
        request.put("callback_url", callbackUrl);
        
        // 添加认证信息
        if (appId != null) {
            request.put("app_id", appId);
        }
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        // 发送请求
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "integration/invoke"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest, 
                HttpResponse.BodyHandlers.ofString());
        
        // 解析响应
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        
        // 检查业务状态
        Boolean success = (Boolean) result.get("success");
        if (success == null || !success) {
            String errorMsg = (String) result.get("message");
            Map<String, Object> error = (Map<String, Object>) result.get("error");
            throw new AIRMPException(errorMsg, error);
        }
        
        return (Map<String, Object>) result.get("data");
    }
    
    /**
     * 异步调用 (立即返回request_id)
     */
    public String invokeAsync(String type, Map<String, Object> requestData) throws Exception {
        return invokeAsync(type, requestData, null);
    }
    
    /**
     * 异步调用 (带回调)
     */
    public String invokeAsync(String type, Map<String, Object> requestData, String callbackUrl) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", type);
        request.put("data", requestData);
        request.put("callback_url", callbackUrl);
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "integration/invoke/async"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        
        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());
        
        Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
        
        return (String) result.get("request_id");
    }
    
    /**
     * 查询结果
     */
    public Map<String, Object> getResult(String requestId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "integration/result/" + requestId))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        
        return objectMapper.readValue(response.body(), Map.class);
    }
    
    /**
     * 等待结果 (轮询)
     */
    public Map<String, Object> waitResult(String requestId, int timeoutSeconds) throws Exception {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
            Map<String, Object> result = getResult(requestId);
            String status = (String) result.get("status");
            
            if ("completed".equals(status)) {
                return result;
            } else if ("failed".equals(status)) {
                throw new AIRMPException("Request failed", (Map<String, Object>) result.get("error"));
            }
            
            Thread.sleep(500);
        }
        
        throw new AIRMPException("Request timeout", null);
    }
    
    // ========== 便捷方法 ==========
    
    /**
     * 预测需求 (默认参数)
     */
    public Map<String, Object> predictDemand(String itemCode) throws Exception {
        return predictDemand(itemCode, 30, "auto");
    }
    
    /**
     * 排程优化 (默认目标)
     */
    public Map<String, Object> optimizeSchedule(
            java.util.List<Map<String, Object>> orders,
            java.util.List<Map<String, Object>> resources) throws Exception {
        return optimizeSchedule(orders, resources, "makespan");
    }
    
    /**
     * AI对话 (默认参数)
     */
    public Map<String, Object> chat(String message) throws Exception {
        return chat(message, "default", false);
    }
    
    /**
     * 关闭客户端
     */
    public void close() {
        // HttpClient 自动管理资源
    }
    
    // ========== 异常类 ==========
    
    public static class AIRMPException extends Exception {
        private final Map<String, Object> error;
        
        public AIRMPException(String message, Map<String, Object> error) {
            super(message);
            this.error = error;
        }
        
        public Map<String, Object> getError() {
            return error;
        }
    }
}
