# SDD 文档评审报告

**评审依据**: `requirement_zh.md`（需求文档）  
**评审日期**: 2026-03-14  
**评审范围**: sdd/spec.md, architecture.md, design.md, data-model.md, task.md, research.md, contracts/openapi.yaml  

---

## 1. 与需求文档的符合性

### 1.1 用户故事覆盖

| 需求 (requirement_zh.md) | SDD 覆盖 | 说明 |
|--------------------------|----------|------|
| US1 Excel 上传 | ✅ spec FR1, design 导入模块 | 完整 |
| US2 模板管理 | ✅ spec FR2, data-model Template/TemplateTask | 完整 |
| US3 发布 Rundown 创建 | ✅ spec FR3, Rundown 编号与状态 | 完整 |
| US4 任务控制（运行/编辑/删除） | ✅ spec FR4, TaskController | 完整 |
| US5 自动化执行（Jenkins/Ansible） | ✅ spec FR5, JenkinsExecutor/AnsibleExecutor | 完整 |
| US6 定时执行（立即/一次性/周期） | ✅ spec FR6, design 中 Rundown 内置调度 | 完整 |
| US7 Rundown Pipeline 顺序执行 | ✅ spec FR3.7/FR3.8, RundownExecutor | 完整 |
| US8 批量 Rundown 执行 | ✅ spec FR7, BatchExecutor | 完整 |

结论：**功能范围与需求文档一致，无需补充用户故事。**

### 1.2 技术栈符合性

需求文档明确写出：

- Spring Boot、Java 21、Maven、**H2(内存数据库)**、JPA

当前 SDD 中的不一致：

- **spec.md** 第 5.1、5.3 节：技术栈与数据库仍标为「待确认」，TBD 列表中仍有「技术栈选择」「数据库选型」等，与已定技术栈冲突。
- **architecture.md / research.md**：仅写 MySQL 8.x，未体现「H2 作为开发/测试默认数据源」。
- **design.md**：已正确区分 dev 用 H2、prod 用 MySQL，与需求中“H2(内存数据库)”一致，建议其他文档与之对齐。

**建议**：在 spec、architecture、research、task 中统一写明：开发/测试默认使用 H2，生产可选 MySQL；并删除或更新已决的 TBD 项。

---

## 2. 需要修改与优化的内容

### 2.1 包名不一致（高）

- **现状**：architecture.md、design.md 中包路径为 `com.taskman.*`。
- **实际代码**：`com.taskmanagement.*`（与项目名 task-management-sys 一致）。
- **建议**：SDD 中所有 `com.taskman` 统一改为 `com.taskmanagement`，避免开发时误建错误包。

### 2.2 OpenAPI 与实现不一致（高）

| 问题 | 位置 | 说明与建议 |
|------|------|------------|
| 服务端口 | servers.port | 已改为 8080，与 application.yml 一致。 |
| ID 类型 | 所有 path/schema 的 id | 文档使用 `format: uuid`，实体为 `Long` 主键。Rundown 等部分 schema 已改为 int64；其余可按需逐步统一。 |
| 任务排序接口路径 | reorder | 已统一为 `PUT /rundowns/{rundown_id}/tasks/reorder`，body 为 `task_ids` 顺序列表。 |
| 定时调度模型 | openapi `/schedules` 与 ScheduledTask | **已落实**：仅 Rundown 支持 schedule，无 ScheduledTask。OpenAPI 已改为 `PUT/DELETE /rundowns/{rundown_id}/schedule`，并移除 `/schedules` 与 ScheduledTask 等 schema；定时任务列表通过 `GET /rundowns?schedule_status=...` 筛选。 |

### 2.3 spec.md 中 TBD/约束（中）

- 第 5.1 节「技术约束」、第 5.3 节「待确认事项」中，技术栈、数据库、认证等已在需求/设计中明确，建议：
  - 将技术栈确定为：Java 21、Spring Boot 3.x、Maven、JPA；开发/测试 H2，生产可选 MySQL。
  - 删除或标记为已决：TBD-1（技术栈）、TBD-2（前端）、TBD-3（数据库）、TBD-6（认证方案）等，或移至「后续版本」说明。

### 2.4 数据库表述统一（中）

- **architecture.md**：表格中仅写 MySQL，建议补充「开发/测试：H2（内存），生产：MySQL」。
- **research.md**：同样补充 H2 作为默认开发库的说明。
- **task.md**：T010「配置 JPA + MySQL 数据源」建议改为「配置 JPA 数据源（H2/MySQL 多环境）」，与 design 的 profile 配置一致。

### 2.5 其他一致性（低）

- **design.md** 第 2.4 节有两处「2.4」（调度服务设计、Rundown 执行模块），建议将后者改为 2.5，章节号连续。
- **data-model.md**：与当前实体定义一致，无需修改；若未来增加软删除，可按文档 5.2 节扩展。

---

## 3. 建议的优化（可选）

- **spec.md**：在 1.3「业务边界」或 5.1「技术约束」中明确「V1.0 默认使用 H2，便于本地与验收；生产可切换 MySQL」。
- **traceability**：保持现有 User Story → FR → AC 追溯矩阵，便于后续迭代。
- **openapi**：若采用「Rundown 内置调度」方案，在 OpenAPI 中为 Rundown 增加 `schedule_type`、`cron_expression`、`run_time`、`schedule_status`、`next_run_time` 等字段，并增加「设置/取消 Rundown 调度」的接口说明。

---

## 4. 修改项汇总

| 优先级 | 文件 | 修改内容 |
|--------|------|----------|
| 高 | spec.md | 技术栈与数据库确定为 Java 21 + Spring Boot + H2/MySQL；清理已决 TBD |
| 高 | architecture.md | 包名 `com.taskman` → `com.taskmanagement`；补充 H2 开发/测试数据源 |
| 高 | design.md | 包名 `com.taskman` → `com.taskmanagement`；章节号 2.4 重复处改为 2.5 |
| 高 | openapi.yaml | 端口 8000 → 8080；id 类型 uuid → integer (int64)；reorder 路径与 body 与实现一致；定时调度与 Rundown 内置设计二选一并统一 |
| 中 | research.md | 补充 H2 作为开发默认数据源；包名若出现则改为 taskmanagement |
| 中 | task.md | T010 描述支持 H2/MySQL 多环境；包名若出现则改为 taskmanagement |

---

## 5. 结论

- SDD 在**功能范围、用户故事、数据模型、Rundown 顺序执行与定时调度设计**上与 `requirement_zh.md` 一致，整体结构清晰，可实施性强。
- **必须修改**：包名统一为 `com.taskmanagement`；OpenAPI 的端口、ID 类型、reorder 路径及定时调度模型与实现/设计一致；spec 技术栈与 TBD 更新。
- **建议修改**：各文档中数据库表述统一为「H2（开发/测试）+ MySQL（生产可选）」。
- 完成上述修改后，SDD 与需求文档及当前代码可实现一致对齐，便于后续维护与迭代。
