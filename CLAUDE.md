# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 常用开发任务

### 运行应用程序
- **开发模式**: `mvn spring-boot:run` (启动在端口 8989)
- **构建并运行**: `mvn clean package && java -jar target/springbootdemo-0.0.1-SNAPSHOT.jar`
- **数据库设置**: 确保 MySQL 在端口 3307 上运行，数据库名为 `demo`（或在 `src/main/resources/application.properties` 中调整 `spring.datasource.url`）。注意：README.md 提到端口 3306，但实际配置使用 3307。如果 `student` 表不存在，将会自动创建（检查 README.md 中的表结构）。

### 测试
- **运行所有测试**: `mvn test`
- **单个测试类**: `mvn test -Dtest=SpringboardApplicationTests`

### 构建
- **清理并打包**: `mvn clean package`
- **跳过测试**: `mvn clean package -DskipTests`

## 架构概述

这是一个使用 Spring Boot 2.2.7 和 MyBatis 进行数据访问的 CRUD 应用程序。项目遵循分层架构：

### 关键组件
1. **控制器层** (`com.springboot.demo.controller`):
   - `StudentController`: 提供 `/api/student` 下的 REST 端点
   - 返回统一的 `Result<T>` 响应（成功: code=200, 错误: code=500）
   - 所有端点遵循 REST 约定（GET/POST/PUT/DELETE）

2. **服务层** (`com.springboot.demo.service`):
   - 接口 (`StudentService`) 和实现 (`StudentServiceImpl`)
   - 使用 `@Service` 注解，写操作使用 `@Transactional` 注解
   - 控制器和映射器之间的业务逻辑

3. **数据访问层** (`com.springboot.demo.mapper`):
   - MyBatis 映射器接口，使用基于注解的 SQL（`@Select`、`@Insert` 等）
   - XML 映射文件 (`StudentMapper.xml`) 用于结果映射（当前内容较少）
   - 配置了驼峰命名到下划线转换

4. **实体/DTO**:
   - `Student`: 类似 JPA 的实体（普通 Java 对象），包含 `id`、`name`、`age`
   - `Result<T>`: 统一的 API 响应包装器，包含 `code`、`message`、`data`、`timestamp`

5. **配置类** (`com.springboot.demo.config`):
   - `MyBatisConfig`: 通过 `@MapperScan` 扫描映射器接口
   - `TransactionConfig`: 启用声明式事务管理
   - MyBatis 属性在 `application.properties` 中配置（下划线转驼峰、生成键）

6. **异常处理** (`com.springboot.demo.exception`):
   - `GlobalExceptionHandler` 捕获 `DataAccessException`、`NullPointerException`、`RuntimeException` 和一般 `Exception`
   - 所有异常返回 `Result.error()` 并附带适当的消息
   - 记录错误信息及请求 URI

7. **工具类**:
   - `DataInitializer`: 实现 `CommandLineRunner`，在启动时如果表为空则插入测试数据

### 关键模式
- **统一响应**: 所有控制器方法返回 `Result<T>` 以保持 API 响应一致性
- **全局异常处理**: 所有异常被捕获并转换为错误响应
- **基于注解的 MyBatis**: 简单查询使用注解中的 SQL，复杂映射使用 XML
- **事务管理**: 写操作在服务层使用 `@Transactional` 注解
- **自动生成 ID**: 插入操作使用 `@Options(useGeneratedKeys = true)`

## 配置说明

### 数据库
- 默认连接: `jdbc:mysql://localhost:3307/demo` (用户名: `root`, 密码: `root`)
- 如需更改，请修改 `src/main/resources/application.properties`
- 表结构在 README.md 中定义（如果不存在会自动创建）

### MyBatis
- 映射器位置: `classpath:mapper/*.xml`
- 类型别名包: `com.springboot.demo.entity`
- 启用下划线到驼峰命名映射
- SQL 日志: `logging.level.com.springboot.demo.mapper=DEBUG`

### 服务器
- 端口: 8989
- 应用名称: `sp_demo`

## API 端点

所有学生相关端点都在 `/api/student` 下：
- `GET /api/student/list` - 获取所有学生
- `GET /api/student/{id}` - 根据ID获取学生
- `POST /api/student/add` - 添加新学生（JSON 请求体）
- `PUT /api/student/update` - 更新学生信息（JSON 请求体）
- `DELETE /api/student/delete/{id}` - 根据ID删除学生
- `GET /api/student/search?name={name}` - 根据姓名搜索（LIKE 查询）
- `GET /api/student/count` - 获取学生总数

所有响应都遵循 `Result<T>` 格式。

## 开发注意事项

- 项目使用 Java 8（在 `pom.xml` 中配置）
- 依赖项：Spring Boot Web、MyBatis Starter、MySQL Connector、Apollo Client（已注释）
- 测试数据在首次启动时通过 `DataInitializer` 自动插入
- 对于新实体，遵循相同的模式：实体 → 映射器 → 服务 → 控制器
- 在映射器接口中添加新的 SQL 方法，并使用适当的注解
- 对于复杂查询，添加到 `StudentMapper.xml` 并在映射器接口中引用