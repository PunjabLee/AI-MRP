# AI MRP 审批流模块设计文档

> **版本**: 1.0  
> **日期**: 2026-03-11  
> **技术选型**: Flowable 6.7.2

---

## 一、技术架构

### 1.1 技术选型

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 流程引擎 | Flowable | 6.7.2 | 开源工作流引擎 |
| 流程设计 | Flowable Modeler | 6.7.2 | 在线流程设计器 |
| 流程部署 | Spring Boot | 3.x | 自动部署 |
| 任务分配 | Spring Security | 6.x | 用户权限 |

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         审批流模块架构                                  │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  用户前端    │────▶│  Controller  │────▶│  Service    │
│  (React)   │     │ (审批接口)   │     │ (业务逻辑)   │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
                                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                        Flowable 引擎                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ RuntimeServ│  │ TaskService │  │ HistoryServ│          │
│  │ 流程运行   │  │ 任务管理   │  │ 历史记录   │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ Repository │  │ FormService│  │ Identity   │          │
│  │ 流程定义   │  │ 表单服务   │  │ 组织架构   │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└──────────────────────────────────────────────────────────────────┘
                                                  │
                                                  ▼
                    ┌──────────────┐     ┌──────────────┐
                    │  Repository │     │   MySQL     │
                    │  (JPA)     │────▶│ (流程数据)  │
                    └──────────────┘     └──────────────┘
```

---

## 二、模块设计

### 2.1 后端模块结构

```
aimrp-approval/
├── pom.xml
└── src/main/java/com/aimrp/approval/
    ├── ApprovalApplication.java          # 启动类
    ├── config/
    │   └── FlowableConfig.java        # Flowable 配置
    ├── controller/
    │   ├── ProcessController.java      # 流程管理
    │   ├── TaskController.java         # 任务管理
    │   └── FormController.java         # 表单管理
    ├── service/
    │   ├── ProcessService.java        # 流程服务
    │   ├── TaskService.java           # 任务服务
    │   └── ApprovalService.java       # 审批服务
    ├── repository/
    │   ├── ProcessDefinitionRepository.java
    │   └── TaskRepository.java
    ├── dto/
    │   ├── StartProcessRequest.java
    │   ├── CompleteTaskRequest.java
    │   └── TaskInfo.java
    └── listener/
        ├── ApprovalEventListener.java  # 审批事件监听
        └── TaskAssignListener.java    # 任务分配监听
```

### 2.2 数据库表设计

Flowable 自动生成 25+ 表，核心表：

| 表名 | 说明 |
|------|------|
| `act_re_procdef` | 流程定义 |
| `act_re_procinst` | 流程实例 |
| `act_ru_task` | 运行任务 |
| `act_hi_taskinst` | 历史任务 |
| `act_hi_procinst` | 历史流程 |

业务扩展表：

```sql
-- 审批业务表
CREATE TABLE t_approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_instance_id VARCHAR(64) NOT NULL,
    task_id VARCHAR(64),
    task_name VARCHAR(100),
    approver_id VARCHAR(64),
    approver_name VARCHAR(100),
    opinion TEXT,
    status VARCHAR(20),  -- pending/approved/rejected
    business_type VARCHAR(50),
    business_key VARCHAR(100),
    create_time DATETIME,
    update_time DATETIME
);

-- 审批配置表
CREATE TABLE t_approval_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_type VARCHAR(50) NOT NULL,  -- purchase_order/production_order/...
    process_key VARCHAR(100) NOT NULL,    -- 对应Flowable流程Key
    is_enabled BOOLEAN DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME
);
```

---

## 三、核心功能

### 3.1 流程定义管理

```java
@Service
@RequiredArgsConstructor
public class ProcessService {

    private final RepositoryService repositoryService;
    private final DeploymentService deploymentService;

    /**
     * 部署流程定义
     */
    public Deployment deployProcess(String processKey, String bpmnXml) {
        return repositoryService.createDeployment()
            .name(processKey)
            .addString(processKey + ".bpmn20.xml", bpmnXml)
            .deploy();
    }

