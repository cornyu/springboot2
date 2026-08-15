package com.springboot.demo.security;

import com.springboot.demo.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * Spring Security 配置。
 * <p>
 * - 表单登录（会话），用户来自 app_user 表
 * - /api/db/** 双通道：登录会话或 apikey（Bearer sk-xxx）均可
 * - /api/admin/**、/api/apikey/** 及页面仅接受会话（ROLE_USER）
 * - /api/** 未认证返回 401 JSON，页面请求重定向登录页
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DbUserDetailsService dbUserDetailsService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(dbUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        ApiKeyAuthenticationFilter apiKeyFilter = new ApiKeyAuthenticationFilter(apiKeyService);

        http
                .csrf().disable()
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/css/**", "/js/**", "/login.html", "/error").permitAll()
                // 数据接口双通道：登录会话 或 apikey 均可
                .antMatchers("/api/db/**").authenticated()
                // 管理类接口与页面仅会话
                .antMatchers("/api/admin/**", "/api/apikey/**",
                        "/index.html", "/admin.html", "/apikey.html").hasRole("USER")
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                .and()
                .formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error=1")
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/login.html?logout=1")
                .permitAll();
    }

    /**
     * 未认证访问：/api/** 返回 401 JSON，页面请求重定向登录页
     */
    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
            } else {
                response.sendRedirect("/login.html");
            }
        };
    }
}
