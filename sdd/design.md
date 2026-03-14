# 任务管理系统 - 技术设计说明书

**版本**: 1.0  
**生成日期**: 2026-03-13  
**文档类型**: 技术设计说明书  

---

## 1. 系统架构概述

### 1.1 系统定位

本系统是一个类 Harness CI/CD 的任务管理系统，用于简化部署任务的创建、管理和执行流程。核心价值在于提供 Excel 导入、模板管理、多任务顺序执行等功能，实现多环境链式部署（SIT → UAT → PROD）。

### 1.2 技术栈

| 层级 | 技术选型 |
|------|----------|
| 开发语言 | Java 21 |
| 后端框架 | Spring Boot 3.x |
| 数据库 | MySQL 8.x |
| ORM | JPA (Hibernate) |
| 构建工具 | Maven |
| API 文档 | SpringDoc OpenAPI |

### 1.3 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Web Frontend                            │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │导入模块 │ │模板管理 │ │Rundown  │ │任务控制 │ │定时任务 │   │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘   │
└───────┼──────────┼──────────┼──────────┼──────────┼───────────┘
        │          │          │          │          │
        └──────────┴──────────┼──────────┴──────────┘
                              │
                    ┌─────────▼─────────┐
                    │   API Gateway     │
                    │   (Spring Boot)   │
                    └─────────┬─────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼───────┐   ┌─────────▼─────────┐  ┌────────▼────────┐
│  导入服务      │   │   调度服务         │  │  执行服务        │
│  - Excel解析  │   │   - 定时任务       │  │  - Jenkins调用  │
│   - 数据验证  │   │   - 周期调度       │  │  - Ansible调用  │
└───────┬───────┘   └─────────┬─────────┘  └────────┬────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │      MySQL        │
                    │   (主数据库)      │
                    └───────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼───────┐   ┌─────────▼─────────┐  ┌────────▼────────┐
│   Jenkins    │   │      Ansible      │  │     Cron        │
│   Server     │   │   Tower/AWX       │  │   System        │
└───────────────┘   └───────────────────┘  └─────────────────┘
```

---

## 2. 模块设计

### 2.1 模块划分

| 模块 | 包路径 | 职责 | 主要类 |
|------|--------|------|--------|
| **导入模块** | `com.taskmanagement` | Excel 文件上传、解析、数据验证 | `ExcelParser`, `ImportController`, `ExcelParserService` |
| **模板管理模块** | `com.taskmanagement` | 模板 CRUD、克隆、从模板生成 Rundown | `TemplateService`, `TemplateController`, `TemplateRepository` |
| **Rundown 管理模块** | `com.taskmanagement` | Rundown CRUD、任务顺序管理、执行控制 | `RundownService`, `RundownController` |
| **任务控制模块** | `com.taskmanagement` | 任务 CRUD、状态管理 | `TaskService`, `TaskController`, `TaskRepository` |
| **自动化执行模块** | `com.taskmanagement.executor` | Jenkins/Ansible 集成、状态同步 | `ExecutorFactory`, `JenkinsExecutor`, `AnsibleExecutor`, `ExecutionService` |
| **调度服务** | `com.taskmanagement.service` | 定时调度 (Rundown 内置) | `SchedulerService`, @Scheduled |
| **批量执行模块** | `com.taskmanagement.service` | 多 Rundown 并发执行、进度聚合 | `ExecutionService`, `RundownService` |
| **系统配置模块** | `com.taskmanagement` | 系统配置管理 | `SystemConfigService`, `EncryptionUtil` |

### 2.2 目录结构

```
src/main/java/com/taskmanagement/
├── TaskManagementApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── AsyncConfig.java
│   └── SchedulerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ImportController.java
│   ├── TemplateController.java
│   ├── RundownController.java
│   ├── TaskController.java
│   └── ConfigController.java
├── service/
│   ├── import/
│   ├── template/
│   ├── rundown/
│   ├── task/
│   ├── executor/
│   ├── scheduler/
│   └── batch/
├── repository/
│   ├── UserRepository.java
│   ├── TemplateRepository.java
│   ├── TaskRepository.java
│   └── RundownRepository.java
├── entity/
│   ├── User.java
│   ├── Template.java
│   ├── TemplateTask.java
│   ├── Rundown.java
│   ├── Task.java
│   ├── ExecutionLog.java
│   └── SystemConfig.java
├── dto/
│   ├── request/
│   └── response/
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── BusinessException.java
└── util/
    ├── ExcelParser.java
    └── EncryptionUtil.java
