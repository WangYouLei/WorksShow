package com.worksshow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全配置
 * <p>
 * 注册密码编码器等安全相关 Bean。
 *
 * @author WorksShow
 */
@Configuration
public class SecurityConfig {

    /**
     * BCrypt 密码编码器(默认强度 10)
     * <p>
     * 使用 Spring Bean 注入而非直接 new,确保单例复用且便于统一配置。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}