    /**
     * 获取流程定义列表
     */
    public List<ProcessDefinition> getProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
            .orderByProcessDefinitionKey().asc()
            .orderByProcessDefinitionVersion().desc()
            .list();
    }

    /**
     * 获取流程图
     */
    public InputStream getProcessDiagram(String processDefinitionId) {
        return repositoryService.getProcessDiagram(processDefinitionId);
    }
}
```

### 3.2 启动流程

```java
@RestController
@RequestMapping("/api/approval/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    /**
     * 启动审批流程
     */
    @PostMapping("/start")
    public ApiResponse<String> startProcess(@RequestBody StartProcessRequest request) {
        // 1. 校验业务数据
        validateBusinessData(request.getBusinessType(), request.getBusinessKey());

        // 2. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("businessType", request.getBusinessType());
        variables.put("businessKey", request.getBusinessKey());
        variables.put("amount", request.getAmount());
        variables.put("applicantId", request.getApplicantId());
        variables.put("applicantName", request.getApplicantName());

        // 3. 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            request.getProcessKey(),
            request.getBusinessKey(),
            variables
        );

        return ApiResponse.success(processInstance.getId());
    }
}
```

### 3.3 审批任务

```java
@RestController
@RequestMapping("/api/approval/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 获取待办任务
     */
    @GetMapping("/todo")
    public ApiResponse<List<TaskInfo>> getTodoTasks(@RequestParam String userId) {
        List<Task> tasks = taskService.getTodoTasks(userId);
        return ApiResponse.success(convertToTaskInfo(tasks));
    }

    /**
     * 审批任务
     */
    @PostMapping("/complete")
    public ApiResponse<Void> completeTask(@RequestBody CompleteTaskRequest request) {
        // 1. 添加审批意见
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", request.isApproved());
        variables.put("opinion", request.getOpinion());
        variables.put("approverId", request.getApproverId());

        // 2. 完成任务
        taskService.complete(request.getTaskId(), variables);

        // 3. 记录审批历史
        approvalRecordService.save(request);

        return ApiResponse.success(null);
    }

    /**
     * 审批通过
     */
    @PostMapping("/approve/{taskId}")
    public ApiResponse<Void> approve(@PathVariable String taskId,
                                   @RequestBody ApproveRequest request) {
        taskService.approve(taskId, request);
        return ApiResponse.success(null);
    }

    /**
     * 审批拒绝
     */
    @PostMapping("/reject/{taskId}")
    public ApiResponse<Void> reject(@PathVariable String taskId,
                                    @RequestBody RejectRequest request) {
        taskService.reject(taskId, request);
        return ApiResponse.success(null);
    }

    /**
     * 转交任务
     */
    @PostMapping("/transfer/{taskId}")
    public ApiResponse<Void> transfer(@PathVariable String taskId,
                                    @RequestBody TransferRequest request) {
        taskService.transfer(taskId, request.getTargetUserId());
        return ApiResponse.success(null);
    }

    /**
     * 委派任务
     */
    @PostMapping("/delegate/{taskId}")
    public ApiResponse<Void> delegate(@PathVariable String taskId,
                                      @RequestBody DelegateRequest request) {
        taskService.delegate(taskId, request.getDelegateUserId());
        return ApiResponse.success(null);
    }
}
```

### 3.4 审批历史

```java
/**
 * 获取审批历史
 */