```

### 2.3 导入模块详细设计

```java
@Service
public class ExcelParserService {
    
    public List<Map<String, Object>> parse(MultipartFile file) {
        // 1. 验证文件扩展名
        // 2. 使用 Apache POI 解析 Excel
        // 3. 转换为 List<Map>
    }
    
    public boolean validateFormat(String filename) {
        String ext = FilenameUtils.getExtension(filename);
        return ext.equalsIgnoreCase("xlsx") || ext.equalsIgnoreCase("xls");
    }
}

@Service
public class DataValidator {
    
    public ValidationResult validate(List<Map<String, Object>> data) {
        // 验证必填字段
        // 验证数据类型
        // 返回验证结果
    }
}
```

### 2.4 调度服务设计 (Rundown 内置)

定时调度功能直接集成在 Rundown 实体中，无需独立的 ScheduledTask 实体：

```java
@Entity
public class Rundown {
    
    // 调度相关字段
    private String scheduleType;    // none/once/cron
    private String cronExpression;
    private LocalDateTime runTime;
    private String scheduleStatus; // scheduled/running/completed/cancelled
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunAt;
}

@Service
public class SchedulerService {
    
    // 查询待执行的定时任务
    public List<Rundown> getPendingScheduledTasks() {
        return rundownRepository.findByScheduleTypeAndNextRunTimeBefore(
            Arrays.asList("once", "cron"),
            LocalDateTime.now()
        );
    }
    
    // 创建定时任务
    public void scheduleRundown(Long rundownId, String scheduleType, 
                                 String cronExpression, LocalDateTime runTime) {
        // 更新 Rundown 调度字段
    }
    
    // 取消定时任务
    public void cancelSchedule(Long rundownId) {
        // 更新 schedule_status 为 cancelled
    }
}

### 2.5 Rundown 执行模块详细设计

```java
@Service
public class RundownExecutor {
    
    @Async
    public CompletableFuture<ExecutionResult> executeAsync(Long rundownId) {
        // 按顺序执行任务
        // 前置成功则继续，失败则中止
    }
    
    private ExecutionResult executeInOrder(List<Task> tasks) {
        for (Task task : tasks) {
            ExecutionResult result = executorFactory.getExecutor(task.getType()).execute(task);
            if (!result.isSuccess()) {
                return ExecutionResult.aborted(task);
            }
        }
        return ExecutionResult.success();
    }
}

@Service
public class BatchExecutor {
    
    public BatchResult executeBatch(List<Long> rundownIds) {
        // 并发执行多个 Rundown
        List<CompletableFuture<ExecutionResult>> futures = rundownIds.stream()
            .map(id -> rundownExecutor.executeAsync(id))
            .collect(Collectors.toList());
        
        // 聚合结果
    }
}
```

---

## 3. 核心流程设计

### 3.1 Excel 导入流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ 用户上传  │ ──▶ │ 验证格式  │ ──▶ │ 解析数据  │ ──▶ │ 数据验证  │
│ Excel文件 │     │ .xlsx/xls│     │ POI解析  │     │ 必填字段  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                                            │
                          ┌──────────┐     ┌──────────┐    │
                          │ 返回错误  │ ◀── │ 验证通过  │    │
                          │ 详情列表  │     │ 返回数据  │ ───┘
                          └──────────┘     └──────────┘
```

### 3.2 Rundown 执行流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ 开始执行  │ ──▶ │ 执行任务1 │ ──▶ │ 任务1成功?│ ──▶ │ 执行任务2 │
│ Rundown │     │          │     │          │     │          │
└──────────┘     └──────────┘     └────┬─────┘     └────┬─────┘
                                        │               │
                          ┌──────────────┴┐    ┌────────┴────────┐
                          │ Yes           │    │ No              │
                          ▼               ▼    ▼                ▼
                    ┌──────────┐   ┌──────────┐   ┌──────────┐
                    │ 继续执行  │   │  执行最后 │   │  中止执行  │
                    │ 下一任务  │   │  任务完成 │   │  标记中止  │
                    └──────────┘   └──────────┘   └──────────┘
```

