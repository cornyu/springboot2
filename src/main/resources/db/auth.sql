-- 认证模块建表脚本
-- 用户表 + API密钥表
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `api_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '密钥ID',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `name` varchar(100) DEFAULT NULL COMMENT '备注/用途',
  `key_hash` varchar(64) NOT NULL COMMENT 'sk-key 的SHA-256哈希',
  `key_preview` varchar(30) NOT NULL COMMENT '掩码显示 sk-xxxx...abcd',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1有效 0已撤销',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_used_at` datetime DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_key_hash` (`key_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API密钥表';
