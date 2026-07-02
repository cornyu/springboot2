package com.springboot.demo.dto;

import java.util.List;
import java.util.Map;

/**
 * 动态查询结果体
 */
public class QueryResult {

    private List<String> columns;
    private List<Map<String, Object>> rows;
    private long total;

    public QueryResult() {
    }

    public QueryResult(List<String> columns, List<Map<String, Object>> rows, long total) {
        this.columns = columns;
        this.rows = rows;
        this.total = total;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
