package com.springboot.demo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * API密钥实体类
 */
public class ApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 密钥ID
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 备注/用途
     */
    private String name;

    /**
     * sk-key 的SHA-256哈希
     */
    private String keyHash;

    /**
     * 掩码显示 sk-xxxx...abcd
     */
    private String keyPreview;

    /**
     * 状态：1有效 0已撤销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后使用时间
     */
    private Date lastUsedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public String getKeyPreview() {
        return keyPreview;
    }

    public void setKeyPreview(String keyPreview) {
        this.keyPreview = keyPreview;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    @Override
    public String toString() {
        return "ApiKey{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", keyPreview='" + keyPreview + '\'' +
                ", status=" + status +
                '}';
    }
}
