package com.worksshow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksshow.common.Result;
import com.worksshow.entity.User;
import com.worksshow.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * <p>
 * 拦截受保护接口,校验 Authorization 头中的 token,并查询数据库确认用户当前状态。
 * 校验通过则将用户信息存入 UserContext,失败则返回 401。
 * 在 WebMvcConfig 中配置放行路径(注册/登录)。
 * <p>
 * 查库目的: 防止被禁用(status=0)或逻辑删除的用户,其已签发 token
 * 在有效期内仍可访问受保护接口(覆盖逻辑删除与账号禁用的实时生效)。
 *
 * @author WorksShow
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        // 复用 parseAndValidate 只解析一次,避免重复 HMAC 验签;同时统一捕获 ExpiredJwtException 等异常
        Claims claims = jwtUtils.parseAndValidate(token);
        if (token == null || claims == null) {
            log.warn("未携带或非法 token,请求路径: {}", request.getRequestURI());
            writeUnauthorized(response, "未登录或登录已失效,请重新登录");
            return false;
        }

        Long userId = Long.parseLong(claims.getSubject());
        // 查库校验用户当前状态:getById 受 @TableLogic 影响,逻辑删除的用户返回 null
        User user = userService.getById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            log.warn("用户状态异常,拒绝访问: userId={}", userId);
            writeUnauthorized(response, "用户不存在或已被禁用");
            return false;
        }

        // 解析用户信息并放入上下文
        String nickname = claims.get("nickname", String.class);
        UserContext.set(new UserContext.LoginUser(userId, nickname));
        log.debug("认证通过, userId={}, nickname={}", userId, nickname);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理,防止线程池复用导致用户信息串号
        UserContext.clear();
    }

    /**
     * 从 Authorization 头解析出 token(去除 "Bearer " 前缀)
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 写出 401 响应
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, message)));
    }
}
