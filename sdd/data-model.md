# 任务管理系统 - 数据模型设计

**版本**: 1.2
**生成日期**: 2026-03-14
**文档类型**: 数据模型设计说明书

---

## 1. 实体概述

本系统包含以下核心实体。**定时调度由 Rundown 内置字段支持，无独立 ScheduledTask 实体；也无独立的调度 REST API**。

Rundown 通过 `schedule_type`、`cron_expression`、`run_time`、`schedule_status`、`next_run_time`、`last_run_at` 等字段承载一次性/周期定时。定时任务的管理和执行由 SchedulerService 的 `@Scheduled` 注解每分钟自动检查并执行，无需通过 REST API 手动设置。

| 实体 | 说明 | 关联 |
|------|------|------|
| User | 用户 | 任务的创建者/执行者 |
| Template | 模板 | 可生成多个 Rundown |
| TemplateTask | 模板任务配置 | 属于 Template |
| Rundown | 发布清单 | 从 Template 生成，包含多个 Task，**支持内置定时执行（无 ScheduledTask，无独立调度 API）** |
| Task | 任务 | 属于 Rundown，对应一次执行 |
| ExecutionLog | 执行日志 | 关联 Task |
| SystemConfig | 系统配置 | 存储 Jenkins/Ansible 配置 |

### 1.1 定时执行机制说明

- **无独立调度 API**：不提供 `/rundowns/{id}/schedule` 等 REST 接口
- **内置字段**：Rundown 实体包含定时相关字段（`schedule_type`, `cron_expression`, `run_time`, `schedule_status`, `next_run_time`, `last_run_at`）
- **自动检查执行**：SchedulerService 使用 `@Scheduled(fixedRate = 60000)` 每分钟检查：
  - 查询 `schedule_type IN (once, cron)` 且 `schedule_status = 'scheduled'` 且 `next_run_time <= now` 的 Rundown
  - 自动执行匹配的 Rundown
  - 根据调度类型更新状态（一次性执行完成后标记为 completed，Cron 计算下次执行时间）

| 定时类型 | 说明 |
|----------|------|
| `none` (null) | 不定时，仅手动执行 |
| `once` | 一次性定时执行，执行后 `schedule_status` 变为 `completed` |
| `cron` | Cron 周期执行，每次执行后重新计算 `next_run_time` |

---

## 2. 实体详细定义

### 2.1 User (用户)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt 哈希 |
| display_name | VARCHAR(100) | NULL | 显示名称 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'user' | 角色 |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |
| updated_at | DATETIME(3) | NOT NULL | 更新时间 |

### 2.2 Template (模板)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NOT NULL | 模板名称 |
| description | TEXT | NULL | 模板描述 |
| created_by | BIGINT | NOT NULL | 创建者 ID |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |
| updated_at | DATETIME(3) | NOT NULL | 更新时间 |

### 2.3 TemplateTask (模板任务配置)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| template_id | BIGINT | NOT NULL | 所属模板 ID |
| name | VARCHAR(100) | NOT NULL | 任务名称 |
| task_type | VARCHAR(20) | NOT NULL | 任务类型 (jenkins/ansible) |
| config | JSON | NOT NULL | 任务配置 |
| order_index | INT | NOT NULL | 执行顺序 |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |

### 2.4 Rundown (发布清单)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| rundown_code | VARCHAR(50) | NOT NULL, UNIQUE | 清单编号 (RD-YYYYMMDD-NNN) |
| name | VARCHAR(100) | NOT NULL | 清单名称 |
| description | TEXT | NULL | 清单描述 |
| template_id | BIGINT | NULL | 来源模板 ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'pending' | 执行状态 |
| created_by | BIGINT | NOT NULL | 创建者 ID |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |
| updated_at | DATETIME(3) | NOT NULL | 更新时间 |
| started_at | DATETIME(3) | NULL | 开始执行时间 |
| finished_at | DATETIME(3) | NULL | 结束执行时间 |
| schedule_type | VARCHAR(20) | NULL | 调度类型 (none/once/cron) |
| cron_expression | VARCHAR(100) | NULL | Cron 表达式 |
| run_time | DATETIME(3) | NULL | 定时执行时间 |
| schedule_status | VARCHAR(20) | NULL | 调度状态 (scheduled/running/completed/cancelled) |
| next_run_time | DATETIME(3) | NULL | 下次执行时间 |
| last_run_at | DATETIME(3) | NULL | 上次执行时间 |

