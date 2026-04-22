# Spring Boot Student CRUD 项目

基于 Spring Boot 2.2.7 和 MyBatis 的学生信息管理系统。

## 项目结构

```
src/main/java/com/springboot/demo/
├── SpringboardApplication.java     # 主启动类
├── config/                         # 配置类
│   ├── MyBatisConfig.java         # MyBatis配置
│   ├── TransactionConfig.java     # 事务配置
├── controller/                     # 控制器层
│   ├── DemoController.java        # 示例控制器
│   └── StudentController.java     # 学生控制器
├── dto/                           # 数据传输对象
│   └── Result.java                # 统一响应封装
├── entity/                        # 实体类
│   └── Student.java               # 学生实体
├── exception/                     # 异常处理
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── mapper/                        # MyBatis Mapper接口
│   └── StudentMapper.java         # 学生Mapper
├── service/                       # 服务层接口
│   └── StudentService.java        # 学生服务接口
├── service/impl/                  # 服务层实现
│   └── StudentServiceImpl.java    # 学生服务实现
└── util/                          # 工具类
    └── DataInitializer.java       # 数据初始化
```

## 数据库配置

在 `src/main/resources/application.properties` 中配置数据库连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

请根据实际环境修改数据库连接信息。

## Student 表结构

```sql
CREATE TABLE `student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id主键',
  `name` varchar(100) DEFAULT NULL COMMENT '姓名',
  `age` double DEFAULT NULL COMMENT '年龄',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

## API 接口

### 学生管理接口

基础URL: `http://localhost:8989/api/student`

#### 1. 获取所有学生
```
GET /api/student/list
```
响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "age": 20.0
    },
    {
      "id": 2,
      "name": "李四",
      "age": 22.0
    }
  ],
  "timestamp": 1618901234567
}
```

#### 2. 根据ID获取学生
```
GET /api/student/{id}
```
示例：`GET /api/student/1`

#### 3. 添加学生
```
POST /api/student/add
Content-Type: application/json

{
  "name": "张三",
  "age": 20.0
}
```

#### 4. 更新学生信息
```
PUT /api/student/update
Content-Type: application/json

{
  "id": 1,
  "name": "张三",
  "age": 21.0
}
```

#### 5. 删除学生
```
DELETE /api/student/delete/{id}
```
示例：`DELETE /api/student/delete/1`

#### 6. 根据姓名搜索
```
GET /api/student/search?name=张
```

#### 7. 获取学生总数
```
GET /api/student/count
```

## 运行项目

1. 确保已安装 Java 8 和 Maven
2. 创建MySQL数据库 `test`，并执行上面的建表SQL
3. 修改 `application.properties` 中的数据库连接信息
4. 运行：
```bash
mvn spring-boot:run
```
或
```bash
mvn clean package
java -jar target/springbootdemo-0.0.1-SNAPSHOT.jar
```

## 技术栈

- Spring Boot 2.2.7.RELEASE
- MyBatis Spring Boot Starter 2.2.2
- MySQL Connector Java
- Java 8

## 特性

- 统一响应格式封装
- 全局异常处理
- 声明式事务管理
- 自动生成主键
- 数据初始化（启动时自动插入测试数据）
- 支持字段驼峰命名和下划线转换
