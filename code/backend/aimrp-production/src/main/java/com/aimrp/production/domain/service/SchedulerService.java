package com.aimrp.production.domain.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OR 排程服务
 * 
 * 基于运筹学算法的生产排程优化
 */
@Slf4j
@Service
public class SchedulerService {
    
    /**
     * 排程目标
     */
    public enum Objective {
        MIN_MAKESPAN,      // 最小化总完工时间
        MIN_DELAY,         // 最小化延期
        MAX_UTILIZATION   // 最大化产能利用率
    }
    
    /**
     * 排程方法
     */
    public enum Method {
        FIFO,              // 先进先出
        PRIORITY,          // 优先级
        EarliestDueDate,   // 最早交期
        ShortestProcessingTime, // 最短加工时间
        GENETIC             // 遗传算法
    }
    
    /**
     * 执行排程
     * 
     * @param moList 工单列表
     * @param routeMap 工艺路线 (itemCode -> 工序列表)
     * @param wcCapacity 工作中心产能 (wcCode -> 小时/天)
     * @param method 排程方法
     * @param objective 优化目标
     * @return 排程结果
     */
    public ScheduleResult schedule(List<ScheduleInput.MoJob> moList,
                                   Map<String, List<ScheduleInput.Operation>> routeMap,
                                   Map<String, BigDecimal> wcCapacity,
                                   Method method,
                                   Objective objective) {
        
        log.info("开始排程 - 工单数: {}, 方法: {}, 目标: {}", moList.size(), method, objective);
        
        ScheduleResult result = new ScheduleResult();
        result.setMethod(method.name());
        
        // 1. 排序工单
        List<ScheduleInput.MoJob> sortedJobs = sortJobs(moList, method);
        
        // 2. 分配到工作中心
        Map<String, List<ScheduleResult.OperationSchedule>> wcSchedule = new HashMap<>();
        Map<String, LocalDate> wcNextAvailableDate = new HashMap<>();
        
        for (ScheduleInput.MoJob job : sortedJobs) {
            List<ScheduleInput.Operation> operations = routeMap.get(job.getItemCode());
            
            if (operations == null || operations.isEmpty()) {
                log.warn("物料 {} 无工艺路线", job.getItemCode());
                continue;
            }
            
            // 为每个工序分配时间
            LocalDate currentDate = job.getStartDate();
            
            for (ScheduleInput.Operation op : operations) {
                String wcCode = op.getWorkCenterCode();
                
                // 获取工作中心可用日期
                LocalDate wcDate = wcNextAvailableDate.getOrDefault(wcCode, currentDate);
                
                // 计算开始日期（取工单计划和工中心可用日期的较晚者）
                LocalDate startDate = currentDate.isAfter(wcDate) ? currentDate : wcDate;
                
                // 计算工序时长
                BigDecimal hours = calculateOperationHours(job.getPlanQty(), op.getStdHours());
                int days = (int) Math.ceil(hours.doubleValue() / wcCapacity.getOrDefault(wcCode, BigDecimal.valueOf(8)).doubleValue());
                
                LocalDate endDate = startDate.plusDays(days);
                
                // 保存工序排程结果
                ScheduleResult.OperationSchedule opSchedule = new ScheduleResult.OperationSchedule();
                opSchedule.setMoNo(job.getMoNo());
                opSchedule.setOperationNo(op.getOperationNo());
                opSchedule.setOperationName(op.getOperationName());
                opSchedule.setWorkCenterCode(wcCode);
                opSchedule.setPlanStartDate(startDate);
                opSchedule.setPlanEndDate(endDate);
                opSchedule.setPlanHours(hours);
                
                wcSchedule.computeIfAbsent(wcCode, k -> new ArrayList<>()).add(opSchedule);
                
                // 更新工作中心可用日期
                wcNextAvailableDate.put(wcCode, endDate.plusDays(1));
                
                // 更新工单当前日期（串行）
                currentDate = endDate;
            }
            
            // 更新工单的计划结束日期
            job.setEndDate(currentDate);
        }
        
        // 3. 计算指标
        result.setOperationSchedules(wcSchedule);
        result.setTotalJobs(moList.size());
        
        // 计算 makespan
        LocalDate maxEndDate = wcNextAvailableDate.values().stream()
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        result.setMakespanDays((int) ChronoUnit.DAYS.between(
                sortedJobs.stream().map(ScheduleInput.MoJob::getStartDate).min(LocalDate::compareTo).orElse(LocalDate.now()),
                maxEndDate));
        
        // 计算延期
        long delayedCount = sortedJobs.stream()
                .filter(j -> j.getEndDate().isAfter(j.getDueDate()))
                .count();
        result.setDelayedJobs((int) delayedCount);
        
        log.info("排程完成 - Makespan: {}天, 延期: {}个", result.getMakespanDays(), result.getDelayedJobs());
        
        return result;
    }
    
    /**
     * 工单排序
     */
    private List<ScheduleInput.MoJob> sortJobs(List<ScheduleInput.MoJob> jobs, Method method) {
        switch (method) {
            case PRIORITY:
                return jobs.stream()
                        .sorted(Comparator.comparing(ScheduleInput.MoJob::getPriority, Comparator.reverseOrder()))
                        .collect(Collectors.toList());
            case EarliestDueDate:
                return jobs.stream()
                        .sorted(Comparator.comparing(ScheduleInput.MoJob::getDueDate))
                        .collect(Collectors.toList());
            case ShortestProcessingTime:
                return jobs.stream()
                        .sorted(Comparator.comparing(ScheduleInput.MoJob::getPlanQty))
                        .collect(Collectors.toList());
            case FIFO:
            default:
                return jobs.stream()
                        .sorted(Comparator.comparing(ScheduleInput.MoJob::getStartDate))
                        .collect(Collectors.toList());
        }
    }
    
    /**
     * 计算工序工时
     */
    private BigDecimal calculateOperationHours(BigDecimal qty, BigDecimal stdHours) {
        // 基础工时 + 单位工时 × 数量
        BigDecimal baseHours = new BigDecimal("1"); // 准备时间1小时
        BigDecimal runHours = stdHours.multiply(qty);
        return baseHours.add(runHours);
    }
    
    /**
     * 排程输入
     */
    @Data
    public static class ScheduleInput {
        
        @Data
        public static class MoJob {
            private String moNo;
            private String itemCode;
            private BigDecimal planQty;
            private LocalDate startDate;
            private LocalDate dueDate;
            private Integer priority;
        }
        
        @Data
        public static class Operation {
            private Long operationId;
            private Integer operationNo;
            private String operationName;
            private String workCenterCode;
            private BigDecimal stdHours;
            private BigDecimal setupTime;
        }
    }
    
    /**
     * 排程结果
     */
    @Data
    public static class ScheduleResult {
        private String method;
        private int totalJobs;
        private int makespanDays;
        private int delayedJobs;
        private Map<String, List<OperationSchedule>> operationSchedules;
        
        @Data
        public static class OperationSchedule {
            private String moNo;
            private Integer operationNo;
            private String operationName;
            private String workCenterCode;
            private LocalDate planStartDate;
            private LocalDate planEndDate;
            private BigDecimal planHours;
        }
    }
}
