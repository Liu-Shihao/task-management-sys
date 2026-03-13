## Implementation Tasks

### 1. 基础设施与配置

- **1.1 项目基础**
  - 确认 Spring Boot 3.x + Java 21 + Maven 配置无误。
  - 确认 H2 内存数据库与 JPA 配置（`ddl-auto`, H2 console）可正常启动。

- **1.2 通用基础**
  - 实现统一响应封装（成功/失败结构）。
  - 实现全局异常处理（参数校验、业务异常、系统异常）。

### 2. 数据模型与仓库

- **2.1 JPA 实体**
  - `Template`, `TemplateTask`, `ReleaseRundown`, `ReleaseTask`, `TaskExecution`。

- **2.2 Repositories**
  - 对应的 Spring Data `Repository` 接口。
  - 常用查询方法（按 template/rundown 查询任务列表等）。

### 3. Excel 模块

- **3.1 Controller**
  - `POST /api/excel/upload` 文件上传接口。

- **3.2 Service**
  - 使用 Apache POI 解析 Excel。
  - 对必填字段和格式进行校验。
  - 定义并返回结构化错误信息模型。

### 4. 模板模块

- **4.1 API**
  - `GET /api/templates`
  - `GET /api/templates/{id}`
  - `POST /api/templates`
  - `PUT /api/templates/{id}`
  - `POST /api/templates/{id}/clone`

- **4.2 业务逻辑**
  - 创建模板及任务列表。
  - 模板克隆实现。
  - 参数校验与错误处理。

### 5. Release Rundown 模块

- **5.1 API**
  - `POST /api/rundowns`
  - `GET /api/rundowns`
  - `GET /api/rundowns/{id}`

- **5.2 业务逻辑**
  - 从模板生成 Rundown 与任务列表。
  - 初始化状态为 `PENDING`。

### 6. 任务控制模块

- **6.1 API**
  - `PUT /api/rundowns/{rundownId}/tasks/{taskId}`
  - `DELETE /api/rundowns/{rundownId}/tasks/{taskId}`
  - `POST /api/rundowns/{rundownId}/tasks/{taskId}/run`

- **6.2 业务逻辑**
  - 状态校验（是否可运行）。
  - 删除任务时的顺序调整（如有必要）。

### 7. 自动化执行模块

- **7.1 接口与实现**
  - 定义 `AutomationClient` 接口。
  - 实现 `JenkinsAutomationClient` 与 `AnsibleAutomationClient`（初期可实现为 Mock）。

- **7.2 TaskExecutionService**
  - 触发外部执行并记录 `TaskExecution`。
  - 更新 `ReleaseTask` 状态。
  - 提供执行记录和日志查询接口。

### 8. 实时状态更新

- **8.1 推送通道**
  - 实现基于 SSE 或 WebSocket 的状态更新接口。

- **8.2 事件发布**
  - 在任务状态变更处发布事件，推送到对应 Rundown 订阅方。

### 9. 测试与验证

- **9.1 单元测试**
  - 对关键 Service（模板克隆、Rundown 生成、任务执行）编写单元测试。

- **9.2 集成测试**
  - 针对每个 User Story 编写端到端用例，验证 Acceptance Criteria。

