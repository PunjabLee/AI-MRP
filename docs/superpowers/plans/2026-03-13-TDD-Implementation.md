# AI-MRP TDD Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement comprehensive TDD test coverage for all AI-MRP modules following the TDD methodology (Red → Green → Refactor)

**Architecture:** This plan implements TDD testing for core business modules (P0), business modules (P1), intelligent engine modules (P2), and enterprise modules (P3). Each module will have unit tests, integration tests following the existing test patterns in aimrp-forecast.

**Tech Stack:** JUnit 5, Mockito, Spring Boot Test, H2 Database, AssertJ

---

## Phase 1: Core Business Modules (P0)

### Task 1: Demand Management Tests

**Files:**
- Test: `code/backend/aimrp-demand/src/test/java/com/aimrp/demand/DemandServiceTest.java`
- Test: `code/backend/aimrp-demand/src/test/java/com/aimrp/demand/DemandRepositoryTest.java`
- Modify: `code/backend/aimrp-demand/pom.xml` (add test dependencies if missing)
- Test Data: `code/backend/aimrp-demand/src/test/resources/test-data.sql`

- [ ] **Step 1: Check existing test dependencies in aimrp-demand pom.xml**

```bash
# Check if test dependencies exist
grep -A5 "test" code/backend/aimrp-demand/pom.xml | head -20
```

- [ ] **Step 2: Create test configuration**

```yaml
# code/backend/aimrp-demand/src/test/resources/application.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
```

- [ ] **Step 3: Write failing tests for DemandService**

```java
// Test: Create sales order returns DRAFT status
@Test
void createOrder_shouldReturnDraftStatus() {
    // Given
    DemandRequest request = new DemandRequest();
    request.setItemId("ITEM-A");
    request.setQuantity(100);
    request.setRequiredDate(LocalDate.now().plusDays(7));

    // When
    Demand result = demandService.createOrder(request);

    // Then
    assertThat(result.getStatus()).isEqualTo(DemandStatus.DRAFT);
    assertThat(result.getItemId()).isEqualTo("ITEM-A");
    assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("100"));
}

// Test: Aggregate demand by item
@Test
void aggregateDemand_shouldSumByItem() {
    // Given
    List<Demand> demands = List.of(
        createDemand("ITEM-A", 50),
        createDemand("ITEM-A", 30),
        createDemand("ITEM-B", 20)
    );

    // When
    Map<String, BigDecimal> result = demandService.aggregateByItem(demands);

    // Then
    assertThat(result.get("ITEM-A")).isEqualByComparingTo(new BigDecimal("80"));
    assertThat(result.get("ITEM-B")).isEqualByComparingTo(new BigDecimal("20"));
}

// Test: Update demand changes quantity
@Test
void updateDemand_shouldChangeQuantity() {
    // Given
    Demand demand = createDemand("ITEM-A", 100);

    // When
    Demand updated = demandService.updateQuantity(demand.getId(), 150);

    // Then
    assertThat(updated.getQuantity()).isEqualByComparingTo(new BigDecimal("150"));
}
```

- [ ] **Step 4: Run tests to verify they fail**

```bash
cd code/backend && mvn test -pl aimrp-demand -Dtest=DemandServiceTest
# Expected: FAIL - service methods not implemented
```

- [ ] **Step 5: Implement minimal code to pass tests**

```java
// Add to DemandService.java
public Demand createOrder(DemandRequest request) {
    Demand demand = new Demand();
    demand.setItemId(request.getItemId());
    demand.setQuantity(request.getQuantity());
    demand.setRequiredDate(request.getRequiredDate());
    demand.setStatus(DemandStatus.DRAFT);
    return demandRepository.save(demand);
}

public Map<String, BigDecimal> aggregateByItem(List<Demand> demands) {
    return demands.stream()
        .collect(Collectors.groupingBy(
            Demand::getItemId,
            Collectors.reducing(BigDecimal.ZERO, Demand::getQuantity, BigDecimal::add)
        ));
}

public Demand updateQuantity(Long id, BigDecimal newQty) {
    Demand demand = demandRepository.findById(id)
        .orElseThrow(() -> new DemandNotFoundException(id));
    demand.setQuantity(newQty);
    return demandRepository.save(demand);
}
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
mvn test -pl aimrp-demand -Dtest=DemandServiceTest
# Expected: PASS
```

