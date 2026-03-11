# Task Management System

## Project Setup

### Requirements
- Java 21 ✅ (已安装)
- Maven (需要安装)

### Install Maven (macOS)
```bash
# 方法1: Homebrew
brew install maven

# 方法2: 手动下载
# https://maven.apache.org/download.cgi
```

### Quick Start

1. **安装 Maven 后运行:**
```bash
cd /Users/liushihao/Downloads/Projects/task-management-sys
mvn spring-boot:run
```

2. **访问:**
- Health Check: http://localhost:8080/api/health

## Project Structure
```
task-management-sys/
├── pom.xml                    # Maven 配置 (Spring Boot 3.2.5)
├── src/
│   ├── main/
│   │   ├── java/com/taskmanagement/
│   │   │   ├── TaskManagementApplication.java
│   │   │   └── controller/
│   │   │       └── HealthController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/taskmanagement/
```

## 技术栈
- Java 21
- Spring Boot 3.2.5 (Web)
- Maven

## 注意事项
- Spring Boot 3.2.5 是最新的稳定版本，支持 Java 21，无已知严重漏洞
- 默认端口: 8080
