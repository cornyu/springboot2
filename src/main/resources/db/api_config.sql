-- API树节点配置表
-- 存储数据浏览器左侧树形菜单的结构
CREATE TABLE IF NOT EXISTS `api_tree_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父节点ID，根节点为NULL',
  `label` varchar(100) NOT NULL COMMENT '节点显示名称',
  `node_type` varchar(20) NOT NULL COMMENT '节点类型: GROUP=分组, TABLE=数据表',
  `table_name` varchar(100) DEFAULT NULL COMMENT '关联的数据库表名（TABLE类型时有效）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序号',
  `exposed` tinyint(1) DEFAULT 1 COMMENT '是否暴露: 1=暴露, 0=隐藏',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API树节点配置';

-- API列暴露配置表
-- 控制每个表节点下哪些字段对外可见
CREATE TABLE IF NOT EXISTS `api_column_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `table_node_id` bigint(20) NOT NULL COMMENT '关联的TABLE类型节点ID',
  `column_name` varchar(100) NOT NULL COMMENT '数据库列名',
  `exposed` tinyint(1) DEFAULT 1 COMMENT '是否暴露: 1=暴露, 0=隐藏',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_column` (`table_node_id`, `column_name`),
  KEY `idx_table_node` (`table_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API列暴露配置';
