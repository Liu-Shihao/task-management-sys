## Product Specification

### Context

- **Product**: FinBlock Task Management System
- **Goal**: 通过模板化和自动化执行能力，帮助用户快速、可重复地生成和执行发布任务（Release Rundown）。
- **Primary Users**: DevOps / 运维 / 发布工程师（FinBlock 用户）

### Scope (MVP)

- **Excel 导入**
  - 支持 `.xlsx` / `.xls` 文件上传。
  - 校验文件类型与基础格式，解析为结构化数据。
  - 对格式和必填字段错误给出清晰的错误信息（行、列、原因）。

- **模板管理**
  - 创建模板及其任务列表。
  - 克隆现有模板，用于快速复制配置。
  - 编辑模板任务（名称、顺序、自动化配置、参数）。

- **Release Rundown 创建**
  - 从选定模板生成一个 Release Rundown。
  - Rundown 具备唯一标识和状态（初始为 `PENDING`）。
  - Rundown 中包含任务列表及其顺序和初始状态。

- **任务控制**
  - 对 Rundown 中的任务提供 `Run` / `Edit` / `Delete` 控制。
  - Edit：修改任务参数和自动化配置。
  - Delete：删除任务（带前端确认）。
  - Run：触发自动化执行流程。

- **自动化执行**
  - `Run` 按钮触发 Jenkins Pipeline 或 Ansible Job。
  - 记录外部执行 ID 与 URL，更新任务状态。
  - 将状态变化（`IN_PROGRESS` / `COMPLETED` / `FAILED`）实时反映到 UI。
  - 失败时，能从任务视图访问错误信息和日志。

### Out of Scope (当前版本不做)

- 完整的权限/角色系统（使用简单用户标识占位）。
- 多租户隔离。
- 复杂的审批流与并行任务编排。
- 高可用部署与微服务拆分（当前为单体 Spring Boot 应用）。

### Non-Functional Requirements

- **可维护性**：清晰的分层结构（controller / service / repository / integration）。
- **可观测性**：关键任务执行路径需有日志记录（包括外部调用结果）。
- **可扩展性**：自动化执行模块通过策略模式支持后续新增执行类型（如脚本、内部 API）。
- **性能**：MVP 面向中小规模发布任务，单次 Rundown 任务数以百级为主。