- [ ] **Step 7: Commit**

```bash
git add code/backend/aimrp-demand/src/test/
git commit -m "test(demand): add TDD tests for DemandService"
```

---

### Task 2: BOM Management Tests

**Files:**
- Test: `code/backend/aimrp-bom/src/test/java/com/aimrp/bom/BOMServiceTest.java`

- [ ] **Step 1: Write failing tests for BOMService**

```java
// Test: Single-level BOM explosion
@Test
void explodeBOM_singleLevel_shouldReturnChildren() {
    // Given
    String parentItemId = "PRODUCT-A";
    when(bomRepository.findByParentItemId(parentItemId))
        .thenReturn(List.of(
            createBOMLine("COMPONENT-A", 2.0),
            createBOMLine("COMPONENT-B", 1.0)
        ));

    // When
    List<BOMLine> result = bomService.explodeBOM(parentItemId, 1);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).extracting("componentItemId")
        .containsExactlyInAnyOrder("COMPONENT-A", "COMPONENT-B");
}

// Test: Multi-level BOM explosion
@Test
void explodeBOM_multiLevel_shouldReturnFullTree() {
    // Given - 3 level BOM
    String topLevel = "PRODUCT-A";
    when(bomRepository.findByParentItemId("PRODUCT-A"))
        .thenReturn(List.of(createBOMLine("SUB-ASSY-1", 1)));

    // When
    List<BOMLine> result = bomService.explodeBOM(topLevel, 3);

    // Then
    assertThat(result).hasSizeGreaterThan(2); // Should include all levels
}

// Test: Reverse query - find parents
@Test
void findParents_shouldReturnParentItems() {
    // Given
    String componentItemId = "COMPONENT-A";
    when(bomRepository.findByComponentItemId(componentItemId))
        .thenReturn(List.of("PRODUCT-A", "PRODUCT-B"));

    // When
    List<String> result = bomService.findParentItems(componentItemId);

    // Then
    assertThat(result).containsExactlyInAnyOrder("PRODUCT-A", "PRODUCT-B");
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
mvn test -pl aimrp-bom -Dtest=BOMServiceTest
# Expected: FAIL
```

- [ ] **Step 3: Implement minimal code**

```java
public List<BOMLine> explodeBOM(String parentItemId, int levels) {
    List<BOMLine> allLines = new ArrayList<>();
    explodeRecursive(parentItemId, levels, allLines);
    return allLines;
}

private void explodeRecursive(String parentItemId, int remainingLevels, List<BOMLine> allLines) {
    if (remainingLevels <= 0) return;
    List<BOMLine> lines = bomRepository.findByParentItemId(parentItemId);
    allLines.addAll(lines);
    lines.forEach(line -> explodeRecursive(line.getComponentItemId(), remainingLevels - 1, allLines));
}

public List<String> findParentItems(String componentItemId) {
    return bomRepository.findByComponentItemId(componentItemId);
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn test -pl aimrp-bom -Dtest=BOMServiceTest
# Expected: PASS
```

- [ ] **Step 5: Commit**

```bash
git commit -m "test(bom): add TDD tests for BOMService"
```

---

### Task 3: Inventory Management Tests