**状态说明 (Rundown.status)**：

| 状态 | 说明 |
|------|------|
| pending | 待处理 |
| running | 执行中 |
| partial_success | 部分成功 |
| success | 全部成功 |
| aborted | 已中止 |
| failed | 失败 |

**调度类型 (Rundown.schedule_type)**：

| 类型 | 说明 |
|------|------|
| null | 无定时，仅手动执行 |
| once | 一次性定时执行，执行后 `schedule_status` 变为 `completed` |
| cron | Cron 周期执行，每次执行后重新计算 `next_run_time` |

**调度状态 (Rundown.schedule_status)**：

| 状态 | 说明 |
|------|------|
| null | 未设置调度 |
| scheduled | 等待下次执行时间到达 |
| running | 正在执行中 |
| completed | 已完成（一次性执行） |
| cancelled | 已取消 |

**定时执行流程**：

```
1. 用户通过 API 设置 Rundown 的定时字段（schedule_type, run_time / cron_expression）
   ↓
2. SchedulerService 每分钟通过 @Scheduled 检查
   ↓
3. 查询条件：schedule_type IN (once, cron)
             AND schedule_status = 'scheduled'
             AND next_run_time <= now
   ↓
4. 自动执行匹配的 Rundown
   ↓
5. 根据类型更新状态：
   - once: schedule_status = 'completed'
   - cron: 重新计算 next_run_time
```

### 2.5 Task (任务)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| rundown_id | BIGINT | NOT NULL | 所属清单 ID |
| name | VARCHAR(100) | NOT NULL | 任务名称 |
| task_type | VARCHAR(20) | NOT NULL | 任务类型 (jenkins/ansible) |
| config | JSON | NOT NULL | 任务配置 |
| order_index | INT | NOT NULL | 执行顺序 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'pending' | 状态 |
| external_id | VARCHAR(255) | NULL | 外部系统ID |
| external_url | VARCHAR(500) | NULL | 外部链接 |
| error_message | TEXT | NULL | 错误信息 |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |
| updated_at | DATETIME(3) | NOT NULL | 更新时间 |
| started_at | DATETIME(3) | NULL | 开始执行时间 |
| finished_at | DATETIME(3) | NULL | 结束执行时间 |

**状态说明 (Task.status)**：

| 状态 | 说明 |
|------|------|
| pending | 待处理 |
| running | 执行中 |
| success | 成功 |
| failed | 失败 |
| aborted | 中止 |
| timeout | 超时 |

### 2.6 ExecutionLog (执行日志)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| task_id | BIGINT | NOT NULL | 关联任务 ID |
| execution_id | VARCHAR(255) | NULL | 外部执行ID |
| status | VARCHAR(20) | NOT NULL | 执行状态 |
| output | LONGTEXT | NULL | 执行输出 |
| error_output | LONGTEXT | NULL | 错误输出 |
| duration_ms | INT | NULL | 执行时长(毫秒) |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |

### 2.7 SystemConfig (系统配置)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| config_key | VARCHAR(100) | NOT NULL, UNIQUE | 配置键 |
| config_value | TEXT | NOT NULL | 配置值 (加密存储) |
| value_type | VARCHAR(20) | NOT NULL, DEFAULT 'string' | 值类型 |
| description | TEXT | NULL | 说明 |
| created_at | DATETIME(3) | NOT NULL | 创建时间 |
| updated_at | DATETIME(3) | NOT NULL | 更新时间 |

**系统配置项**：

| 配置键 | 说明 |
|--------|------|
| jenkins_url | Jenkins 服务器地址 |
| jenkins_username | Jenkins 用户名 |
| jenkins_api_token | Jenkins API Token (加密) |
| ansible_type | Ansible 类型 (cli/awx) |
| ansible_awx_url | AWX 服务器地址 |
| ansible_awx_token | AWX Token (加密) |
| ansible_inventory | Ansible Inventory 路径 |
| task_timeout | 任务默认超时时间(秒) |

---

## 4. 定时任务调度器

### 4.1 SchedulerService 设计

定时任务由 `SchedulerService` 负责，通过 Spring 的 `@Scheduled` 注解自动执行，**无需外部调用 REST API**。

**核心方法**：

