package com.springboot.demo.service;

import com.springboot.demo.entity.ApiKey;

import java.util.List;

/**
 * API密钥服务接口
 */
public interface ApiKeyService {

    /**
     * 为用户创建新密钥（返回完整sk-key，仅此一次）
     *
     * @param userId 用户ID
     * @param name   备注/用途
     * @return 完整的 sk-key
     */
    String createKey(Long userId, String name);

    /**
     * 查询用户的密钥列表
     *
     * @param userId 用户ID
     * @return 密钥列表
     */
    List<ApiKey> listKeys(Long userId);

    /**
     * 撤销用户的某个密钥
     *
     * @param userId 用户ID
     * @param keyId  密钥ID
     * @return 是否成功（非本人密钥返回false）
     */
    boolean revokeKey(Long userId, Long keyId);

    /**
     * 校验明文sk-key，有效则返回密钥信息并更新最后使用时间
     *
     * @param rawKey 明文 sk-key
     * @return 密钥信息，无效返回null
     */
    ApiKey validate(String rawKey);
}