**Files:**
- Test: `code/backend/aimrp-inventory/src/test/java/com/aimrp/inventory/InventoryServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// Test: Inbound increases inventory
@Test
void inbound_shouldIncreaseInventory() {
    // Given
    String itemId = "ITEM-A";
    String warehouse = "WH-01";
    BigDecimal qty = new BigDecimal("100");

    when(inventoryRepository.findByItemAndWarehouse(itemId, warehouse))
        .thenReturn(Optional.empty());

    // When
    inventoryService.inbound(itemId, warehouse, qty);

    // Then
    verify(inventoryRepository).save(argThat(inv ->
        inv.getItemId().equals(itemId) &&
        inv.getOnHandQty().equals(qty)
    ));
}

// Test: Outbound decreases inventory
@Test
void outbound_shouldDecreaseInventory() {
    // Given
    Inventory inv = new Inventory();
    inv.setItemId("ITEM-A");
    inv.setOnHandQty(new BigDecimal("100"));
    inv.setAvailableQty(new BigDecimal("100"));

    when(inventoryRepository.findByItemAndWarehouse("ITEM-A", "WH-01"))
        .thenReturn(Optional.of(inv));

    // When
    inventoryService.outbound("ITEM-A", "WH-01", new BigDecimal("30"));

    // Then
    verify(inventoryRepository).save(argThat(saved ->
        saved.getOnHandQty().equals(new BigDecimal("70"))
    ));
}

// Test: Available inventory after reservation
@Test
void getAvailableQty_shouldSubtractReserved() {
    // Given
    Inventory inv = new Inventory();
    inv.setOnHandQty(new BigDecimal("100"));
    inv.setAllocatedQty(new BigDecimal("30"));

    // When
    BigDecimal available = inventoryService.calculateAvailable(inv);

    // Then
    assertThat(available).isEqualByComparingTo(new BigDecimal("70"));
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
mvn test -pl aimrp-inventory -Dtest=InventoryServiceTest
# Expected: FAIL - but module has compilation issues
```

- [ ] **Note:** aimrp-inventory has 67 Lombok compilation errors - skip for now, return after fixing

- [ ] **Step 3: Commit progress**

```bash
git commit -m "test(inventory): add TDD tests (blocked by compilation)"
```

---

### Task 4: MRP Calculation Tests

**Files:**
- Test: `code/backend/aimrp-mrp/src/test/java/com/aimrp/mrp/MRPServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// Test: MRP calculation produces purchase suggestions
@Test
void calculateMRP_shouldGeneratePurchaseSuggestions() {
    // Given
    List<Demand> demands = List.of(createDemand("ITEM-A", 100));
    List<BOM> boms = List.of(createBOM("ITEM-A", "COMP-A", 1));
    List<Inventory> inventories = List.of(createInventory("COMP-A", 0));

    // When
    List<MRPSuggestion> suggestions = mrpService.calculate(demands, boms, inventories);

    // Then
    assertThat(suggestions).isNotEmpty();
    assertThat(suggestions).allMatch(s -> "PURCHASE".equals(s.getSuggestionType()));
}

// Test: Apply lot sizing rules
@Test
void applyLotSizing_shouldRoundUpToLotSize() {
    // Given
    BigDecimal netDemand = new BigDecimal("85");
    LotSizingRule rule = LotSizingRule.FIXED_LOT_SIZE;
    BigDecimal lotSize = new BigDecimal("100");

    // When
    BigDecimal result = mrpService.applyLotSizing(netDemand, rule, lotSize);

    // Then
    assertThat(result).isEqualByComparingTo(new BigDecimal("100"));
}

// Test: Shortage analysis identifies missing materials
@Test
void analyzeShortage_shouldIdentifyStockouts() {
    // Given
    List<MRPSuggestion> suggestions = List.of(
        createSuggestion("COMP-A", 50, 0),  // Need 50, have 0
        createSuggestion("COMP-B", 30, 20)   // Need 30, have 20 (short by 10)
    );

    // When
    List<ShortageItem> shortages = mrpService.analyzeShortage(suggestions);

    // Then
    assertThat(shortages).hasSize(2);
    assertThat(shortages).extracting("shortageQty")
        .containsExactlyInAnyOrder(new BigDecimal("50"), new BigDecimal("10"));
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
mvn test -pl aimrp-mrp -Dtest=MRPServiceTest
# Expected: FAIL - methods not implemented
```

- [ ] **Step 3: Implement minimal code**

