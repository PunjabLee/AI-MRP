package com.aimrp.mrp;

import com.aimrp.mrp.application.service.MrpApplicationService;
import com.aimrp.mrp.domain.entity.MrpRun;
import com.aimrp.mrp.domain.valueobject.MrpResult;
import com.aimrp.mrp.domain.service.BomExpander;
import com.aimrp.mrp.domain.service.DemandMerger;
import com.aimrp.mrp.domain.valueobject.MrpContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MRP 集成测试
 * 
 * 测试完整的 MRP 计算流程
 */
@SpringBootTest
public class MrpIntegrationTest {
    
    @Autowired
    private MrpApplicationService mrpApplicationService;
    
    @Autowired
    private BomExpander bomExpander;
    
    @Autowired
    private DemandMerger demandMerger;
    
    /**
     * 测试 BOM 展开功能
     */
    @Test
    public void testBomExpand() {
        // 准备 BOM 数据
        Map<String, List<MrpContext.BomLineVO>> bomMap = new HashMap<>();
        
        List<MrpContext.BomLineVO> a001Lines = new ArrayList<>();
        a001Lines.add(MrpContext.BomLineVO.builder()
                .bomId(1L)
                .parentItemCode("A001")
                .childItemCode("B001")
                .childItemName("部件B")
                .usageQty(new BigDecimal("2"))
                .lossRate(BigDecimal.ZERO)
                .level(1)
                .build());
        a001Lines.add(MrpContext.BomLineVO.builder()
                .bomId(1L)
                .parentItemCode("A001")
                .childItemCode("C001")
                .childItemName("物料C")
                .usageQty(new BigDecimal("5"))
                .lossRate(new BigDecimal("0.05"))
                .level(1)
                .build());
        bomMap.put("A001", a001Lines);
        
        List<MrpContext.BomLineVO> b001Lines = new ArrayList<>();
        b001Lines.add(MrpContext.BomLineVO.builder()
                .bomId(2L)
                .parentItemCode("B001")
                .childItemCode("C001")
                .childItemName("物料C")
                .usageQty(new BigDecimal("3"))
                .lossRate(BigDecimal.ZERO)
                .level(1)
                .build());
        bomMap.put("B001", b001Lines);
        
        // 执行展开
        List<BomExpander.BomExpandResult> results = bomExpander.expand(
                "A001", 
                new BigDecimal("100"), 
                bomMap, 
                5
        );
        
        // 验证
        assertNotNull(results);
        assertTrue(results.size() > 0);
        
        // 打印结果
        System.out.println("=== BOM 展开结果 ===");
        for (BomExpander.BomExpandResult result : results) {
            System.out.printf("物料: %s, 数量: %.2f, 层级: %d%n", 
                    result.getChildItemCode(), 
                    result.getRequiredQty().doubleValue(),
                    result.getLevel());
        }
        
        // 验证层级1
        List<BomExpander.BomExpandResult> level1 = results.stream()
                .filter(r -> r.getLevel() == 1)
                .toList();
        assertEquals(2, level1.size());
        
        // 验证层级2
        List<BomExpander.BomExpandResult> level2 = results.stream()
                .filter(r -> r.getLevel() == 2)
                .toList();
        assertTrue(level2.size() > 0);
    }
    
    /**
     * 测试需求合并功能
     */
    @Test
    public void testDemandMerger() {
        // 准备需求数据
        List<MrpContext.DemandVO> salesOrders = new ArrayList<>();
        salesOrders.add(MrpContext.DemandVO.builder()
                .demandId(1L)
                .demandType("ORDER")
                .itemCode("A001")
                .qty(new BigDecimal("100"))
                .dueDate(LocalDate.now().plusDays(30))
                .priority(5)
                .build());
        salesOrders.add(MrpContext.DemandVO.builder()
                .demandId(2L)
                .demandType("ORDER")
                .itemCode("A001")
                .qty(new BigDecimal("50"))
                .dueDate(LocalDate.now().plusDays(15))
                .priority(10)
                .build());
        
        List<MrpContext.DemandVO> forecasts = new ArrayList<>();
        forecasts.add(MrpContext.DemandVO.builder()
                .demandId(3L)
                .demandType("FORECAST")
                .itemCode("A001")
                .qty(new BigDecimal("200"))
                .dueDate(LocalDate.now().plusDays(60))
                .priority(3)
                .build());
        
        // 执行合并
        Map<String, List<MrpContext.DemandVO>> merged = demandMerger.merge(
                salesOrders, forecasts, null
        );
        
        // 验证
        assertNotNull(merged);
        assertTrue(merged.containsKey("A001"));
        
        List<MrpContext.DemandVO> a001Demands = merged.get("A001");
        assertEquals(3, a001Demands.size());
        
        // 验证优先级排序（高优先级在前）
        assertEquals(10, a001Demands.get(0).getPriority());
        
        System.out.println("=== 需求合并结果 ===");
        for (MrpContext.DemandVO demand : a001Demands) {
            System.out.printf("类型: %s, 数量: %.2f, 优先级: %d, 交期: %s%n",
                    demand.getDemandType(),
                    demand.getQty().doubleValue(),
                    demand.getPriority(),
                    demand.getDueDate());
        }
    }
    
