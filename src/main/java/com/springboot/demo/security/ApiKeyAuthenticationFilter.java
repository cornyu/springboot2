package com.springboot.demo.security;

import com.springboot.demo.entity.ApiKey;
import com.springboot.demo.service.ApiKeyService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * API Key 认证过滤器（双通道）。
 * <p>
 * 请求携带 {@code Authorization: Bearer sk-xxx} 头时按 apikey 认证（ROLE_API），
 * 无效则直接返回 401 JSON；未携带 Bearer 头则放行，由会话认证决定。
 * </p>
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // 携带 Bearer 头 → 按 apikey 认证
        if (header != null && header.startsWith("Bearer ")) {
            String rawKey = header.substring(7).trim();
            ApiKey apiKey = apiKeyService.validate(rawKey);
            if (apiKey == null) {
                write401(response, "apikey 无效或已撤销");
                return;
            }
            // 以 apikey 身份认证，请求结束后恢复原有上下文，避免污染会话
            SecurityContext original = SecurityContextHolder.getContext();
            SecurityContextHolder.clearContext();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            apiKey, rawKey, Collections.singletonList(new SimpleGrantedAuthority("ROLE_API"))));
            try {
                chain.doFilter(request, response);
            } finally {
                SecurityContextHolder.setContext(original);
            }
            return;
        }

        // 未携带 Bearer → 放行，由会话认证决定
        chain.doFilter(request, response);
    }

    private void write401(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
