# 技术调研与风险分析 (Research)

## 1. 技术方案对比

### 1.1 Excel 解析方案

| 方案 | 优点 | 缺点 | 推荐 |
|------|------|------|------|
| **xlsx (SheetJS)** | 成熟稳定、社区活跃、纯 JS | 大文件性能一般 | ✅ 推荐 |
| exceljs | 功能丰富、支持样式 | 体积较大 | 备选 |
| papaparse | 轻量 (CSV 为主) | 不适合 Excel | 不适用 |

### 1.2 任务队列方案

| 方案 | 优点 | 缺点 | 推荐 |
|------|------|------|------|
| **Bull + Redis** | 成熟、延迟队列、分布式支持 | 需要 Redis | ✅ 推荐 |
| RabbitMQ | 功能丰富 | 部署复杂 | 备选 |
| Kafka | 高吞吐 | 延迟较高 | 不适合 |

### 1.3 实时推送方案

| 方案 | 优点 | 缺点 | 推荐 |
|------|------|------|------|
| **Socket.io** | 简单易用、自动重连 | WebSocket 兼容 | ✅ 推荐 |
| WebSocket原生 | 轻量 | 需要自己处理断线 | 备选 |
| SSE | 简单、单向 | 不支持双向 | 不适用 |

### 1.4 执行器插件架构

```typescript
interface ExecutorPlugin {
  type: string;              // 'jenkins' | 'ansible' | 'custom'
  
  // 执行任务
  execute(config: TaskConfig): Promise<ExecutionResult>;
  
  // 获取执行状态
  getStatus(jobUrl: string): Promise<JobStatus>;
  
  // 取消执行
  cancel(jobUrl: string): Promise<void>;
}

// 插件注册
const executors = new Map<string, ExecutorPlugin>();
executors.set('jenkins', new JenkinsExecutor());
executors.set('ansible', new AnsibleExecutor());
```

---

## 2. 风险点分析

### 2.1 高风险

| 风险 | 描述 | 缓解措施 |
|------|------|----------|
| **外部系统不可用** | Jenkins/Ansible 服务宕机 | 添加重试机制 + 告警 |
| **Webhook 回调失败** | 外部系统未回调导致状态不一致 | 定时轮询补偿 + 告警 |
| **文件上传安全** | 恶意文件上传 | MIME 校验 + 文件扫描 |

### 2.2 中风险

| 风险 | 描述 | 缓解措施 |
|------|------|----------|
| **大文件解析超时** | Excel 文件过大导致解析慢 | 分片解析 + 异步处理 |
| **任务并发冲突** | 多用户同时操作同一 rundown | 乐观锁 + 状态校验 |
| **日志存储膨胀** | 大量执行日志占用空间 | 冷热分离 + 定期归档 |

### 2.3 低风险

| 风险 | 描述 | 缓解措施 |
|------|------|----------|
| **时区问题** | 多时区用户时间显示 | 统一存 UTC，前端转换 |
| **中文文件名** | 文件名编码问题 | URL 编码处理 |

---

## 3. 外部系统集成要点

### 3.1 Jenkins 集成

```bash
# 触发 Jenkins Job
curl -X POST http://JENKINS_URL/job/{jobName}/buildWithParameters \
  --user USER:API_TOKEN \
  -d "PARAM1=value1" \
  -d "PARAM2=value2"

# Webhook 回调 payload (Jenkins 插件)
{
  "build": {
    "number": 123,
    "status": "SUCCESS",
    "url": "http://jenkins/job/xxx/123/"
  }
}
```

### 3.2 Ansible Tower 集成

```bash
# 触发 Ansible Job Template
curl -X POST http://TOWER_URL/api/v2/job_templates/{id}/launch/ \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"extra_vars": {...}}'

# 回调 payload
{
  "job": {
    "id": 456,
    "status": "successful",
    "web_url": "https://tower/job_output/456"
  }
}
```

---

## 4. 性能估算

| 场景 | 估算 | 说明 |
|------|------|------|
| 文件上传 | < 10MB | 单文件大小限制 |
| Excel 解析 | < 5s (1000行) | 异步处理 |
| API 响应 | < 200ms | P95 延迟 |
| 任务状态推送 | < 500ms | WebSocket |
| 并发任务数 | 50+ | 支持多 rundown 并行 |
