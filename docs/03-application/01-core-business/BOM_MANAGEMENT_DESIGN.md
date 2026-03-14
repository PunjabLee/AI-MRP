# AI MRP BOM管理模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-12

---

## 一、业务需求

### 1.1 功能概述

BOM（Bill of Material，物料清单）是MRP的核心基础，负责定义产品与物料之间的结构关系。

### 1.2 BOM类型

| 类型 | 说明 | 用途 |
|------|------|------|
| 工程BOM | 设计阶段的结构 | 设计参考 |
| 制造BOM | 生产使用的结构 | MRP计算 |
| 计划BOM | 虚拟BOM | 需求汇总 |
| 成本BOM | 成本核算用 | 成本分析 |

---

## 二、功能设计

### 2.1 BOM结构管理

```java
// BOM树结构
BOMHeader header = BOMHeader.builder()
    .itemId(productId)      // 父项物料
    .version("V1.0")        // 版本
    .effectiveDate(new Date())  // 生效日期
    .build();

// BOM明细
BOMLine line = BOMLine.builder()
    .componentId(componentId)  // 子件物料
    .quantity(2.0)              // 用量
    .scrapRate(0.05)           // 损耗率
    .sequence(1)                // 序号
    .build();
```

### 2.2 BOM展开

```java
// 单一层级展开
List<BOMLine> expand(Long itemId, int level);

// 多层级递归展开
List<BOMExpansionResult> expandFull(Long itemId, int maxLevel) {
    List<BOMExpansionResult> result = new ArrayList<>();
    expandRecursive(itemId, 1, BigDecimal.ONE, result);
    return result;
}
```

### 2.3 反向查询

```java
// 查找父项 (Where-Used)
List<WhereUsedResult> whereUsed(Long componentId) {
    return bomRepository.findParents(componentId);
}
```

---

## 三、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/bom/create` | POST | 创建BOM |
| `/api/bom/update` | PUT | 更新BOM |
| `/api/bom/expand` | POST | 展开BOM |
| `/api/bom/where-used` | GET | 反向查询 |
| `/api/bom/version` | POST | 版本管理 |

---

## 四、数据模型

```sql
-- BOM头表
CREATE TABLE t_bom_header (
    id BIGINT PRIMARY KEY,
    item_id BIGINT NOT NULL,      -- 父项物料ID
    version VARCHAR(20),          -- 版本
    bom_type VARCHAR(20),        -- BOM类型
    effective_date DATE,         -- 生效日期
    status VARCHAR(20),          -- DRAFT/ACTIVE/OBSOLETE
    create_time DATETIME
);

-- BOM明细表
CREATE TABLE t_bom_line (
    id BIGINT PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    line_no INT,                 -- 行号
    component_id BIGINT NOT NULL,-- 子件物料ID
    quantity DECIMAL(18,6),     -- 用量
    unit_id BIGINT,              -- 单位
    scrap_rate DECIMAL(5,4),     -- 损耗率
    sequence INT,                -- 工序序号
    is_optional BOOLEAN          -- 是否可选
);
```

---

## 五、MRP集成

```java
// MRP计算时获取BOM
public List<BOMLine> getBOMForMRP(Long itemId, Date date) {
    return bomRepository.findEffectiveBOM(itemId, date);
}
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
