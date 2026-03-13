## Detailed Design

### 1. Excel Module

- **Controller**
  - `POST /api/excel/upload`
    - Request: `multipart/form-data` with file field `file`.
    - Validations:
      - 文件必填。
      - 扩展名与 MIME 类型必须为 `.xlsx` / `.xls`。
    - Responses:
      - 200：解析成功，返回结构化数据（示例：列表形式的行对象）。
      - 400：文件类型错误或格式校验失败，返回错误列表（行、列、原因）。

- **Service**
  - `ExcelService.parse(MultipartFile file): ExcelParseResult`
    - 使用 Apache POI 读取表头和数据行。
    - 校验必填列、数据类型和业务规则。
    - 收集错误信息并返回。

### 2. Template Module

- **Entities**
  - `Template`
  - `TemplateTask`

- **Repositories**
  - `TemplateRepository`
  - `TemplateTaskRepository`

- **APIs**
  - `GET /api/templates`
  - `GET /api/templates/{id}`
  - `POST /api/templates`
  - `PUT /api/templates/{id}`
  - `POST /api/templates/{id}/clone`

- **Service Responsibilities**
  - 创建模板及其任务列表。
  - 更新模板基本信息与任务。
  - 克隆模板：
    - 复制 `Template`（重置标识与创建时间）。
    - 按顺序复制相关 `TemplateTask`。

### 3. Release Rundown Module

- **Entities**
  - `ReleaseRundown`
  - `ReleaseTask`

- **Repositories**
  - `ReleaseRundownRepository`
  - `ReleaseTaskRepository`

- **APIs**
  - `POST /api/rundowns`
    - Request: `{ "templateId": Long, "name": String?, "createdBy": String? }`
    - Behavior:
      - 创建 `ReleaseRundown`，状态设为 `PENDING`。
      - 使用 `TemplateTask` 列表生成 `ReleaseTask` 列表。
  - `GET /api/rundowns`
  - `GET /api/rundowns/{id}`

### 4. Task Control Module

- **APIs**
  - `PUT /api/rundowns/{rundownId}/tasks/{taskId}`
    - 更新任务名称、顺序、自动化类型、引用、参数等。
  - `DELETE /api/rundowns/{rundownId}/tasks/{taskId}`
    - 删除任务，且可根据需要重排 `sequenceOrder`。
  - `POST /api/rundowns/{rundownId}/tasks/{taskId}/run`
    - 检查任务状态是否可运行（`PENDING` / `FAILED`）。
    - 调用 `TaskExecutionService.runTask(taskId)`。

### 5. Automation Module

- **接口设计**
  - `AutomationClient`
    - `AutomationResult startExecution(ReleaseTask task)`
    - `AutomationStatus fetchStatus(AutomationExecutionRef ref)`
  - 实现类：
    - `JenkinsAutomationClient`
    - `AnsibleAutomationClient`

- **Task Execution Service**
  - `TaskExecutionService`
    - 根据 `automationType` 选择具体 `AutomationClient`。
    - 创建 `TaskExecution` 记录，保存外部 ID 和 URL。
    - 更新 `ReleaseTask` 的 `status` 字段。
    - 提供查询执行记录和日志的接口。

### 6. Real-time Updates

- **方案选型**
  - 首选 SSE：
    - 简单实现单向推送，适合状态更新场景。
    - API 示例：`GET /api/rundowns/{id}/events`
  - 备选 WebSocket：
    - 若后期有更复杂的双向通信需求可切换。

- **事件模型**
  - `TaskStatusChangedEvent`
    - `rundownId`
    - `taskId`
    - `status`
    - `externalUrl`
    - `errorSummary?`

