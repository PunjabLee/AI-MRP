# AI MRP MRP计算模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

MRP（Material Requirements Planning）计算是系统的核心引擎，根据需求和BOM展开计算物料需求。

### 1.2 MRP类型

| 类型 | 说明 |
|------|------|
| 再生MRP | 重新计算全部需求 |
| 净改变MRP | 只计算变更部分 |
| 时段MRP | 按时间段计算 |

---

## 二、MRP计算流程

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  需求汇总   │───▶│  BOM展开   │───▶│  库存匹配  │───▶│  建议生成  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

---

## 三、核心算法

### 3.1 MRP计算逻辑

```java
public MrpResult calculate(MrpRequest request) {
    List<MrpResult> results = new ArrayList<>();
    
    // 1. 获取需求
    List<Demand> demands = demandService.getDemands(request);
    
    for (Demand demand : demands) {
        // 2. 展开BOM
        List<BOMLine> bomLines = bomService.expand(demand.getItemId());
        
        for (BOMLine line : bomLines) {
            // 3. 计算毛需求
            BigDecimal grossNeed = line.getQuantity()
                .multiply(demand.getQuantity())
                .multiply(BigDecimal.ONE.add(line.getScrapRate()));
            
            // 4. 减去可用库存
            BigDecimal onHand = inventoryService.getOnHand(line.getComponentId());
            BigDecimal netNeed = grossNeed.subtract(onHand);
            
            // 5. 考虑在途订单
            BigDecimal onOrder = purchaseOrderService.getOnOrder(line.getComponentId());
            netNeed = netNeed.subtract(onOrder);
            
            // 6. 生成建议
            if (netNeed.compareTo(BigDecimal.ZERO) > 0) {
                results.add(createSuggestion(line.getComponentId(), netNeed, request));
            }
        }
    }
    
    return MrpResult.builder()
        .suggestions(results)
        .build();
}
```

### 3.2 批量规则

```java
// 批量策略
public BigDecimal applyLotSize(BigDecimal netNeed, LotSizeRule rule) {
    switch (rule) {
        case FIXED_LOT:     // 固定批量
            return roundToFixedLot(netNeed, lotSize);
        case LEAST_LOT:     // 最小批量
            return netNeed.compareTo(minLot) < 0 ? minLot : netNeed;
        case ROUND_UP:      // 向上取整
            return netNeed.divide(lotSize, 0, RoundingMode.UP).multiply(lotSize);
        default:            // 刚好满足
            return netNeed;
    }
}
```

---

## 四、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/mrp/calculate` | POST | 执行MRP计算 |
| `/api/mrp/regenerate` | POST | 再生MRP |
| `/api/mrp/incremental` | POST | 增量MRP |
| `/api/mrp/suggestions` | GET | 获取建议 |
| `/api/mrp/trace` | GET | 需求追溯 |

---

## 五、MRP建议

```java
// 采购建议
public class PurchaseSuggestion {
    private Long itemId;
    private BigDecimal quantity;
    private Date requiredDate;
    private String supplierId;
    private SuggestionType type;  // NEW/CHANGE/CANCEL
}

// 生产建议
public class ProductionSuggestion {
    private Long itemId;
    private BigDecimal quantity;
    private Date requiredDate;
    private Date plannedDate;
}
```

---

## 六、数据模型

```sql
-- MRP计算结果
CREATE TABLE t_mrp_suggestion (
    id BIGINT PRIMARY KEY,
    suggestion_no VARCHAR(50),
    item_id BIGINT,
    suggestion_type VARCHAR(20),  -- PURCHASE/PRODUCTION
    quantity DECIMAL(18,3),
    required_date DATE,
    status VARCHAR(20),         -- NEW/APPROVED/RELEASED/CANCELLED
    source_type VARCHAR(20),    -- 来源类型
    source_id BIGINT,           -- 来源ID
    create_time DATETIME
);
```

---

## 七、配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| 批量规则 | Lot Size | LEAST |
| 提前期 | Lead Time | 0 |
| 安全库存 | Safety Stock | 0 |
| 最小起订量 | MOQ | 1 |

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
