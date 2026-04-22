package com.springboot.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 事务配置类
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    // 启用声明式事务管理
}