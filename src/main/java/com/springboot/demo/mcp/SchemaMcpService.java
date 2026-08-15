package com.springboot.demo.mcp;

import com.ajaxjs.mcp.server.feature.annotation.McpService;
import com.ajaxjs.mcp.server.feature.annotation.Tool;
import com.ajaxjs.mcp.server.feature.annotation.ToolArg;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.demo.dto.QueryRequest;
import com.springboot.demo.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@McpService
public class SchemaMcpService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaService schemaService;

    @Tool(description = "获取数据库中所有可查询的表列表")
    public String listTables() {
        return toJson(schemaService.getTables());
    }

    @Tool(description = "获取指定表的所有字段信息（名称、类型、注释等）")
    public String getColumns(
            @ToolArg(description = "表名，例如 student") String tableName) {
        return toJson(schemaService.getColumns(tableName));
    }

    @Tool(description = "对数据库表执行动态查询，支持 WHERE 条件过滤、排序、分页。"
            + " whereList 为 JSON 数组，每项包含 field(字段名)、operator(运算符)、value(值)。"
            + " 支持的运算符: =, !=, >, <, >=, <=, LIKE, IN")
    public String executeQuery(
            @ToolArg(description = "表名") String tableName,
            @ToolArg(description = "要查询的列名，用逗号分隔，例如 id,name,age") String columns,
            @ToolArg(description = "WHERE 条件 JSON，例如 [{\"field\":\"age\",\"operator\":\">\",\"value\":\"18\"}]") String whereConditions,
            @ToolArg(description = "排序字段名，例如 age") String orderBy,
            @ToolArg(description = "排序方向: ASC 或 DESC") String order,
            @ToolArg(description = "返回记录数上限，默认 100，最大 1000") Integer limit,
            @ToolArg(description = "分页偏移量，从 0 开始") Integer offset) {

        QueryRequest request = new QueryRequest();
        request.setTableName(tableName);

        if (columns != null && !columns.isEmpty()) {
            request.setColumns(java.util.Arrays.asList(columns.split("\\s*,\\s*")));
        }

        if (whereConditions != null && !whereConditions.isEmpty()) {
            try {
                List<QueryRequest.WhereCondition> whereList = MAPPER.readValue(
                        whereConditions, new TypeReference<List<QueryRequest.WhereCondition>>() {});
                request.setWhereList(whereList);
            } catch (Exception e) {
                throw new IllegalArgumentException("WHERE 条件 JSON 格式错误: " + e.getMessage());
            }
        }

        if (orderBy != null && !orderBy.isEmpty()) {
            request.setOrderBy(orderBy);
        }
        request.setOrder(order);
        request.setLimit(limit);
        request.setOffset(offset);

        return toJson(schemaService.executeQuery(request));
    }

    private static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }
}