    /**
     * 测试 MRP 完整流程
     */
    @Test
    public void testMrpFullFlow() {
        // 准备 MRP 运行参数
        MrpRun mrpRun = new MrpRun();
        mrpRun.setRunType("TEST");
        mrpRun.setPlanStartDate(LocalDate.now());
        mrpRun.setPlanEndDate(LocalDate.now().plusDays(90));
        
        // 执行 MRP
        MrpResult result = mrpApplicationService.runMrp(mrpRun);
        
        // 验证
        assertNotNull(result);
        assertNotNull(result.getRunId());
        
        // 打印结果
        System.out.println("=== MRP 计算结果 ===");
        System.out.println("运行ID: " + result.getRunId());
        System.out.println("状态: " + result.getStatus());
        
        if (result.getStatistics() != null) {
            System.out.println("计算物料数: " + result.getStatistics().getTotalItems());
            System.out.println("需求单数: " + result.getStatistics().getTotalDemands());
            System.out.println("采购建议: " + result.getStatistics().getPurchaseSuggestions());
            System.out.println("生产建议: " + result.getStatistics().getProductionSuggestions());
        }
        
        if (result.getSuggestions() != null) {
            System.out.println("建议列表:");
            result.getSuggestions().forEach(s -> 
                System.out.printf("  - %s: %s, 数量: %.2f, 需求日期: %s%n",
                        s.getSuggestionType(),
                        s.getItemCode(),
                        s.getSuggestQty().doubleValue(),
                        s.getNeedDate())
            );
        }
    }
    
    /**
     * 测试净需求计算
     */
    @Test
    public void testNetRequirementCalculation() {
        // 场景：需求100个A001，库存400个，安全库存100个
        
        BigDecimal demandQty = new BigDecimal("100");      // 需求
        BigDecimal onHandQty = new BigDecimal("400");       // 库存
        BigDecimal safetyStock = new BigDecimal("100");    // 安全库存
        BigDecimal allocatedQty = new BigDecimal("50");    // 已分配
        
        // 计算可用库存
        BigDecimal availableQty = onHandQty.subtract(allocatedQty);
        
        // 计算净需求
        BigDecimal netRequirement = demandQty.subtract(availableQty.subtract(safetyStock));
        if (netRequirement.compareTo(BigDecimal.ZERO) < 0) {
            netRequirement = BigDecimal.ZERO;
        }
        
        System.out.println("=== 净需求计算 ===");
        System.out.println("需求数量: " + demandQty);
        System.out.println("可用库存: " + availableQty);
        System.out.println("安全库存: " + safetyStock);
        System.out.println("净需求: " + netRequirement);
        
        // 验证
        // 可用库存 = 400 - 50 = 350
        assertEquals(new BigDecimal("350"), availableQty);
        // 净需求 = 100 - (350 - 100) = 100 - 250 = 0 (不需要采购)
        assertEquals(BigDecimal.ZERO, netRequirement);
        
        // 场景2：需求增加
        demandQty = new BigDecimal("500");
        netRequirement = demandQty.subtract(availableQty.subtract(safetyStock));
        if (netRequirement.compareTo(BigDecimal.ZERO) < 0) {
            netRequirement = BigDecimal.ZERO;
        }
        
        System.out.println("=== 场景2：需求增加 ===");
        System.out.println("需求数量: " + demandQty);
        System.out.println("净需求: " + netRequirement);
        
        // 净需求 = 500 - 250 = 250
        assertEquals(new BigDecimal("250"), netRequirement);
    }
}
