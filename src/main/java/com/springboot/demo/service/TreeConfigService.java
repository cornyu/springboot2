package com.springboot.demo.service;

import com.springboot.demo.dto.TreeNodeVO;
import com.springboot.demo.entity.ColumnConfig;
import com.springboot.demo.entity.TreeNode;

import java.util.List;

/**
 * 树配置服务接口
 */
public interface TreeConfigService {

    /** 初始化配置（系统启动时调用） */
    void init();

    /**
     * 获取完整的树结构（含所有节点，管理员用）
     */
    List<TreeNodeVO> getFullTree();

    /**
     * 获取暴露给用户的树结构（仅暴露的节点）
     */
    List<TreeNodeVO> getExposedTree();

    /**
     * 获取单个节点
     */
    TreeNode getNode(Long id);

    /**
     * 添加节点
     */
    TreeNode addNode(TreeNode node);

    /**
     * 更新节点
     */
    void updateNode(TreeNode node);

    /**
     * 删除节点（会递归删除子节点和关联的列配置）
     */
    void deleteNode(Long id);

    /**
     * 获取表节点的列配置
     */
    List<ColumnConfig> getColumnConfigs(Long tableNodeId);

    /**
     * 批量更新列暴露状态
     */
    void updateColumnExposure(Long tableNodeId, List<ColumnConfig> configs);
}
