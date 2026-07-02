package com.springboot.demo.dto;

import java.util.List;

/**
 * 树节点VO（含子节点，用于前端树形展示）
 */
public class TreeNodeVO {

    private Long id;
    private Long parentId;
    private String label;
    private String nodeType;
    private String tableName;
    private Integer sortOrder;
    private Boolean exposed;
    private List<TreeNodeVO> children;

    public TreeNodeVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getExposed() {
        return exposed;
    }

    public void setExposed(Boolean exposed) {
        this.exposed = exposed;
    }

    public List<TreeNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNodeVO> children) {
        this.children = children;
    }
}
