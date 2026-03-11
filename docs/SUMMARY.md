# FinBlock Task Management System - 技术文档

## 项目概述

基于 Spring Boot 3.5.10 + Java 21 的任务管理系统，用于管理 FinBlock 的部署任务。

## 技术栈

- **Spring Boot**: 3.5.10
- **Java**: 21
- **数据库**: MySQL
- **ORM**: Spring Data JPA + Hibernate
- **数据库迁移**: Flyway
- **Excel 解析**: Apache POI 5.4.1
- **构建工具**: Maven 3.9.9

## 数据库表结构

### 1. templates (模板表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(255) | 模板名称 |
| description | TEXT | 模板描述 |
| created_by | VARCHAR(100) | 创建人 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted_at | TIMESTAMP | 删除时间（软删除） |

### 2. template_tasks (模板任务表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| template_id | BIGINT | 外键，关联 templates |
| task_name | VARCHAR(255) | 任务名称 |
| task_type | VARCHAR(50) | 任务类型：JENKINS, ANSIBLE, MANUAL |
| sequence_order | INT | 顺序 |
| config_json | TEXT | JSON 配置 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3. release_rundowns (发布 Rundown 表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| rundown_name | VARCHAR(255) | Rundown 名称 |
| template_id | BIGINT | 外键，关联 templates |
| status | VARCHAR(50) | 状态：PENDING, IN_PROGRESS, COMPLETED, FAILED |
| created_by | VARCHAR(100) | 创建人 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted_at | TIMESTAMP | 删除时间（软删除） |

### 4. tasks (任务表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| rundown_id | BIGINT | 外键，关联 release_rundowns |
| task_name | VARCHAR(255) | 任务名称 |
| task_type | VARCHAR(50) | 任务类型：JENKINS, ANSIBLE, MANUAL |
| status | VARCHAR(50) | 状态：PENDING, IN_PROGRESS, COMPLETED, FAILED |
| sequence_order | INT | 顺序 |
| config_json | TEXT | JSON 配置 |
| execution_url | VARCHAR(500) | 执行 URL（Jenkins/Ansible） |
| execution_log | TEXT | 执行日志 |
| error_message | TEXT | 错误信息 |
| started_at | TIMESTAMP | 开始时间 |
| completed_at | TIMESTAMP | 完成时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

## 核心功能

### 1. Excel 文件上传

**API**: `POST /api/excel/upload`

**请求**:
- Content-Type: multipart/form-data
- 参数: file (MultipartFile)

**Excel 格式要求**:

| Task Name | Task Type | Sequence Order | Config JSON (可选) |
|-----------|-----------|----------------|-------------------|
| 任务名称 | JENKINS/ANSIBLE/MANUAL | 数字 | JSON 字符串 |

**响应示例**:
```json
{
  "success": true,
  "message": "Successfully parsed 3 tasks",
  "rowCount": 3,
  "tasks": [
    {
      "taskName": "Deploy Service A",
      "taskType": "JENKINS",
      "sequenceOrder": 1,
      "configJson": "{\"pipeline\": \"deploy-a\"}"
    }
  ]
}
```

**错误响应**:
```json
{
  "success": false,
  "message": "Invalid file type",
  "errors": ["Only .xlsx and .xls files are supported"]
}
```

## 枚举类型

### TaskType
- `JENKINS` - Jenkins 流水线任务
- `ANSIBLE` - Ansible 自动化任务
- `MANUAL` - 手动执行任务

### TaskStatus
- `PENDING` - 待执行
- `IN_PROGRESS` - 执行中
- `COMPLETED` - 已完成
- `FAILED` - 失败

## 项目结构

```
src/main/java/com/taskmanagement/
├── TaskManagementApplication.java    # 启动类
├── controller/
│   └── ExcelController.java           # Excel 上传控制器
├── dto/
│   ├── ExcelUploadResult.java         # Excel 解析结果
│   ├── TemplateDTO.java               # 模板 DTO
│   └── TemplateTaskDTO.java           # 模板任务 DTO
├── entity/
│   ├── Template.java                  # 模板实体
│   ├── TemplateTask.java              # 模板任务实体
│   ├── ReleaseRundown.java            # 发布 Rundown 实体
│   └── Task.java                      # 任务实体
├── enums/
│   ├── TaskType.java                  # 任务类型枚举
│   └── TaskStatus.java                # 任务状态枚举
├── repository/
│   ├── TemplateRepository.java        # 模板仓库
│   ├── ReleaseRundownRepository.java  # Rundown 仓库
│   └── TaskRepository.java            # 任务仓库
└── service/
    └── ExcelService.java               # Excel 解析服务
```

## 待实现功能

1. **模板管理** (User Story 2)
   - 创建新模板
   - 克隆模板

2. **Release Rundown 创建** (User Story 3)
   - 从模板生成 Rundown

3. **任务控制** (User Story 4)
   - 编辑任务
   - 删除任务
   - 执行任务

4. **自动化执行** (User Story 5)
   - Jenkins 流水线触发
   - Ansible Job 触发
   - 实时状态更新

## 配置文件

`application.yml` 主要配置:
- 服务器端口: 8080
- MySQL 数据库连接
- 文件上传限制: 20MB
- JPA/Hibernate 配置

## 运行项目

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package
```

---

**生成日期**: 2026-03-11
**版本**: 1.0.0
