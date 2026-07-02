package com.springboot.demo.entity;

/**
 * API列暴露配置实体
 */
public class ColumnConfig {

    private Long id;
    private Long tableNodeId;
    private String columnName;
    private Boolean exposed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTableNodeId() {
        return tableNodeId;
    }

    public void setTableNodeId(Long tableNodeId) {
        this.tableNodeId = tableNodeId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Boolean getExposed() {
        return exposed;
    }

    public void setExposed(Boolean exposed) {
        this.exposed = exposed;
    }
}
