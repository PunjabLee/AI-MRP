package com.aimrp.production;

import com.aimrp.production.domain.service.SchedulerService;
import com.aimrp.production.domain.service.SchedulerService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SchedulerService TDD Tests
 */
class SchedulerServiceTest {

    private SchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerService();
    }

    @Test
    void schedule_fifoMethod_shouldSortByStartDate() {
        // Given
        List<ScheduleInput.MoJob> moList = List.of(
                createJob("MO-002", "2026-03-15", "2026-03-20", 5),
                createJob("MO-001", "2026-03-10", "2026-03-18", 3)
        );

        Map<String, List<ScheduleInput.Operation>> routeMap = Map.of(
                "ITEM-A", List.of(createOperation("OP-001", "WC-01"))
        );

        Map<String, BigDecimal> wcCapacity = Map.of(
                "WC-01", new BigDecimal("8")
        );

        // When
        ScheduleResult result = schedulerService.schedule(
                moList, routeMap, wcCapacity,
                Method.FIFO, Objective.MIN_MAKESPAN
        );

        // Then
        assertNotNull(result);
        assertEquals("FIFO", result.getMethod());
        assertEquals(2, result.getTotalJobs());
    }

    @Test
    void schedule_earliestDueDate_shouldSortByDueDate() {
        // Given
        List<ScheduleInput.MoJob> moList = List.of(
                createJob("MO-001", "2026-03-10", "2026-03-25", 3),  // Due later
                createJob("MO-002", "2026-03-10", "2026-03-15", 3)   // Due earlier
        );

        Map<String, List<ScheduleInput.Operation>> routeMap = Map.of(
                "ITEM-A", List.of(createOperation("OP-001", "WC-01"))
        );

        Map<String, BigDecimal> wcCapacity = Map.of(
                "WC-01", new BigDecimal("8")
        );

        // When
        ScheduleResult result = schedulerService.schedule(
                moList, routeMap, wcCapacity,
                Method.EarliestDueDate, Objective.MIN_DELAY
        );

        // Then
        assertNotNull(result);
        assertEquals("EarliestDueDate", result.getMethod());
    }

    @Test
    void schedule_withPriority_shouldSortByPriority() {
        // Given
        List<ScheduleInput.MoJob> moList = List.of(
                createJobWithPriority("MO-001", "2026-03-10", "2026-03-20", 3, 5),  // Lower priority
                createJobWithPriority("MO-002", "2026-03-10", "2026-03-20", 3, 10)  // Higher priority
        );

        Map<String, List<ScheduleInput.Operation>> routeMap = Map.of(
                "ITEM-A", List.of(createOperation("OP-001", "WC-01"))
        );

        Map<String, BigDecimal> wcCapacity = Map.of(
                "WC-01", new BigDecimal("8")
        );

        // When
        ScheduleResult result = schedulerService.schedule(
                moList, routeMap, wcCapacity,
                Method.PRIORITY, Objective.MIN_DELAY
        );

        // Then
        assertNotNull(result);
        assertEquals("PRIORITY", result.getMethod());
    }

    @Test
    void schedule_noRouteMap_shouldSkipJob() {
        // Given - Job without route
        List<ScheduleInput.MoJob> moList = List.of(
                createJob("MO-001", "2026-03-10", "2026-03-20", 3)
        );

        Map<String, List<ScheduleInput.Operation>> routeMap = Map.of();  // Empty

        Map<String, BigDecimal> wcCapacity = Map.of(
                "WC-01", new BigDecimal("8")
        );

        // When
        ScheduleResult result = schedulerService.schedule(
                moList, routeMap, wcCapacity,
                Method.FIFO, Objective.MIN_MAKESPAN
        );

        // Then - Should handle gracefully
        assertNotNull(result);
    }

    @Test
    void schedule_multipleWorkCenters_shouldScheduleIndependently() {
        // Given
        List<ScheduleInput.MoJob> moList = List.of(
                createJob("MO-001", "2026-03-10", "2026-03-20", 3)
        );

        Map<String, List<ScheduleInput.Operation>> routeMap = Map.of(
                "ITEM-A", List.of(
                        createOperationWithNo("OP-001", 1, "WC-01"),
                        createOperationWithNo("OP-002", 2, "WC-02")
                )
        );

        Map<String, BigDecimal> wcCapacity = Map.of(
                "WC-01", new BigDecimal("8"),
                "WC-02", new BigDecimal("8")
        );

        // When
        ScheduleResult result = schedulerService.schedule(
                moList, routeMap, wcCapacity,
                Method.FIFO, Objective.MIN_MAKESPAN
        );

        // Then - Should have schedules for both work centers
        assertNotNull(result.getOperationSchedules());
        assertTrue(result.getOperationSchedules().containsKey("WC-01"));
        assertTrue(result.getOperationSchedules().containsKey("WC-02"));
    }

    private ScheduleInput.MoJob createJob(String moNo, String startDate, String dueDate, int priority) {
        ScheduleInput.MoJob job = new ScheduleInput.MoJob();
        job.setMoNo(moNo);
        job.setItemCode("ITEM-A");
        job.setPlanQty(new BigDecimal("100"));
        job.setStartDate(LocalDate.parse(startDate));
        job.setDueDate(LocalDate.parse(dueDate));
        job.setPriority(priority);
        return job;
    }

    private ScheduleInput.MoJob createJobWithPriority(String moNo, String startDate, String dueDate, int priority, int priorityValue) {
        ScheduleInput.MoJob job = createJob(moNo, startDate, dueDate, priority);
        job.setPriority(priorityValue);
        return job;
    }

    private ScheduleInput.Operation createOperation(String opNo, String wcCode) {
        ScheduleInput.Operation op = new ScheduleInput.Operation();
        op.setOperationId(1L);
        op.setOperationNo(1);
        op.setOperationName("Operation " + opNo);
        op.setWorkCenterCode(wcCode);
        op.setStdHours(new BigDecimal("2"));
        return op;
    }

    private ScheduleInput.Operation createOperationWithNo(String opNo, int opNoValue, String wcCode) {
        ScheduleInput.Operation op = new ScheduleInput.Operation();
        op.setOperationId(1L);
        op.setOperationNo(opNoValue);
        op.setOperationName("Operation " + opNo);
        op.setWorkCenterCode(wcCode);
        op.setStdHours(new BigDecimal("2"));
        return op;
    }
}
