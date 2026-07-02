package com.springboot.demo.controller;

import com.springboot.demo.dto.QueryRequest;
import com.springboot.demo.dto.Result;
import com.springboot.demo.service.SchemaService;
import com.springboot.demo.service.TreeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据库Schema查询控制器
 * 提供数据库表浏览、字段查询、动态查询功能
 */
@RestController
@RequestMapping("/api/db")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private TreeConfigService treeConfigService;

    /**
     * 获取所有可查询的表（平铺列表）
     */
    @GetMapping("/tables")
    public Result<?> getTables() {
        return Result.success(schemaService.getTables());
    }

    /**
     * 获取暴露给用户的树结构（含可编辑标签）
     */
    @GetMapping("/tree")
    public Result<?> getTree() {
        return Result.success(treeConfigService.getExposedTree());
    }

    /**
     * 获取指定表的字段信息
     */
    @GetMapping("/columns")
    public Result<?> getColumns(@RequestParam String table) {
        return Result.success(schemaService.getColumns(table));
    }

    /**
     * 执行动态查询
     */
    @PostMapping("/query")
    public Result<?> executeQuery(@RequestBody QueryRequest request) {
        return Result.success(schemaService.executeQuery(request));
    }
}
