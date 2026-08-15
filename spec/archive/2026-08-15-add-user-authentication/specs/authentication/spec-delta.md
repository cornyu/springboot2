# 规范差异：authentication

本文件包含对 `spec/specs/authentication/spec.md` 的规范变更。

## ADDED 需求

### Requirement: 用户登录认证
WHEN 用户提交用户名和密码,
系统 SHALL 认证用户并创建会话。

#### Scenario: 登录成功
GIVEN 用户已注册，用户名 "admin" 且密码 "123456"
WHEN 用户提交登录表单
THEN 系统创建已认证会话
AND 重定向至数据浏览器页面

#### Scenario: 登录失败
GIVEN 用户提供了错误的密码
WHEN 用户提交登录表单
THEN 系统拒绝登录尝试
AND 显示错误信息并返回登录页

---

### Requirement: 数据接口 apikey 校验
WHEN 请求访问 `/api/db/**` 数据接口,
系统 SHALL 校验 `Authorization: Bearer` 头中的 apikey 有效且未撤销。

#### Scenario: 有效 apikey
GIVEN 请求携带有效且未撤销的 sk-key
WHEN 请求访问数据接口
THEN 系统放行请求并返回数据

#### Scenario: 缺少 apikey
GIVEN 请求未携带 `Authorization: Bearer` 请求头
WHEN 请求访问数据接口
THEN 系统返回 HTTP 401
AND 提示需携带 apikey

#### Scenario: apikey 已撤销
GIVEN 请求携带已撤销的 sk-key
WHEN 请求访问数据接口
THEN 系统返回 HTTP 401
AND 提示 "apikey 无效或已撤销"

---

### Requirement: apikey 管理
WHEN 已登录用户访问 apikey 管理页面,
系统 SHALL 允许其创建、查看和撤销自己的 apikey。

#### Scenario: 创建密钥
GIVEN 用户已登录
WHEN 用户提交创建请求并填写名称
THEN 系统生成 sk-key 并返回完整密钥
AND 完整密钥仅在创建响应中展示一次
AND 系统仅存储密钥的不可逆哈希值，丢弃明文

#### Scenario: 撤销密钥
GIVEN 用户拥有某个 apikey
WHEN 用户请求撤销该密钥
THEN 系统将该密钥状态置为已撤销
AND 撤销后该密钥不可再用于调用数据接口
AND 用户无法操作他人的密钥