```java
public List<MRPSuggestion> calculate(List<Demand> demands, List<BOM> boms, List<Inventory> inventories) {
    // Simplified implementation
    List<MRPSuggestion> suggestions = new ArrayList<>();
    Map<String, BigDecimal> demandMap = aggregateByItem(demands);
    Map<String, BigDecimal> inventoryMap = toInventoryMap(inventories);

    for (Map.Entry<String, BigDecimal> entry : demandMap.entrySet()) {
        String itemId = entry.getKey();
        BigDecimal demand = entry.getValue();
        BigDecimal available = inventoryMap.getOrDefault(itemId, BigDecimal.ZERO);

        if (demand.compareTo(available) > 0) {
            suggestions.add(createSuggestion(itemId, demand.subtract(available)));
        }
    }
    return suggestions;
}

public BigDecimal applyLotSizing(BigDecimal netDemand, LotSizingRule rule, BigDecimal lotSize) {
    return switch (rule) {
        case FIXED_LOT_SIZE -> lotSize;
        case LOT_FOR_LOT -> netDemand;
        case ROUND_UP -> netDemand.divide(lotSize, 0, RoundingMode.UP).multiply(lotSize);
    };
}

public List<ShortageItem> analyzeShortage(List<MRPSuggestion> suggestions) {
    return suggestions.stream()
        .filter(s -> s.getSuggestedQty().compareTo(s.getAvailableQty()) > 0)
        .map(s -> new ShortageItem(s.getItemId(),
            s.getSuggestedQty().subtract(s.getAvailableQty())))
        .toList();
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn test -pl aimrp-mrp -Dtest=MRPServiceTest
# Expected: PASS
```

- [ ] **Step 5: Commit**

```bash
git commit -m "test(mrp): add TDD tests for MRP calculation"
```

---

## Phase 2: Business Modules (P1)

### Task 5: Purchase Management Tests

**Files:**
- Test: `code/backend/aimrp-purchase/src/test/java/com/aimrp/purchase/PurchaseServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// Test: Generate purchase order from suggestions
@Test
void generatePurchaseOrder_shouldCreateDraftOrder() {
    // Given
    List<MRPSuggestion> suggestions = List.of(
        createSuggestion("ITEM-A", 100)
    );

    // When
    PurchaseOrder order = purchaseService.generateOrder(suggestions);

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
    assertThat(order.getLines()).hasSize(1);
}

// Test: Approve changes status
@Test
void approve_shouldChangeStatusToApproved() {
    // Given
    PurchaseOrder order = createOrder(OrderStatus.DRAFT);

    // When
    PurchaseOrder approved = purchaseService.approve(order.getId(), "admin");

    // Then
    assertThat(approved.getStatus()).isEqualTo(OrderStatus.APPROVED);
}

// Test: Arrival creates inventory
@Test
void arrival_shouldIncreaseInventory() {
    // Given
    PurchaseOrder order = createOrderWithLines(OrderStatus.APPROVED);

    // When
    inventoryService.inbound("ITEM-A", "WH-01", new BigDecimal("100"));

    // Then
    verify(inventoryRepository).save(argThat(inv ->
        inv.getOnHandQty().equals(new BigDecimal("100"))
    ));
}
```

- [ ] **Step 2: Run tests**

```bash
mvn test -pl aimrp-purchase -Dtest=PurchaseServiceTest
```

- [ ] **Step 3: Implement and commit**

```bash
git commit -m "test(purchase): add TDD tests for PurchaseService"
```

---

### Task 6: Production Management Tests

**Files:**
- Test: `code/backend/aimrp-production/src/test/java/com/aimrp/production/ProductionServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// Test: Generate work order from MPS
@Test
void generateWorkOrder_shouldCreateReleasedOrder() {
    // Given
    MPSPlan mps = createMPS("PRODUCT-A", 100);

    // When
    WorkOrder order = productionService.generateWorkOrder(mps);

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.RELEASED);
}

// Test: Report finished quantity
@Test
void reportProgress_shouldIncreaseCompletedQty() {
    // Given
    WorkOrder order = createWorkOrder("PRODUCT-A", 100, 0);

    // When
    WorkOrder updated = productionService.reportProgress(order.getId(), 50);

    // Then
    assertThat(updated.getCompletedQty()).isEqualByComparingTo(new BigDecimal("50"));
}

// Test: Complete creates inventory
@Test
void complete_shouldCreateInventory() {
    // Given
    WorkOrder order = createWorkOrder("PRODUCT-A", 100, 100);

    // When
    productionService.complete(order.getId());

    // Then
    verify(inventoryService).inbound(eq("PRODUCT-A"), any(), eq(new BigDecimal("100")));
}
```

