package com.springboot.demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.springboot.demo.mapper")
public class MyBatisConfig {
    // MyBatis配置，通过@MapperScan注解扫描Mapper接口
}