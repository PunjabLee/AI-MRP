package com.aimrp.conversation.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 决策可解释性服务
 * 为AI生成的决策提供可理解的解释
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionExplanationService {

    /**
     * 生成决策解释
     *
     * @param decision AI生成的决策
     * @param context 决策上下文
     * @return 解释结果
     */
    public ExplanationResult explainDecision(AI decision, Map<String, Object> context) {
        log.info("生成决策解释，决策类型: {}", decision.getType());

        // 1. 分析决策因素
        Map<String, Object> factors = analyzeFactors(decision, context);

        // 2. 生成解释文本
        String explanation = generateExplanation(decision, factors);

        // 3. 计算置信度
        double confidence = calculateConfidence(decision, factors);

        // 4. 识别风险
        Map<String, String> risks = identifyRisks(decision, factors);

        return ExplanationResult.builder()
                .decision(decision)
                .factors(factors)
                .explanation(explanation)
                .confidence(confidence)
                .risks(risks)
                .build();
    }

    /**
     * 分析决策因素
     */
    private Map<String, Object> analyzeFactors(AI decision, Map<String, Object> context) {
        Map<String, Object> factors = new HashMap<>();

        // 库存因素
        if (context.containsKey("inventory")) {
            factors.put("inventory", context.get("inventory"));
        }

        // 需求因素
        if (context.containsKey("demand")) {
            factors.put("demand", context.get("demand"));
        }

        // 供应商因素
        if (context.containsKey("supplier")) {
            factors.put("supplier", context.get("supplier"));
        }

        // 时间因素
        if (context.containsKey("leadTime")) {
            factors.put("leadTime", context.get("leadTime"));
        }

        // 成本因素
        if (context.containsKey("cost")) {
            factors.put("cost", context.get("cost"));
        }

        return factors;
    }

    /**
     * 生成解释文本
     */
    private String generateExplanation(AI decision, Map<String, Object> factors) {
        StringBuilder sb = new StringBuilder();

        switch (decision.getType()) {
            case "PURCHASE_SUGGESTION":
                sb.append("基于以下分析，建议生成采购订单：\n");
                if (factors.containsKey("demand")) {
                    sb.append("1. 需求分析：当前需求量为 ").append(factors.get("demand")).append("\n");
                }
                if (factors.containsKey("inventory")) {
                    sb.append("2. 库存分析：当前库存为 ").append(factors.get("inventory")).append("\n");
                }
                if (factors.containsKey("leadTime")) {
                    sb.append("3. 交货期分析：供应商交货期为 ").append(factors.get("leadTime")).append(" 天\n");
                }
                break;

            case "PRODUCTION_SUGGESTION":
                sb.append("基于以下分析，建议生成生产订单：\n");
                if (factors.containsKey("demand")) {
                    sb.append("1. 需求分析：预测需求为 ").append(factors.get("demand")).append("\n");
                }
                if (factors.containsKey("capacity")) {
                    sb.append("2. 产能分析：可用产能为 ").append(factors.get("capacity")).append("\n");
                }
                break;

            case "RISK_ALERT":
                sb.append("检测到以下风险：\n");
                if (factors.containsKey("riskLevel")) {
                    sb.append("风险等级：").append(factors.get("riskLevel")).append("\n");
                }
                break;

            default:
                sb.append("AI根据综合分析做出了该决策。");
        }

        return sb.toString();
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(AI decision, Map<String, Object> factors) {
        // 基于数据完整性计算置信度
        double baseConfidence = 0.7;

        if (factors.containsKey("inventory")) baseConfidence += 0.1;
        if (factors.containsKey("demand")) baseConfidence += 0.1;
        if (factors.containsKey("cost")) baseConfidence += 0.05;

        return Math.min(baseConfidence, 0.95);
    }

    /**
     * 识别风险
     */
    private Map<String, String> identifyRisks(AI decision, Map<String, Object> factors) {
        Map<String, String> risks = new HashMap<>();

        // 库存风险
        Object inventory = factors.get("inventory");
        if (inventory instanceof Number && ((Number) inventory).doubleValue() < 100) {
            risks.put("库存风险", "库存低于安全库存");
        }

        // 交货期风险
        Object leadTime = factors.get("leadTime");
        if (leadTime instanceof Number && ((Number) leadTime).intValue() > 30) {
            risks.put("交货期风险", "交货期较长，可能影响生产");
        }

        return risks;
    }

    // ===== 内部类 =====

    @lombok.Data
    @lombok.Builder
    public static class ExplanationResult {
        private AI decision;
        private Map<String, Object> factors;
        private String explanation;
        private double confidence;
        private Map<String, String> risks;
    }

    @lombok.Data
    @lombok.Builder
    public static class AI {
        private String type;
        private String content;
        private Map<String, Object> parameters;
    }
}
