package com.aimrp.conversation.application.service;

import com.aimrp.conversation.domain.service.*;
import com.aimrp.conversation.domain.service.ExecutionRouter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话应用服务
 * 
 * 整合意图识别、实体提取、执行路由的对外服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationApplicationService {
    
    private final IntentRecognitionService intentRecognitionService;
    private final EntityExtractionService entityExtractionService;
    private final ExecutionRouter executionRouter;
    
    /**
     * 处理用户对话
     * 
     * @param userId 用户ID
     * @param userInput 用户输入
     * @return 对话响应
     */
    public ConversationResponse process(String userId, String userInput) {
        log.info("处理用户对话 - userId: {}, input: {}", userId, userInput);
        
        try {
            // 1. 意图识别
            IntentRecognitionService.Intent intent = intentRecognitionService.recognize(userInput);
            
            // 2. 实体提取
            List<EntityExtractionService.Entity> entities = entityExtractionService.extractAll(userInput);
            
            // 3. 路由执行
            RouterResult routerResult = executionRouter.route(userInput);
            
            // 4. 构建响应
            ConversationResponse response = ConversationResponse.builder()
                    .userInput(userInput)
                    .intent(intent.getLabel())
                    .intentDescription(intent.getDescription())
                    .entities(entities)
                    .message(routerResult.getHandlerResult().getMessage())
                    .data(routerResult.getHandlerResult().getData())
                    .action(routerResult.getHandlerResult().getAction())
                    .success(routerResult.getHandlerResult().isSuccess())
                    .build();
            
            return response;
            
        } catch (Exception e) {
            log.error("对话处理失败", e);
            return ConversationResponse.builder()
                    .userInput(userInput)
                    .intent("ERROR")
                    .message("处理出错: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }
    
    /**
     * 获取帮助信息
     */
    public ConversationResponse getHelp() {
        return ConversationResponse.builder()
                .intent("HELP")
                .message("你好！我是 AI MRP 助手，可以帮你完成以下操作：\n\n" +
                         "📋 订单管理\n" +
                         "  - 查询订单：'查看订单'\n" +
                         "  - 创建订单：'创建订单'\n\n" +
                         "📦 库存管理\n" +
                         "  - 查询库存：'库存还有多少'\n" +
                         "  - 入库：'入库100个A001'\n" +
                         "  - 出库：'出库50个B001'\n\n" +
                         "🔧 BOM管理\n" +
                         "  - 查询BOM：'查看BOM A001'\n" +
                         "  - BOM展开：'展开BOM A001'\n\n" +
                         "⚙️ MRP计算\n" +
                         "  - 执行MRP：'运行MRP'\n" +
                         "  - 查看结果：'MRP结果'\n\n" +
                         "请直接输入你的需求！")
                .action("SHOW_HELP")
                .success(true)
                .build();
    }
    
    /**
     * 对话响应
     */
    @lombok.Data
    @lombok.Builder
    public static class ConversationResponse {
        private String userInput;
        private String intent;
        private String intentDescription;
        private List<EntityExtractionService.Entity> entities;
        private String message;
        private Object data;
        private String action;
        private boolean success;
    }
}