### 3.3 定时任务调度流程 (Rundown 内置)

定时调度功能已集成到 Rundown 中，通过 Spring @Scheduled 定时查询待执行任务：

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ 用户设置  │ ──▶ │ 保存调度  │ ──▶ │ 定时检查  │ ──▶ │ 触发执行  │
│ 定时配置  │     │ 到Rundown │     │ 待执行任务 │     │ 执行服务  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                                            │
                          ┌──────────┐     ┌──────────┐    │
                          │ 更新状态  │ ◀── │ 执行完成  │    │
                          │ 为已完成  │     │ 返回结果  │ ───┘
                          └──────────┘     └──────────┘
```

---

## 4. 接口设计

### 4.1 REST API 分层

| 层级 | 注解 | 职责 |
|------|------|------|
| Controller | `@RestController` | 接收请求、返回响应 |
| Service | `@Service` | 业务逻辑处理 |
| Repository | `@Repository` | 数据访问 |

### 4.2 执行器接口

```java
public interface TaskExecutor {
    
    ExecutionResult execute(Task task);
    
    TaskStatus getStatus(String executionId);
    
    boolean cancel(String executionId);
}

@Component
public class JenkinsExecutor implements TaskExecutor {
    
    @Override
    public ExecutionResult execute(Task task) {
        // 调用 Jenkins REST API
    }
}

@Component
public class AnsibleExecutor implements TaskExecutor {
    
    @Override
    public ExecutionResult execute(Task task) {
        // 调用 Ansible AWX API
    }
}

@Component
public class ExecutorFactory {
    
    public TaskExecutor getExecutor(String taskType) {
        return switch (taskType) {
            case "jenkins" -> jenkinsExecutor;
            case "ansible" -> ansibleExecutor;
            default -> throw new UnsupportedTaskTypeException(taskType);
        };
    }
}
```

### 4.3 调度器接口 (Rundown 内置)

定时调度功能直接操作 Rundown 实体：

```java
@Service
public class SchedulerService {
    
    @Autowired
    private RundownRepository rundownRepository;
    
    @Autowired
    private RundownExecutor rundownExecutor;
    
    // 创建一次性定时任务
    public void scheduleOnce(Long rundownId, LocalDateTime runTime) {
        Rundown rundown = rundownRepository.findById(rundownId)
            .orElseThrow(() -> new ResourceNotFoundException("Rundown not found"));
        
        rundown.setScheduleType("once");
        rundown.setRunTime(runTime);
        rundown.setScheduleStatus("scheduled");
        rundown.setNextRunTime(runTime);
        
        rundownRepository.save(rundown);
    }
    
    // 创建 Cron 周期任务
    public void scheduleCron(Long rundownId, String cronExpression) {
        Rundown rundown = rundownRepository.findById(rundownId)
            .orElseThrow(() -> new ResourceNotFoundException("Rundown not found"));
        
        CronExpression cron = new CronExpression(cronExpression);
        rundown.setScheduleType("cron");
        rundown.setCronExpression(cronExpression);
        rundown.setScheduleStatus("scheduled");
        rundown.setNextRunTime(cron.getNextValidTimeAfter(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())));
        
