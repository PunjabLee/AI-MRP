package com.aimrp.production.application;

import com.aimrp.production.domain.service.SchedulerService;
import com.aimrp.production.domain.service.SchedulerService.*;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 排程 API
 */
@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {
    
    private final SchedulerService schedulerService;
    
    /**
     * 执行排程
     * 
     * POST /api/scheduler/run
     * {
     *   "jobs": [
     *     {"moNo": "MO001", "itemCode": "A001", "planQty": 100, "startDate": "2024-01-01", "dueDate": "2024-01-10", "priority": 5}
     *   ],
     *   "routes": {
     *     "A001": [
     *       {"operationNo": 1, "operationName": "加工", "workCenterCode": "WC01", "stdHours": 2}
     *     ]
     *   },
     *   "capacities": {"WC01": 8},
     *   "method": "PRIORITY",
     *   "objective": "MIN_MAKESPAN"
     * }
     */
    @PostMapping("/run")
    public ScheduleResult runSchedule(@RequestBody ScheduleRequest request) {
        log.info("接收排程请求 - 工单数: {}", request.getJobs().size());
        
        // 转换输入
        List<ScheduleInput.MoJob> jobs = new ArrayList<>();
        for (ScheduleRequest.JobDTO dto : request.getJobs()) {
            ScheduleInput.MoJob job = new ScheduleInput.MoJob();
            job.setMoNo(dto.getMoNo());
            job.setItemCode(dto.getItemCode());
            job.setPlanQty(dto.getPlanQty());
            job.setStartDate(dto.getStartDate());
            job.setDueDate(dto.getDueDate());
            job.setPriority(dto.getPriority() != null ? dto.getPriority() : 5);
            jobs.add(job);
        }
        
        // 转换工艺路线
        Map<String, List<ScheduleInput.Operation>> routes = new HashMap<>();
        for (Map.Entry<String, List<ScheduleRequest.OperationDTO>> entry : request.getRoutes().entrySet()) {
            List<ScheduleInput.Operation> ops = new ArrayList<>();
            for (ScheduleRequest.OperationDTO dto : entry.getValue()) {
                ScheduleInput.Operation op = new ScheduleInput.Operation();
                op.setOperationId(dto.getOperationId());
                op.setOperationNo(dto.getOperationNo());
                op.setOperationName(dto.getOperationName());
                op.setWorkCenterCode(dto.getWorkCenterCode());
                op.setStdHours(dto.getStdHours() != null ? dto.getStdHours() : BigDecimal.ONE);
                op.setSetupTime(dto.getSetupTime() != null ? dto.getSetupTime() : BigDecimal.ZERO);
                ops.add(op);
            }
            routes.put(entry.getKey(), ops);
        }
        
        // 转换产能
        Map<String, BigDecimal> capacities = new HashMap<>();
        if (request.getCapacities() != null) {
            request.getCapacities().forEach((k, v) -> 
                    capacities.put(k, v != null ? v : BigDecimal.valueOf(8)));
        }
        
        // 执行排程
        SchedulerService.Method method = request.getMethod() != null 
                ? SchedulerService.Method.valueOf(request.getMethod())
                : SchedulerService.Method.PRIORITY;
        
        SchedulerService.Objective objective = request.getObjective() != null
                ? SchedulerService.Objective.valueOf(request.getObjective())
                : SchedulerService.Objective.MIN_MAKESPAN;
        
        return schedulerService.schedule(jobs, routes, capacities, method, objective);
    }
    
    /**
     * 排程请求
     */
    @Data
    public static class ScheduleRequest {
        private List<JobDTO> jobs;
        private Map<String, List<OperationDTO>> routes;
        private Map<String, BigDecimal> capacities;
        private String method;
        private String objective;
        
        @Data
        public static class JobDTO {
            private String moNo;
            private String itemCode;
            private BigDecimal planQty;
            private LocalDate startDate;
            private LocalDate dueDate;
            private Integer priority;
        }
        
        @Data
        public static class OperationDTO {
            private Long operationId;
            private Integer operationNo;
            private String operationName;
            private String workCenterCode;
            private BigDecimal stdHours;
            private BigDecimal setupTime;
        }
    }
}
