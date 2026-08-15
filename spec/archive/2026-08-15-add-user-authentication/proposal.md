# 提案：用户登录认证与 API Key 管理

## Why

数据浏览器页面（`index.html`、`admin.html`）与管理接口当前无任何认证，任何人可访问数据库数据。需要为所有用户提供登录认证，并要求数据接口通过 apikey 校验后调用，机制参考调用大模型时的 apikey（`sk-xxx`、`Authorization: Bearer`、可创建/撤销）。

**背景**：
- 项目为 Spring Boot 2.2.7 + MyBatis 的数据库数据浏览器
- 现有 `/api/db/**`、`/api/admin/**`、`/api/student/**` 接口无鉴权
- 无任何认证基础设施（无 Spring Security、无用户体系）

**当前状态**：任何知道 URL 的人均可直接访问页面并调用接口查询数据库。

**期望状态**：
- 用户须以用户名/密码登录后才能进入页面
- 数据接口 `/api/db/**` 仅接受有效 apikey 认证
- 登录用户在 "apikey" 页面管理自己的密钥

## What Changes

- 新增 `app_user`、`api_key` 表与建表脚本 `db/auth.sql`
- `pom.xml` 引入 `spring-boot-starter-security`
- 新增 AppUser / ApiKey 实体、Mapper、Service（sk-key 生成、SHA-256 哈希、创建/列表/撤销/校验）
- 新增安全配置：`SecurityConfig`、`DbUserDetailsService`、`ApiKeyAuthenticationFilter`
- 新增 `ApiKeyController`（`/api/apikey/list|create|{id}`）
- `DataInitializer` 预置默认管理员 `admin/123456`（BCrypt）
- 新增登录页 `login.html`、密钥管理页 `apikey.html` 及 `js/apikey.js`
- `index.html`/`admin.html` 增加 apikey 菜单与退出登录按钮
- `app.js`/`admin.js` 增加 axios 拦截器（自动携带 Bearer、401 处理）

## Impact

### 受影响的规范
- `spec/specs/authentication/spec.md` - 新增（用户登录认证、数据接口 apikey 校验、apikey 管理）

### 受影响的代码
- `src/main/java/com/springboot/demo/security/` - 新增安全组件
- `src/main/java/com/springboot/demo/entity|mapper|service/` - 新增用户与密钥分层
- `src/main/java/com/springboot/demo/controller/ApiKeyController.java` - 新增
- `src/main/resources/static/` - 新增 login.html、apikey.html、js/apikey.js；修改 index.html、admin.html、js/app.js、js/admin.js
- `pom.xml`、`src/main/java/com/springboot/demo/util/DataInitializer.java` - 修改

### 用户影响
- 所有用户须登录后使用页面
- 网页端查询数据前须先创建并启用 apikey
- 外部程序调用数据接口须携带 `Authorization: Bearer <sk-key>` 头

### API 变更
- 新增端点：`POST /api/apikey/create`、`GET /api/apikey/list`、`DELETE /api/apikey/{id}`
- 行为变更：`/api/db/**` 现在仅接受 apikey 认证（未认证返回 401 JSON）
- 新增页面：`/login.html`、`/apikey.html`；未登录访问页面跳转登录页

### 需要迁移
- [x] 数据库迁移（执行 `db/auth.sql` 建表）
- [ ] API 版本提升
- [ ] 用户沟通
- [x] 文档更新（README、规范文档）

## 时间线评估

中（一次性实现 + 端到端验证）

## 风险

- Spring Security 与既有静态资源/接口兼容性：通过 `permitAll` 放行静态资源并逐项验证
- 会话与 apikey 双体系并存：`/api/db/**` 严格仅 apikey，前端通过拦截器携带，已消除混淆
- 密钥明文安全性：服务端仅存哈希，明文仅创建时展示一次
