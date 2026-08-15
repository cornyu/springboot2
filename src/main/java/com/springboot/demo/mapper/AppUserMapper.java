package com.springboot.demo.mapper;

import com.springboot.demo.entity.AppUser;
import org.apache.ibatis.annotations.*;

/**
 * 用户表Mapper接口
 */
@Mapper
public interface AppUserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT id, username, password, created_at FROM app_user WHERE username = #{username}")
    AppUser findByUsername(@Param("username") String username);

    /**
     * 插入用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    @Insert("INSERT INTO app_user(username, password) VALUES(#{username}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppUser user);

    /**
     * 统计用户总数
     *
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM app_user")
    long count();
}
