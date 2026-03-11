using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;

namespace AIRMP.Client
{
    /// <summary>
    /// AI MRP Python Service C# Client
    /// 
    /// 使用示例:
    /// 
    /// <code>
    /// var client = new AIRMPClient("http://localhost:8000");
    /// 
    /// // 需求预测
    /// var result = await client.PredictDemandAsync("ITEM001", 30, "prophet");
    /// 
    /// // 排程优化
    /// var scheduleResult = await client.OptimizeScheduleAsync(orders, resources, "makespan");
    /// </code>
    /// </summary>
    public class AIRMPClient : IDisposable
    {
        private readonly string _baseUrl;
        private readonly HttpClient _httpClient;
        private readonly JsonSerializerOptions _jsonOptions;
        private string _appId;
        private string _appSecret;
        
        public AIRMPClient(string baseUrl)
        {
            _baseUrl = baseUrl.TrimEnd('/') + "/";
            _httpClient = new HttpClient
            {
                Timeout = TimeSpan.FromSeconds(60)
            };
            _jsonOptions = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                WriteIndented = false
            };
        }
        
        public AIRMPClient(string baseUrl, string appId, string appSecret) : this(baseUrl)
        {
            _appId = appId;
            _appSecret = appSecret;
        }
        
        // ========== 预测服务 ==========
        
        /// <summary>
        /// 需求预测
        /// </summary>
        public async Task<Dictionary<string, object>> PredictDemandAsync(
            string itemCode, 
            int forecastDays = 30, 
            string method = "auto")
        {
            var data = new Dictionary<string, object>
            {
                { "item_code", itemCode },
                { "forecast_days", forecastDays },
                { "method", method }
            };
            
            return await InvokeAsync("PREDICT_DEMAND", data);
        }
        
        /// <summary>
        /// 批量预测
        /// </summary>
        public async Task<Dictionary<string, object>> BatchPredictAsync(
            List<string> itemCodes, 
            int forecastDays = 30)
        {
            var data = new Dictionary<string, object>
            {
                { "item_codes", itemCodes },
                { "forecast_days", forecastDays }
            };
            
            return await InvokeAsync("PREDICT_BATCH", data);
        }
        
        /// <summary>
        /// 安全库存计算
        /// </summary>
        public async Task<Dictionary<string, object>> CalculateSafetyStockAsync(
            string itemCode, 
            int leadTimeDays = 7, 
            double serviceLevel = 0.95)
        {
            var data = new Dictionary<string, object>
            {
                { "item_code", itemCode },
                { "lead_time_days", leadTimeDays },
                { "service_level", serviceLevel }
            };
            
            return await InvokeAsync("PREDICT_SAFETY_STOCK", data);
        }
        
        /// <summary>
        /// 预测方法对比
        /// </summary>
        public async Task<Dictionary<string, object>> CompareForecastMethodsAsync(
            string itemCode, 
            List<string> methods)
        {
            var data = new Dictionary<string, object>
            {
                { "item_code", itemCode },
                { "methods", methods }
            };
            
            return await InvokeAsync("PREDICT_COMPARE", data);
        }
        
        // ========== 排程服务 ==========
        
        /// <summary>
        /// 排程优化
        /// </summary>
        public async Task<Dictionary<string, object>> OptimizeScheduleAsync(
            List<Dictionary<string, object>> orders,
            List<Dictionary<string, object>> resources,
            string goal = "makespan")
        {
            var data = new Dictionary<string, object>
            {
                { "orders", orders },
                { "resources", resources },
                { "goal", goal }
            };
            
            return await InvokeAsync("SCHEDULE_OPTIMIZE", data);
        }
        
        /// <summary>
        /// 可行性检查
        /// </summary>
        public async Task<Dictionary<string, object>> CheckFeasibilityAsync(
            List<Dictionary<string, object>> orders,
            List<Dictionary<string, object>> resources)
        {
            var data = new Dictionary<string, object>
            {
                { "orders", orders },
                { "resources", resources }
            };
            
            return await InvokeAsync("SCHEDULE_FEASIBILITY", data);
        }
        
        /// <summary>
        /// 产能分析
        /// </summary>
        public async Task<Dictionary<string, object>> AnalyzeCapacityAsync(
            List<Dictionary<string, object>> resources,
            List<Dictionary<string, object>> orders)
        {
            var data = new Dictionary<string, object>
            {
                { "resources", resources },
                { "orders", orders }
            };
            
            return await InvokeAsync("SCHEDULE_CAPACITY", data);
        }
        
        /// <summary>
        /// 场景对比
        /// </summary>
        public async Task<Dictionary<string, object>> CompareScenariosAsync(
            List<Dictionary<string, object>> scenarios)
        {
            var data = new Dictionary<string, object>
            {
                { "scenarios", scenarios }
            };
            
            return await InvokeAsync("SCHEDULE_SCENARIOS", data);
        }
        
        // ========== 对话服务 ==========
        
        /// <summary>
        /// AI对话
        /// </summary>
        public async Task<Dictionary<string, object>> ChatAsync(
            string message, 
            string sessionId = "default", 
            bool useLlm = false)
        {
            var data = new Dictionary<string, object>
            {
                { "message", message },
                { "session_id", sessionId },
                { "use_llm", useLlm }
            };
            
            return await InvokeAsync("CHAT_MESSAGE", data);
        }
        
        // ========== 核心调用 ==========
        
        /// <summary>
        /// 统一调用接口
        /// </summary>
        public async Task<Dictionary<string, object>> InvokeAsync(
            string type, 
            Dictionary<string, object> requestData)
        {
            return await InvokeAsync(type, requestData, null);
        }
        
