## Data Model

### ER Overview

- 核心实体：
  - 模板：`Template`
  - 模板任务：`TemplateTask`
  - Release Rundown：`ReleaseRundown`
  - Rundown 任务：`ReleaseTask`
  - 任务执行记录：`TaskExecution`

```mermaid
erDiagram
  TEMPLATE ||--o{ TEMPLATE_TASK : contains
  TEMPLATE ||--o{ RELEASE_RUNDOWN : used_by
  RELEASE_RUNDOWN ||--o{ RELEASE_TASK : has
  TEMPLATE_TASK ||--o{ RELEASE_TASK : based_on
  RELEASE_TASK ||--o{ TASK_EXECUTION : executed_as

  TEMPLATE {
    long id
    string name
    string description
    string createdBy
    datetime createdAt
    datetime updatedAt
  }

  TEMPLATE_TASK {
    long id
    long template_id
    string name
    string description
    int sequenceOrder
    string automationType
    string automationRef
    string paramsJson
  }

  RELEASE_RUNDOWN {
    long id
    long template_id
    string name
    string status
    string createdBy
    datetime createdAt
  }

  RELEASE_TASK {
    long id
    long rundown_id
    long template_task_id
    string name
    int sequenceOrder
    string status
    string automationType
    string automationRef
    string paramsJson
  }

  TASK_EXECUTION {
    long id
    long release_task_id
    string externalRunId
    string externalUrl
    string status
    text logs
    datetime startedAt
    datetime finishedAt
  }
```

### Table Summaries

- **template**
  - 描述可复用的任务模板。

- **template_task**
  - 定义模板中的单个任务和执行配置。

- **release_rundown**
  - 从模板实例化出的发布执行单元。

- **release_task**
  - Rundown 内部的实际任务实例，可与模板任务不同步演进（允许编辑）。

- **task_execution**
  - 每次执行的历史记录，包括外部系统的运行 ID、URL 与日志。

