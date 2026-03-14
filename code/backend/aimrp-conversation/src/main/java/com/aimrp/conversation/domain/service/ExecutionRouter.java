package com.aimrp.conversation.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 执行路由服务
 * 
 * 负责将识别的意图路由到对应的业务处理器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionRouter {
    
    private final IntentRecognitionService intentRecognitionService;
    private final EntityExtractionService entityExtractionService;
    
    /**
     * 处理器注册表
     */
    private final Map<IntentRecognitionService.Intent, IntentHandler> handlers = new HashMap<>();
    
    /**
     * 意图处理器接口
     */
    @FunctionalInterface
    public interface IntentHandler {
        /**
         * 处理意图
         * 
         * @param userInput 用户输入
         * @param entities 提取的实体
         * @return 处理结果
         */
        HandlerResult handle(String userInput, List<EntityExtractionService.Entity> entities);
    }
    
    /**
     * 处理器结果
     */
    @Data
    @Builder
    public static class HandlerResult {
        private boolean success;
        private String message;
        private Object data;
        private String action; // 建议的前端动作
    }
    
    /**
     * 注册处理器
     */
    public void registerHandler(IntentRecognitionService.Intent intent, IntentHandler handler) {
        handlers.put(intent, handler);
        log.info("注册意图处理器: {} -> {}", intent.getLabel(), handler.getClass().getSimpleName());
    }
    
    /**
     * 路由执行
     * 
     * @param userInput 用户输入
     * @return 执行结果
     */
    public RouterResult route(String userInput) {
        log.info("开始路由处理: {}", userInput);
        
        // 1. 意图识别
        IntentRecognitionService.Intent intent = intentRecognitionService.recognize(userInput);
        
        // 2. 实体提取
        List<EntityExtractionService.Entity> entities = entityExtractionService.extractAll(userInput);
        
        // 3. 查找处理器
        IntentHandler handler = handlers.get(intent);
        
        // 4. 执行处理
        HandlerResult result;
        if (handler != null) {
            try {
                result = handler.handle(userInput, entities);
            } catch (Exception e) {
                log.error("处理器执行失败", e);
                result = HandlerResult.builder()
                        .success(false)
                        .message("处理失败: " + e.getMessage())
                        .build();
            }
        } else {
            // 无处理器，返回帮助信息
            result = HandlerResult.builder()
                    .success(false)
                    .message(getHelpMessage(intent))
                    .action("SHOW_HELP")
                    .build();
        }
        
        // 5. 构建路由结果
        RouterResult routerResult = RouterResult.builder()
                .userInput(userInput)
                .intent(intent)
                .entities(entities)
                .handlerResult(result)
                .build();
        
        log.info("路由完成: intent={}, entities={}, success={}", 
                intent.getLabel(), entities.size(), result.isSuccess());
        
        return routerResult;
    }
    
    /**
     * 获取帮助信息
     */
    private String getHelpMessage(IntentRecognitionService.Intent intent) {
        if (intent == IntentRecognitionService.Intent.UNKNOWN) {
            return "抱歉，我无法理解你的请求。你可以尝试：\n" +
                   "1. 查询订单：如 '查询订单'\n" +
                   "2. 查询库存：如 '库存还有多少'\n" +
                   "3. 执行MRP：如 '运行MRP'\n" +
                   "4. BOM展开：如 '展开BOM A001'\n" +
                   "5. 获取帮助：输入 '帮助'";
        }
        
        return String.format("暂支持 '%s' 功能，相关处理器未配置", intent.getLabel());
    }
    
    /**
     * 路由结果
     */
    @Data
    @Builder
    public static class RouterResult {
        private String userInput;
        private IntentRecognitionService.Intent intent;
        private List<EntityExtractionService.Entity> entities;
        private HandlerResult handlerResult;
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 快速路由（静态方法）
     */
    public static RouterResult quickRoute(String userInput) {
        // 实际使用时通过 Spring 注入
        throw new UnsupportedOperationException("请通过 Spring 注入使用");
    }
}
