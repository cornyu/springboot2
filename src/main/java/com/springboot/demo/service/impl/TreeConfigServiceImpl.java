package com.springboot.demo.service.impl;

import com.springboot.demo.dto.TreeNodeVO;
import com.springboot.demo.entity.ColumnConfig;
import com.springboot.demo.entity.TreeNode;
import com.springboot.demo.mapper.ColumnConfigMapper;
import com.springboot.demo.mapper.TreeNodeMapper;
import com.springboot.demo.service.TreeConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TreeConfigServiceImpl implements TreeConfigService {

    private static final Logger log = LoggerFactory.getLogger(TreeConfigServiceImpl.class);

    @Autowired
    private TreeNodeMapper treeNodeMapper;

    @Autowired
    private ColumnConfigMapper columnConfigMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            // 确保配置表存在
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `api_tree_node` (\n" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',\n" +
                    "  `parent_id` bigint(20) DEFAULT NULL COMMENT '父节点ID',\n" +
                    "  `label` varchar(100) NOT NULL COMMENT '节点显示名称',\n" +
                    "  `node_type` varchar(20) NOT NULL COMMENT '节点类型: GROUP/TABLE',\n" +
                    "  `table_name` varchar(100) DEFAULT NULL COMMENT '关联的数据库表名',\n" +
                    "  `sort_order` int(11) DEFAULT 0 COMMENT '排序号',\n" +
                    "  `exposed` tinyint(1) DEFAULT 1 COMMENT '是否暴露',\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  KEY `idx_parent_id` (`parent_id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API树节点配置'");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `api_column_config` (\n" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',\n" +
                    "  `table_node_id` bigint(20) NOT NULL COMMENT '关联的TABLE节点ID',\n" +
                    "  `column_name` varchar(100) NOT NULL COMMENT '数据库列名',\n" +
                    "  `exposed` tinyint(1) DEFAULT 1 COMMENT '是否暴露',\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `uk_node_column` (`table_node_id`, `column_name`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API列暴露配置'");

            // 如果树为空，自动导入所有数据库表
            if (treeNodeMapper.count() == 0) {
                autoImportTables();
            }
        } catch (Exception e) {
            log.warn("初始化配置表失败，可能是数据库连接问题: {}", e.getMessage());
        }
    }

    private void autoImportTables() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                int sort = 1;
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    // 跳过配置表自身
                    if ("api_tree_node".equals(tableName) || "api_column_config".equals(tableName)) {
                        continue;
                    }
                    TreeNode node = new TreeNode();
                    node.setLabel(tableName);
                    node.setNodeType("TABLE");
                    node.setTableName(tableName);
                    node.setSortOrder(sort++);
                    node.setExposed(true);
                    treeNodeMapper.insert(node);

                    // 为该表创建列配置（默认全部暴露）
                    autoImportColumns(node.getId(), tableName);
                }
            }
        } catch (Exception e) {
            log.error("自动导入数据库表失败", e);
        }
    }

    private void autoImportColumns(Long tableNodeId, String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, "%")) {
                while (rs.next()) {
                    ColumnConfig cc = new ColumnConfig();
                    cc.setTableNodeId(tableNodeId);
                    cc.setColumnName(rs.getString("COLUMN_NAME"));
                    cc.setExposed(true);
                    columnConfigMapper.insert(cc);
                }
            }
        } catch (Exception e) {
            log.error("自动导入列配置失败, tableName={}", tableName, e);
        }
    }

    @Override
    public List<TreeNodeVO> getFullTree() {
        List<TreeNode> allNodes = treeNodeMapper.selectAll();
        return buildTree(allNodes, false);
    }

    @Override
    public List<TreeNodeVO> getExposedTree() {
        List<TreeNode> allNodes = treeNodeMapper.selectAll();
        return buildTree(allNodes, true);
    }

    private List<TreeNodeVO> buildTree(List<TreeNode> flatNodes, boolean filterExposed) {
        // 过滤
        List<TreeNode> nodes = flatNodes;
        if (filterExposed) {
            nodes = flatNodes.stream().filter(n -> Boolean.TRUE.equals(n.getExposed())).collect(Collectors.toList());
            // 如果某个节点暴露但父节点不暴露，父节点也需要出现
            Set<Long> exposedIds = nodes.stream().map(TreeNode::getId).collect(Collectors.toSet());
            Set<Long> neededParentIds = nodes.stream()
                    .map(TreeNode::getParentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            // 添加需要的父节点
            for (TreeNode n : flatNodes) {
                if (neededParentIds.contains(n.getId()) && !exposedIds.contains(n.getId())) {
                    nodes.add(n);
                    exposedIds.add(n.getId());
                }
            }
        }

        Map<Long, TreeNodeVO> voMap = new LinkedHashMap<>();
        for (TreeNode n : nodes) {
            TreeNodeVO vo = toVO(n);
            vo.setChildren(new ArrayList<>());
            voMap.put(vo.getId(), vo);
        }

        List<TreeNodeVO> roots = new ArrayList<>();
        for (TreeNodeVO vo : voMap.values()) {
            if (vo.getParentId() == null) {
                roots.add(vo);
            } else {
                TreeNodeVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    private TreeNodeVO toVO(TreeNode n) {
        TreeNodeVO vo = new TreeNodeVO();
        vo.setId(n.getId());
        vo.setParentId(n.getParentId());
        vo.setLabel(n.getLabel());
        vo.setNodeType(n.getNodeType());
        vo.setTableName(n.getTableName());
        vo.setSortOrder(n.getSortOrder());
        vo.setExposed(n.getExposed());
        return vo;
    }

    @Override
    public TreeNode getNode(Long id) {
        return treeNodeMapper.selectById(id);
    }

    @Override
    @Transactional
    public TreeNode addNode(TreeNode node) {
        if (node.getSortOrder() == null) node.setSortOrder(0);
        if (node.getExposed() == null) node.setExposed(true);
        treeNodeMapper.insert(node);

        // 如果是TABLE类型，自动创建列配置
        if ("TABLE".equals(node.getNodeType()) && node.getTableName() != null) {
            autoImportColumns(node.getId(), node.getTableName());
        }
        return node;
    }

    @Override
    @Transactional
    public void updateNode(TreeNode node) {
        treeNodeMapper.update(node);
    }

    @Override
    @Transactional
    public void deleteNode(Long id) {
        // 递归删除子节点
        List<TreeNode> children = treeNodeMapper.selectByParentId(id);
        for (TreeNode child : children) {
            deleteNode(child.getId());
        }
        // 删除列配置
        columnConfigMapper.deleteByTableNodeId(id);
        // 删除自身
        treeNodeMapper.deleteById(id);
    }

    @Override
    public List<ColumnConfig> getColumnConfigs(Long tableNodeId) {
        return columnConfigMapper.selectByTableNodeId(tableNodeId);
    }

    @Override
    @Transactional
    public void updateColumnExposure(Long tableNodeId, List<ColumnConfig> configs) {
        for (ColumnConfig cc : configs) {
            columnConfigMapper.updateExposedByNodeAndColumn(
                    tableNodeId, cc.getColumnName(), cc.getExposed());
        }
    }
}