| 方法 | 说明 |
|------|------|
| `checkScheduledRundowns()` | 每分钟执行，检查并触发待执行的 Rundown |
| `scheduleOnce(rundownId, runTime)` | 设置一次性定时执行（由模板或 Rundown 创建时调用） |
| `scheduleCron(rundownId, cronExpression)` | 设置 Cron 周期执行（由模板或 Rundown 创建时调用） |
| `cancelSchedule(rundownId)` | 取消定时执行 |
| `calculateNextRunTime(rundown)` | 计算 Cron 表达式的下次执行时间 |

**定时检查逻辑**：

```
1. 每分钟执行一次 @Scheduled 方法
2. 查询条件：
   - schedule_type IN (once, cron)
   - schedule_status = 'scheduled'
   - next_run_time <= now
3. 对每个匹配的 Rundown：
   - 更新 schedule_status = 'running'
   - 调用 ExecutionService.executeRundown()
   - 根据 schedule_type 更新状态：
     * once: schedule_status = 'completed'
     * cron: 重新计算 next_run_time
   - 更新 last_run_at
```

---

## 5. ER 图 (无外键)

```plantuml
@startuml
hide circle

entity "users" as users {
  * id : BIGINT <<PK>>
  * username : VARCHAR(50) <<UNIQUE>>
  * password_hash : VARCHAR(255)
  * display_name : VARCHAR(100)
  * role : VARCHAR(20)
  * created_at : DATETIME(3)
  * updated_at : DATETIME(3)
}

entity "templates" as templates {
  * id : BIGINT <<PK>>
  * name : VARCHAR(100)
  * description : TEXT
  * created_by : BIGINT
  * created_at : DATETIME(3)
  * updated_at : DATETIME(3)
}

entity "template_tasks" as template_tasks {
  * id : BIGINT <<PK>>
  * template_id : BIGINT
  * name : VARCHAR(100)
  * task_type : VARCHAR(20)
  * config : JSON
  * order_index : INT
  * created_at : DATETIME(3)
}

entity "rundowns" as rundowns {
  * id : BIGINT <<PK>>
  * rundown_code : VARCHAR(50) <<UNIQUE>>
  * name : VARCHAR(100)
  * description : TEXT
  * template_id : BIGINT
  * status : VARCHAR(20)
  * created_by : BIGINT
  * created_at : DATETIME(3)
  * updated_at : DATETIME(3)
  * started_at : DATETIME(3)
  * finished_at : DATETIME(3)
  * schedule_type : VARCHAR(20)
  * cron_expression : VARCHAR(100)
  * run_time : DATETIME(3)
  * schedule_status : VARCHAR(20)
  * next_run_time : DATETIME(3)
  * last_run_at : DATETIME(3)
}

entity "tasks" as tasks {
  * id : BIGINT <<PK>>
  * rundown_id : BIGINT
  * name : VARCHAR(100)
  * task_type : VARCHAR(20)
  * config : JSON
  * order_index : INT
  * status : VARCHAR(20)
  * external_id : VARCHAR(255)
  * external_url : VARCHAR(500)
  * error_message : TEXT
  * created_at : DATETIME(3)
  * updated_at : DATETIME(3)
  * started_at : DATETIME(3)
  * finished_at : DATETIME(3)
}

entity "execution_logs" as execution_logs {
  * id : BIGINT <<PK>>
  * task_id : BIGINT
  * execution_id : VARCHAR(255)
  * status : VARCHAR(20)
  * output : LONGTEXT
  * error_output : LONGTEXT
  * duration_ms : INT
  * created_at : DATETIME(3)
}

entity "system_configs" as system_configs {
  * id : BIGINT <<PK>>
  * config_key : VARCHAR(100) <<UNIQUE>>
  * config_value : TEXT
  * value_type : VARCHAR(20)
  * description : TEXT
  * created_at : DATETIME(3)
  * updated_at : DATETIME(3)
}

' 逻辑关联 (无外键)
templates :: created_by -o users
template_tasks :: template_id -o templates
rundowns :: template_id -o templates
rundowns :: created_by -o users
tasks :: rundown_id -o rundowns
execution_logs :: task_id -o tasks

@enduml
```

---

## 4. 索引设计

### 4.1 主键索引

所有表的主键 (id) 自动创建 B-tree 索引。

### 4.2 业务索引