@GetMapping("/history/{processInstanceId}")
public ApiResponse<List<ApprovalHistory>> getHistory(@PathVariable String processInstanceId) {
    List<HistoricActivityInstance> activities = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId)
        .finished()
        .orderByHistoricActivityInstanceEndTime().asc()
        .list();

    return ApiResponse.success(convertToHistory(activities));
}
```

---

## 四、流程设计

### 4.1 采购订单审批流程

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             targetNamespace="http://www.flowable.org/processdef">

    <!-- 采购订单审批流程 -->
    <process id="purchase_order_approval" name="采购订单审批" isExecutable="true">
        
        <!-- 开始事件 -->
        <startEvent id="startEvent" />
        
        <!-- 顺序流 -->
        <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="departmentApproval" />
        
        <!-- 部门审批 -->
        <userTask id="departmentApproval" name="部门审批" 
                  flowable:candidateUsers="${initiator}">
            <documentation>部门负责人审批</documentation>
        </userTask>
        
        <sequenceFlow id="flow2" sourceRef="departmentApproval" targetRef="exclusiveGateway" />
        
        <!-- 排他网关 -->
        <exclusiveGateway id="exclusiveGateway" />
        
        <sequenceFlow id="flow3" sourceRef="exclusiveGateway" targetRef="financeApproval">
            <conditionExpression xsi:type="tFormalExpression">
                ${amount > 10000}
            </conditionExpression>
        </sequenceFlow>
        
        <sequenceFlow id="flow4" sourceRef="exclusiveGateway" targetRef="endEvent">
            <conditionExpression xsi:type="tFormalExpression">
                ${amount <= 10000}
            </conditionExpression>
        </sequenceFlow>
        
        <!-- 财务审批 -->
        <userTask id="financeApproval" name="财务审批" 
                  flowable:candidateGroups="FINANCE">
            <documentation>财务审批</documentation>
        </userTask>
        
        <sequenceFlow id="flow5" sourceRef="financeApproval" targetRef="endEvent" />
        
        <!-- 结束事件 -->
        <endEvent id="endEvent" />
        
    </process>
</definitions>
```

### 4.2 审批流程类型

| 流程Key | 流程名称 | 审批规则 |
|---------|----------|----------|
| `purchase_order_approval` | 采购订单审批 | ≤1万部门审批，>1万财务审批 |
| `production_order_approval` | 生产工单审批 | 车间→生产部→技术部 |
| `inventory_transfer_approval` | 库存调拨审批 | 仓库→运营 |
| `supplier_approval` | 供应商准入审批 | 采购→风控→总经理 |

### 4.3 会签/并行审批

```xml
<!-- 并行网关 -->
<parallelGateway id="parallelGateway" />

<!-- 会签用户任务 -->
<userTask id="multiSignApproval" name="会签审批" 
          flowable:assignee="${assignee1}">
    <multiInstanceLoopCharacteristics 
        isSequential="false"
        collection="${assigneeList}"
        elementVariable="assignee">
    </multiInstanceLoopCharacteristics>
</userTask>
```

---

## 五、前端集成

### 5.1 审批页面组件

```
src/pages/ApprovalPage.tsx
├── components/
│   ├── ApprovalList.tsx        # 审批列表
│   ├── ApprovalDetail.tsx     # 审批详情
│   ├── ApprovalForm.tsx       # 审批表单
│   ├── ProcessViewer.tsx      # 流程图
│   └── TaskForm.tsx           # 任务处理表单
```

### 5.2 审批列表API

```typescript
// src/api/approval.ts
export const approvalApi = {
  // 获取待办任务
  getTodoTasks: (userId: string) => 
    request.get('/api/approval/task/todo', { params: { userId } }),
  
  // 获取已办任务
  getDoneTasks: (userId: string) => 
    request.get('/api/approval/task/done', { params: { userId } }),
  
  // 审批任务
  approve: (taskId: string, data: ApproveRequest) =>
    request.post(`/api/approval/task/approve/${taskId}`, data),
  
  reject: (taskId: string, data: RejectRequest) =>
    request.post(`/api/approval/task/reject/${taskId}`, data),
  
  // 转交/委派
  transfer: (taskId: string, targetUserId: string) =>
    request.post(`/api/approval/task/transfer/${taskId}`, { targetUserId }),
  
  // 获取审批历史
  getHistory: (processInstanceId: string) =>
    request.get(`/api/approval/history/${processInstanceId}`)
};
```

