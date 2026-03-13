## Research & Assumptions

### Technology Choices

- **Spring Boot + Java 21**
  - 利用了 Spring Boot 3.x 对 Java 21 的支持，简化配置与开发。

- **Persistence**
  - 开发阶段使用 H2 内存数据库：
    - 配置简单，适合快速迭代与单元/集成测试。
    - 生产阶段可以平滑切换到 MySQL/PostgreSQL。
  - 通过 Spring Data JPA 屏蔽大部分底层 SQL 实现细节。

- **Excel 解析**
  - Apache POI：
    - 功能成熟，支持 `.xls` 与 `.xlsx`。
    - 可与 EasyExcel 搭配提升大文件读写性能。

- **自动化执行集成**
  - Jenkins：
    - 通过 REST API 触发构建并查询构建状态。
  - Ansible（Tower/AWX）：
    - 通过其 HTTP API 触发 job template 执行并查询状态。
  - 使用统一接口封装，避免在业务层散落 Jenkins/Ansible 细节。

### Constraints & Open Questions

- **认证与授权**
  - 当前设计假定：
    - 应用已在受控环境中部署。
    - 用户认证与权限在网关或上游系统完成。
  - 后续需明确：
    - 是否需要在本服务中引入用户/角色模型。
    - 不同用户对模板和 Rundown 的访问控制策略。

- **Jenkins/Ansible 连接配置**
  - 需要在配置中约定：
    - Base URL、认证方式（token / basic auth）。
    - 触发 pipeline/job 所需的参数格式约定。

- **任务执行状态更新机制**
  - 初版可以采用轮询或回调二选一：
    - 轮询：由本服务周期性查询外部系统状态。
    - Webhook：由 Jenkins/Ansible 调用回调接口。
  - 目前倾向先实现轮询（简单可控），后续可根据需要引入 webhook。

### Risks

- **外部系统不可用**
  - Jenkins/Ansible 不可用时，Run 操作会失败。
  - 需要：
    - 清晰的错误信息与重试策略。
    - 对外暴露健康检查状态（如自动化集成是否可用）。

- **Excel 质量问题**
  - 不同团队可能使用略有偏差的模板。
  - 需在解析逻辑中做好兼容性处理（如容忍额外列、忽略空行等）。

