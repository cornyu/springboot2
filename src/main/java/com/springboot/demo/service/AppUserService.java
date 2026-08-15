package com.springboot.demo.service;

import com.springboot.demo.entity.AppUser;

/**
 * 用户服务接口
 */
public interface AppUserService {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    AppUser findByUsername(String username);

    /**
     * 创建用户（密码BCrypt加密）
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 是否成功（用户名已存在返回false）
     */
    boolean createUser(String username, String rawPassword);
}
