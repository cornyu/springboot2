package com.springboot.demo.service;

import com.springboot.demo.dto.QueryRequest;
import com.springboot.demo.dto.QueryResult;
import com.springboot.demo.entity.ColumnInfo;
import com.springboot.demo.entity.TableInfo;

import java.util.List;

/**
 * 数据库Schema服务接口
 */
public interface SchemaService {

    /**
     * 获取所有可查询的表
     */
    List<TableInfo> getTables();

    /**
     * 获取指定表的字段信息
     */
    List<ColumnInfo> getColumns(String tableName);

    /**
     * 执行动态查询
     */
    QueryResult executeQuery(QueryRequest request);
}
