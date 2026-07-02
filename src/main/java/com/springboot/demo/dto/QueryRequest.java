package com.springboot.demo.dto;

import java.util.List;

/**
 * 动态查询请求体
 */
public class QueryRequest {

    private String tableName;
    private List<String> columns;
    private List<WhereCondition> whereList;
    private String orderBy;
    private String order;
    private Integer limit;
    private Integer offset;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<WhereCondition> getWhereList() {
        return whereList;
    }

    public void setWhereList(List<WhereCondition> whereList) {
        this.whereList = whereList;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /**
     * WHERE 条件
     */
    public static class WhereCondition {
        private String field;
        private String operator;
        private String value;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