| 表名 | 索引字段 | 类型 | 说明 |
|------|----------|------|------|
| **users** | | | |
| | username | UNIQUE | 登录查询 |
| **templates** | | | |
| | created_by | INDEX | 按创建者查询 |
| | created_at | INDEX | 时间排序 |
| **template_tasks** | | | |
| | template_id | INDEX | 查询模板任务 |
| | (template_id, order_index) | INDEX | 任务排序 |
| **rundowns** | | | |
| | rundown_code | UNIQUE | 编号查询 |
| | template_id | INDEX | 按模板查询 |
| | status | INDEX | 状态筛选 |
| | created_by | INDEX | 按创建者查询 |
| | created_at | INDEX | 时间排序 |
| | schedule_type | INDEX | 调度类型筛选 |
| | schedule_status | INDEX | 调度状态筛选 |
| | next_run_time | INDEX | 定时触发查询 |
| **tasks** | | | |
| | rundown_id | INDEX | 查询清单任务 |
| | (rundown_id, order_index) | INDEX | 任务排序 |
| | status | INDEX | 状态筛选 |
| | external_id | INDEX | 外部ID查询 |
| **execution_logs** | | | |
| | task_id | INDEX | 任务日志查询 |
| | created_at | INDEX | 时间查询 |
| **system_configs** | | | |
| | config_key | UNIQUE | 配置查询 |

### 4.3 复合索引优化

| 表名 | 复合索引 | 使用场景 |
|------|----------|----------|
| template_tasks | (template_id, order_index) | 模板任务列表按顺序 |
| tasks | (rundown_id, order_index) | Rundown 任务列表按顺序 |
| rundowns | (schedule_type, next_run_time) | 查询待执行的定时任务 |

---

## 5. 数据完整性策略

### 5.1 应用层级联

由于无外键约束，数据一致性由应用层保证：

| 操作 | 处理方式 |
|------|----------|
| 删除模板 | 应用层检查：若存在关联的 Rundown 则拒绝删除 |
| 删除 Rundown | 应用层级联删除关联的 Task |
| 删除 Task | 应用层级联删除关联的 ExecutionLog |
| 更新关联ID | 应用层先验证目标存在再更新 |

### 5.2 软删除考虑

V1.0 采用物理删除，如需保留历史可扩展：

```sql
-- 扩展：添加 deleted_at 字段实现软删除
ALTER TABLE templates ADD COLUMN deleted_at DATETIME(3) NULL;
ALTER TABLE rundowns ADD COLUMN deleted_at DATETIME(3) NULL;
ALTER TABLE tasks ADD COLUMN deleted_at DATETIME(3) NULL;
```

---

## 6. JPA Entity 配置

### 6.1 无外键配置

```java
@Entity
@Table(name = "templates")
public class Template {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    // 无 @OneToMany 关系，由应用层管理
}
```

### 6.2 JSON 字段映射

```java
@Entity
public class Task {
    
    @JdbcTypeCode(SqlType.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> config;
}
```

---

## 7. SQL 建表语句 (无外键)

```sql
-- users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- templates table
CREATE TABLE templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- template_tasks table
CREATE TABLE template_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    config JSON NOT NULL,
    order_index INT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_template_id (template_id),
    INDEX idx_template_order (template_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- rundowns table (with scheduling fields)
CREATE TABLE rundowns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rundown_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    template_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_by BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    started_at DATETIME(3),
    finished_at DATETIME(3),
    schedule_type VARCHAR(20),
    cron_expression VARCHAR(100),
    run_time DATETIME(3),
    schedule_status VARCHAR(20),
    next_run_time DATETIME(3),
    last_run_at DATETIME(3),
    INDEX idx_template_id (template_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at),
    INDEX idx_schedule (schedule_type, next_run_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- tasks table
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rundown_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    config JSON NOT NULL,
    order_index INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    external_id VARCHAR(255),
    external_url VARCHAR(500),
    error_message TEXT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    started_at DATETIME(3),
    finished_at DATETIME(3),
    INDEX idx_rundown_id (rundown_id),
    INDEX idx_rundown_order (rundown_id, order_index),
    INDEX idx_status (status),
    INDEX idx_external_id (external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- execution_logs table
CREATE TABLE execution_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    execution_id VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    output LONGTEXT,
    error_output LONGTEXT,
    duration_ms INT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_task_id (task_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- system_configs table
CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    value_type VARCHAR(20) NOT NULL DEFAULT 'string',
    description TEXT,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

*本文档由 sdd-design-generator 自动生成*
