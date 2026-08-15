package com.springboot.demo.controller;

import com.springboot.demo.dto.Result;
import com.springboot.demo.entity.AppUser;
import com.springboot.demo.entity.ApiKey;
import com.springboot.demo.service.ApiKeyService;
import com.springboot.demo.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API密钥管理控制器（仅登录会话可访问）
 */
@RestController
@RequestMapping("/api/apikey")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private AppUserService appUserService;

    /**
     * 获取当前用户的密钥列表
     */
    @GetMapping("/list")
    public Result<?> list() {
        AppUser user = currentUser();
        List<ApiKey> keys = apiKeyService.listKeys(user.getId());
        return Result.success(keys);
    }

    /**
     * 创建新密钥，返回完整 sk-key（仅此一次）
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody(required = false) Map<String, String> body) {
        AppUser user = currentUser();
        String name = body != null ? body.get("name") : null;
        String rawKey = apiKeyService.createKey(user.getId(), name);

        Map<String, Object> data = new HashMap<>();
        data.put("key", rawKey);
        data.put("preview", preview(rawKey));
        return Result.success(data);
    }

    /**
     * 撤销密钥
     */
    @DeleteMapping("/{id}")
    public Result<?> revoke(@PathVariable Long id) {
        AppUser user = currentUser();
        if (!apiKeyService.revokeKey(user.getId(), id)) {
            return Result.error(400, "密钥不存在或无权操作");
        }
        return Result.success();
    }

    /**
     * 获取当前登录用户
     */
    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return appUserService.findByUsername(auth.getName());
    }

    /**
     * 生成掩码预览
     */
    private String preview(String rawKey) {
        if (rawKey == null || rawKey.length() <= 11) {
            return rawKey;
        }
        return rawKey.substring(0, 7) + "..." + rawKey.substring(rawKey.length() - 4);
    }
}
