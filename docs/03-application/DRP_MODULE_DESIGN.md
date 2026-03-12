# AI MRP DRP 配送需求计划模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-11

---

## 一、业务需求

### 1.1 核心功能

| 功能 | 说明 |
|------|------|
| 配送网络配置 | 仓库/配送中心/门店关系 |
| 需求汇总 | 多门店需求汇总 |
| DRP运算 | 配送需求计划计算 |
| 运输管理 | 运输计划/在途跟踪 |
| 配送成本 | 运输成本优化 |

### 1.2 业务场景

```
┌─────────────────────────────────────────────────────────────────┐
│                     DRP 业务流程                              │
└─────────────────────────────────────────────────────────────┘

  门店A ──┐
  门店B ──┼──→ 需求汇总 ──→ DRP运算 ──→ 配送计划 ──→ 仓库发货
  门店C ──┘     (多门店)    (优化分配)   (生成配送单)   (执行配送)

  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │ 区域配送中心  │ ──→ │  干线运输   │ ──→ │  门店仓库   │
  └──────────────┘     └──────────────┘     └──────────────┘
```

---

## 二、技术架构

### 2.1 模块结构

```
aimrp-drp/
├── src/main/java/com/aimrp/dr/
│   ├── controller/
│   │   ├── DistributionNetworkController.java   # 配送网络
│   │   ├── DemandController.java              # 需求管理
│   │   ├── DrpController.java                 # DRP运算
│   │   └── TransportController.java            # 运输管理
│   ├── service/
│   │   ├── NetworkService.java
│   │   ├── DemandService.java
│   │   ├── DrpCalculatorService.java
│   │   └── TransportService.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── DistributionNetwork.java
│   │   │   ├── Demand.java
│   │   │   ├── DistributionPlan.java
│   │   │   └── TransportRecord.java
│   │   └── repository/
│   └── dto/
│       ├── DrpRequest.java
│       └── DrpResult.java
```

### 2.2 数据库设计

```sql
-- 配送网络表
CREATE TABLE t_distribution_network (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    network_code VARCHAR(50) NOT NULL,
    network_name VARCHAR(100),
    network_type VARCHAR(20),  -- REGION/CITY/STORE
    parent_id BIGINT,          -- 上级网络ID
    warehouse_id BIGINT,       -- 关联仓库ID
    address VARCHAR(200),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME
);

-- 配送需求表
CREATE TABLE t_drp_demand (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_no VARCHAR(50) NOT NULL,
    network_id BIGINT NOT NULL,     -- 门店ID
    item_id BIGINT NOT NULL,       -- 物料ID
    demand_qty DECIMAL(18,3),     -- 需求数量
    demand_date DATE,             -- 需求日期
    priority INT DEFAULT 5,         -- 优先级
    status VARCHAR(20),          -- PENDING/ALLOCATED/SHIPPED
    source_type VARCHAR(20),      -- FORECAST/ORDER/MANUAL
    create_time DATETIME,
    update_time DATETIME
);

-- 配送计划表
CREATE TABLE t_distribution_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_no VARCHAR(50) NOT NULL,
    network_id BIGINT,            -- 目的门店
    warehouse_id BIGINT,          -- 配送仓库
    item_id BIGINT,
    plan_qty DECIMAL(18,3),
    allocate_qty DECIMAL(18,3),  -- 已配货数量
    ship_qty DECIMAL(18,3),     -- 已发货数量
    plan_date DATE,
    status VARCHAR(20),          -- PENDING/ALLOCATED/SHIPPED/CANCELLED
    create_time DATETIME,
    update_time DATETIME
);

-- 运输记录表
CREATE TABLE t_transport_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transport_no VARCHAR(50) NOT NULL,
    plan_ids VARCHAR(500),        -- 关联配送计划IDs
    warehouse_id BIGINT,
    vehicle_no VARCHAR(50),       -- 车牌号
    driver_name VARCHAR(50),
    driver_phone VARCHAR(20),
    depart_time DATETIME,
    estimate_arrival DATETIME,
    actual_arrival DATETIME,
    status VARCHAR(20),          -- IN_TRANSIT/ARRIVED/CANCELLED
    create_time DATETIME,
    update_time DATETIME
);
```

---

## 三、核心功能

### 3.1 配送网络管理

```java
@Service
@RequiredArgsConstructor
public class NetworkService {

    /**
     * 创建配送网络
     */
    public DistributionNetwork createNetwork(DistributionNetworkRequest request) {
        DistributionNetwork network = DistributionNetwork.builder()
            .networkCode(generateCode())
            .networkName(request.getNetworkName())
            .networkType(request.getNetworkType())
            .parentId(request.getParentId())
            .warehouseId(request.getWarehouseId())
            .build();
        
        return save(network);
    }

    /**
     * 获取配送网络树
     */
    public List<NetworkTreeNode> getNetworkTree() {
        List<DistributionNetwork> all = findAll();
        return buildTree(all, null);  // 递归构建树
    }
}
```

