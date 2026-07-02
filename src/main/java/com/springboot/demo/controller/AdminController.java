package com.springboot.demo.controller;

import com.springboot.demo.dto.Result;
import com.springboot.demo.entity.ColumnConfig;
import com.springboot.demo.entity.TreeNode;
import com.springboot.demo.service.TreeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员配置控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TreeConfigService treeConfigService;

    /**
     * 获取完整树结构（含未暴露节点）
     */
    @GetMapping("/tree")
    public Result<?> getFullTree() {
        return Result.success(treeConfigService.getFullTree());
    }

    /**
     * 获取单个节点
     */
    @GetMapping("/tree/node/{id}")
    public Result<?> getNode(@PathVariable Long id) {
        return Result.success(treeConfigService.getNode(id));
    }

    /**
     * 添加节点
     */
    @PostMapping("/tree/node")
    public Result<?> addNode(@RequestBody TreeNode node) {
        TreeNode created = treeConfigService.addNode(node);
        return Result.success(created);
    }

    /**
     * 更新节点
     */
    @PutMapping("/tree/node")
    public Result<?> updateNode(@RequestBody TreeNode node) {
        treeConfigService.updateNode(node);
        return Result.success();
    }

    /**
     * 删除节点
     */
    @DeleteMapping("/tree/node/{id}")
    public Result<?> deleteNode(@PathVariable Long id) {
        treeConfigService.deleteNode(id);
        return Result.success();
    }

    /**
     * 获取表节点的列配置
     */
    @GetMapping("/tree/node/{id}/columns")
    public Result<?> getColumnConfigs(@PathVariable Long id) {
        return Result.success(treeConfigService.getColumnConfigs(id));
    }

    /**
     * 更新列暴露状态
     */
    @PutMapping("/tree/node/{id}/columns")
    public Result<?> updateColumnExposure(@PathVariable Long id, @RequestBody List<ColumnConfig> configs) {
        treeConfigService.updateColumnExposure(id, configs);
        return Result.success();
    }
}
