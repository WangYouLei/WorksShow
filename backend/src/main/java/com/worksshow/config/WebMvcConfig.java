package com.worksshow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 仅保留 CORS 配置。
 * 拦截器与放行路径已迁移至 {@link SaTokenConfig},
 * 由 Sa-Token 的 {@code SaInterceptor} 统一接管认证校验。
 *
 * @author WorksShow
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

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
