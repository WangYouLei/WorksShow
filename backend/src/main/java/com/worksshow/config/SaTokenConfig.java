package com.worksshow.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.worksshow.exception.BusinessException;
import com.worksshow.entity.User;
import com.worksshow.service.TokenBlacklistService;
import com.worksshow.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * <p>
 * 1. 注册 {@link StpLogicJwtForStateless} 切换为 JWT 无状态模式:
 *    token 自包含 userId 与 nickname(自定义 claim),服务端不维护会话。
 * 2. 注册 {@link SaInterceptor} 拦截所有请求,放行注册/登录等公开接口。
 * 3. 在拦截器中复用原逻辑:JWT 校验通过后查库确认用户当前状态,
 *    覆盖账号禁用(status=0)与逻辑删除的实时生效。
 * 4. 新增黑名单校验:从 Redis 检查 token 是否已被主动失效(登出/改密)。
 *
 * @author WorksShow
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 切换为 JWT StateLess(无状态)模式
     * <p>
     * 三种 JWT 模式对比:
     * <ul>
     *   <li>Simple: JWT 装 sessionId,真实数据放 SaSession(需存储)</li>
     *   <li>Mixin: JWT 装 sessionId + 部分数据,剩余放 SaSession</li>
     *   <li>StateLess: 完全无状态,所有数据在 JWT,无 SaSession(本工程采用)</li>
     * </ul>
     */
    @Bean
    public StpLogic stpLogicJwt() {
        return new StpLogicJwtForStateless();
    }

    /**
     * 注册 Sa-Token 拦截器
     * <p>
     * 路径相对于 context-path(/api),即 /user/** 对应 /api/user/**。
     * 放行路径与原 JwtAuthenticationInterceptor 保持一致。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 1. 获取请求中的 token
                    String token = SaHolder.getRequest().getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    // 2. 黑名单校验:已被主动失效的 token(登出/改密)直接拒绝
                    if (token != null && tokenBlacklistService.isBlacklisted(token)) {
                        log.warn("token 已被加入黑名单: {}", token);
                        throw new cn.dev33.satoken.exception.NotLoginException(
                                "登录已失效,请重新登录", null, cn.dev33.satoken.exception.NotLoginException.NOT_TOKEN);
                    }
                    // 3. JWT 签名与过期校验,失败抛 NotLoginException
                    StpUtil.checkLogin();
                    // 4. 查库校验用户当前状态:getById 受 @TableLogic 影响,
                    //    逻辑删除的用户返回 null;同时覆盖 status=0 的禁用账号
                    Long userId = StpUtil.getLoginIdAsLong();
                    User user = userService.getById(userId);
                    if (user == null || user.getStatus() == null || user.getStatus() == 0) {
                        log.warn("用户状态异常,拒绝访问: userId={}", userId);
                        StpUtil.logout();
                        throw new BusinessException(HttpStatus.FORBIDDEN.value(), "账号已被禁用或不存在,请联系管理员");
                    }
                }))
                .addPathPatterns("/**")
                // 放行: 发送验证码、注册、登录、忘记密码、刷新 token(公开接口)
                .excludePathPatterns(
                        "/user/sendCode",
                        "/user/register",
                        "/user/login",
                        "/user/refresh-token",
                        "/user/forgot-password/**",
                        "/error",
                        "/favicon.ico");
    }
}
