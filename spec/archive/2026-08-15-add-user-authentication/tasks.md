# 实施任务：用户登录认证与 API Key 管理

## 阶段 1：基础设施

- [x] 1. 新增 `src/main/resources/db/auth.sql`，创建 `app_user`、`api_key` 表
- [x] 2. `pom.xml` 添加 `spring-boot-starter-security` 依赖

## 阶段 2：后端核心实现

- [x] 3. 新增 `AppUser`、`ApiKey` 实体
- [x] 4. 新增 `AppUserMapper`、`ApiKeyMapper`（MyBatis 注解 SQL）
- [x] 5. 新增 `AppUserService`、`ApiKeyService`（sk-key 生成、SHA-256 哈希、创建/列表/撤销/校验）
- [x] 6. 新增 `security/SecurityConfig`（表单登录、权限规则、401 JSON 入口）
- [x] 7. 新增 `security/DbUserDetailsService`（从 `app_user` 加载用户）
- [x] 8. 新增 `security/ApiKeyAuthenticationFilter`（Bearer apikey 校验、注入 ROLE_API）
- [x] 9. 新增 `controller/ApiKeyController`（`/api/apikey/list|create|{id}`）
- [x] 10. `DataInitializer` 启动时预置默认管理员 `admin/123456`

## 阶段 3：前端集成

- [x] 11. 新增 `static/login.html` 登录页
- [x] 12. 新增 `static/apikey.html` + `static/js/apikey.js` 密钥管理页
- [x] 13. `index.html`/`admin.html` 头部增加 apikey 菜单与退出登录按钮
- [x] 14. `app.js` 增加 axios 请求/响应拦截器（Bearer 注入、401 处理）
- [x] 15. `admin.js` 增加 401 拦截器（会话过期回登录页）

## 阶段 4：验证（端到端，全部通过）

- [x] 16. 未登录访问 index/admin/apikey.html → 跳转 `/login.html`
- [x] 17. `admin/123456` 登录成功；错误密码返回登录页并提示
- [x] 18. 会话访问 `/api/admin/**` 正常；会话访问 `/api/db/**` 返回 401（仅 apikey）
- [x] 19. 创建 apikey 返回完整 sk-key；列表显示掩码与状态；`last_used_at` 记录
- [x] 20. Bearer 调用 `/api/db/tree`、`/api/db/query` 返回数据
- [x] 21. 撤销 apikey 后再调用返回 401「apikey 无效或已撤销」
- [x] 22. 退出登录后会话失效，访问接口返回 401
