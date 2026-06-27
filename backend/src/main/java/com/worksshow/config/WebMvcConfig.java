package com.worksshow.config;

import com.worksshow.security.JwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 1. 注册 JWT 拦截器,放行注册/登录等公开接口
 * 2. 配置 CORS,允许前端(Vite 默认 5173 端口)跨域访问
 *
 * @author WorksShow
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     * 注意: 路径相对于 context-path(/api),因此配置 /user/** 即对应 /api/user/**
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 放行: 发送验证码、注册、登录、忘记密码(公开接口)
                .excludePathPatterns(
                        "/user/sendCode",
                        "/user/register",
                        "/user/login",
                        "/user/forgot-password/**",
                        "/error",
                        "/favicon.ico");
    }

    /**
     * 跨域配置: 允许前端开发服务器访问
     * 生产环境请按实际域名调整
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Vite 默认端口 5173;如端口不同请修改
                .allowedOriginPatterns("http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