### 3.2 DRP 运算

```java
@Service
@RequiredArgsConstructor
public class DrpCalculatorService {

    /**
     * DRP运算核心算法
     * 
     * 输入: 各门店需求
     * 输出: 配送计划
     */
    public DrpResult calculate(DrpRequest request) {
        // 1. 汇总需求
        Map<Long, BigDecimal> demandSummary = summarizeDemand(request);
        
        // 2. 获取各仓库库存
        Map<Long, BigDecimal> warehouseStock = getWarehouseStock(request.getItemIds());
        
        // 3. 分配逻辑 (基于仓库覆盖+库存+成本)
        List<DistributionPlan> plans = allocate(demandSummary, warehouseStock, request.getStrategy());
        
        // 4. 生成配送计划
        List<DistributionPlan> result = generatePlans(plans);
        
        return DrpResult.builder()
            .plans(result)
            .totalShipQty(calculateTotalShipQty(result))
            .totalCost(calculateCost(result))
            .build();
    }

    /**
     * 分配策略
     */
    private List<DistributionPlan> allocate(
            Map<Long, BigDecimal> demands,
            Map<Long, BigDecimal> stocks,
            AllocateStrategy strategy) {
        
        List<DistributionPlan> plans = new ArrayList<>();
        
        switch (strategy) {
            case NEAREST:    // 就近原则
                plans = allocateByDistance(demands, stocks);
                break;
            case LOWEST_COST:  // 最低成本
                plans = allocateByCost(demands, stocks);
                break;
            case BALANCED:    // 平衡策略
                plans = allocateBalanced(demands, stocks);
                break;
        }
        
        return plans;
    }

    /**
     * 最低成本分配算法
     */
    private List<DistributionPlan> allocateByCost(
            Map<Long, BigDecimal> demands,
            Map<Long, BigDecimal> stocks) {
        
        // 按运输成本排序仓库
        List<Warehouse> warehouses = getWarehouses()
            .stream()
            .sorted(Comparator.comparing(Warehouse::getTransportCost))
            .collect(Collectors.toList());
        
        List<DistributionPlan> plans = new ArrayList<>();
        
        // 贪婪分配
        for (Long networkId : demands.keySet()) {
            BigDecimal remaining = demands.get(networkId);
            
            for (Warehouse wh : warehouses) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                
                BigDecimal available = stocks.getOrDefault(wh.getId(), BigDecimal.ZERO);
                BigDecimal allocateQty = remaining.min(available);
                
                if (allocateQty.compareTo(BigDecimal.ZERO) > 0) {
                    plans.add(createPlan(networkId, wh.getId(), allocateQty));
                    remaining = remaining.subtract(allocateQty);
                }
            }
        }
        
        return plans;
    }
}
```

### 3.3 运输管理

```java
@Service
@RequiredArgsConstructor
public class TransportService {

    /**
     * 创建配送单
     */
    public TransportRecord createTransport(TransportRequest request) {
        // 1. 校验库存
        validateStock(request.getPlans());
        
        // 2. 锁定库存
        lockStock(request.getPlans());
        
        // 3. 创建运输记录
        TransportRecord record = TransportRecord.builder()
            .transportNo(generateTransportNo())
            .planIds(joinPlanIds(request.getPlans()))
            .warehouseId(request.getWarehouseId())
            .vehicleNo(request.getVehicleNo())
            .driverName(request.getDriverName())
            .driverPhone(request.getDriverPhone())
            .departTime(request.getDepartTime())
            .estimateArrival(calculateEstimateArrival(request))
            .status("IN_TRANSIT")
            .build();
        
        return save(record);
    }

    /**
     * 运输跟踪
     */
    public TransportTrackResult track(String transportNo) {
        TransportRecord record = findByNo(transportNo);
        
        // 获取在途位置 (可对接GPS)
        Location location = getGpsLocation(record.getVehicleNo());
        
        return TransportTrackResult.builder()
            .transportNo(transportNo)
            .currentLocation(location)
            .estimatedArrival(record.getEstimateArrival())
            .actualArrival(record.getActualArrival())
            .status(record.getStatus())
            .build();
    }
}
```

---

## 四、API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/dr/network/create` | POST | 创建配送网络 |
| `/api/dr/network/tree` | GET | 获取网络结构 |
| `/api/dr/demand/sync` | POST | 同步需求 |
| `/api/dr/calculate` | POST | DRP运算 |
| `/api/dr/plan/approve` | POST | 审核配送计划 |
| `/api/dr/transport/create` | POST | 创建配送单 |
| `/api/dr/transport/track` | GET | 运输跟踪 |

---

## 五、实施计划

| 周次 | 内容 |
|------|------|
| 第1周 | 配送网络、需求管理 |
| 第2周 | DRP运算核心算法 |
| 第3周 | 运输管理、在途跟踪 |
| 第4周 | 成本分析、测试优化 |

**预估工时**: 80小时

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
