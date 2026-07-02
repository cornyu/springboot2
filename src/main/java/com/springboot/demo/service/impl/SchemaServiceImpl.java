package com.springboot.demo.service.impl;

import com.springboot.demo.dto.QueryRequest;
import com.springboot.demo.dto.QueryResult;
import com.springboot.demo.entity.ColumnInfo;
import com.springboot.demo.entity.TableInfo;
import com.springboot.demo.mapper.ColumnConfigMapper;
import com.springboot.demo.mapper.TreeNodeMapper;
import com.springboot.demo.service.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SchemaServiceImpl implements SchemaService {

    private static final Logger log = LoggerFactory.getLogger(SchemaServiceImpl.class);

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_LIMIT = 100;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private TreeNodeMapper treeNodeMapper;

    @Autowired(required = false)
    private ColumnConfigMapper columnConfigMapper;

    @Override
    public List<TableInfo> getTables() {
        List<TableInfo> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    String comment = rs.getString("REMARKS");
                    tables.add(new TableInfo(name, comment));
                }
            }
        } catch (Exception e) {
            log.error("获取数据库表列表失败", e);
            throw new RuntimeException("获取数据库表列表失败", e);
        }
        return tables;
    }

    @Override
    public List<ColumnInfo> getColumns(String tableName) {
        validateTableName(tableName);
        List<ColumnInfo> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, "%")) {
                while (rs.next()) {
                    ColumnInfo info = new ColumnInfo();
                    info.setName(rs.getString("COLUMN_NAME"));
                    String typeName = rs.getString("TYPE_NAME");
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    info.setType(typeName + (columnSize > 0 ? "(" + columnSize + ")" : ""));
                    info.setComment(rs.getString("REMARKS"));
                    info.setNullable("YES".equals(rs.getString("IS_NULLABLE")) ? "YES" : "NO");
                    info.setDefaultValue(rs.getString("COLUMN_DEF"));
                    columns.add(info);
                }
            }
        } catch (Exception e) {
            log.error("获取表字段信息失败, tableName={}", tableName, e);
            throw new RuntimeException("获取表字段信息失败: " + tableName, e);
        }

        // 根据列配置过滤暴露的字段
        return filterExposedColumns(tableName, columns);
    }

    /**
     * 根据配置过滤仅返回暴露的字段
     */
    private List<ColumnInfo> filterExposedColumns(String tableName, List<ColumnInfo> allColumns) {
        if (treeNodeMapper == null || columnConfigMapper == null) {
            return allColumns;
        }
        try {
            // 查找到该表名对应的TABLE节点
            List<com.springboot.demo.entity.TreeNode> nodes = treeNodeMapper.selectAll().stream()
                    .filter(n -> "TABLE".equals(n.getNodeType())
                            && tableName.equals(n.getTableName())
                            && Boolean.TRUE.equals(n.getExposed()))
                    .collect(Collectors.toList());
            if (nodes.isEmpty()) return allColumns;

            // 取第一个匹配节点的列配置
            List<com.springboot.demo.entity.ColumnConfig> configs =
                    columnConfigMapper.selectByTableNodeId(nodes.get(0).getId());
            if (configs.isEmpty()) return allColumns;

            Set<String> exposedColumnNames = configs.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getExposed()))
                    .map(com.springboot.demo.entity.ColumnConfig::getColumnName)
                    .collect(Collectors.toSet());

            return allColumns.stream()
                    .filter(c -> exposedColumnNames.contains(c.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("过滤列配置失败，返回全部字段", e);
            return allColumns;
        }
    }

    @Override
    public QueryResult executeQuery(QueryRequest request) {
        String tableName = request.getTableName();
        validateTableName(tableName);

        List<String> validColumns = getValidColumnNames(tableName);
        String columnsClause = buildColumnsClause(request.getColumns(), validColumns);
        String countClause = "COUNT(*)";

        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(request.getWhereList(), validColumns, params);

        StringBuilder orderClause = new StringBuilder();
        if (request.getOrderBy() != null && !request.getOrderBy().isEmpty()) {
            if (!SAFE_NAME_PATTERN.matcher(request.getOrderBy()).matches()) {
                throw new IllegalArgumentException("非法的排序字段: " + request.getOrderBy());
            }
            if (!validColumns.contains(request.getOrderBy())) {
                throw new IllegalArgumentException("排序字段不存在: " + request.getOrderBy());
            }
            orderClause.append(" ORDER BY ").append(escapeName(request.getOrderBy()));
            if ("DESC".equalsIgnoreCase(request.getOrder())) {
                orderClause.append(" DESC");
            } else {
                orderClause.append(" ASC");
            }
        }

        int limit = (request.getLimit() == null || request.getLimit() <= 0) ? DEFAULT_LIMIT : Math.min(request.getLimit(), MAX_LIMIT);
        int offset = (request.getOffset() == null || request.getOffset() < 0) ? 0 : request.getOffset();

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM " + escapeName(tableName) + whereClause;
        log.debug("Count SQL: {}", countSql);
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        if (total == null) total = 0L;

        // 查询数据
        String dataSql = "SELECT " + columnsClause + " FROM " + escapeName(tableName)
                + whereClause + orderClause + " LIMIT ? OFFSET ?";
        log.debug("Query SQL: {}", dataSql);
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(limit);
        dataParams.add(offset);

        // 确定返回的列名
        List<String> resultColumns;
        if (request.getColumns() != null && !request.getColumns().isEmpty()) {
            resultColumns = request.getColumns().stream()
                    .filter(validColumns::contains)
                    .collect(Collectors.toList());
        } else {
            resultColumns = validColumns;
        }

        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(dataSql, dataParams.toArray());

        // 将下划线列名转为驼峰（前端展示用）
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> rawRow : rawRows) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String col : resultColumns) {
                row.put(toCamelCase(col), rawRow.get(col));
            }
            rows.add(row);
        }

        // 列名也转为驼峰
        List<String> displayColumns = resultColumns.stream()
                .map(this::toCamelCase)
                .collect(Collectors.toList());

        return new QueryResult(displayColumns, rows, total);
    }

    /**
     * 校验表名合法性
     */
    private void validateTableName(String tableName) {
        if (tableName == null || !SAFE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException("非法的表名: " + tableName);
        }
        Set<String> validTables = getTables().stream()
                .map(TableInfo::getName)
                .collect(Collectors.toSet());
        if (!validTables.contains(tableName)) {
            throw new IllegalArgumentException("表不存在或未授权: " + tableName);
        }
    }

    /**
     * 获取表中所有有效字段名
     */
    private List<String> getValidColumnNames(String tableName) {
        List<String> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, "%")) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取字段信息失败", e);
        }
        return columns;
    }

    /**
     * 构建 SELECT 列子句
     */
    private String buildColumnsClause(List<String> columns, List<String> validColumns) {
        if (columns == null || columns.isEmpty()) {
            return "*";
        }
        return columns.stream()
                .filter(c -> SAFE_NAME_PATTERN.matcher(c).matches() && validColumns.contains(c))
                .map(this::escapeName)
                .collect(Collectors.joining(", "));
    }

    /**
     * 构建 WHERE 子句（使用?占位符）
     */
    private String buildWhereClause(List<QueryRequest.WhereCondition> whereList,
                                     List<String> validColumns, List<Object> params) {
        if (whereList == null || whereList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE 1=1");
        for (QueryRequest.WhereCondition cond : whereList) {
            if (cond.getField() == null || cond.getOperator() == null) {
                continue;
            }
            if (!SAFE_NAME_PATTERN.matcher(cond.getField()).matches() || !validColumns.contains(cond.getField())) {
                throw new IllegalArgumentException("非法的字段名: " + cond.getField());
            }
            sb.append(" AND ").append(escapeName(cond.getField()));
            String op = cond.getOperator().toUpperCase();
            switch (op) {
                case "=":
                case "!=":
                case ">":
                case "<":
                case ">=":
                case "<=":
                    sb.append(" ").append(op).append(" ?");
                    params.add(cond.getValue());
                    break;
                case "LIKE":
                    sb.append(" LIKE CONCAT('%', ?, '%')");
                    params.add(cond.getValue());
                    break;
                case "IN":
                    if (cond.getValue() != null && !cond.getValue().isEmpty()) {
                        String[] values = cond.getValue().split(",");
                        sb.append(" IN (");
                        for (int i = 0; i < values.length; i++) {
                            sb.append(i > 0 ? ", ?" : "?");
                            params.add(values[i].trim());
                        }
                        sb.append(")");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("不支持的运算符: " + op);
            }
        }
        return sb.toString();
    }

    private String escapeName(String name) {
        return "`" + name + "`";
    }

    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