        rundownRepository.save(rundown);
    }
    
    // 取消定时任务
    public void cancelSchedule(Long rundownId) {
        Rundown rundown = rundownRepository.findById(rundownId).orElse(null);
        if (rundown != null) {
            rundown.setScheduleStatus("cancelled");
            rundownRepository.save(rundown);
        }
    }
    
    // 定时检查任务 (由 @Scheduled 调用)
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkScheduledTasks() {
        List<Rundown> pendingTasks = rundownRepository
            .findByScheduleTypeInAndScheduleStatusAndNextRunTimeBefore(
                Arrays.asList("once", "cron"),
                "scheduled",
                LocalDateTime.now()
            );
        
        for (Rundown rundown : pendingTasks) {
            rundownExecutor.executeAsync(rundown.getId());
        }
    }
}
```

---

## 5. 数据流设计

### 5.1 主要数据流

| 场景 | 数据流 |
|------|--------|
| Excel 导入 | Excel → POI Parser → Validator → Task Service → DB |
| 模板创建 | Form → Template Service → JPA → MySQL |
| Rundown 执行 | User Trigger → Rundown Service → Executor → Jenkins/Ansible → Status Sync → DB |
| 定时执行 | Spring @Scheduled → Scheduler → Executor → Jenkins/Ansible → Status Sync → DB |

### 5.2 状态同步策略

采用**混合模式**：
- **立即执行**: 同步等待结果
- **定时任务**: `@Scheduled` 触发 + 定期轮询 (作为兜底)

---

## 6. 错误处理设计

### 6.1 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(), e.getDetails()));
    }
    
    @ExceptionHandler(ExternalSystemException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalSystemException e) {
        log.error("External system error", e);
        return ResponseEntity.status(502)
            .body(new ErrorResponse("EXTERNAL_ERROR", "External system unavailable"));
    }
}
```

### 6.2 错误响应格式

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Data validation failed",
    "details": [
      {
        "field": "tasks[0].name",
        "message": "Task name is required"
      }
    ]
  }
}
```

---

## 7. 安全设计

### 7.1 认证与授权

- **认证**: Spring Security Session-based 认证
- **授权**: 基于角色的简单授权 (V1.0 单一角色 `USER`)

### 7.2 敏感信息保护

| 信息类型 | 保护措施 |
|----------|----------|
| Jenkins API Token | AES-256 加密存储 |
| Ansible 凭据 | AES-256 加密存储 |
| 用户密码 | BCrypt 哈希存储 |
| 日志 | 禁止打印明文 Token/Password |

### 7.3 输入校验

```java
@Validated
@RestController
public class TemplateController {
    
    public ResponseEntity<Template> create(@RequestBody @Valid CreateTemplateRequest request) {
        // Request body validated by @Valid
    }
}

public class CreateTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100)
    private String name;
    
    @NotEmpty(message = "Task list cannot be empty")
    private List<@Valid TaskConfig> tasks;
}
```

---

## 8. 性能设计

### 8.1 性能指标

| 指标 | 目标值 |
|------|--------|
| API 响应时间 (P95) | < 500ms |
| Excel 解析 (1000行) | < 5s |
| 并发执行支持 | ≥ 10 个 Rundown |

### 8.2 优化策略

- **数据库**: 合理使用索引，避免 N+1 查询 (使用 `@EntityGraph` 或 `JOIN FETCH`)
- **连接池**: HikariCP 连接池化管理
- **异步**: `@Async` 注解处理长时间操作
- **缓存**: Spring Cache 缓存模板列表等读多写少场景

### 8.3 异步执行配置

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("task-");
        return executor;
    }
}
```

---

## 9. 部署设计

### 9.1 部署架构

```
┌─────────────────────────────────────────┐
│              Load Balancer              │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           Application Servers           │
│         (Spring Boot + JAR)             │
│              x 2+ (冗余)                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│              MySQL                       │
│           (主从复制)                     │
└─────────────────────────────────────────┘
```

### 9.2 环境配置

| 环境 | 配置 |
|------|------|
| 开发 | 本地 IDE 运行 |
| 测试 | Docker Compose |
| 生产 | JAR 包部署 / Docker / Kubernetes |

### 9.3 pom.xml 关键依赖

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Database: H2 for dev, MySQL for prod -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Excel -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>
    
    <!-- API Docs -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

### 9.4 数据库配置

#### 开发环境 (H2 内存数据库)

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:taskman
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  sql:
    init:
      mode: always
```

#### 生产环境 (MySQL)

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taskman?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: taskman
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

---

## 10. 监控与日志

### 10.1 日志规范

| 级别 | 使用场景 |
|------|----------|
| INFO | 关键操作 (创建、修改、执行) |
| WARN | 可恢复的异常 (重试、降级) |
| ERROR | 系统异常 (需要人工介入) |

### 10.2 日志配置

```yaml
logging:
  level:
    com.taskmanagement: INFO
    org.springframework: WARN
    org.hibernate: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

*本文档由 sdd-design-generator 自动生成*