### 5.3 审批详情页面

```tsx
// ApprovalDetail.tsx
const ApprovalDetail: React.FC<{ taskId: string }> = ({ taskId }) => {
  const { data: task } = useTaskDetail(taskId);
  
  return (
    <div className="approval-detail">
      <ProcessViewer processDefinitionId={task.processDefinitionId} 
                     processInstanceId={task.processInstanceId} />
      
      <Card title="业务信息">
        <DescriptionList>
          <Description term="单据类型">{task.businessType}</Description>
          <Description term="单据编号">{task.businessKey}</Description>
          <Description term="申请金额">{task.amount}</Description>
        </DescriptionList>
      </Card>
      
      <Card title="审批意见">
        <Form onFinish={handleSubmit}>
          <Radio.Group>
            <Radio value={true}>同意</Radio>
            <Radio value={false}>拒绝</Radio>
          </Radio.Group>
          <Form.Item name="opinion">
            <Input.TextArea rows={4} placeholder="请输入审批意见" />
          </Form.Item>
          <Button type="primary" htmlType="submit">提交</Button>
        </Form>
      </Card>
    </div>
  );
};
```

---

## 六、集成点

### 6.1 与业务模块集成

```java
// 采购订单审批触发
@Service
public class PurchaseOrderService {

    @Autowired
    private ProcessService processService;

    public void submitForApproval(PurchaseOrder order) {
        // 1. 保存订单
        save(order);
        
        // 2. 启动审批流程
        StartProcessRequest request = StartProcessRequest.builder()
            .processKey("purchase_order_approval")
            .businessKey(order.getId())
            .amount(order.getTotalAmount())
            .applicantId(order.getCreatorId())
            .applicantName(order.getCreatorName())
            .build();
        
        processService.startProcess(request);
        
        // 3. 更新订单状态
        order.setStatus("PENDING_APPROVAL");
        update(order);
    }
}
```

### 6.2 审批回调

```java
// 审批完成监听
@Component
public class ApprovalEventListener {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @org.springframework.context.event.EventListener
    public void onApprovalCompleted(ApprovalCompletedEvent event) {
        String businessType = event.getBusinessType();
        String businessKey = event.getBusinessKey();
        boolean approved = event.isApproved();
        
        switch (businessType) {
            case "PURCHASE_ORDER":
                if (approved) {
                    purchaseOrderService.approve(businessKey);
                } else {
                    purchaseOrderService.reject(businessKey, event.getOpinion());
                }
                break;
            // 其他业务类型...
        }
    }
}
```

---

## 七、实施计划

### 7.1 开发周期

| 周次 | 内容 | 交付物 |
|------|------|----------|
| 第1周 | Flowable集成、流程引擎配置 | 环境搭建、基础配置 |
| 第2周 | 流程定义服务、任务服务 | 核心API |
| 第3周 | 审批历史、流程图展示 | 前端集成 |
| 第4周 | 业务集成、测试 | 与采购/生产模块集成 |

### 7.2 预估工时

| 模块 | 工时 |
|------|------|
| Flowable集成配置 | 16h |
| 核心API开发 | 24h |
| 前端审批页面 | 24h |
| 业务模块集成 | 16h |
| 测试与调优 | 8h |
| **合计** | **88h** |

---

## 八、技术债务

### 8.1 待完善功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 流程设计器 | P1 | Flowable Modeler集成 |
| 移动端审批 | P2 | 简化审批表单 |
| 催办提醒 | P2 | 定时任务催办 |
| 流程监控 | P2 | 流程统计 |

### 8.2 扩展点

```java
// 审批前校验
public interface ApprovalValidator {
    boolean validate(String businessType, String businessKey);
}

// 审批通过后处理
public interface ApprovalPostProcessor {
    void process(ApprovalResult result);
}

// 审批通知
public interface ApprovalNotifier {
    void notify(ApprovalEvent event);
}
```

---

*文档版本: 1.0*
*由 小jeep 🚙 整理*