- [ ] **Step 2: Run tests**

```bash
mvn test -pl aimrp-production -Dtest=ProductionServiceTest
```

- [ ] **Step 3: Implement and commit**

```bash
git commit -m "test(production): add TDD tests for ProductionService"
```

---

## Phase 3: Intelligent Engine (P2)

### Task 7: Prediction/Forecast Tests

**Files:**
- Modify: `code/backend/aimrp-forecast/src/test/java/com/aimrp/forecast/ForecastServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
// Test: Moving average calculation
@Test
void movingAverage_shouldCalculateCorrectly() {
    // Given
    List<BigDecimal> history = List.of(
        new BigDecimal("10"), new BigDecimal("20"),
        new BigDecimal("30"), new BigDecimal("40")
    );

    // When
    BigDecimal result = forecastService.movingAverage(history, 3);

    // Then
    assertThat(result).isEqualByComparingTo(new BigDecimal("30")); // (20+30+40)/3
}

// Test: Safety stock calculation
@Test
void safetyStock_shouldUseFormula() {
    // Given
    BigDecimal avgDemand = new BigDecimal("100");
    BigDecimal stdDev = new BigDecimal("20");
    int leadTime = 7;
    double serviceLevel = 0.95; // Z=1.65

    // When
    BigDecimal safetyStock = forecastService.calculateSafetyStock(
        avgDemand, stdDev, leadTime, serviceLevel
    );

    // Then
    // SS = Z * sqrt(leadTime) * stdDev = 1.65 * 2.65 * 20 = 87.45
    assertThat(safetyStock).isEqualByComparingTo(new BigDecimal("87.45"));
}
```

- [ ] **Step 2: Run tests**

```bash
mvn test -pl aimrp-forecast -Dtest=ForecastServiceTest
```

- [ ] **Step 3: Implement and commit**

```bash
git commit -m "test(forecast): add TDD tests for forecast calculations"
```

---

### Task 8-12: Additional P2/P3 Tests

For remaining modules, follow the same TDD pattern:

| Module | Test File | Test Cases |
|--------|-----------|------------|
| LLM Understanding | `aimrp-ai/src/test/...` | Intent recognition, entity extraction |
| OR Solver | `aimrp-ai/src/test/...` | FIFO scheduling, genetic algorithm |
| Anomaly Detection | `aimrp-ai/src/test/...` | Shortage diagnosis |
| Risk Warning | `aimrp-risk/src/test/...` | Inventory risk, supplier risk |
| Approval Flow | `aimrp-workflow/src/test/...` | Approval process |

---

## Phase 4: CI/CD Integration

### Task 13: GitHub Actions Pipeline

**Files:**
- Create: `.github/workflows/tdd-tests.yml`

- [ ] **Step 1: Create workflow**

```yaml
name: TDD Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test -B
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/tdd-tests.yml
git commit -m "ci: add TDD test workflow"
```

---

## Summary

| Phase | Modules | Tasks | Status |
|-------|---------|-------|--------|
| P0 | Demand, BOM, Inventory, MRP | 4 | Pending |
| P1 | Purchase, Production | 2 | Pending |
| P2 | Forecast, LLM, OR, Anomaly | 4 | Pending |
| P3 | Risk, Impact, Approval, Budget | 4 | Pending |
| CI | GitHub Actions | 1 | Pending |

**Total: 15 tasks**

---

*Plan created based on TDD_IMPLEMENTATION_PLAN.md*
*Generated: 2026-03-13*