        /// <summary>
        /// 统一调用接口 (带回调)
        /// </summary>
        public async Task<Dictionary<string, object>> InvokeAsync(
            string type, 
            Dictionary<string, object> requestData, 
            string callbackUrl)
        {
            var request = new Dictionary<string, object>
            {
                { "type", type },
                { "data", requestData }
            };
            
            if (!string.IsNullOrEmpty(callbackUrl))
            {
                request["callback_url"] = callbackUrl;
            }
            
            if (!string.IsNullOrEmpty(_appId))
            {
                request["app_id"] = _appId;
            }
            
            var requestJson = JsonSerializer.Serialize(request, _jsonOptions);
            
            var httpRequest = new HttpRequestMessage(
                HttpMethod.Post, 
                $"{_baseUrl}integration/invoke")
            {
                Content = new StringContent(requestJson, System.Text.Encoding.UTF8, "application/json")
            };
            
            var response = await _httpClient.SendAsync(httpRequest);
            var responseJson = await response.Content.ReadAsStringAsync();
            
            var result = JsonSerializer.Deserialize<Dictionary<string, object>>(
                responseJson, _jsonOptions);
            
            if (result == null)
            {
                throw new AIRMPException("Invalid response");
            }
            
            // 检查业务状态
            if (!result.TryGetValue("success", out var successObj) || 
                !(bool)successObj)
            {
                var message = result.TryGetValue("message", out var msgObj) 
                    ? msgObj?.ToString() 
                    : "Unknown error";
                
                object errorObj = null;
                result.TryGetValue("error", out errorObj);
                
                throw new AIRMPException(message, errorObj as Dictionary<string, object>);
            }
            
            if (result.TryGetValue("data", out var dataObj))
            {
                return dataObj as Dictionary<string, object>;
            }
            
            return new Dictionary<string, object>();
        }
        
        /// <summary>
        /// 异步调用 (立即返回request_id)
        /// </summary>
        public async Task<string> InvokeAsync(string type, Dictionary<string, object> requestData)
        {
            return await InvokeAsync(type, requestData, null);
        }
        
        /// <summary>
        /// 异步调用 (带回调)
        /// </summary>
        public async Task<string> InvokeAsync(
            string type, 
            Dictionary<string, object> requestData, 
            string callbackUrl)
        {
            var request = new Dictionary<string, object>
            {
                { "type", type },
                { "data", requestData }
            };
            
            if (!string.IsNullOrEmpty(callbackUrl))
            {
                request["callback_url"] = callbackUrl;
            }
            
            var requestJson = JsonSerializer.Serialize(request, _jsonOptions);
            
            var httpRequest = new HttpRequestMessage(
                HttpMethod.Post,
                $"{_baseUrl}integration/invoke/async")
            {
                Content = new StringContent(requestJson, System.Text.Encoding.UTF8, "application/json")
            };
            
            var response = await _httpClient.SendAsync(httpRequest);
            var responseJson = await response.Content.ReadAsStringAsync();
            
            var result = JsonSerializer.Deserialize<Dictionary<string, object>>(
                responseJson, _jsonOptions);
            
            if (result != null && result.TryGetValue("request_id", out var requestIdObj))
            {
                return requestIdObj?.ToString();
            }
            
            throw new AIRMPException("Failed to get request_id", null);
        }
        
        /// <summary>
        /// 查询结果
        /// </summary>
        public async Task<Dictionary<string, object>> GetResultAsync(string requestId)
        {
            var response = await _httpClient.GetAsync($"{_baseUrl}integration/result/{requestId}");
            var responseJson = await response.Content.ReadAsStringAsync();
            
            return JsonSerializer.Deserialize<Dictionary<string, object>>(
                responseJson, _jsonOptions);
        }
        
        /// <summary>
        /// 等待结果 (轮询)
        /// </summary>
        public async Task<Dictionary<string, object>> WaitResultAsync(
            string requestId, 
            int timeoutSeconds = 60)
        {
            var startTime = DateTime.UtcNow;
            
            while ((DateTime.UtcNow - startTime).TotalSeconds < timeoutSeconds)
            {
                var result = await GetResultAsync(requestId);
                
                if (result.TryGetValue("status", out var statusObj))
                {
                    var status = statusObj?.ToString();
                    
                    if (status == "completed")
                    {
                        return result;
                    }
                    else if (status == "failed")
                    {
                        throw new AIRMPException(
                            result.TryGetValue("message", out var msg) ? msg?.ToString() : "Request failed",
                            null);
                    }
                }
                
                await Task.Delay(500);
            }
            
            throw new AIRMPException("Request timeout", null);
        }
        
        // ========== 便捷方法 ==========
        
        /// <summary>
        /// 预测需求 (默认参数)
        /// </summary>
        public async Task<Dictionary<string, object>> PredictDemandAsync(string itemCode)
        {
            return await PredictDemandAsync(itemCode, 30, "auto");
        }
        
        /// <summary>
        /// 排程优化 (默认目标)
        /// </summary>
        public async Task<Dictionary<string, object>> OptimizeScheduleAsync(
            List<Dictionary<string, object>> orders,
            List<Dictionary<string, object>> resources)
        {
            return await OptimizeScheduleAsync(orders, resources, "makespan");
        }
        
        /// <summary>
        /// AI对话 (默认参数)
        /// </summary>
        public async Task<Dictionary<string, object>> ChatAsync(string message)
        {
            return await ChatAsync(message, "default", false);
        }
        
        /// <summary>
        /// 释放资源
        /// </summary>
        public void Dispose()
        {
            _httpClient.Dispose();
        }
        
        /// <summary>
        /// 异常类
        /// </summary>
        public class AIRMPException : Exception
        {
            public Dictionary<string, object> Error { get; }
            
            public AIRMPException(string message, Dictionary<string, object> error) 
                : base(message)
            {
                Error = error;
            }
        }
    }
}
