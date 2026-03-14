# 数据结构对齐说明（data-model / design / openapi）

**结论**：三份文档在「Rundown 内置调度、实体集合、字段名」上一致。以下**未严格对应**项已按本节修正，当前已对齐。

---

## 1. 对应关系总览

| 来源 | 作用 | 与 data-model 关系 |
|------|------|---------------------|
| **data-model.md** | 数据库表/实体字段定义（BIGINT/VARCHAR/JSON 等） | 基准 |
| **design.md** | 模块、目录、实体类名（对应 data-model 实体） | 实体列表、目录结构须与 data-model 一致 |
| **openapi.yaml** | API 请求/响应结构（JSON，常用 snake_case） | Schema 字段与 data-model 实体字段对应；id 类型应为 int64 |

---

## 2. 已发现的不一致与处理

### 2.1 design.md 与 data-model 不一致

| 问题 | 位置 | 说明 |
|------|------|------|
| 存在 ScheduledTask 相关类 | design.md 2.2 目录结构 | data-model 明确「无独立 ScheduledTask，仅 Rundown 支持 schedule」；design 中仍列有 `ScheduleController.java`、`ScheduledTaskRepository.java`、`ScheduledTask.java`。 |

**处理**：已从 design.md 目录结构中删除上述三个类，与 data-model 及「Rundown 内置调度」一致。

### 2.2 openapi.yaml 与 data-model 类型不一致

data-model 中所有主键、外键均为 **BIGINT**，实现为 Long (int64)。OpenAPI 中部分 schema 仍使用 **format: uuid**，与 data-model/实现不符。

| Schema / 字段 | 当前 openapi | data-model / 实现 | 处理 |
|---------------|--------------|-------------------|------|
| User.id | uuid | BIGINT | 改为 integer, format: int64 |
| Template.id, created_by | uuid | BIGINT | 改为 int64 |
| TemplateTask.id | uuid | BIGINT | 改为 int64 |
| Rundown.* | 已部分 int64 | BIGINT | 已对齐 |
| Task.id, rundown_id | uuid | BIGINT | 改为 int64 |
| ExecutionLog.id, task_id | uuid | BIGINT | 改为 int64 |
| SystemConfig.id | uuid | BIGINT | 改为 int64 |
| 各 path 的 template_id / rundown_id / task_id 等 | 部分 uuid | Long | 统一为 integer, format: int64 |

**处理**：已在 openapi.yaml 中将上述 id/外键及 path 参数统一为 `type: integer`, `format: int64`。

### 2.3 字段命名与可选性

- **data-model** 与 **openapi** 均使用 **snake_case**（如 `rundown_code`, `created_at`, `schedule_type`），命名一致。
- **design.md** 仅涉及类名与包路径，不定义字段，无需改字段名。
- 可选性：data-model 中 NULL 字段与 openapi 中 `nullable: true` / 无 required 对应一致；Rundown、Task 等状态枚举与 data-model 说明一致。

### 2.4 其他

- **TemplateTask**：data-model 有 `created_at`，openapi 的 TemplateTask 未暴露该字段；若 API 不需要可保持现状，仅列表/详情若需“创建时间”可补充。
- **User**：API 不返回 `password_hash`，与安全约定一致，无需在 openapi 中增加。

---

## 3. 修正后的严格对应关系

完成上述修正后：

- **data-model.md**：唯一的数据结构基准（表名、字段名、类型、约束、枚举值）。
- **design.md**：实体类与 data-model 实体一一对应，无 ScheduledTask；目录中无 ScheduleController/ScheduledTaskRepository/ScheduledTask。
- **openapi.yaml**：  
  - 所有实体 id 及外键为 **integer (int64)**；  
  - 字段名与 data-model 的 snake_case 一致；  
  - 枚举（如 status、schedule_type、schedule_status）与 data-model 一致；  
  - 定时调度仅通过 Rundown 的 schedule 相关字段与 `PUT/DELETE /rundowns/{id}/schedule` 表达，无 ScheduledTask 资源。

---

## 4. 维护建议

- 新增实体或字段时，先在 **data-model.md** 增加表/字段定义，再在 **design.md** 补实体/仓库类（若有），最后在 **openapi.yaml** 补/改 schema 与 path，并保持 id 为 int64。
- 定期用本文件做一次三文档对照检查，避免再次出现「uuid vs int64」「ScheduledTask 残留」类偏差。
