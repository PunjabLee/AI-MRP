# Controller参数类型Review报告

> **日期**：2026-03-10  
> **问题**：Controller参数未使用VO类型 + 缺少@Validated校验

---

## 一、问题分析

### 1.1 当前问题

| 问题类型 | 描述 |
|----------|------|
| 使用Entity代替Request | @RequestBody直接接收Entity |
| 使用Map代替VO | @RequestBody使用Map<String, Object> |
| 缺少参数校验 | 缺少@Validated注解 |
| 缺少Valid注解 | @RequestBody参数缺少@Valid |

### 1.2 正确示例

```java
// ❌ 错误
@PostMapping
public void create(@RequestBody SalesOrder order) { }

// ✅ 正确
@PostMapping
public ApiResponse<Void> create(@Validated @RequestBody SalesOrderRequest request) { }
```

---

## 二、按模块问题清单

### 2.1 demand (销售订单)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| SalesOrderController | @RequestBody Entity | 创建Request/Response VO |

### 2.2 item (物料主数据)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| ItemController | @RequestBody Entity | 创建Request/Response VO |

### 2.3 supplier (供应商)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| SupplierController | @RequestBody Entity | 创建Request/Response VO |

### 2.4 bom (BOM)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| BomController | @RequestBody Entity | 创建Request/Response VO |

### 2.5 inventory (库存)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| InventoryController | @RequestBody Map | 创建Request/Response VO |
| InventoryTransferController | @RequestBody Entity | 创建Request/Response VO |
| InventoryCheckController | @RequestBody Entity | 创建Request/Response VO |
| InventoryAnalysisController | - | 较完善 |

### 2.6 purchase (采购)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| PurchaseOrderController | @RequestBody Entity | 创建Request/Response VO |

### 2.7 production (生产)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| ProductionOrderController | @RequestBody Entity | 创建Request/Response VO |

### 2.8 mrp (MRP)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| MrpController | @RequestBody Map | 创建Request/Response VO |
| CostAnalysisController | @RequestBody Entity | 创建Request/Response VO |

### 2.9 warehouse (仓库)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| WarehouseController | @RequestBody Entity | 创建Request/Response VO |
| WarehouseLocationController | @RequestBody Entity | 创建Request/Response VO |

### 2.10 equipment (设备)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| EquipmentController | @RequestBody Entity | 创建Request/Response VO |

### 2.11 quality (质量)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| QualityController | @RequestBody Entity | 创建Request/Response VO |

### 2.12 cost (成本)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| CostController | @RequestBody Entity | 创建Request/Response VO |

### 2.13 org (组织)

| Controller | 问题 | 修复计划 |
|------------|------|----------|
| OrganizationController | @RequestBody Entity | 创建Request/Response VO |

---

## 三、修复计划

### 阶段一：MVP核心模块 (Day 1)

1. **demand** - 销售订单
2. **item** - 物料主数据
3. **supplier** - 供应商
4. **bom** - BOM管理
5. **inventory** - 库存管理

### 阶段二：业务模块 (Day 2)

6. **purchase** - 采购管理
7. **production** - 生产管理
8. **mrp** - MRP计算
9. **warehouse** - 仓库管理

### 阶段三：Enterprise模块 (Day 3)

10. **equipment** - 设备管理
11. **quality** - 质量管理
12. **cost** - 成本管理
13. **org** - 组织管理

---

## 四、VO规范

### 4.1 Request VO命名规范

```
模块名 + 功能名 + Request
例：SalesOrderCreateRequest, ItemQueryRequest
```

### 4.2 Response VO命名规范

```
模块名 + 功能名 + Response /VO
例：SalesOrderResponse, ItemListVO
```

### 4.3 校验注解使用

```java
@NotNull    // 不能为空
@NotBlank   // 不能为空字符串
@Size       // 长度限制
@Min/@Max   // 数值范围
@Pattern    // 正则表达式
@Email      // 邮箱格式
@Date       // 日期格式
```

---

*Review完成 - 2026-03-10*
