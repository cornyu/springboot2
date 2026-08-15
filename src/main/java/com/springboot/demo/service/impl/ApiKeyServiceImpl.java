package com.springboot.demo.service.impl;

import com.springboot.demo.entity.ApiKey;
import com.springboot.demo.mapper.ApiKeyMapper;
import com.springboot.demo.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

/**
 * API密钥服务实现类
 */
@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final String KEY_PREFIX = "sk-";
    private static final int RANDOM_LENGTH = 32;
    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Override
    @Transactional
    public String createKey(Long userId, String name) {
        String rawKey = KEY_PREFIX + randomString(RANDOM_LENGTH);

        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setName(name);
        apiKey.setKeyHash(sha256(rawKey));
        apiKey.setKeyPreview(preview(rawKey));
        apiKey.setStatus(1);
        apiKeyMapper.insert(apiKey);

        return rawKey;
    }

    @Override
    public List<ApiKey> listKeys(Long userId) {
        return apiKeyMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean revokeKey(Long userId, Long keyId) {
        ApiKey apiKey = apiKeyMapper.findById(keyId);
        if (apiKey == null || !apiKey.getUserId().equals(userId)) {
            return false;
        }
        return apiKeyMapper.updateStatus(keyId, 0) > 0;
    }

    @Override
    @Transactional
    public ApiKey validate(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) {
            return null;
        }
        ApiKey apiKey = apiKeyMapper.findActiveByHash(sha256(rawKey));
        if (apiKey != null) {
            apiKeyMapper.updateLastUsedAt(apiKey.getId(), new Date());
        }
        return apiKey;
    }

    /**
     * 生成随机字符串
     */
    private String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    /**
     * 计算 SHA-256 十六进制
     */
    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }

    /**
     * 生成掩码预览，例如 sk-abcd...wxyz
     */
    private String preview(String rawKey) {
        if (rawKey.length() <= 11) {
            return rawKey;
        }
        return rawKey.substring(0, 7) + "..." + rawKey.substring(rawKey.length() - 4);
    }
}
