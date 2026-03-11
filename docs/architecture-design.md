# FinBlock 任务管理系统 - 架构设计

## 1. 设计背景

基于需求分析，设计一套支持任务管理、模板复用、自动化执行的系统架构。

**核心假设**：
- 需要用户认证
- Jenkins/Ansible 已存在并提供 API
- Excel 上传和解析需要可靠处理
- 任务状态需要实时/近实时同步

---

## 2. 系统架构图

```mermaid
C4Context
    title FinBlock System Context
    
    Person(user, "FinBlock User", "管理部署任务的用户")
    
    System_Boundary(finblock, "FinBlock System") {
        System(frontend, "Web Frontend", "React/Vue 用户界面")
        System(backend, "Backend API", "Node.js/Java/Python 后端服务")
        System(scheduler, "Job Scheduler", "任务调度和状态同步")
    }
    
    System_Ext(jenkins, "Jenkins", "CI/CD Pipeline 触发")
    System_Ext(ansible, "Ansible", "自动化作业执行")
    System_Ext(storage, "File Storage", "Excel 文件存储")
    System_Ext(db, "Database", "PostgreSQL/MySQL")
    
    Rel(user, frontend, "使用")
    Rel(frontend, backend, "REST API")
    Rel(backend, db, "读写数据")
    Rel(backend, storage, "上传/下载文件")
    Rel(scheduler, jenkins, "触发构建")
    Rel(scheduler, ansible, "触发作业")
    Rel(jenkins, scheduler, "状态回调")
    Rel(ansible, scheduler, "状态回调")
```

---

## 3. 后端服务架构

```mermaid
flowchart TB
    subgraph Client["前端层"]
        Web[Web App]
    end
    
    subgraph Gateway["网关层"]
        API[API Gateway / Nginx]
        Auth[认证服务]
    end
    
    subgraph Core["核心服务"]
        TM[任务管理服务]
        Template[模板服务]
        Excel[Excel 处理服务]
        Exec[执行引擎]
    end
    
    subgraph Platform["集成平台"]
        Jenkins[Jenkins API]
        Ansible[Ansible API]
    end
    
    subgraph Data["数据层"]
        DB[(数据库)]
        Cache[(Redis Cache)]
        File[文件存储]
    end
    
    Web --> API
    API --> Auth
    Auth --> TM
    Auth --> Template
    TM --> Excel
    TM --> Exec
    Exec --> Jenkins
    Exec --> Ansible
    TM --> DB
    TM --> Cache
    Excel --> File
```

---

## 4. 核心模块设计

### 4.1 模块职责

| 模块 | 职责 | 技术建议 |
|------|------|----------|
| **Task Management** | 任务的 CRUD、状态管理 | REST API |
| **Template Service** | 模板的创建、克隆、存储 | 继承 Task Management |
| **Excel Processor** | Excel 解析、验证、转换 | Apache POI / xlsx |
| **Execution Engine** | 触发 Jenkins/Ansible、状态同步 | 消息队列 + Webhook |
| **Auth Service** | 用户认证、权限 | JWT / OAuth2 |

### 4.2 数据库 Schema

```mermaid
erDiagram
    User ||--o{ Project : owns
    Project ||--o{ Template : contains
    Project ||--o{ ReleaseRundown : contains
    ReleaseRundown ||--o{ Task : contains
    Task ||--o{ ExecutionLog : has
    
    User {
        uuid id
        string name
        string email
        string password_hash
        datetime created_at
    }
    
    Project {
        uuid id
        string name
        uuid owner_id
        datetime created_at
    }
    
    Template {
        uuid id
        uuid project_id
        string name
        jsonb structure
        datetime created_at
        datetime updated_at
    }
    
    ReleaseRundown {
        uuid id
        uuid project_id
        uuid template_id
        string name
        string status
        datetime created_at
    }
    
    Task {
        uuid id
        uuid rundown_id
        string name
        jsonb params
        string type
        string status
        string external_job_url
        datetime created_at
        datetime updated_at
    }
    
    ExecutionLog {
        uuid id
        uuid task_id
        string status
        string output
        string error_message
        datetime started_at
        datetime finished_at
    }
```

---

## 5. 关键流程设计

### 5.1 Excel 上传流程

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant S as Storage
    participant DB as Database
    
    U->>F: 选择 Excel 文件
    F->>B: POST /api/upload
    B->>S: 保存文件
    B->>B: 解析 Excel
    alt 解析成功
        B->>DB: 创建任务
        B->>F: 返回任务列表
    else 解析失败
        B->>F: 返回错误详情
    end
```

### 5.2 任务执行流程

```mermaid
sequenceDiagram
    participant U as User
    participant B as Backend
    participant J as Jenkins/Ansible
    participant DB as Database
    participant W as Webhook
    
    U->>B: 点击 Run
    B->>DB: 更新状态为 Running
    alt Jenkins Job
        B->>J: POST /job/{name}/build
    else Ansible Job
        B->>J: POST /api/jobs
    end
    J-->>W: 构建状态变更
    W->>DB: 更新任务状态
    B->>U: 推送状态更新
```

---

## 6. API 设计 (REST)

### 资源定义

| 资源 | 路径 | 方法 |
|------|------|------|
| 项目 | `/api/v1/projects` | GET, POST |
| 模板 | `/api/v1/templates` | GET, POST, PUT, DELETE |
| 模板克隆 | `/api/v1/templates/{id}/clone` | POST |
| Release Rundown | `/api/v1/rundowns` | GET, POST |
| 任务 | `/api/v1/tasks` | GET, PUT, DELETE |
| 任务执行 | `/api/v1/tasks/{id}/run` | POST |
| 文件上传 | `/api/v1/upload` | POST |

### 响应格式

```json
{
  "success": true,
  "data": { },
  "error": null
}
```

---

## 7. 技术选型建议

| 层级 | 推荐方案 | 备选 |
|------|----------|------|
| **后端** | Spring Boot (Java) / NestJS (Node) | FastAPI (Python) |
| **数据库** | PostgreSQL | MySQL |
| **缓存** | Redis | - |
| **文件存储** | 本地 / S3 / MinIO | - |
| **消息队列** | Redis Pub/Sub / RabbitMQ | - |
| **前端** | React / Vue 3 | - |

---

## 8. 安全考虑

- [ ] 用户认证 (JWT)
- [ ] API 权限校验
- [ ] 文件类型白名单
- [ ] Jenkins/Ansible 凭证加密存储
- [ ] 请求限流
- [ ] 敏感日志脱敏

---

## 9. 待确认事项

1. **技术栈偏好**：Java / Node.js / Python？
2. **部署环境**：云还是私有？
3. **Excel 模板格式**：有现成的模板定义吗？
4. **Jenkins/Ansible 认证**：使用 API Token？
性要求**：WebSocket 还是轮5. **实时询？

---

*文档版本：v1.0*
*创建时间：2026-03-11*
