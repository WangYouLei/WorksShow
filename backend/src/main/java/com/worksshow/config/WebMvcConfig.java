package com.worksshow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web MVC 配置
 * <p>
 * 仅保留 CORS 配置。
 * 拦截器与放行路径已迁移至 {@link SaTokenConfig},
 * 由 Sa-Token 的 {@code SaInterceptor} 统一接管认证校验。
 *
 * CORS 白名单按 profile 注入:
 *   - dev profile 默认放行 localhost 开发端口
 *   - prod profile 通过允许实际前端域名(或以环境变量注入)配置,
 *     不再随源码泄漏生产入口
 *
 * @author WorksShow
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 允许的跨域来源(逗号分隔)。
     * 默认值覆盖本地常用前端开发端口;生产部署务必通过配置文件或环境变量
     * 注入实际前端域名,并移除通配与 localhost 行。
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (origins.length == 0) {
            log.warn("cors.allowed-origins 配置为空,已禁用跨域访问");
            return;
        }
        log.info("CORS 允许来源: {}", Arrays.toString(origins));
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
