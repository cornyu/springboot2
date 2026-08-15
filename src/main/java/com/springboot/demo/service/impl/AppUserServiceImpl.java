package com.springboot.demo.service.impl;

import com.springboot.demo.entity.AppUser;
import com.springboot.demo.mapper.AppUserMapper;
import com.springboot.demo.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AppUser findByUsername(String username) {
        return appUserMapper.findByUsername(username);
    }

    @Override
    @Transactional
    public boolean createUser(String username, String rawPassword) {
        if (appUserMapper.findByUsername(username) != null) {
            return false;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return appUserMapper.insert(user) > 0;
    }
}
