package com.springboot.demo.mapper;

import com.springboot.demo.entity.ApiKey;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * API密钥表Mapper接口
 */
@Mapper
public interface ApiKeyMapper {

    /**
     * 插入密钥
     *
     * @param apiKey 密钥信息
     * @return 影响行数
     */
    @Insert("INSERT INTO api_key(user_id, name, key_hash, key_preview, status) " +
            "VALUES(#{userId}, #{name}, #{keyHash}, #{keyPreview}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApiKey apiKey);

    /**
     * 根据哈希查询有效密钥
     *
     * @param keyHash sk-key 的SHA-256哈希
     * @return 密钥信息（status=1）
     */
    @Select("SELECT * FROM api_key WHERE key_hash = #{keyHash} AND status = 1")
    ApiKey findActiveByHash(@Param("keyHash") String keyHash);

    /**
     * 根据ID查询密钥
     *
     * @param id 密钥ID
     * @return 密钥信息
     */
    @Select("SELECT * FROM api_key WHERE id = #{id}")
    ApiKey findById(@Param("id") Long id);

    /**
     * 查询用户的全部密钥
     *
     * @param userId 用户ID
     * @return 密钥列表
     */
    @Select("SELECT * FROM api_key WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ApiKey> findByUserId(@Param("userId") Long userId);

    /**
     * 更新密钥状态
     *
     * @param id     密钥ID
     * @param status 状态：1有效 0已撤销
     * @return 影响行数
     */
    @Update("UPDATE api_key SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新最后使用时间
     *
     * @param id        密钥ID
     * @param lastUsedAt 最后使用时间
     * @return 影响行数
     */
    @Update("UPDATE api_key SET last_used_at = #{lastUsedAt} WHERE id = #{id}")
    int updateLastUsedAt(@Param("id") Long id, @Param("lastUsedAt") Date lastUsedAt);
}
