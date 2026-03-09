package com.aimrp.conversation;

import com.aimrp.conversation.application.service.ConversationApplicationService;
import com.aimrp.conversation.domain.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对话服务集成测试
 */
@SpringBootTest
public class ConversationIntegrationTest {
    
    @Autowired
    private IntentRecognitionService intentRecognitionService;
    
    @Autowired
    private EntityExtractionService entityExtractionService;
    
    @Autowired
    private ConversationApplicationService conversationService;
    
    /**
     * 测试意图识别
     */
    @Test
    public void testIntentRecognition() {
        // 测试订单查询
        IntentRecognitionService.Intent intent1 = intentRecognitionService.recognize("查询订单");
        assertEquals(IntentRecognitionService.Intent.ORDER_QUERY, intent1);
        
        // 测试库存查询
        IntentRecognitionService.Intent intent2 = intentRecognitionService.recognize("库存还有多少");
        assertEquals(IntentRecognitionService.Intent.INVENTORY_QUERY, intent2);
        
        // 测试MRP执行
        IntentRecognitionService.Intent intent3 = intentRecognitionService.recognize("运行MRP");
        assertEquals(IntentRecognitionService.Intent.MRP_RUN, intent3);
        
        // 测试BOM展开
        IntentRecognitionService.Intent intent4 = intentRecognitionService.recognize("展开BOM A001");
        assertEquals(IntentRecognitionService.Intent.BOM_EXPAND, intent4);
        
        // 测试帮助
        IntentRecognitionService.Intent intent5 = intentRecognitionService.recognize("帮助");
        assertEquals(IntentRecognitionService.Intent.HELP, intent5);
        
        System.out.println("=== 意图识别测试通过 ===");
    }
    
    /**
     * 测试实体提取
     */
    @Test
    public void testEntityExtraction() {
        // 测试物料编码提取
        List<EntityExtractionService.Entity> entities1 = 
                entityExtractionService.extractAll("查询物料A001的库存");
        
        System.out.println("=== 测试1: 查询物料A001的库存 ===");
        for (EntityExtractionService.Entity entity : entities1) {
            System.out.printf("实体: [%s] %s%n", entity.getType().getLabel(), entity.getValue());
        }
        
        // 应该有物料编码
        List<String> itemCodes = entityExtractionService.extractItemCodes("查询A001和B002");
        assertTrue(itemCodes.size() >= 1);
        
        // 测试数量提取
        List<Double> quantities = entityExtractionService.extractQuantities("入库100个A001");
        assertTrue(quantities.size() > 0);
        assertEquals(100.0, quantities.get(0));
        
        System.out.println("=== 实体提取测试通过 ===");
    }
    
    /**
     * 测试完整对话流程
     */
    @Test
    public void testFullConversation() {
        // 测试帮助
        ConversationApplicationService.ConversationResponse response1 = 
                conversationService.getHelp();
        
        System.out.println("=== 测试: 获取帮助 ===");
        System.out.println("意图: " + response1.getIntent());
        System.out.println("消息: " + response1.getMessage().substring(0, 50) + "...");
        assertTrue(response1.isSuccess());
        
        // 测试订单查询
        ConversationApplicationService.ConversationResponse response2 = 
                conversationService.process("user123", "查询订单");
        
        System.out.println("\n=== 测试: 查询订单 ===");
        System.out.println("意图: " + response2.getIntent());
        System.out.println("描述: " + response2.getIntentDescription());
        assertEquals("订单查询", response2.getIntent());
        
        // 测试MRP执行
        ConversationApplicationService.ConversationResponse response3 = 
                conversationService.process("user123", "运行MRP");
        
        System.out.println("\n=== 测试: 运行MRP ===");
        System.out.println("意图: " + response3.getIntent());
        assertEquals("MRP执行", response3.getIntent());
        
        // 测试库存查询
        ConversationApplicationService.ConversationResponse response4 = 
                conversationService.process("user123", "A001库存还有多少");
        
        System.out.println("\n=== 测试: 库存查询 ===");
        System.out.println("意图: " + response4.getIntent());
        System.out.println("实体数: " + response4.getEntities().size());
        assertEquals("库存查询", response4.getIntent());
        
        System.out.println("\n=== 对话服务测试全部通过 ===");
    }
}
